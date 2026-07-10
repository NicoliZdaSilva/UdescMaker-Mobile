package com.udescmaker.api.repository;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriUtils;

import com.udescmaker.api.config.UdescMakerProperties;
import com.udescmaker.api.domain.Projeto;
import com.udescmaker.api.exception.GitHubAutenticacaoException;
import com.udescmaker.api.exception.GitHubIndisponivelException;
import com.udescmaker.api.exception.MarkdownInvalidoException;
import com.udescmaker.api.markdown.FrontmatterParser;

@Repository
@ConditionalOnProperty(name = "udescmaker.catalog.mode", havingValue = "github")
public class GitHubProjetoRepository implements ProjetoRepository {
    private final FrontmatterParser parser;
    private final UdescMakerProperties properties;
    private final RestClient client;
    private final Clock clock;
    private volatile Cache cache;

    @Autowired
    public GitHubProjetoRepository(FrontmatterParser parser, UdescMakerProperties properties, RestClient.Builder builder) {
        this(parser, properties, criarCliente(properties, builder), Clock.systemUTC());
    }

    GitHubProjetoRepository(FrontmatterParser parser, UdescMakerProperties properties, RestClient client, Clock clock) {
        this.parser = parser;
        this.properties = properties;
        this.client = client;
        this.clock = clock;
    }

    

    private static RestClient criarCliente(UdescMakerProperties properties, RestClient.Builder builder) {
        RestClient.Builder configurado = builder.clone().baseUrl(properties.getGithub().getApiUrl().toString())
                .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github+json")
                .defaultHeader("X-GitHub-Api-Version", "2026-03-10")
                .defaultHeader(HttpHeaders.USER_AGENT, "udescmaker-mobile-api");
        if (!properties.getGithub().getToken().isBlank()) {
            configurado.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getGithub().getToken());
        }
        return configurado.build();
    }

    @Override
    public List<Projeto> listarTodos() {
        Cache atual = cache;
        if (atual != null && clock.instant().isBefore(atual.carregadoEm.plus(properties.getCatalog().getCacheTtl()))) {
            return atual.projetos;
        }
        synchronized (this) {
            atual = cache;
            if (atual == null || !clock.instant().isBefore(atual.carregadoEm.plus(properties.getCatalog().getCacheTtl()))) {
                cache = new Cache(clock.instant(), carregar());
            }
            return cache.projetos;
        }
    }

    private List<Projeto> carregar() {
        var github = properties.getGithub();
        try {
            GitRefResponse ref = client.get()
                    .uri("/repos/{owner}/{repo}/git/ref/heads/{branch}",
                            github.getOwner(), github.getRepository(), github.getBranch())
                    .retrieve().body(GitRefResponse.class);
            String commitSha = exigir(ref == null || ref.object() == null ? null : ref.object().sha(),
                    "HEAD da branch do catálogo");
            GitCommitResponse commit = client.get()
                    .uri("/repos/{owner}/{repo}/git/commits/{sha}",
                            github.getOwner(), github.getRepository(), commitSha)
                    .retrieve().body(GitCommitResponse.class);
            String treeSha = exigir(commit == null || commit.tree() == null ? null : commit.tree().sha(),
                    "árvore do commit do catálogo");
            GitTreeResponse arvore = client.get()
                    .uri("/repos/{owner}/{repo}/git/trees/{sha}?recursive=1",
                            github.getOwner(), github.getRepository(), treeSha)
                    .retrieve().body(GitTreeResponse.class);
            if (arvore == null || arvore.tree() == null) throw new GitHubIndisponivelException("Resposta vazia ao ler catálogo no GitHub");
            if (arvore.truncated()) throw new GitHubIndisponivelException("Árvore do catálogo retornada pelo GitHub foi truncada");
            String prefixo = limpar(github.getProjectsPath()) + "/";
            return arvore.tree().stream()
                    .filter(item -> "blob".equals(item.type()))
                    .filter(item -> item.path().startsWith(prefixo) && item.path().endsWith("/index.md"))
                    .sorted(Comparator.comparing(TreeItem::path))
                    .map(item -> carregarProjeto(item, commitSha))
                    .toList();
        } catch (RestClientResponseException exception) {
            throw traduzir(exception, "ler catálogo");
        } catch (RestClientException exception) {
            throw new GitHubIndisponivelException("GitHub temporariamente indisponível ao ler catálogo", exception);
        }
    }

    private Projeto carregarProjeto(TreeItem item, String commitSha) {
        var github = properties.getGithub();
        GitBlobResponse blob = client.get()
                .uri("/repos/{owner}/{repo}/git/blobs/{sha}", github.getOwner(), github.getRepository(), item.sha())
                .retrieve().body(GitBlobResponse.class);
        if (blob == null || blob.content() == null || !"base64".equalsIgnoreCase(blob.encoding())) {
            throw new GitHubIndisponivelException("Blob Markdown inválido retornado pelo GitHub: " + item.path());
        }
        String[] partes = item.path().split("/");
        String slug = partes[partes.length - 2];
        String markdown = new String(Base64.getMimeDecoder().decode(blob.content()), StandardCharsets.UTF_8);
        Projeto projeto = parser.parse(slug, markdown);
        String base = properties.getGithub().getRawUrl().toString().replaceAll("/$", "") + "/"
                + segmento(github.getOwner()) + "/" + segmento(github.getRepository()) + "/"
                + segmento(commitSha) + "/" + caminho(github.getProjectsPath()) + "/" + segmento(slug) + "/";
        return ProjetoUrlResolver.resolver(projeto, referencia -> resolver(base, referencia));
    }

    private String resolver(String base, String referencia) {
        if (referencia == null || referencia.matches("(?i)^https?://.*")) return referencia;
        String nome = referencia.replaceFirst("^\\./", "");
        if (nome.contains("/") || nome.contains("\\") || nome.equals("..")) {
            throw new MarkdownInvalidoException("Referência de arquivo insegura: " + referencia);
        }
        return base + segmento(nome);
    }

    private RuntimeException traduzir(RestClientResponseException exception, String operacao) {
        if (exception.getStatusCode().value() == 401 || exception.getStatusCode().value() == 403) {
            return new GitHubAutenticacaoException("GitHub recusou a autenticação ao " + operacao, exception);
        }
        return new GitHubIndisponivelException("Falha do GitHub ao " + operacao + " (HTTP " + exception.getStatusCode().value() + ")", exception);
    }

    private String segmento(String valor) { return UriUtils.encodePathSegment(valor, StandardCharsets.UTF_8); }
    private String caminho(String valor) {
        return Arrays.stream(limpar(valor).split("/")).map(this::segmento).reduce((a, b) -> a + "/" + b).orElse("");
    }
    private String limpar(String valor) { return valor.replace('\\', '/').replaceAll("^/+|/+$", ""); }
    private String exigir(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new GitHubIndisponivelException("Resposta do GitHub sem " + campo);
        }
        return valor;
    }

    @Override public void invalidarCache() { cache = null; }

    private record Cache(Instant carregadoEm, List<Projeto> projetos) { }
    private record GitObject(String sha) { }
    private record GitRefResponse(GitObject object) { }
    private record GitCommitResponse(GitObject tree) { }
    private record GitTreeResponse(List<TreeItem> tree, boolean truncated) { }
    private record TreeItem(String path, String type, String sha) { }
    private record GitBlobResponse(String content, String encoding) { }
}

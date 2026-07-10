package com.udescmaker.api.integration.git;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.udescmaker.api.config.UdescMakerProperties;
import com.udescmaker.api.exception.ArquivoInvalidoException;
import com.udescmaker.api.exception.ConfiguracaoException;
import com.udescmaker.api.exception.ConflitoBranchException;
import com.udescmaker.api.exception.GitHubAutenticacaoException;
import com.udescmaker.api.exception.GitHubIndisponivelException;
import com.udescmaker.api.exception.SlugDuplicadoException;

@Component
@Profile("!test")
public class GitHubGitRepositoryGateway implements GitRepositoryGateway {
    private final UdescMakerProperties properties;
    private final RestClient client;

    @Autowired
    public GitHubGitRepositoryGateway(UdescMakerProperties properties, RestClient.Builder builder) {
        this.properties = properties;
        var github = properties.getGithub();
        RestClient.Builder configurado = builder.clone()
                .baseUrl(github.getApiUrl().toString())
                .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github+json")
                .defaultHeader("X-GitHub-Api-Version", "2026-03-10")
                .defaultHeader(HttpHeaders.USER_AGENT, "udescmaker-mobile-api");
        if (!github.getToken().isBlank()) configurado.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + github.getToken());
        this.client = configurado.build();
    }

    GitHubGitRepositoryGateway(UdescMakerProperties properties, RestClient client) {
        this.properties = properties;
        this.client = client;
    }

    @Override
    public ResultadoCommit publicar(String slug, List<ArquivoCommit> arquivos, String mensagem) {
        var github = properties.getGithub();
        if (!github.isPublishEnabled()) throw new ConfiguracaoException("Publicação no GitHub está desabilitada");
        if (!"github".equalsIgnoreCase(properties.getCatalog().getMode())) {
            throw new ConfiguracaoException(
                    "Publicação no GitHub exige UDESCMAKER_CATALOG_MODE=github para manter o catálogo consistente");
        }
        if (github.getToken() == null || github.getToken().isBlank()) {
            throw new ConfiguracaoException("UDESCMAKER_GITHUB_TOKEN não está configurado");
        }
        if (arquivos == null || arquivos.isEmpty()) throw new ArquivoInvalidoException("Publicação sem arquivos");

        ConflitoBranchException ultimoConflito = null;
        for (int tentativa = 0; tentativa < 2; tentativa++) {
            try {
                return publicarTentativa(slug, arquivos, mensagem);
            } catch (ConflitoBranchException conflito) {
                ultimoConflito = conflito;
            }
        }
        throw ultimoConflito == null ? new ConflitoBranchException("Conflito ao atualizar a branch") : ultimoConflito;
    }

    private ResultadoCommit publicarTentativa(String slug, List<ArquivoCommit> arquivos, String mensagem) {
        var github = properties.getGithub();
        try {
            GitRef ref = client.get().uri("/repos/{owner}/{repo}/git/ref/heads/{branch}",
                    github.getOwner(), github.getRepository(), github.getBranch()).retrieve().body(GitRef.class);
            String head = exigir(ref == null || ref.object() == null ? null : ref.object().sha(), "HEAD da branch");
            GitCommit commitBase = client.get().uri("/repos/{owner}/{repo}/git/commits/{sha}",
                    github.getOwner(), github.getRepository(), head).retrieve().body(GitCommit.class);
            String arvoreBase = exigir(commitBase == null || commitBase.tree() == null ? null : commitBase.tree().sha(), "árvore base");

            GitTree arvore = client.get().uri("/repos/{owner}/{repo}/git/trees/{sha}?recursive=1",
                    github.getOwner(), github.getRepository(), arvoreBase).retrieve().body(GitTree.class);
            if (arvore == null || arvore.tree() == null) {
                throw new GitHubIndisponivelException("Resposta do GitHub sem árvore base");
            }
            if (arvore.truncated()) {
                throw new GitHubIndisponivelException(
                        "Árvore base retornada pelo GitHub foi truncada; a publicação foi cancelada com segurança");
            }
            String index = limpar(github.getProjectsPath()) + "/" + slug + "/index.md";
            if (arvore.tree().stream().anyMatch(item -> index.equals(item.path()))) {
                throw new SlugDuplicadoException(slug);
            }

            List<NovoItemArvore> itens = new ArrayList<>();
            for (ArquivoCommit arquivo : arquivos) {
                GitSha blob = client.post().uri("/repos/{owner}/{repo}/git/blobs", github.getOwner(), github.getRepository())
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(Map.of("content", Base64.getEncoder().encodeToString(arquivo.conteudo()), "encoding", "base64"))
                        .retrieve().body(GitSha.class);
                itens.add(new NovoItemArvore(arquivo.caminho(), "100644", "blob", exigir(blob == null ? null : blob.sha(), "blob")));
            }

            GitSha novaArvore = client.post().uri("/repos/{owner}/{repo}/git/trees", github.getOwner(), github.getRepository())
                    .contentType(MediaType.APPLICATION_JSON).body(new CriarArvore(arvoreBase, itens))
                    .retrieve().body(GitSha.class);
            String shaArvore = exigir(novaArvore == null ? null : novaArvore.sha(), "nova árvore");
            GitCommit novoCommit = client.post().uri("/repos/{owner}/{repo}/git/commits", github.getOwner(), github.getRepository())
                    .contentType(MediaType.APPLICATION_JSON).body(new CriarCommit(mensagem, shaArvore, List.of(head)))
                    .retrieve().body(GitCommit.class);
            String shaCommit = exigir(novoCommit == null ? null : novoCommit.sha(), "commit");

            client.patch().uri("/repos/{owner}/{repo}/git/refs/heads/{branch}",
                            github.getOwner(), github.getRepository(), github.getBranch())
                    .contentType(MediaType.APPLICATION_JSON).body(new AtualizarRef(shaCommit, false))
                    .retrieve().toBodilessEntity();
            String url = novoCommit.htmlUrl();
            if (url == null || url.isBlank()) {
                url = "https://github.com/" + github.getOwner() + "/" + github.getRepository() + "/commit/" + shaCommit;
            }
            Instant publicadoEm = novoCommit.committer() == null ? null : novoCommit.committer().date();
            if (publicadoEm == null) throw new GitHubIndisponivelException("Resposta do GitHub sem data/hora do commit");
            return new ResultadoCommit(shaCommit, url, publicadoEm);
        } catch (SlugDuplicadoException exception) {
            throw exception;
        } catch (RestClientResponseException exception) {
            int status = exception.getStatusCode().value();
            if (status == 401 || status == 403) throw new GitHubAutenticacaoException("Token do GitHub inválido ou sem permissão", exception);
            if (status == 409 || status == 422) throw new ConflitoBranchException("A branch mudou durante a publicação", exception);
            throw new GitHubIndisponivelException("Falha do GitHub durante a publicação (HTTP " + status + ")", exception);
        } catch (RestClientException exception) {
            throw new GitHubIndisponivelException("GitHub temporariamente indisponível durante a publicação", exception);
        }
    }

    private String exigir(String valor, String campo) {
        if (valor == null || valor.isBlank()) throw new GitHubIndisponivelException("Resposta do GitHub sem " + campo);
        return valor;
    }
    private String limpar(String valor) { return valor.replace('\\', '/').replaceAll("^/+|/+$", ""); }

    private record GitObject(String sha) { }
    private record GitRef(GitObject object) { }
    private record GitTreeObject(String sha) { }
    private record GitSignature(Instant date) { }
    private record GitCommit(String sha, GitTreeObject tree,
                             @com.fasterxml.jackson.annotation.JsonProperty("html_url") String htmlUrl,
                             GitSignature committer) { }
    private record GitTree(List<GitTreeItem> tree, boolean truncated) { }
    private record GitTreeItem(String path) { }
    private record GitSha(String sha) { }
    private record NovoItemArvore(String path, String mode, String type, String sha) { }
    private record CriarArvore(@com.fasterxml.jackson.annotation.JsonProperty("base_tree") String baseTree,
                               List<NovoItemArvore> tree) { }
    private record CriarCommit(String message, String tree, List<String> parents) { }
    private record AtualizarRef(String sha, boolean force) { }
}

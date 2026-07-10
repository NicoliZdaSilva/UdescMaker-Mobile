package com.udescmaker.api.service;

import com.udescmaker.api.config.UdescMakerProperties;
import com.udescmaker.api.domain.*;
import com.udescmaker.api.dto.*;
import com.udescmaker.api.exception.*;
import com.udescmaker.api.integration.git.GitRepositoryGateway;
import com.udescmaker.api.markdown.*;
import com.udescmaker.api.taxonomy.TipoArquivo;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;
import java.util.function.Function;

@Service
public class ProjetoPublicacaoService {
    private final ProjetoCatalogService catalogo;
    private final GitRepositoryGateway gateway;
    private final ProjetoMarkdownSerializer serializer;
    private final FrontmatterParser parser;
    private final SlugService slugService;
    private final ArquivoUploadService uploads;
    private final UdescMakerProperties properties;
    private final Clock clock;

    @org.springframework.beans.factory.annotation.Autowired
    public ProjetoPublicacaoService(ProjetoCatalogService catalogo, GitRepositoryGateway gateway,
                                    ProjetoMarkdownSerializer serializer, FrontmatterParser parser,
                                    SlugService slugService, ArquivoUploadService uploads,
                                    UdescMakerProperties properties) {
        this(catalogo, gateway, serializer, parser, slugService, uploads, properties, Clock.systemDefaultZone());
    }

    ProjetoPublicacaoService(ProjetoCatalogService catalogo, GitRepositoryGateway gateway,
                             ProjetoMarkdownSerializer serializer, FrontmatterParser parser,
                             SlugService slugService, ArquivoUploadService uploads,
                             UdescMakerProperties properties, Clock clock) {
        this.catalogo = catalogo;
        this.gateway = gateway;
        this.serializer = serializer;
        this.parser = parser;
        this.slugService = slugService;
        this.uploads = uploads;
        this.properties = properties;
        this.clock = clock;
    }

    public ProjetoPublicacaoResponse publicar(ProjetoPublicacaoRequest request, MultipartFile capa,
                                               List<MultipartFile> galeriaArquivos,
                                               List<MultipartFile> passosImagens,
                                               List<MultipartFile> baixaveisArquivos,
                                               List<MultipartFile> arquivosComplementares) {
        String slug = slugService.gerar(request.titulo().trim());
        if (catalogo.buscarPorSlug(slug).isPresent()) throw new SlugDuplicadoException(slug);

        List<MultipartFile> galeriaPartes = lista(galeriaArquivos);
        List<MultipartFile> passoPartes = lista(passosImagens);
        List<MultipartFile> baixavelPartes = lista(baixaveisArquivos);
        List<MultipartFile> arquivoPartes = lista(arquivosComplementares);
        List<ProjetoPublicacaoRequest.GaleriaPublicacao> galeriaMeta = lista(request.galeria());
        List<ProjetoPublicacaoRequest.PassoPublicacao> passosMeta = lista(request.passos());
        List<ProjetoPublicacaoRequest.ArquivoPublicacao> baixaveisMeta = lista(request.baixaveis());
        List<ProjetoPublicacaoRequest.ArquivoPublicacao> arquivosMeta = lista(request.arquivos());

        validarAssociacao("galeria", galeriaMeta.stream().map(ProjetoPublicacaoRequest.GaleriaPublicacao::arquivoIndice).toList(), galeriaPartes.size());
        validarAssociacao("passosImagens", passosMeta.stream().map(ProjetoPublicacaoRequest.PassoPublicacao::imagemArquivoIndice)
                .filter(Objects::nonNull).toList(), passoPartes.size());
        validarAssociacao("baixaveis", baixaveisMeta.stream().map(ProjetoPublicacaoRequest.ArquivoPublicacao::arquivoIndice).toList(), baixavelPartes.size());
        validarAssociacao("arquivos", arquivosMeta.stream().map(ProjetoPublicacaoRequest.ArquivoPublicacao::arquivoIndice).toList(), arquivoPartes.size());

        LinkedHashMap<String, byte[]> conteudos = new LinkedHashMap<>();
        ArquivoUploadService.ArquivoValidado capaLida = uploads.ler(capa, ArquivoUploadService.EXTENSOES_IMAGEM, "capa");
        String nomeCapa = "capa." + normalizarExtensaoImagem(capaLida.extensao());
        conteudos.put(nomeCapa, capaLida.bytes());

        List<ImagemRef> galeria = new ArrayList<>();
        for (int i = 0; i < galeriaMeta.size(); i++) {
            var meta = galeriaMeta.get(i);
            var lido = uploads.ler(galeriaPartes.get(meta.arquivoIndice()), ArquivoUploadService.EXTENSOES_IMAGEM, "galeria[" + i + "]");
            String nome = String.format(Locale.ROOT, "galeria-%02d.%s", i + 1, normalizarExtensaoImagem(lido.extensao()));
            nome = nomeUnico(nome, conteudos.keySet());
            conteudos.put(nome, lido.bytes());
            galeria.add(new ImagemRef("./" + nome, meta.alt().trim()));
        }

        List<PassoProjeto> passos = new ArrayList<>();
        for (int i = 0; i < passosMeta.size(); i++) {
            var meta = passosMeta.get(i);
            String imagem = null;
            if (meta.imagemArquivoIndice() != null) {
                var lido = uploads.ler(passoPartes.get(meta.imagemArquivoIndice()), ArquivoUploadService.EXTENSOES_IMAGEM, "passosImagens[" + meta.imagemArquivoIndice() + "]");
                String nome = String.format(Locale.ROOT, "passo-%02d.%s", i + 1, normalizarExtensaoImagem(lido.extensao()));
                nome = nomeUnico(nome, conteudos.keySet());
                conteudos.put(nome, lido.bytes());
                imagem = "./" + nome;
            }
            passos.add(new PassoProjeto(meta.titulo().trim(), meta.corpo().trim(), imagem));
        }

        List<ArquivoRef> baixaveis = montarArquivos(baixaveisMeta, baixavelPartes, true, conteudos);
        List<ArquivoRef> arquivos = montarArquivos(arquivosMeta, arquivoPartes, false, conteudos);
        uploads.validarTotal(conteudos.values().stream().mapToLong(bytes -> bytes.length).sum());

        var autorRequest = request.autor();
        Autor autor = new Autor(autorRequest.nome().trim(), vazioParaNulo(autorRequest.github()));
        Projeto projeto = new Projeto(slug, request.titulo().trim(), request.resumo().trim(),
                LocalDate.now(clock), autor, request.dificuldade(), request.idadeMinima(), request.duracaoMinutos(),
                request.videoYoutube().trim(), distintos(request.categorias()), tags(request.tags()), false,
                new ImagemRef("./" + nomeCapa, request.capaAlt().trim()), List.copyOf(galeria),
                textos(request.materiais(), false), textos(request.ferramentas(), false), List.copyOf(passos),
                lista(request.dicas()).stream().map(d -> new DicaProjeto(d.tom(), d.texto().trim())).toList(),
                baixaveis, arquivos, List.of(), request.descricaoLonga() == null ? "" : request.descricaoLonga().strip());

        validarContratoAstro(projeto);
        String markdown = serializer.serializar(projeto);
        parser.parse(slug, markdown);
        String base = caminhoBaseSeguro(properties.getGithub().getProjectsPath()) + "/" + slug;
        List<GitRepositoryGateway.ArquivoCommit> commit = new ArrayList<>();
        commit.add(new GitRepositoryGateway.ArquivoCommit(base + "/index.md", markdown.getBytes(StandardCharsets.UTF_8)));
        conteudos.forEach((nome, bytes) -> commit.add(new GitRepositoryGateway.ArquivoCommit(base + "/" + nome, bytes)));

        GitRepositoryGateway.ResultadoCommit resultado = gateway.publicar(slug, List.copyOf(commit),
                "feat(project): publica " + slug + " via aplicativo mobile");
        catalogo.invalidarCache();
        String site = properties.getSiteUrl().toString().replaceAll("/$", "") + "/projetos/" + slug + "/";
        return new ProjetoPublicacaoResponse(slug, base, resultado.sha(), resultado.url(), site, resultado.publicadoEm(),
                "Projeto publicado. O GitHub Pages pode levar alguns instantes para atualizar.");
    }

    private List<ArquivoRef> montarArquivos(List<ProjetoPublicacaoRequest.ArquivoPublicacao> metadados,
                                            List<MultipartFile> partes, boolean baixavel,
                                            LinkedHashMap<String, byte[]> conteudos) {
        List<ArquivoRef> resultado = new ArrayList<>();
        for (int i = 0; i < metadados.size(); i++) {
            var meta = metadados.get(i);
            if (baixavel && !meta.tipo().isBaixavel()) throw new ArquivoInvalidoException("Tipo inválido em baixaveis[" + i + "]");
            if (!baixavel && !meta.tipo().isComplementar()) throw new ArquivoInvalidoException("Tipo inválido em arquivos[" + i + "]");
            Set<String> extensoes = extensoes(meta.tipo(), baixavel);
            var lido = uploads.ler(partes.get(meta.arquivoIndice()), extensoes, (baixavel ? "baixaveis[" : "arquivos[") + i + "]");
            String nome = nomeUnico(lido.nomeSanitizado(), conteudos.keySet());
            conteudos.put(nome, lido.bytes());
            resultado.add(new ArquivoRef(meta.rotulo().trim(), "./" + nome, meta.tipo()));
        }
        return List.copyOf(resultado);
    }

    private Set<String> extensoes(TipoArquivo tipo, boolean baixavel) {
        if (baixavel) return switch (tipo) {
            case PDF -> Set.of("pdf"); case DOC -> Set.of("doc", "docx"); case ZIP -> Set.of("zip");
            default -> Set.of();
        };
        return switch (tipo) {
            case STL -> Set.of("stl"); case JPG -> Set.of("jpg", "jpeg"); case PNG -> Set.of("png");
            case SVG -> Set.of("svg"); case ZIP -> Set.of("zip"); case XLSX -> Set.of("xlsx");
            case OTHER -> Set.of("txt", "csv", "json", "sb3", "webp", "avif"); default -> Set.of();
        };
    }

    private void validarAssociacao(String campo, List<Integer> indices, int quantidadeArquivos) {
        Set<Integer> unicos = new HashSet<>(indices);
        if (unicos.size() != indices.size() || indices.stream().anyMatch(i -> i == null || i < 0 || i >= quantidadeArquivos)
                || unicos.size() != quantidadeArquivos) {
            throw new ArquivoInvalidoException("Associação inválida em " + campo + ": índices devem usar cada arquivo exatamente uma vez");
        }
    }

    private String caminhoBaseSeguro(String caminho) {
        String limpo = caminho == null ? "" : caminho.replace('\\', '/').replaceAll("^/+|/+$", "");
        if (limpo.isBlank() || Arrays.stream(limpo.split("/")).anyMatch(item -> !item.matches("[A-Za-z0-9._-]+") || item.equals(".."))) {
            throw new ConfiguracaoException("UDESCMAKER_GITHUB_PROJECTS_PATH é inválido");
        }
        return limpo;
    }

    private String nomeUnico(String nome, Set<String> existentes) {
        String candidato = nome;
        int ponto = nome.lastIndexOf('.');
        String base = ponto > 0 ? nome.substring(0, ponto) : nome;
        String extensao = ponto > 0 ? nome.substring(ponto) : "";
        for (int sufixo = 2; existentes.contains(candidato) || candidato.equalsIgnoreCase("index.md"); sufixo++) {
            candidato = base + "-" + sufixo + extensao;
        }
        return candidato;
    }

    private String normalizarExtensaoImagem(String extensao) { return extensao.equals("jpeg") ? "jpg" : extensao; }
    private String vazioParaNulo(String valor) { return valor == null || valor.isBlank() ? null : valor.trim(); }

    private List<String> tags(List<String> valores) {
        return textos(valores, true);
    }

    private List<String> textos(List<String> valores, boolean minusculas) {
        if (valores == null) return List.of();
        LinkedHashMap<String, String> unicos = new LinkedHashMap<>();
        for (String valor : valores) {
            if (valor == null || valor.isBlank()) continue;
            String texto = valor.trim().replaceAll("\\s+", " ");
            if (minusculas) texto = texto.toLowerCase(Locale.ROOT);
            unicos.putIfAbsent(texto.toLowerCase(Locale.ROOT), texto);
        }
        return List.copyOf(unicos.values());
    }

    private <T> List<T> distintos(List<T> valores) {
        return valores == null ? List.of() : List.copyOf(new LinkedHashSet<>(valores));
    }

    /**
     * A validação Jakarta ocorre antes da normalização. Esta segunda barreira garante que
     * trim, deduplicação e nomes gerados nunca produzam conteúdo fora do schema do Astro.
     */
    private void validarContratoAstro(Projeto projeto) {
        Map<String, String> erros = new LinkedHashMap<>();
        texto(erros, "titulo", projeto.titulo(), 4, 120);
        texto(erros, "resumo", projeto.resumo(), 12, 180);
        texto(erros, "descricaoLonga", projeto.corpoMarkdown(), 1, 30_000);
        texto(erros, "autor.nome", projeto.autor().nome(), 3, 120);
        if (projeto.idadeMinima() < 0) erros.put("idadeMinima", "deve ser maior ou igual a zero");
        if (projeto.duracaoMinutos() <= 0) erros.put("duracaoMinutos", "deve ser maior que zero");
        if (projeto.categorias().isEmpty()) erros.put("categorias", "selecione ao menos uma categoria");
        if (projeto.tags().isEmpty()) erros.put("tags", "informe ao menos uma tag");
        for (int i = 0; i < projeto.tags().size(); i++) texto(erros, "tags[" + i + "]", projeto.tags().get(i), 2, 40);
        texto(erros, "videoYoutube", projeto.videoYoutube(), 1, 2_048);
        texto(erros, "capa.src", projeto.capa().src(), 4, 500);
        texto(erros, "capa.alt", projeto.capa().alt(), 8, 240);
        for (int i = 0; i < projeto.galeria().size(); i++) {
            texto(erros, "galeria[" + i + "].src", projeto.galeria().get(i).src(), 4, 500);
            texto(erros, "galeria[" + i + "].alt", projeto.galeria().get(i).alt(), 8, 240);
        }
        validarTextos(erros, "materiais", projeto.materiais(), 2, 200);
        validarTextos(erros, "ferramentas", projeto.ferramentas(), 2, 200);
        for (int i = 0; i < projeto.passos().size(); i++) {
            texto(erros, "passos[" + i + "].titulo", projeto.passos().get(i).titulo(), 4, 160);
            texto(erros, "passos[" + i + "].corpo", projeto.passos().get(i).corpo(), 8, 5_000);
            if (projeto.passos().get(i).imagem() != null) {
                texto(erros, "passos[" + i + "].imagem", projeto.passos().get(i).imagem(), 4, 500);
            }
        }
        for (int i = 0; i < projeto.dicas().size(); i++) {
            texto(erros, "dicas[" + i + "].texto", projeto.dicas().get(i).texto(), 4, 1_000);
        }
        validarArquivos(erros, "baixaveis", projeto.baixaveis());
        validarArquivos(erros, "arquivos", projeto.arquivos());
        if (!erros.isEmpty()) throw new ValidacaoPublicacaoException(erros);
    }

    private void validarTextos(Map<String, String> erros, String campo, List<String> valores, int minimo, int maximo) {
        for (int i = 0; i < valores.size(); i++) texto(erros, campo + "[" + i + "]", valores.get(i), minimo, maximo);
    }

    private void validarArquivos(Map<String, String> erros, String campo, List<ArquivoRef> valores) {
        for (int i = 0; i < valores.size(); i++) {
            texto(erros, campo + "[" + i + "].rotulo", valores.get(i).rotulo(), 3, 160);
            texto(erros, campo + "[" + i + "].arquivo", valores.get(i).arquivo(), 4, 500);
        }
    }

    private void texto(Map<String, String> erros, String campo, String valor, int minimo, int maximo) {
        int tamanho = valor == null ? 0 : valor.length();
        if (tamanho < minimo) erros.put(campo, "deve ter ao menos " + minimo + " caracteres após a normalização");
        else if (tamanho > maximo) erros.put(campo, "deve ter no máximo " + maximo + " caracteres");
    }

    private <T> List<T> lista(List<T> valores) { return valores == null ? List.of() : valores; }
}

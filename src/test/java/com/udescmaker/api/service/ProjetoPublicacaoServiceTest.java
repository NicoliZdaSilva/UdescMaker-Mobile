package com.udescmaker.api.service;

import com.udescmaker.api.TestFixtures;
import com.udescmaker.api.config.UdescMakerProperties;
import com.udescmaker.api.domain.Projeto;
import com.udescmaker.api.dto.ProjetoPublicacaoRequest;
import com.udescmaker.api.exception.*;
import com.udescmaker.api.integration.git.GitRepositoryGateway;
import com.udescmaker.api.markdown.*;
import com.udescmaker.api.repository.ProjetoRepository;
import com.udescmaker.api.taxonomy.*;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

class ProjetoPublicacaoServiceTest {
    private static final Instant INSTANTE_COMMIT = Instant.parse("2026-07-10T12:34:56Z");

    @Test
    void publicaMarkdownETodosArquivosEmUmUnicoCommitSemRede() {
        FakeRepository repository = new FakeRepository(List.of());
        FakeGateway gateway = new FakeGateway();
        ProjetoPublicacaoService service = service(repository, gateway);

        var resposta = service.publicar(requestCompleto(), imagem("capa.png"), List.of(imagem("foto.png")),
                List.of(imagem("passo.png")), List.of(pdf("manual.pdf")), List.of(zip("lista.xlsx")));

        assertThat(gateway.chamadas).isEqualTo(1);
        assertThat(gateway.arquivos).hasSize(6).allMatch(a -> a.caminho().startsWith("src/content/projects/projeto-incrivel/"));
        assertThat(gateway.arquivos).extracting(GitRepositoryGateway.ArquivoCommit::caminho)
                .contains("src/content/projects/projeto-incrivel/index.md",
                        "src/content/projects/projeto-incrivel/capa.png",
                        "src/content/projects/projeto-incrivel/galeria-01.png",
                        "src/content/projects/projeto-incrivel/passo-01.png");
        String markdown = gateway.arquivos.stream().filter(a -> a.caminho().endsWith("index.md"))
                .map(a -> new String(a.conteudo(), StandardCharsets.UTF_8)).findFirst().orElseThrow();
        Projeto parseado = new FrontmatterParser().parse("projeto-incrivel", markdown);
        assertThat(parseado.videoYoutube()).isEqualTo("https://youtu.be/dQw4w9WgXcQ");
        assertThat(parseado.relacionados()).isEmpty();
        assertThat(resposta.publicadoEm()).isEqualTo(INSTANTE_COMMIT);
        assertThat(resposta.shaCommit()).isEqualTo("abc123");
        assertThat(repository.invalidacoes).isEqualTo(1);
    }

    @Test
    void rejeitaSlugDuplicadoAntesDoGateway() {
        Projeto existente = TestFixtures.projeto("projeto-incrivel", LocalDate.of(2026, 1, 1),
                Dificuldade.INICIANTE, 10, 60, List.of(CategoriaProjeto.EDUCACAO), List.of("maker"));
        FakeGateway gateway = new FakeGateway();
        ProjetoPublicacaoService service = service(new FakeRepository(List.of(existente)), gateway);
        assertThatThrownBy(() -> service.publicar(requestCompleto(), imagem("capa.png"), List.of(imagem("foto.png")),
                List.of(imagem("passo.png")), List.of(pdf("manual.pdf")), List.of(zip("lista.xlsx"))))
                .isInstanceOf(SlugDuplicadoException.class);
        assertThat(gateway.chamadas).isZero();
    }

    @Test
    void rejeitaAssociacaoDeArquivoForaDoIndice() {
        FakeGateway gateway = new FakeGateway();
        var request = requestCompleto();
        ProjetoPublicacaoService service = service(new FakeRepository(List.of()), gateway);
        assertThatThrownBy(() -> service.publicar(request, imagem("capa.png"), List.of(),
                List.of(imagem("passo.png")), List.of(pdf("manual.pdf")), List.of(zip("lista.xlsx"))))
                .isInstanceOf(ArquivoInvalidoException.class).hasMessageContaining("galeria");
    }

    @Test
    void rejeitaValorQueFicaCurtoDepoisDoTrimAntesDoCommit() {
        FakeGateway gateway = new FakeGateway();
        var original = requestCompleto();
        var request = new ProjetoPublicacaoRequest(" abc ", original.resumo(), original.autor(),
                original.dificuldade(), original.idadeMinima(), original.duracaoMinutos(), original.categorias(),
                original.tags(), original.videoYoutube(), original.capaAlt(), original.descricaoLonga(),
                original.galeria(), original.materiais(), original.ferramentas(), original.passos(), original.dicas(),
                original.baixaveis(), original.arquivos());
        ProjetoPublicacaoService service = service(new FakeRepository(List.of()), gateway);

        assertThatThrownBy(() -> service.publicar(request, imagem("capa.png"), List.of(imagem("foto.png")),
                List.of(imagem("passo.png")), List.of(pdf("manual.pdf")), List.of(zip("lista.xlsx"))))
                .isInstanceOf(ValidacaoPublicacaoException.class).hasMessageContaining("normalização");
        assertThat(gateway.chamadas).isZero();
    }

    @Test
    void rejeitaDescricaoVaziaAntesDoCommit() {
        FakeGateway gateway = new FakeGateway();
        var original = requestCompleto();
        var request = new ProjetoPublicacaoRequest(original.titulo(), original.resumo(), original.autor(),
                original.dificuldade(), original.idadeMinima(), original.duracaoMinutos(), original.categorias(),
                original.tags(), original.videoYoutube(), original.capaAlt(), "   ", original.galeria(),
                original.materiais(), original.ferramentas(), original.passos(), original.dicas(),
                original.baixaveis(), original.arquivos());
        ProjetoPublicacaoService service = service(new FakeRepository(List.of()), gateway);

        assertThatThrownBy(() -> service.publicar(request, imagem("capa.png"), List.of(imagem("foto.png")),
                List.of(imagem("passo.png")), List.of(pdf("manual.pdf")), List.of(zip("lista.xlsx"))))
                .isInstanceOf(ValidacaoPublicacaoException.class)
                .hasMessageContaining("normalização");
        assertThat(gateway.chamadas).isZero();
    }

    private ProjetoPublicacaoService service(FakeRepository repository, FakeGateway gateway) {
        UdescMakerProperties properties = new UdescMakerProperties();
        ProjetoCatalogService catalogo = new ProjetoCatalogService(repository);
        return new ProjetoPublicacaoService(catalogo, gateway, new ProjetoMarkdownSerializer(),
                new FrontmatterParser(), new SlugService(), new ArquivoUploadService(properties), properties,
                Clock.fixed(Instant.parse("2026-07-10T09:00:00Z"), ZoneId.of("America/Sao_Paulo")));
    }

    private ProjetoPublicacaoRequest requestCompleto() {
        return new ProjetoPublicacaoRequest("Projeto incrível", "Resumo válido para o projeto maker",
                new ProjetoPublicacaoRequest.AutorPublicacao("Pessoa Autora", "pessoa-autora"),
                Dificuldade.INICIANTE, 10, 90, List.of(CategoriaProjeto.EDUCACAO, CategoriaProjeto.EDUCACAO),
                List.of("Maker", "maker"), "https://youtu.be/dQw4w9WgXcQ", "Imagem principal acessível",
                "Descrição longa: com caracteres especiais.",
                List.of(new ProjetoPublicacaoRequest.GaleriaPublicacao("Imagem da galeria acessível", 0)),
                List.of("Papelão"), List.of("Tesoura"),
                List.of(new ProjetoPublicacaoRequest.PassoPublicacao("Montagem", "Realize a montagem com cuidado", 0)),
                List.of(new ProjetoPublicacaoRequest.DicaPublicacao(TomDica.WARNING, "Use proteção adequada")),
                List.of(new ProjetoPublicacaoRequest.ArquivoPublicacao("Manual em PDF", TipoArquivo.PDF, 0)),
                List.of(new ProjetoPublicacaoRequest.ArquivoPublicacao("Lista de materiais", TipoArquivo.XLSX, 0)));
    }

    private MockMultipartFile imagem(String nome) {
        return new MockMultipartFile("arquivo", nome, "image/png", new byte[]{(byte) 0x89, 'P', 'N', 'G', 1, 2, 3, 4});
    }
    private MockMultipartFile pdf(String nome) {
        return new MockMultipartFile("arquivo", nome, "application/pdf", "%PDF-1.4 teste".getBytes(StandardCharsets.US_ASCII));
    }
    private MockMultipartFile zip(String nome) {
        return new MockMultipartFile("arquivo", nome, "application/zip", new byte[]{'P', 'K', 3, 4, 1, 2});
    }

    private static class FakeRepository implements ProjetoRepository {
        private final List<Projeto> projetos;
        int invalidacoes;
        FakeRepository(List<Projeto> projetos) { this.projetos = projetos; }
        @Override public List<Projeto> listarTodos() { return projetos; }
        @Override public void invalidarCache() { invalidacoes++; }
    }

    private static class FakeGateway implements GitRepositoryGateway {
        int chamadas;
        List<ArquivoCommit> arquivos = List.of();
        @Override public ResultadoCommit publicar(String slug, List<ArquivoCommit> arquivos, String mensagem) {
            chamadas++;
            this.arquivos = arquivos;
            assertThat(mensagem).isEqualTo("feat(project): publica projeto-incrivel via aplicativo mobile");
            return new ResultadoCommit("abc123", "https://github.test/commit/abc123", INSTANTE_COMMIT);
        }
    }
}

package com.udescmaker.api.repository;

import com.udescmaker.api.TestFixtures;
import com.udescmaker.api.config.UdescMakerProperties;
import com.udescmaker.api.exception.ConfiguracaoException;
import com.udescmaker.api.exception.GitHubIndisponivelException;
import com.udescmaker.api.markdown.FrontmatterParser;
import com.udescmaker.api.markdown.ProjetoMarkdownSerializer;
import com.udescmaker.api.taxonomy.CategoriaProjeto;
import com.udescmaker.api.taxonomy.Dificuldade;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class GitHubProjetoRepositoryTest {
    @TempDir
    Path tempDir;

    @Test
    void resolveRefCommitETreeEUsaCommitImutavelNosAssets() {
        UdescMakerProperties properties = properties();
        RestClient.Builder builder = RestClient.builder().baseUrl("https://api.example.test");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        GitHubProjetoRepository repository = new GitHubProjetoRepository(
                new FrontmatterParser(), properties, builder.build(), java.time.Clock.systemUTC());

        String markdown = new ProjetoMarkdownSerializer().serializar(TestFixtures.projeto(
                "projeto-seguro", LocalDate.of(2026, 7, 10), Dificuldade.INICIANTE,
                8, 60, List.of(CategoriaProjeto.EDUCACAO), List.of("maker")));
        String blob = Base64.getEncoder().encodeToString(markdown.getBytes(StandardCharsets.UTF_8));

        esperarSnapshot(server, false, "[{\"path\":\"src/content/projects/projeto-seguro/index.md\","
                + "\"type\":\"blob\",\"sha\":\"blob-md\"}]");
        server.expect(requestTo("https://api.example.test/repos/owner/catalog/git/blobs/blob-md"))
                .andRespond(withSuccess("{\"content\":\"" + blob + "\",\"encoding\":\"base64\"}",
                        MediaType.APPLICATION_JSON));

        var projetos = repository.listarTodos();

        assertThat(projetos).singleElement().satisfies(projeto ->
                assertThat(projeto.capa().src()).isEqualTo(
                        "https://raw.example.test/owner/catalog/commit-imutavel/"
                                + "src/content/projects/projeto-seguro/capa.png"));
        server.verify();
    }

    @Test
    void rejeitaSnapshotTruncadoAntesDeLerBlobs() {
        UdescMakerProperties properties = properties();
        RestClient.Builder builder = RestClient.builder().baseUrl("https://api.example.test");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        GitHubProjetoRepository repository = new GitHubProjetoRepository(
                new FrontmatterParser(), properties, builder.build(), java.time.Clock.systemUTC());
        esperarSnapshot(server, true, "[]");

        assertThatThrownBy(repository::listarTodos)
                .isInstanceOf(GitHubIndisponivelException.class).hasMessageContaining("truncada");
        server.verify();
    }

    @Test
    void repositorioLocalRejeitaUmaUnicaBarraInvertidaNaReferencia() throws Exception {
        Path projeto = Files.createDirectories(tempDir.resolve("projeto-seguro"));
        String markdown = new ProjetoMarkdownSerializer().serializar(TestFixtures.projeto(
                "projeto-seguro", LocalDate.of(2026, 7, 10), Dificuldade.INICIANTE,
                8, 60, List.of(CategoriaProjeto.EDUCACAO), List.of("maker")))
                .replace("./capa.png", "capa\\segredo.png");
        Files.writeString(projeto.resolve("index.md"), markdown, StandardCharsets.UTF_8);
        UdescMakerProperties properties = new UdescMakerProperties();
        properties.getCatalog().setLocalPath(tempDir);

        LocalProjetoRepository repository = new LocalProjetoRepository(new FrontmatterParser(), properties);

        assertThatThrownBy(repository::listarTodos)
                .isInstanceOf(ConfiguracaoException.class).hasMessageContaining("insegura");
    }

    private UdescMakerProperties properties() {
        UdescMakerProperties properties = new UdescMakerProperties();
        properties.getCatalog().setMode("github");
        properties.getGithub().setOwner("owner");
        properties.getGithub().setRepository("catalog");
        properties.getGithub().setBranch("main");
        properties.getGithub().setProjectsPath("src/content/projects");
        properties.getGithub().setRawUrl(URI.create("https://raw.example.test"));
        return properties;
    }

    private void esperarSnapshot(MockRestServiceServer server, boolean truncated, String tree) {
        server.expect(requestTo("https://api.example.test/repos/owner/catalog/git/ref/heads/main"))
                .andRespond(withSuccess("{\"object\":{\"sha\":\"commit-imutavel\"}}", MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://api.example.test/repos/owner/catalog/git/commits/commit-imutavel"))
                .andRespond(withSuccess("{\"tree\":{\"sha\":\"tree-imutavel\"}}", MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://api.example.test/repos/owner/catalog/git/trees/tree-imutavel?recursive=1"))
                .andRespond(withSuccess("{\"tree\":" + tree + ",\"truncated\":" + truncated + "}",
                        MediaType.APPLICATION_JSON));
    }
}

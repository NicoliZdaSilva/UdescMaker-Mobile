package com.udescmaker.api.integration.git;

import com.udescmaker.api.config.UdescMakerProperties;
import com.udescmaker.api.exception.ConfiguracaoException;
import com.udescmaker.api.exception.GitHubIndisponivelException;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.test.web.client.MockRestServiceServer;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class GitHubGitRepositoryGatewayTest {
    @Test
    void exigeCatalogoGithubAntesDePublicar() {
        UdescMakerProperties properties = properties();
        properties.getCatalog().setMode("local");

        GitHubGitRepositoryGateway gateway = new GitHubGitRepositoryGateway(properties, RestClient.create());

        assertThatThrownBy(() -> gateway.publicar("novo-projeto", arquivos(), "Publica projeto"))
                .isInstanceOf(ConfiguracaoException.class)
                .hasMessageContaining("CATALOG_MODE=github");
    }

    @Test
    void cancelaPublicacaoQuandoArvoreBaseEstaTruncada() {
        UdescMakerProperties properties = properties();
        RestClient.Builder builder = RestClient.builder().baseUrl("https://api.example.test");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        GitHubGitRepositoryGateway gateway = new GitHubGitRepositoryGateway(properties, builder.build());

        server.expect(requestTo("https://api.example.test/repos/owner/catalog/git/ref/heads/main"))
                .andRespond(withSuccess("{\"object\":{\"sha\":\"commit-base\"}}", MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://api.example.test/repos/owner/catalog/git/commits/commit-base"))
                .andRespond(withSuccess("{\"tree\":{\"sha\":\"tree-base\"}}", MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://api.example.test/repos/owner/catalog/git/trees/tree-base?recursive=1"))
                .andRespond(withSuccess("{\"tree\":[],\"truncated\":true}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> gateway.publicar("novo-projeto", arquivos(), "Publica projeto"))
                .isInstanceOf(GitHubIndisponivelException.class)
                .hasMessageContaining("truncada");
        server.verify();
    }

    private UdescMakerProperties properties() {
        UdescMakerProperties properties = new UdescMakerProperties();
        properties.getCatalog().setMode("github");
        properties.getGithub().setOwner("owner");
        properties.getGithub().setRepository("catalog");
        properties.getGithub().setBranch("main");
        properties.getGithub().setToken("token-de-teste");
        properties.getGithub().setPublishEnabled(true);
        return properties;
    }

    private List<GitRepositoryGateway.ArquivoCommit> arquivos() {
        return List.of(new GitRepositoryGateway.ArquivoCommit(
                "src/content/projects/novo-projeto/index.md", "conteudo".getBytes(StandardCharsets.UTF_8)));
    }
}

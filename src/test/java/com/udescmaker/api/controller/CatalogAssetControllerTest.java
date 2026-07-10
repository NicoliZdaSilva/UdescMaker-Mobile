package com.udescmaker.api.controller;

import com.udescmaker.api.config.UdescMakerProperties;
import com.udescmaker.api.exception.RecursoNaoEncontradoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class CatalogAssetControllerTest {
    @TempDir
    Path raiz;

    @Test
    void serveSomenteArquivoRegularDentroDaPastaDoProjeto() throws Exception {
        Path pasta = Files.createDirectories(raiz.resolve("projeto-seguro"));
        Files.writeString(pasta.resolve("capa.png"), "imagem");
        CatalogAssetController controller = controller();

        var response = controller.arquivo("projeto-seguro", "capa.png");

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getFile().toPath().toRealPath())
                .isEqualTo(pasta.resolve("capa.png").toRealPath());
    }

    @Test
    void rejeitaUmaUnicaBarraInvertidaMesmoQuandoExisteComoNomeNoLinux() throws Exception {
        Path pasta = Files.createDirectories(raiz.resolve("projeto-seguro"));
        Files.writeString(pasta.resolve("capa\\segredo.png"), "imagem");

        assertThatThrownBy(() -> controller().arquivo("projeto-seguro", "capa\\segredo.png"))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void rejeitaLinkSimbolicoQueEscapaDaPastaDoProjeto() throws Exception {
        Path pasta = Files.createDirectories(raiz.resolve("projeto-seguro"));
        Path segredo = Files.writeString(raiz.resolve("segredo.pdf"), "segredo");
        try {
            Files.createSymbolicLink(pasta.resolve("material.pdf"), segredo);
        } catch (IOException | UnsupportedOperationException | SecurityException exception) {
            assumeTrue(false, "Sistema de arquivos não permite link simbólico: " + exception.getMessage());
        }

        assertThatThrownBy(() -> controller().arquivo("projeto-seguro", "material.pdf"))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    private CatalogAssetController controller() {
        UdescMakerProperties properties = new UdescMakerProperties();
        properties.getCatalog().setLocalPath(raiz);
        return new CatalogAssetController(properties);
    }
}

package com.udescmaker.api.controller;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TaxonomiaControllerTest {
    @Test
    void todasAsColecoesUsamContratoIdELabelDoAplicativo() {
        var resposta = new TaxonomiaController().taxonomia();

        assertThat(resposta.categorias()).allSatisfy(item -> {
            assertThat(item.id()).isNotBlank();
            assertThat(item.label()).isNotBlank();
        });
        assertThat(resposta.dificuldades()).allSatisfy(item -> assertThat(item.label()).isNotBlank());
        assertThat(resposta.tonsDica()).extracting("id").containsExactly("info", "warning", "success");
        assertThat(resposta.tiposBaixaveis()).extracting("id").containsExactly("pdf", "doc", "zip");
        assertThat(resposta.tiposArquivos()).extracting("id")
                .containsExactly("zip", "stl", "jpg", "png", "svg", "xlsx", "other");
    }
}

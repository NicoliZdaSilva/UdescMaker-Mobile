package com.udescmaker.api.service;

import com.udescmaker.api.TestFixtures;
import com.udescmaker.api.domain.Projeto;
import com.udescmaker.api.repository.ProjetoRepository;
import com.udescmaker.api.taxonomy.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class ProjetoCatalogServiceTest {
    private ProjetoCatalogService service;
    private Projeto idade8;
    private Projeto idade12;
    private Projeto avancado;

    @BeforeEach
    void preparar() {
        idade8 = TestFixtures.projeto("idade-oito", LocalDate.of(2026, 7, 8), Dificuldade.INICIANTE,
                8, 60, List.of(CategoriaProjeto.EDUCACAO), List.of("maker", "escola"));
        idade12 = TestFixtures.projeto("idade-doze", LocalDate.of(2026, 7, 10), Dificuldade.INTERMEDIARIO,
                12, 30, List.of(CategoriaProjeto.ARDUINO), List.of("arduino"));
        avancado = TestFixtures.projeto("avancado", LocalDate.of(2026, 7, 9), Dificuldade.AVANCADO,
                10, 120, List.of(CategoriaProjeto.EDUCACAO), List.of("robotica"));
        service = new ProjetoCatalogService(() -> List.of(idade8, idade12, avancado));
    }

    @Test
    void filtroIdadeUsaIdadeMinimaMenorOuIgual() {
        var resultado = service.filtrar(new ProjetoCatalogService.Filtro(null, List.of(), null, null,
                10, null, OrdenacaoProjeto.RECENTES, null));
        assertThat(resultado).extracting(Projeto::slug).containsExactly("avancado", "idade-oito");
    }

    @Test
    void combinaBuscaTagCategoriaDificuldadeDuracaoELimite() {
        var resultado = service.filtrar(new ProjetoCatalogService.Filtro("execucao", List.of("maker"),
                CategoriaProjeto.EDUCACAO, Dificuldade.INICIANTE, 10, 90, OrdenacaoProjeto.RECENTES, 1));
        assertThat(resultado).containsExactly(idade8);
    }

    @Test
    void implementaAsTresOrdenacoes() {
        assertThat(filtrar(OrdenacaoProjeto.RECENTES)).extracting(Projeto::slug)
                .containsExactly("idade-doze", "avancado", "idade-oito");
        assertThat(filtrar(OrdenacaoProjeto.DURACAO)).extracting(Projeto::slug)
                .containsExactly("idade-doze", "idade-oito", "avancado");
        assertThat(filtrar(OrdenacaoProjeto.DIFICULDADE)).extracting(Projeto::slug)
                .containsExactly("idade-oito", "idade-doze", "avancado");
    }

    @Test
    void relacionadosUsamPesosCincoTresDesempateDataEExcluemZero() {
        Projeto atual = TestFixtures.projeto("atual", LocalDate.of(2026, 7, 1), Dificuldade.INICIANTE, 10, 60,
                List.of(CategoriaProjeto.EDUCACAO), List.of("maker"));
        Projeto categoriaAntigo = TestFixtures.projeto("categoria-antigo", LocalDate.of(2026, 7, 2), Dificuldade.INICIANTE, 10, 60,
                List.of(CategoriaProjeto.EDUCACAO), List.of("outro"));
        Projeto categoriaRecente = TestFixtures.projeto("categoria-recente", LocalDate.of(2026, 7, 5), Dificuldade.INICIANTE, 10, 60,
                List.of(CategoriaProjeto.EDUCACAO), List.of("diferente"));
        Projeto somenteTag = TestFixtures.projeto("somente-tag", LocalDate.of(2026, 7, 9), Dificuldade.INICIANTE, 10, 60,
                List.of(CategoriaProjeto.ARDUINO), List.of("maker"));
        Projeto zero = TestFixtures.projeto("zero", LocalDate.of(2026, 7, 10), Dificuldade.INICIANTE, 10, 60,
                List.of(CategoriaProjeto.COSTURA), List.of("sem-relacao"));
        service = new ProjetoCatalogService(() -> List.of(atual, categoriaAntigo, categoriaRecente, somenteTag, zero));

        assertThat(service.relacionados(atual)).extracting(Projeto::slug)
                .containsExactly("categoria-recente", "categoria-antigo", "somente-tag")
                .doesNotContain("atual", "zero");
        assertThat(service.pontuar(atual, categoriaAntigo)).isEqualTo(5);
        assertThat(service.pontuar(atual, somenteTag)).isEqualTo(3);
        assertThat(service.pontuar(atual, zero)).isZero();
    }

    private List<Projeto> filtrar(OrdenacaoProjeto ordenacao) {
        return service.filtrar(new ProjetoCatalogService.Filtro(null, List.of(), null, null,
                null, null, ordenacao, null));
    }
}

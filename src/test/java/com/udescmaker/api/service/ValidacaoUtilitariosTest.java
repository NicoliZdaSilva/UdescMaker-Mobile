package com.udescmaker.api.service;

import com.udescmaker.api.config.UdescMakerProperties;
import com.udescmaker.api.dto.ProjetoPublicacaoRequest;
import com.udescmaker.api.exception.ArquivoInvalidoException;
import com.udescmaker.api.taxonomy.*;
import com.udescmaker.api.validation.YoutubeUrlValidator;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class ValidacaoUtilitariosTest {
    @Test
    void taxonomiasAceitamIdsDoSite() {
        assertThat(CategoriaProjeto.fromId("impressao-3d")).isEqualTo(CategoriaProjeto.IMPRESSAO_3D);
        assertThat(Dificuldade.fromId("intermediario")).isEqualTo(Dificuldade.INTERMEDIARIO);
        assertThat(TipoArquivo.fromId("xlsx")).isEqualTo(TipoArquivo.XLSX);
        assertThat(TomDica.fromId("success")).isEqualTo(TomDica.SUCCESS);
    }

    @Test
    void slugTransliteraLimpaEImpedeVazio() {
        SlugService service = new SlugService();
        assertThat(service.gerar("  Luminária: ação & Arduino! ")).isEqualTo("luminaria-acao-arduino");
        assertThatThrownBy(() -> service.gerar("東京" )).isInstanceOf(ArquivoInvalidoException.class);
        assertThatThrownBy(() -> service.gerar("CON"))
                .isInstanceOf(ArquivoInvalidoException.class).hasMessageContaining("reservado");
    }

    @Test
    void sanitizaArquivoERejeitaPathTraversal() {
        ArquivoUploadService service = new ArquivoUploadService(new UdescMakerProperties());
        byte[] png = new byte[]{(byte) 0x89, 'P', 'N', 'G', 1, 2, 3, 4};
        var seguro = new MockMultipartFile("capa", "Minha Cápá.PNG", "image/png", png);
        assertThat(service.ler(seguro, ArquivoUploadService.EXTENSOES_IMAGEM, "capa").nomeSanitizado())
                .isEqualTo("minha-capa.png");
        var travessia = new MockMultipartFile("capa", "../segredo.png", "image/png", png);
        assertThatThrownBy(() -> service.ler(travessia, ArquivoUploadService.EXTENSOES_IMAGEM, "capa"))
                .isInstanceOf(ArquivoInvalidoException.class).hasMessageContaining("inseguro");
        var reservado = new MockMultipartFile("capa", "LPT1.PNG", "image/png", png);
        assertThatThrownBy(() -> service.ler(reservado, ArquivoUploadService.EXTENSOES_IMAGEM, "capa"))
                .isInstanceOf(ArquivoInvalidoException.class).hasMessageContaining("reservado");
    }

    @Test
    void resumoNaoPodeExceder180Caracteres() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            var request = new ProjetoPublicacaoRequest("Projeto válido", "x".repeat(181),
                    new ProjetoPublicacaoRequest.AutorPublicacao("Pessoa Autora", ""), Dificuldade.INICIANTE,
                    10, 60, List.of(CategoriaProjeto.EDUCACAO), List.of("maker"),
                    "https://youtu.be/dQw4w9WgXcQ", "Imagem principal acessível", "Descrição",
                    List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of());
            assertThat(validator.validate(request)).anyMatch(erro -> erro.getPropertyPath().toString().equals("resumo"));
        }
    }

    @Test
    void descricaoLongaNaoPodeSerVazia() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            var request = new ProjetoPublicacaoRequest("Projeto válido", "Resumo válido para o projeto",
                    new ProjetoPublicacaoRequest.AutorPublicacao("Pessoa Autora", ""), Dificuldade.INICIANTE,
                    10, 60, List.of(CategoriaProjeto.EDUCACAO), List.of("maker"),
                    "https://youtu.be/dQw4w9WgXcQ", "Imagem principal acessível", "   ",
                    List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of());
            assertThat(validator.validate(request))
                    .anyMatch(erro -> erro.getPropertyPath().toString().equals("descricaoLonga"));
        }
    }

    @Test
    void youtubeAceitaSomenteHttpsWatchShortsOuLinkCurto() {
        YoutubeUrlValidator validator = new YoutubeUrlValidator();
        assertThat(validator.isValid("https://www.youtube.com/watch?v=dQw4w9WgXcQ", null)).isTrue();
        assertThat(validator.isValid("https://youtube.com/shorts/dQw4w9WgXcQ", null)).isTrue();
        assertThat(validator.isValid("https://youtu.be/dQw4w9WgXcQ", null)).isTrue();
        assertThat(validator.isValid("http://youtu.be/dQw4w9WgXcQ", null)).isFalse();
        assertThat(validator.isValid("https://youtube.com/embed/dQw4w9WgXcQ", null)).isFalse();
        assertThat(validator.isValid("https://music.youtube.com/watch?v=dQw4w9WgXcQ", null)).isFalse();
        assertThat(validator.isValid("https://youtu.be/dQw4w9WgXcQ/outro", null)).isFalse();
        assertThat(validator.isValid("https://youtube.com/shorts/dQw4w9WgXcQ/outro", null)).isFalse();
        assertThat(validator.isValid("https://usuario@youtube.com/watch?v=dQw4w9WgXcQ", null)).isFalse();
    }

    @Test
    void listasComElementoNuloOuExcessoSaoInvalidas() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            var request = new ProjetoPublicacaoRequest("Projeto válido", "Resumo válido para o projeto",
                    new ProjetoPublicacaoRequest.AutorPublicacao("Pessoa Autora", ""), Dificuldade.INICIANTE,
                    10, 60, List.of(CategoriaProjeto.EDUCACAO), List.of("maker"),
                    "https://youtu.be/dQw4w9WgXcQ", "Imagem principal acessível", "Descrição",
                    Arrays.asList((ProjetoPublicacaoRequest.GaleriaPublicacao) null), List.of(), List.of(),
                    List.of(), List.of(), List.of(), List.of());
            assertThat(validator.validate(request)).anyMatch(erro -> erro.getPropertyPath().toString().equals("galeria[0].<list element>"));
        }
    }
}

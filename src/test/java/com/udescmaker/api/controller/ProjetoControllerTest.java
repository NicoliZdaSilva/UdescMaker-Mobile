package com.udescmaker.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.udescmaker.api.dto.*;
import com.udescmaker.api.exception.*;
import com.udescmaker.api.mapper.ProjetoMapper;
import com.udescmaker.api.service.*;
import com.udescmaker.api.taxonomy.*;
import org.junit.jupiter.api.*;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ProjetoControllerTest {
    private ProjetoPublicacaoService publicacaoService;
    private MockMvc mvc;
    private ObjectMapper objectMapper;
    private LocalValidatorFactoryBean validator;

    @BeforeEach
    void preparar() {
        publicacaoService = mock(ProjetoPublicacaoService.class);
        objectMapper = new ObjectMapper().findAndRegisterModules();
        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        ProjetoController controller = new ProjetoController(mock(ProjetoCatalogService.class),
                mock(ProjetoMapper.class), publicacaoService, objectMapper, validator);
        mvc = MockMvcBuilders.standaloneSetup(controller).setValidator(validator)
                .setControllerAdvice(new ApiExceptionHandler()).build();
    }

    @Test
    void recebePublicacaoMultipartComParteJsonECapa() throws Exception {
        ProjetoPublicacaoRequest request = request("Resumo válido para publicação");
        var resposta = new ProjetoPublicacaoResponse("projeto", "src/content/projects/projeto", "sha",
                "https://github.test/sha", "https://site.test/projetos/projeto/",
                Instant.parse("2026-07-10T12:00:00Z"), "Publicado");
        when(publicacaoService.publicar(any(), any(), any(), any(), any(), any())).thenReturn(resposta);
        MockMultipartFile json = new MockMultipartFile("projeto", "projeto.json", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request));
        MockMultipartFile capa = new MockMultipartFile("capa", "capa.png", "image/png",
                new byte[]{(byte) 0x89, 'P', 'N', 'G'});

        mvc.perform(multipart("/api/projetos").file(json).file(capa))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.slug").value("projeto"))
                .andExpect(jsonPath("$.shaCommit").value("sha"));
        verify(publicacaoService).publicar(any(), any(), isNull(), isNull(), isNull(), isNull());
    }

    @Test
    void retornaErroConsistenteQuandoResumoExcede180() throws Exception {
        MockMultipartFile json = new MockMultipartFile("projeto", "projeto.json", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request("x".repeat(181))));
        MockMultipartFile capa = new MockMultipartFile("capa", "capa.png", "image/png", new byte[]{1});
        mvc.perform(multipart("/api/projetos").file(json).file(capa))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VALIDACAO"))
                .andExpect(jsonPath("$.campos.resumo").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/api/projetos"));
        verifyNoInteractions(publicacaoService);
    }

    @Test
    void rejeitaJsonMultipartMalformado() throws Exception {
        MockMultipartFile json = new MockMultipartFile("projeto", "", MediaType.TEXT_PLAIN_VALUE,
                "{json-incompleto".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        MockMultipartFile capa = new MockMultipartFile("capa", "capa.png", "image/png", new byte[]{1});

        mvc.perform(multipart("/api/projetos").file(json).file(capa))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("REQUISICAO_INVALIDA"));
        verifyNoInteractions(publicacaoService);
    }

    @Test
    void traduzSlugDuplicadoParaConflito() throws Exception {
        when(publicacaoService.publicar(any(), any(), any(), any(), any(), any()))
                .thenThrow(new SlugDuplicadoException("projeto-valido"));

        mvc.perform(publicacaoValida())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("SLUG_DUPLICADO"))
                .andExpect(jsonPath("$.mensagem").value(org.hamcrest.Matchers.containsString("projeto-valido")));
    }

    @Test
    void traduzIndisponibilidadeDoGitHubSemExporDetalhesInternos() throws Exception {
        when(publicacaoService.publicar(any(), any(), any(), any(), any(), any()))
                .thenThrow(new GitHubIndisponivelException("GitHub temporariamente indisponível"));

        mvc.perform(publicacaoValida())
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.codigo").value("GITHUB_INDISPONIVEL"))
                .andExpect(jsonPath("$.path").value("/api/projetos"));
    }

    private org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder publicacaoValida()
            throws Exception {
        MockMultipartFile json = new MockMultipartFile("projeto", "projeto.json", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request("Resumo válido para publicação")));
        MockMultipartFile capa = new MockMultipartFile("capa", "capa.png", "image/png",
                new byte[]{(byte) 0x89, 'P', 'N', 'G'});
        return multipart("/api/projetos").file(json).file(capa);
    }

    private ProjetoPublicacaoRequest request(String resumo) {
        return new ProjetoPublicacaoRequest("Projeto válido", resumo,
                new ProjetoPublicacaoRequest.AutorPublicacao("Pessoa Autora", ""), Dificuldade.INICIANTE,
                10, 60, List.of(CategoriaProjeto.EDUCACAO), List.of("maker"),
                "https://youtu.be/dQw4w9WgXcQ", "Imagem principal acessível", "Descrição longa",
                List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of());
    }
}

package com.udescmaker.api.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.udescmaker.api.domain.Projeto;
import com.udescmaker.api.dto.ProjetoDetalheDTO;
import com.udescmaker.api.dto.ProjetoResumoDTO;
import com.udescmaker.api.dto.ProjetoPublicacaoRequest;
import com.udescmaker.api.dto.ProjetoPublicacaoResponse;
import com.udescmaker.api.exception.RecursoNaoEncontradoException;
import com.udescmaker.api.mapper.ProjetoMapper;
import com.udescmaker.api.service.ProjetoCatalogService;
import com.udescmaker.api.service.ProjetoPublicacaoService;
import com.udescmaker.api.taxonomy.CategoriaProjeto;
import com.udescmaker.api.taxonomy.Dificuldade;
import com.udescmaker.api.taxonomy.OrdenacaoProjeto;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import com.udescmaker.api.exception.RequisicaoInvalidaException;

import java.util.List;

@RestController
@RequestMapping("/api/projetos")
public class ProjetoController {

    private final ProjetoCatalogService catalogService;
    private final ProjetoMapper projetoMapper;
    private final ProjetoPublicacaoService publicacaoService;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    public ProjetoController(ProjetoCatalogService catalogService,
                             ProjetoMapper projetoMapper,
                             ProjetoPublicacaoService publicacaoService,
                             ObjectMapper objectMapper,
                             Validator validator) {
        this.catalogService = catalogService;
        this.projetoMapper = projetoMapper;
        this.publicacaoService = publicacaoService;
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ProjetoPublicacaoResponse publicar(
            @RequestPart("projeto") String projetoJson,
            @RequestPart("capa") MultipartFile capa,
            @RequestPart(value = "galeria", required = false) List<MultipartFile> galeria,
            @RequestPart(value = "passosImagens", required = false) List<MultipartFile> passosImagens,
            @RequestPart(value = "baixaveis", required = false) List<MultipartFile> baixaveis,
            @RequestPart(value = "arquivos", required = false) List<MultipartFile> arquivos) {
        ProjetoPublicacaoRequest projeto = lerEValidarProjeto(projetoJson);
        return publicacaoService.publicar(projeto, capa, galeria, passosImagens, baixaveis, arquivos);
    }

    private ProjetoPublicacaoRequest lerEValidarProjeto(String json) {
        if (json == null || json.isBlank() || json.length() > 262_144) {
            throw new RequisicaoInvalidaException("A parte projeto deve conter um JSON válido e limitado a 256 KiB");
        }
        try {
            ProjetoPublicacaoRequest projeto = objectMapper.readValue(json, ProjetoPublicacaoRequest.class);
            if (projeto == null) throw new RequisicaoInvalidaException("A parte projeto não pode ser nula");
            var violacoes = validator.validate(projeto);
            if (!violacoes.isEmpty()) throw new ConstraintViolationException(violacoes);
            return projeto;
        } catch (JsonProcessingException exception) {
            throw new RequisicaoInvalidaException("JSON inválido na parte projeto", exception);
        }
    }

    @GetMapping
    public List<ProjetoResumoDTO> listar(
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String dificuldade,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(required = false) Integer idade,
            @RequestParam(required = false) Integer idadeMinima,
            @RequestParam(required = false) Integer duracaoMaxima,
            @RequestParam(required = false) String busca,
            @RequestParam(required = false) String ordenacao,
            @RequestParam(required = false) Integer limite) {

        CategoriaProjeto categoriaProjeto = null;

        if (categoria != null) {
            categoriaProjeto = CategoriaProjeto.fromId(categoria);
        }

        Dificuldade dificuldadeProjeto = dificuldade == null ? null : Dificuldade.fromId(dificuldade);
        Integer idadeEfetiva = idade != null ? idade : idadeMinima;

        return catalogService
                .filtrar(new ProjetoCatalogService.Filtro(busca, tags, categoriaProjeto, dificuldadeProjeto,
                        idadeEfetiva, duracaoMaxima, OrdenacaoProjeto.fromId(ordenacao), limite))
                .stream()
                .map(projetoMapper::toResumo)
                .toList();
    }

    @GetMapping("/destaques")
    public List<ProjetoResumoDTO> listarDestaques() {
        return catalogService
                .listarDestaques()
                .stream()
                .map(projetoMapper::toResumo)
                .toList();
    }

    @GetMapping("/{slug}")
    public ProjetoDetalheDTO detalhe(@PathVariable String slug) {

        Projeto projeto = catalogService.buscarPorSlug(slug)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException("Projeto não encontrado: " + slug));

        return projetoMapper.toDetalhe(projeto, catalogService.relacionados(projeto));
    }
}

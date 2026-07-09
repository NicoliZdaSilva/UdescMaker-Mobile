package com.udescmaker.api.controller;

import com.udescmaker.api.domain.Projeto;
import com.udescmaker.api.dto.ProjetoDetalheDTO;
import com.udescmaker.api.dto.ProjetoResumoDTO;
import com.udescmaker.api.exception.RecursoNaoEncontradoException;
import com.udescmaker.api.mapper.ProjetoMapper;
import com.udescmaker.api.service.ProjetoCatalogService;
import com.udescmaker.api.taxonomy.CategoriaProjeto;
import com.udescmaker.api.taxonomy.Dificuldade;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projetos")
public class ProjetoController {

    private final ProjetoCatalogService catalogService;
    private final ProjetoMapper projetoMapper;

    public ProjetoController(ProjetoCatalogService catalogService,
                             ProjetoMapper projetoMapper) {
        this.catalogService = catalogService;
        this.projetoMapper = projetoMapper;
    }

    @GetMapping
    public List<ProjetoResumoDTO> listar(
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) Dificuldade dificuldade,
            @RequestParam(required = false) Integer idadeMinima,
            @RequestParam(required = false) Integer duracaoMaxima,
            @RequestParam(required = false) String busca) {

        CategoriaProjeto categoriaProjeto = null;

        if (categoria != null) {
            categoriaProjeto = CategoriaProjeto.fromId(categoria);
        }

        return catalogService
                .filtrar(busca, categoriaProjeto, dificuldade, idadeMinima, duracaoMaxima)
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

        return projetoMapper.toDetalhe(projeto);
    }
}
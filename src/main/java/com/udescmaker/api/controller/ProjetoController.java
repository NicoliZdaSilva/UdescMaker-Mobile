package com.udescmaker.api.controller;

import com.udescmaker.api.domain.Projeto;
import com.udescmaker.api.exception.RecursoNaoEncontradoException;
import com.udescmaker.api.service.ProjetoCatalogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/projetos")
public class ProjetoController {
    private final ProjetoCatalogService catalogService;

    public ProjetoController(ProjetoCatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping
    public List<Projeto> listar() {
        return catalogService.listarTodos();
    }

    @GetMapping("/{slug}")
    public Projeto detalhe(@PathVariable String slug) {
        return catalogService.buscarPorSlug(slug)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Projeto não encontrado: " + slug));
    }
}

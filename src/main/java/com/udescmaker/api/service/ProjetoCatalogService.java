package com.udescmaker.api.service;

import com.udescmaker.api.domain.Projeto;
import com.udescmaker.api.repository.ProjetoRepository;
import com.udescmaker.api.taxonomy.CategoriaProjeto;
import com.udescmaker.api.taxonomy.Dificuldade;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProjetoCatalogService {

    private final ProjetoRepository repository;

    public ProjetoCatalogService(ProjetoRepository repository) {
        this.repository = repository;
    }

    public List<Projeto> listarTodos() {
        return repository.listarTodos();
    }

    public Optional<Projeto> buscarPorSlug(String slug) {
        return repository.listarTodos().stream()
                .filter(p -> p.slug().equals(slug))
                .findFirst();
    }

    public List<Projeto> filtrar(
            String busca,
            CategoriaProjeto categoria,
            Dificuldade dificuldade,
            Integer idadeMinima,
            Integer duracaoMaxima) {

        return repository.listarTodos().stream()
                .filter(p -> categoria == null || p.categorias().contains(categoria))
                .filter(p -> dificuldade == null || p.dificuldade() == dificuldade)
                .filter(p -> idadeMinima == null || p.idadeMinima() >= idadeMinima)
                .filter(p -> duracaoMaxima == null || p.duracaoMinutos() <= duracaoMaxima)
                .filter(p -> busca == null || busca.isBlank()
                        || p.titulo().toLowerCase().contains(busca.toLowerCase())
                        || p.resumo().toLowerCase().contains(busca.toLowerCase())
                        || p.tags().stream()
                        .anyMatch(tag -> tag.toLowerCase().contains(busca.toLowerCase())))
                .toList();
    }

    public List<Projeto> listarDestaques() {
        return repository.listarTodos().stream()
                .filter(Projeto::destaque)
                .toList();
    }
}
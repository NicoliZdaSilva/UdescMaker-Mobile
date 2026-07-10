package com.udescmaker.api.repository;

import com.udescmaker.api.domain.Projeto;

import java.util.List;
import java.util.Optional;

public interface ProjetoRepository {
    List<Projeto> listarTodos();

    default Optional<Projeto> buscarPorSlug(String slug) {
        return listarTodos().stream().filter(projeto -> projeto.slug().equals(slug)).findFirst();
    }

    default boolean existe(String slug) { return buscarPorSlug(slug).isPresent(); }

    default void invalidarCache() { }
}

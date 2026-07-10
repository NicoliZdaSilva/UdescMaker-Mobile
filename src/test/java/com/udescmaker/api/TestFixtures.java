package com.udescmaker.api;

import com.udescmaker.api.domain.*;
import com.udescmaker.api.taxonomy.*;

import java.time.LocalDate;
import java.util.List;

public final class TestFixtures {
    private TestFixtures() { }

    public static Projeto projeto(String slug, LocalDate data, Dificuldade dificuldade, int idade,
                                  int duracao, List<CategoriaProjeto> categorias, List<String> tags) {
        return new Projeto(slug, "Projeto " + slug, "Resumo suficientemente longo para teste", data,
                new Autor("Pessoa Autora", "autora"), dificuldade, idade, duracao,
                "https://www.youtube.com/watch?v=dQw4w9WgXcQ", categorias, tags, false,
                new ImagemRef("./capa.png", "Imagem de capa acessível"), List.of(),
                List.of("Madeira reciclada"), List.of("Martelo"),
                List.of(new PassoProjeto("Preparar", "Prepare todos os materiais", null)),
                List.of(new DicaProjeto(TomDica.INFO, "Faça com cuidado")), List.of(), List.of(), List.of(),
                "Descrição longa sobre educação maker e execução do projeto.");
    }
}

package com.udescmaker.api.dto;

import com.udescmaker.api.domain.ImagemRef;
import com.udescmaker.api.domain.Autor;
import com.udescmaker.api.taxonomy.CategoriaProjeto;
import com.udescmaker.api.taxonomy.Dificuldade;

import java.time.LocalDate;
import java.util.List;

public record ProjetoResumoDTO(
        String slug,
        String titulo,
        String resumo,
        LocalDate publicadoEm,
        Autor autor,
        Dificuldade dificuldade,
        int idadeMinima,
        int duracaoMinutos,
        ImagemRef capa,
        List<CategoriaProjeto> categorias,
        List<String> tags,
        boolean destaque
) {
}

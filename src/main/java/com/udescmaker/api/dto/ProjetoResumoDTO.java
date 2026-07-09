package com.udescmaker.api.dto;

import com.udescmaker.api.domain.ImagemRef;
import com.udescmaker.api.taxonomy.Dificuldade;

public record ProjetoResumoDTO(
        String slug,
        String titulo,
        String resumo,
        Dificuldade dificuldade,
        ImagemRef capa,
        boolean destaque
) {
}

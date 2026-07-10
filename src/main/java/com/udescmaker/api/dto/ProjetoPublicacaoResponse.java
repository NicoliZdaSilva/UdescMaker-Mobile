package com.udescmaker.api.dto;

import java.time.Instant;

public record ProjetoPublicacaoResponse(
        String slug,
        String caminho,
        String shaCommit,
        String urlCommit,
        String urlProjeto,
        Instant publicadoEm,
        String mensagem
) {}

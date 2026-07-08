package com.udescmaker.api.dto;
import java.util.List;

public record TaxonomiaResponse(
    List<OpcaoDTO> categorias,
    List<OpcaoDTO> dificuldades
) {
        public record OpcaoDTO(String id, String label) {
        }
}

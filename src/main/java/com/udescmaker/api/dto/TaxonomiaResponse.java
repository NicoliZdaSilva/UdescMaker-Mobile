package com.udescmaker.api.dto;
import java.util.List;

public record TaxonomiaResponse(
    List<OpcaoDTO> categorias,
    List<OpcaoDTO> dificuldades,
    List<OpcaoDTO> tonsDica,
    List<OpcaoDTO> tiposBaixaveis,
    List<OpcaoDTO> tiposArquivos
) {
        public record OpcaoDTO(String id, String label) {
        }
}

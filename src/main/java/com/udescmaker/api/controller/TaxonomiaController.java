package com.udescmaker.api.controller;

import com.udescmaker.api.dto.TaxonomiaResponse;
import com.udescmaker.api.taxonomy.CategoriaProjeto;
import com.udescmaker.api.taxonomy.Dificuldade;
import com.udescmaker.api.taxonomy.TipoArquivo;
import com.udescmaker.api.taxonomy.TomDica;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
public class TaxonomiaController {
    @GetMapping("/api/taxonomia")
    public TaxonomiaResponse taxonomia() {
        List<TaxonomiaResponse.OpcaoDTO> categorias = Arrays.stream(CategoriaProjeto.values())
                .map(c -> new TaxonomiaResponse.OpcaoDTO(c.getId(), c.getLabel()))
                .toList();

        List<TaxonomiaResponse.OpcaoDTO> dificuldades = Arrays.stream(Dificuldade.values())
                .map(d -> new TaxonomiaResponse.OpcaoDTO(d.getId(), d.getLabel()))
                .toList();

        return new TaxonomiaResponse(categorias, dificuldades,
                Arrays.stream(TomDica.values()).map(t -> new TaxonomiaResponse.OpcaoDTO(t.getId(), t.getLabel())).toList(),
                Arrays.stream(TipoArquivo.values()).filter(TipoArquivo::isBaixavel)
                        .map(t -> new TaxonomiaResponse.OpcaoDTO(t.getId(), t.getLabel())).toList(),
                Arrays.stream(TipoArquivo.values()).filter(TipoArquivo::isComplementar)
                        .map(t -> new TaxonomiaResponse.OpcaoDTO(t.getId(), t.getLabel())).toList());
    }
}

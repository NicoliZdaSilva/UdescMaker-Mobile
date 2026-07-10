package com.udescmaker.api.taxonomy;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum TomDica {
    INFO("info", "Informação"), WARNING("warning", "Atenção"), SUCCESS("success", "Sucesso");

    private final String id;
    private final String label;
    TomDica(String id, String label) { this.id = id; this.label = label; }
    @JsonValue public String getId() { return id; }
    public String getLabel() { return label; }
    @JsonCreator public static TomDica fromId(String id) {
        return Arrays.stream(values())
                .filter(value -> value.id.equalsIgnoreCase(id == null ? "" : id.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Tom de dica desconhecido: " + id));
    }
}

package com.udescmaker.api.taxonomy;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum TipoArquivo {
    PDF("pdf", "PDF"), DOC("doc", "Documento"), ZIP("zip", "ZIP"), STL("stl", "STL"),
    JPG("jpg", "JPG"), PNG("png", "PNG"), SVG("svg", "SVG"), XLSX("xlsx", "XLSX"),
    OTHER("other", "Outro");

    private final String id;
    private final String label;
    TipoArquivo(String id, String label) { this.id = id; this.label = label; }
    @JsonValue public String getId() { return id; }
    public String getLabel() { return label; }

    @JsonCreator
    public static TipoArquivo fromId(String id) {
        return Arrays.stream(values())
                .filter(value -> value.id.equalsIgnoreCase(id == null ? "" : id.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Tipo de arquivo desconhecido: " + id));
    }

    public boolean isBaixavel() { return this == PDF || this == DOC || this == ZIP; }
    public boolean isComplementar() {
        return this == STL || this == JPG || this == PNG || this == SVG || this == ZIP || this == XLSX || this == OTHER;
    }
}

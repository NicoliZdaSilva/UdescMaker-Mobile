package com.udescmaker.api.taxonomy;

public enum OrdenacaoProjeto {
    RECENTES("recentes"), DURACAO("duracao"), DIFICULDADE("dificuldade");

    private final String id;
    OrdenacaoProjeto(String id) { this.id = id; }
    public String getId() { return id; }

    public static OrdenacaoProjeto fromId(String id) {
        if (id == null || id.isBlank()) return RECENTES;
        for (OrdenacaoProjeto value : values()) {
            if (value.id.equalsIgnoreCase(id.trim())) return value;
        }
        throw new IllegalArgumentException("Ordenação desconhecida: " + id);
    }
}

package com.udescmaker.api.taxonomy;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Dificuldade {
    INICIANTE("iniciante", "Iniciante"),
    INTERMEDIARIO("intermediario", "Intermediário"),
    AVANCADO("avancado", "Avançado");

    private final String id;
    private final String label;

    Dificuldade(String id, String label){
        this.id = id;
        this.label = label;
    }

    @JsonValue
    public String getId(){
        return id;
    }

    public String getLabel(){
        return label;
    }

    @JsonCreator
    public static Dificuldade fromId(String id){
        if (id == null) {
            throw new IllegalArgumentException("Dificuldade não pode ser nula");
        }
        for (Dificuldade dificuldade : values()){
            if (dificuldade.getId().equalsIgnoreCase(id)){
                return dificuldade;
            }
        }
        throw new IllegalArgumentException("Dificuldade não encontrada: "+ id);
    }
}

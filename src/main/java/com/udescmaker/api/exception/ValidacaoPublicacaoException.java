package com.udescmaker.api.exception;

import java.util.LinkedHashMap;
import java.util.Map;

public class ValidacaoPublicacaoException extends RuntimeException {
    private final Map<String, String> campos;

    public ValidacaoPublicacaoException(Map<String, String> campos) {
        super("Existem campos inválidos após a normalização");
        this.campos = Map.copyOf(new LinkedHashMap<>(campos));
    }

    public Map<String, String> getCampos() {
        return campos;
    }
}

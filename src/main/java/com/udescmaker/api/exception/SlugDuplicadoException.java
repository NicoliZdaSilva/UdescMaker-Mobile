package com.udescmaker.api.exception;

public class SlugDuplicadoException extends RuntimeException {
    public SlugDuplicadoException(String slug) { super("Já existe um projeto com o slug: " + slug); }
}

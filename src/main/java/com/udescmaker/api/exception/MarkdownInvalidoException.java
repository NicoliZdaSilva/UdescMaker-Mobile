package com.udescmaker.api.exception;

public class MarkdownInvalidoException extends RuntimeException {
    public MarkdownInvalidoException(String mensagem) { super(mensagem); }
    public MarkdownInvalidoException(String mensagem, Throwable causa) { super(mensagem, causa); }
}

package com.udescmaker.api.exception;

public class RequisicaoInvalidaException extends RuntimeException {
    public RequisicaoInvalidaException(String mensagem) { super(mensagem); }
    public RequisicaoInvalidaException(String mensagem, Throwable causa) { super(mensagem, causa); }
}

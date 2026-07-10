package com.udescmaker.api.exception;

public class ConfiguracaoException extends RuntimeException {
    public ConfiguracaoException(String mensagem) { super(mensagem); }
    public ConfiguracaoException(String mensagem, Throwable causa) { super(mensagem, causa); }
}

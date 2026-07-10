package com.udescmaker.api.exception;

public class GitHubAutenticacaoException extends RuntimeException {
    public GitHubAutenticacaoException(String mensagem) { super(mensagem); }
    public GitHubAutenticacaoException(String mensagem, Throwable causa) { super(mensagem, causa); }
}

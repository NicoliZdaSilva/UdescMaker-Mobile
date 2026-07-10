package com.udescmaker.api.exception;

public class GitHubIndisponivelException extends RuntimeException {
    public GitHubIndisponivelException(String mensagem) { super(mensagem); }
    public GitHubIndisponivelException(String mensagem, Throwable causa) { super(mensagem, causa); }
}

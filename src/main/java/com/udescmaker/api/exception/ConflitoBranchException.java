package com.udescmaker.api.exception;

public class ConflitoBranchException extends RuntimeException {
    public ConflitoBranchException(String mensagem) { super(mensagem); }
    public ConflitoBranchException(String mensagem, Throwable causa) { super(mensagem, causa); }
}

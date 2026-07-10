package com.udescmaker.api.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.time.Instant;
import java.util.*;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validacao(MethodArgumentNotValidException exception, HttpServletRequest request) {
        Map<String, String> campos = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(erro -> campos.putIfAbsent(erro.getField(), erro.getDefaultMessage()));
        return resposta(HttpStatus.BAD_REQUEST, "VALIDACAO", "Existem campos inválidos", campos, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> restricao(ConstraintViolationException exception, HttpServletRequest request) {
        Map<String, String> campos = new LinkedHashMap<>();
        exception.getConstraintViolations().forEach(erro -> campos.put(erro.getPropertyPath().toString(), erro.getMessage()));
        return resposta(HttpStatus.BAD_REQUEST, "VALIDACAO", "Existem campos inválidos", campos, request);
    }

    @ExceptionHandler(ValidacaoPublicacaoException.class)
    public ResponseEntity<ApiError> validacaoNormalizada(ValidacaoPublicacaoException exception,
                                                          HttpServletRequest request) {
        return resposta(HttpStatus.BAD_REQUEST, "VALIDACAO", exception.getMessage(), exception.getCampos(), request);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MissingServletRequestPartException.class,
            RequisicaoInvalidaException.class})
    public ResponseEntity<ApiError> requisicaoInvalida(Exception exception, HttpServletRequest request) {
        return resposta(HttpStatus.BAD_REQUEST, "REQUISICAO_INVALIDA", "JSON ou partes multipart ausentes/inválidos", Map.of(), request);
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ApiError> naoEncontrado(RecursoNaoEncontradoException exception, HttpServletRequest request) {
        return resposta(HttpStatus.NOT_FOUND, "NAO_ENCONTRADO", exception.getMessage(), Map.of(), request);
    }

    @ExceptionHandler(SlugDuplicadoException.class)
    public ResponseEntity<ApiError> duplicado(SlugDuplicadoException exception, HttpServletRequest request) {
        return resposta(HttpStatus.CONFLICT, "SLUG_DUPLICADO", exception.getMessage(), Map.of(), request);
    }

    @ExceptionHandler(ConflitoBranchException.class)
    public ResponseEntity<ApiError> conflito(ConflitoBranchException exception, HttpServletRequest request) {
        return resposta(HttpStatus.CONFLICT, "CONFLITO_BRANCH", exception.getMessage(), Map.of(), request);
    }

    @ExceptionHandler(ArquivoInvalidoException.class)
    public ResponseEntity<ApiError> arquivo(ArquivoInvalidoException exception, HttpServletRequest request) {
        return resposta(HttpStatus.UNPROCESSABLE_ENTITY, "ARQUIVO_INVALIDO", exception.getMessage(), Map.of(), request);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiError> arquivoGrande(MaxUploadSizeExceededException exception, HttpServletRequest request) {
        return resposta(HttpStatus.PAYLOAD_TOO_LARGE, "ARQUIVO_MUITO_GRANDE", "O upload excede o tamanho configurado", Map.of(), request);
    }

    @ExceptionHandler(MarkdownInvalidoException.class)
    public ResponseEntity<ApiError> markdown(MarkdownInvalidoException exception, HttpServletRequest request) {
        return resposta(HttpStatus.UNPROCESSABLE_ENTITY, "MARKDOWN_INVALIDO", exception.getMessage(), Map.of(), request);
    }

    @ExceptionHandler(ConfiguracaoException.class)
    public ResponseEntity<ApiError> configuracao(ConfiguracaoException exception, HttpServletRequest request) {
        return resposta(HttpStatus.SERVICE_UNAVAILABLE, "CONFIGURACAO_AUSENTE", exception.getMessage(), Map.of(), request);
    }

    @ExceptionHandler(GitHubAutenticacaoException.class)
    public ResponseEntity<ApiError> autenticacao(GitHubAutenticacaoException exception, HttpServletRequest request) {
        return resposta(HttpStatus.BAD_GATEWAY, "GITHUB_AUTENTICACAO", exception.getMessage(), Map.of(), request);
    }

    @ExceptionHandler(GitHubIndisponivelException.class)
    public ResponseEntity<ApiError> github(GitHubIndisponivelException exception, HttpServletRequest request) {
        return resposta(HttpStatus.SERVICE_UNAVAILABLE, "GITHUB_INDISPONIVEL", exception.getMessage(), Map.of(), request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> argumento(IllegalArgumentException exception, HttpServletRequest request) {
        return resposta(HttpStatus.BAD_REQUEST, "PARAMETRO_INVALIDO", exception.getMessage(), Map.of(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> inesperado(Exception exception, HttpServletRequest request) {
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR, "ERRO_INTERNO", "Ocorreu um erro inesperado", Map.of(), request);
    }

    private ResponseEntity<ApiError> resposta(HttpStatus status, String codigo, String mensagem,
                                               Map<String, String> campos, HttpServletRequest request) {
        return ResponseEntity.status(status).body(new ApiError(codigo, mensagem, campos, Instant.now(), request.getRequestURI()));
    }

    public record ApiError(String codigo, String mensagem, Map<String, String> campos, Instant timestamp, String path) { }
}

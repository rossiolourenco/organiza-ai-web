package br.com.organizaai.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.util.Map;

@RestControllerAdvice
public class ErrosApi {
    private static final Logger log = LoggerFactory.getLogger(ErrosApi.class);

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<?> invalido(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("message",
            e.getMessage() == null ? "Dados inválidos." : e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<?> validacao(MethodArgumentNotValidException e) {
        return ResponseEntity.badRequest().body(Map.of("message", "Confira os campos informados."));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MissingServletRequestParameterException.class,
        MethodArgumentTypeMismatchException.class})
    ResponseEntity<?> requisicaoInvalida(Exception e) {
        return ResponseEntity.badRequest().body(Map.of("message", "Requisição inválida."));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<?> conflito(DataIntegrityViolationException e) {
        return ResponseEntity.status(409).body(Map.of("message", "Registro duplicado ou associação inválida."));
    }

    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<?> status(ResponseStatusException e) {
        String mensagem = e.getReason() == null ? "Operação não permitida." : e.getReason();
        return ResponseEntity.status(e.getStatusCode()).body(Map.of("message", mensagem));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<?> inesperado(Exception e) {
        log.error("Erro interno não tratado", e);
        return ResponseEntity.status(500).body(Map.of("message", "Erro interno. Tente novamente mais tarde."));
    }
}

package br.com.organizaai.backend.dto;

import java.time.Instant;

public record TentativaResponse(Long id, String email, boolean sucesso, String ip,
    String userAgent, String motivo, Instant criadoEm) {}

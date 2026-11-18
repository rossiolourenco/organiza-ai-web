package br.com.organizaai.backend.dto;

import java.time.Instant;

public record AuditoriaResponse(Long id, Instant criadoEm, Long atorId, String atorEmail,
    String atorPapel, String acao, String recurso, String recursoId, String resultado,
    String ip, String userAgent, String detalhes, String correlacao) {}

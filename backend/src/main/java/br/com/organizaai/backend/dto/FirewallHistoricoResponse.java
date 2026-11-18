package br.com.organizaai.backend.dto;

import java.time.Instant;

public record FirewallHistoricoResponse(Long id, Instant criadoEm, String atorEmail,
    String operacao, Long regraId, String detalhes, String resultado) {}

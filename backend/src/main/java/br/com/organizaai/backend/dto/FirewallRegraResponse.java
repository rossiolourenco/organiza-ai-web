package br.com.organizaai.backend.dto;

import java.time.Instant;

public record FirewallRegraResponse(Long id, String nome, String descricao, String ipCidr,
    Integer porta, String protocolo, String direcao, String acao, int prioridade,
    boolean ativa, Instant criadoEm, Instant atualizadoEm, String criadoPor, String atualizadoPor) {}

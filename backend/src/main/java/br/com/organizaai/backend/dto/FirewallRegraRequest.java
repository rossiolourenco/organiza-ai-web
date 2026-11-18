package br.com.organizaai.backend.dto;

public record FirewallRegraRequest(String nome, String descricao, String ipCidr, Integer porta,
    String protocolo, String direcao, String acao, Integer prioridade, Boolean ativa) {}

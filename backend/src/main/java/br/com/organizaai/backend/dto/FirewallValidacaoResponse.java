package br.com.organizaai.backend.dto;

import java.util.List;

public record FirewallValidacaoResponse(boolean valido, List<Conflito> conflitos) {
    public record Conflito(Long regraA, String nomeA, Long regraB, String nomeB, String motivo) {}
}

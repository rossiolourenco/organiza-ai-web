package br.com.organizaai.backend.dto;

import java.util.List;

public record SegurancaOverviewResponse(long contasAtivas, long contasBloqueadas,
    long falhas24h, long sucessos24h, long sessoesAtivas, List<Alerta> alertas,
    List<AuditoriaResponse> ultimosEventos, PoliticaResponse politica) {
    public record Alerta(String nivel, String titulo, String detalhe) {}
}

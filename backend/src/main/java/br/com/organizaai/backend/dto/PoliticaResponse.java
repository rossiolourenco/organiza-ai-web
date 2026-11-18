package br.com.organizaai.backend.dto;

import java.time.Instant;

public record PoliticaResponse(int senhaMinima, boolean exigirNumero, boolean exigirMaiuscula,
    int maxTentativas, int janelaMinutos, int bloqueioMinutos, int sessaoDias,
    Instant atualizadoEm, String atualizadoPor) {}

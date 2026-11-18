package br.com.organizaai.backend.dto;

import java.time.Instant;

public record FirewallEstadoResponse(String modo, boolean demo, boolean aplicado,
    Instant aplicadoEm, Long snapshotAtualId, int totalRegras, int regrasAtivas) {}

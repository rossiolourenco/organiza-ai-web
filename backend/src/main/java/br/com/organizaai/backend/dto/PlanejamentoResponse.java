package br.com.organizaai.backend.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record PlanejamentoResponse(
    Long id,
    LocalDate data,
    List<PlanejamentoItem> itens,
    Long versao,
    Instant atualizadoEm
) {}

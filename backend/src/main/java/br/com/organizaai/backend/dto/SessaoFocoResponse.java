package br.com.organizaai.backend.dto;

import java.time.Instant;
import java.time.LocalDate;

public record SessaoFocoResponse(
    Long id,
    Integer minutos,
    LocalDate data,
    Instant iniciadaEm,
    Long tarefaId
) {}

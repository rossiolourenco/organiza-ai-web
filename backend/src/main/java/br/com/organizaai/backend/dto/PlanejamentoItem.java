package br.com.organizaai.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

/** Item do plano diário: tarefa, ordem e estimativa de minutos. */
public record PlanejamentoItem(
    @Positive long tarefaId,
    @PositiveOrZero int ordem,
    @Min(1) @Max(1440) int minutosEstimados
) {}

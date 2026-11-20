package br.com.organizaai.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/** Registro de uma sessão de foco concluída. */
public record SessaoFocoRequest(
    @NotNull @Min(1) @Max(1440) Integer minutos,
    Long tarefaId,
    @NotNull LocalDate data
) {}

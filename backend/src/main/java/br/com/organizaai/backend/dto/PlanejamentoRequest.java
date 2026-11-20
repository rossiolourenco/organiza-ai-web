package br.com.organizaai.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

/** Upsert do plano diário. `versao` habilita controle otimista (omitir/0 para criar). */
public record PlanejamentoRequest(
    @NotNull LocalDate data,
    @NotNull @Size(max = 100) List<@Valid PlanejamentoItem> itens,
    Long versao
) {}

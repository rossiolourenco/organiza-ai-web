package br.com.organizaai.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Confirmação de exclusão da própria conta; a senha evita exclusão por sessão sequestrada. */
public record ExcluirContaRequest(
    @NotBlank @Size(max = 72) String senha
) {}

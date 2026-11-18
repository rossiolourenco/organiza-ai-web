package br.com.organizaai.backend.dto;

import jakarta.validation.constraints.*;

public record CadastroRequest(
    @NotBlank @Size(max = 100) String nome,
    @NotBlank @Email @Size(max = 150) String email,
    @NotBlank @Size(min = 4, max = 72) String senha
) {}

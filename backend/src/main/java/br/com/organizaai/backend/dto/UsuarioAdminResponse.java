package br.com.organizaai.backend.dto;

import java.time.Instant;
import java.time.LocalDateTime;

public record UsuarioAdminResponse(
    Long id, String nome, String email, String papel, String status,
    LocalDateTime dataCriacao, Instant ultimoLoginEm, Instant bloqueadoAte,
    String motivoBloqueio, boolean deveTrocarSenha, long sessoesAtivas
) {}

package br.com.organizaai.backend.dto;

public record SenhaTemporariaResponse(Long usuarioId, String email, String senhaTemporaria, String aviso) {}

package br.com.organizaai.backend.dto;

import java.util.List;

public record AdminMeResponse(Long id, String nome, String email, String papel, List<String> permissoes) {}

package br.com.organizaai.backend.dto;

import br.com.organizaai.backend.model.Papel;
import br.com.organizaai.backend.model.StatusConta;
import br.com.organizaai.backend.model.Usuario;

import java.time.Instant;
import java.time.LocalDateTime;

/** Dados públicos do próprio usuário; nunca inclui hash, token ou metadados de segurança sensíveis. */
public record PerfilResponse(
    Long id,
    String nome,
    String email,
    Papel papel,
    StatusConta status,
    boolean deveTrocarSenha,
    LocalDateTime dataCriacao,
    Instant senhaAlteradaEm
) {
    public static PerfilResponse de(Usuario usuario) {
        return new PerfilResponse(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getPapel(),
            usuario.getStatus(), usuario.isDeveTrocarSenha(), usuario.getDataCriacao(),
            usuario.getSenhaAlteradaEm());
    }
}

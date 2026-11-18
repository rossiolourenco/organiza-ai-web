package br.com.organizaai.backend.dto;

import java.time.Instant;
import java.util.List;

public record UsuarioAdminDetalheResponse(UsuarioAdminResponse usuario, List<SessaoResumoResponse> sessoes,
    List<AuditoriaResponse> eventosRecentes) {
    public record SessaoResumoResponse(Instant criadoEm, Instant ultimoUsoEm,
        Instant expiraEm, String ip, String userAgent) {}
}

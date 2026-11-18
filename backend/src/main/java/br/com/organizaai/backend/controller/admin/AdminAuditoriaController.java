package br.com.organizaai.backend.controller.admin;

import br.com.organizaai.backend.dto.AuditoriaResponse;
import br.com.organizaai.backend.dto.PaginaResponse;
import br.com.organizaai.backend.security.AuditoriaService;
import br.com.organizaai.backend.security.UsuarioAtual;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

@RestController
@RequestMapping("/api/admin/auditoria")
public class AdminAuditoriaController {
    private final AuditoriaService auditoria;
    private final UsuarioAtual atual;

    public AdminAuditoriaController(AuditoriaService auditoria, UsuarioAtual atual) {
        this.auditoria = auditoria;
        this.atual = atual;
    }

    @GetMapping
    public PaginaResponse<AuditoriaResponse> listar(
            @RequestParam(required = false) String acao,
            @RequestParam(required = false) String ator,
            @RequestParam(required = false) String desde,
            @RequestParam(required = false) String ate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size) {
        atual.requireAdmin();
        return PaginaResponse.de(auditoria.buscar(acao, ator, data(desde), dataFim(ate), page, size), a -> a);
    }

    private Instant data(String valor) {
        if (valor == null || valor.isBlank()) return null;
        try {
            return valor.length() == 10
                ? LocalDate.parse(valor).atStartOfDay(ZoneOffset.UTC).toInstant()
                : Instant.parse(valor);
        } catch (Exception e) {
            throw new IllegalArgumentException("Data inválida. Use AAAA-MM-DD ou ISO-8601.");
        }
    }

    private Instant dataFim(String valor) {
        if (valor == null || valor.isBlank()) return null;
        try {
            return valor.length() == 10
                ? LocalDate.parse(valor).plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant()
                : Instant.parse(valor);
        } catch (Exception e) {
            throw new IllegalArgumentException("Data inválida. Use AAAA-MM-DD ou ISO-8601.");
        }
    }
}

package br.com.organizaai.backend.controller.admin;

import br.com.organizaai.backend.dto.*;
import br.com.organizaai.backend.model.Papel;
import br.com.organizaai.backend.security.AuditoriaService;
import br.com.organizaai.backend.security.LoginAttemptService;
import br.com.organizaai.backend.security.PoliticaService;
import br.com.organizaai.backend.security.UsuarioAtual;
import br.com.organizaai.backend.service.SecurityService;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

@RestController
@RequestMapping("/api/admin/seguranca")
public class AdminSegurancaController {
    private final SecurityService seguranca;
    private final LoginAttemptService tentativas;
    private final PoliticaService politica;
    private final UsuarioAtual atual;
    private final AuditoriaService auditoria;

    public AdminSegurancaController(SecurityService seguranca, LoginAttemptService tentativas,
                                    PoliticaService politica, UsuarioAtual atual, AuditoriaService auditoria) {
        this.seguranca = seguranca;
        this.tentativas = tentativas;
        this.politica = politica;
        this.atual = atual;
        this.auditoria = auditoria;
    }

    @GetMapping("/overview")
    public SegurancaOverviewResponse overview() {
        return seguranca.overview();
    }

    @GetMapping("/tentativas")
    public PaginaResponse<TentativaResponse> tentativas(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Boolean sucesso,
            @RequestParam(required = false) String desde,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        atual.requireAdmin();
        return tentativas.buscar(email, sucesso, data(desde), page, size);
    }

    @GetMapping("/politica")
    public PoliticaResponse politica() {
        atual.requireAdmin();
        return politica.toResponse(politica.obter());
    }

    @PutMapping("/politica")
    public PoliticaResponse atualizarPolitica(@RequestBody PoliticaRequest request) {
        var ator = atual.requireSuperAdmin();
        var atualizada = politica.atualizar(request, ator);
        auditoria.registrar(ator, "POLITICA_ALTERADA", "seguranca", 1, "SUCESSO",
            "Política atualizada: senhaMinima=" + atualizada.getSenhaMinima()
                + ", maxTentativas=" + atualizada.getMaxTentativas()
                + ", bloqueio=" + atualizada.getBloqueioMinutos() + "min"
                + ", sessao=" + atualizada.getSessaoDias() + "d.");
        return politica.toResponse(atualizada);
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
}

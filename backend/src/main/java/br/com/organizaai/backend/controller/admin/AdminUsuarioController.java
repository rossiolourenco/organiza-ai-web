package br.com.organizaai.backend.controller.admin;

import br.com.organizaai.backend.dto.*;
import br.com.organizaai.backend.service.AdminUsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/usuarios")
public class AdminUsuarioController {
    private final AdminUsuarioService service;

    public AdminUsuarioController(AdminUsuarioService service) {
        this.service = service;
    }

    @GetMapping("/me")
    public AdminMeResponse me() {
        return service.me();
    }

    @GetMapping
    public PaginaResponse<UsuarioAdminResponse> listar(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String papel,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return service.listar(q, status, papel, page, size);
    }

    @GetMapping("/{id}")
    public UsuarioAdminDetalheResponse detalhar(@PathVariable Long id) {
        return service.detalhar(id);
    }

    @PostMapping
    public SenhaTemporariaResponse criar(@RequestBody CriarUsuarioAdminRequest request) {
        return service.criar(request);
    }

    @PatchMapping("/{id}/status")
    public UsuarioAdminResponse alterarStatus(@PathVariable Long id,
                                              @RequestBody AtualizarStatusRequest request) {
        return service.alterarStatus(id, request);
    }

    @PatchMapping("/{id}/papel")
    public UsuarioAdminResponse alterarPapel(@PathVariable Long id,
                                             @RequestBody AtualizarPapelRequest request) {
        return service.alterarPapel(id, request);
    }

    @PostMapping("/{id}/revogar-sessoes")
    public Map<String, Object> revogarSessoes(@PathVariable Long id) {
        return Map.of("revogadas", service.revogarSessoes(id));
    }

    @PostMapping("/{id}/redefinir-senha")
    public SenhaTemporariaResponse redefinirSenha(@PathVariable Long id) {
        return service.redefinirSenha(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> anonimizar(@PathVariable Long id) {
        service.anonimizar(id);
        return ResponseEntity.noContent().build();
    }
}

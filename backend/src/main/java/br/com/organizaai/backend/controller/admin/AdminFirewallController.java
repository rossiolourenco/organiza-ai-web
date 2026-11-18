package br.com.organizaai.backend.controller.admin;

import br.com.organizaai.backend.dto.*;
import br.com.organizaai.backend.service.FirewallService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/firewall")
public class AdminFirewallController {
    private final FirewallService service;

    public AdminFirewallController(FirewallService service) {
        this.service = service;
    }

    @GetMapping("/estado")
    public FirewallEstadoResponse estado() {
        return service.estado();
    }

    @GetMapping("/regras")
    public List<FirewallRegraResponse> regras() {
        return service.listar();
    }

    @PostMapping("/regras")
    public FirewallRegraResponse criar(@RequestBody FirewallRegraRequest request) {
        return service.criar(request);
    }

    @PutMapping("/regras/{id}")
    public FirewallRegraResponse atualizar(@PathVariable Long id, @RequestBody FirewallRegraRequest request) {
        return service.atualizar(id, request);
    }

    @PatchMapping("/regras/{id}/ativa")
    public FirewallRegraResponse alternar(@PathVariable Long id, @RequestParam boolean ativa) {
        return service.alternar(id, ativa);
    }

    @DeleteMapping("/regras/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/validar")
    public FirewallValidacaoResponse validar() {
        return service.validar();
    }

    @PostMapping("/aplicar")
    public FirewallEstadoResponse aplicar(@RequestBody(required = false) FirewallAplicarRequest request) {
        return service.aplicar(request);
    }

    @PostMapping("/rollback")
    public FirewallEstadoResponse rollback() {
        return service.rollback();
    }

    @PostMapping("/reset-demo")
    public FirewallEstadoResponse reset() {
        return service.resetDemo();
    }

    @GetMapping("/historico")
    public List<FirewallHistoricoResponse> historico() {
        return service.historico();
    }
}

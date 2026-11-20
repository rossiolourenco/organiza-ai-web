package br.com.organizaai.backend.controller;

import br.com.organizaai.backend.dto.*;
import br.com.organizaai.backend.security.UsuarioAtual;
import br.com.organizaai.backend.service.ProdutividadeService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/produtividade")
public class ProdutividadeController {

    private final ProdutividadeService service;
    private final UsuarioAtual atual;

    public ProdutividadeController(ProdutividadeService service, UsuarioAtual atual) {
        this.service = service;
        this.atual = atual;
    }

    @GetMapping("/planejamento")
    public PlanejamentoResponse obterPlanejamento(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return service.obter(atual.id(), data);
    }

    @PutMapping("/planejamento")
    public PlanejamentoResponse salvarPlanejamento(@Valid @RequestBody PlanejamentoRequest req) {
        return service.salvar(atual.id(), req);
    }

    @GetMapping("/foco")
    public List<SessaoFocoResponse> sessoesFoco(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return service.sessoes(atual.id(), data);
    }

    @PostMapping("/foco")
    public SessaoFocoResponse registrarFoco(@Valid @RequestBody SessaoFocoRequest req) {
        return service.registrar(atual.id(), req);
    }

    @GetMapping("/foco/resumo")
    public FocoResumoResponse resumoFoco(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return service.resumo(atual.id(), inicio, fim);
    }
}

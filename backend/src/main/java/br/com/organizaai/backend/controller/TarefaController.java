package br.com.organizaai.backend.controller;

import br.com.organizaai.backend.model.Tarefa;
import br.com.organizaai.backend.service.TarefaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tarefas")
public class TarefaController {

    private final TarefaService tarefaService;

    public TarefaController(TarefaService tarefaService) {
        this.tarefaService = tarefaService;
    }

    @GetMapping
    public List<Tarefa> listarTodas(@RequestParam Long usuarioId) {
        return tarefaService.listarTodas(usuarioId);
    }

    @GetMapping("/status/{status}")
    public List<Tarefa> listarPorStatus(
            @PathVariable String status,
            @RequestParam Long usuarioId) {

        return tarefaService.listarPorStatus(usuarioId, status);
    }

    @GetMapping("/prioridade/{prioridade}")
    public List<Tarefa> listarPorPrioridade(
            @PathVariable String prioridade,
            @RequestParam Long usuarioId) {

        return tarefaService.listarPorPrioridade(usuarioId, prioridade);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tarefa> buscarPorId(
            @PathVariable Long id,
            @RequestParam Long usuarioId) {

        return tarefaService.buscarPorId(id, usuarioId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Tarefa> criar(
            @RequestParam Long usuarioId,
            @RequestBody Tarefa tarefa) {

        return tarefaService.criar(usuarioId, tarefa)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Tarefa> atualizar(
            @PathVariable Long id,
            @RequestParam Long usuarioId,
            @RequestBody Tarefa tarefa) {

        return tarefaService.atualizar(id, usuarioId, tarefa)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/concluir")
    public ResponseEntity<Tarefa> concluir(
            @PathVariable Long id,
            @RequestParam Long usuarioId) {

        return tarefaService.concluir(id, usuarioId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(
            @PathVariable Long id,
            @RequestParam Long usuarioId) {

        boolean excluiu = tarefaService.excluir(id, usuarioId);

        if (!excluiu) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }
}

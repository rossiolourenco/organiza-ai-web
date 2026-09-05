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
    public List<Tarefa> listarTodas() {
        return tarefaService.listarTodas();
    }

    @GetMapping("/status/{status}")
    public List<Tarefa> listarPorStatus(@PathVariable String status) {
        return tarefaService.listarPorStatus(status);
    }

    @GetMapping("/prioridade/{prioridade}")
    public List<Tarefa> listarPorPrioridade(@PathVariable String prioridade) {
        return tarefaService.listarPorPrioridade(prioridade);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tarefa> buscarPorId(@PathVariable Long id) {
        return tarefaService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Tarefa> criar(@RequestBody Tarefa tarefa) {
        Tarefa novaTarefa = tarefaService.criar(tarefa);
        return ResponseEntity.ok(novaTarefa);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Tarefa> atualizar(
            @PathVariable Long id,
            @RequestBody Tarefa tarefa) {

        return tarefaService.atualizar(id, tarefa)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/concluir")
    public ResponseEntity<Tarefa> concluir(@PathVariable Long id) {
        return tarefaService.concluir(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        if (tarefaService.buscarPorId(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        tarefaService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}

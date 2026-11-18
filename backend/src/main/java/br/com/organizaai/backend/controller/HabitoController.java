package br.com.organizaai.backend.controller;

import br.com.organizaai.backend.dto.RegistroHabitoRequest;
import br.com.organizaai.backend.model.Habito;
import br.com.organizaai.backend.service.HabitoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@RequestMapping("/api/habitos")
public class HabitoController {

    private final HabitoService habitos;

    public HabitoController(HabitoService habitos) {
        this.habitos = habitos;
    }

    @GetMapping
    public List<Habito> listar(@RequestParam Long usuarioId) {
        return habitos.listar(usuarioId);
    }

    @PostMapping
    public ResponseEntity<Habito> criar(@RequestParam Long usuarioId, @RequestBody Habito dados) {
        return habitos.criar(usuarioId, dados)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Habito> atualizar(@PathVariable Long id, @RequestParam Long usuarioId,
                                            @RequestBody Habito dados) {
        return habitos.atualizar(id, usuarioId, dados)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id, @RequestParam Long usuarioId) {
        return habitos.excluir(id, usuarioId) ? ResponseEntity.noContent().build()
            : ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/registros")
    public ResponseEntity<Habito> registrar(@PathVariable Long id, @RequestParam Long usuarioId,
                                            @RequestBody RegistroHabitoRequest dados) {
        return habitos.registrar(id, usuarioId, data(dados.data()))
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}/registros")
    public ResponseEntity<Habito> remover(@PathVariable Long id, @RequestParam Long usuarioId,
                                          @RequestParam String data) {
        return habitos.removerRegistro(id, usuarioId, data(data))
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    private LocalDate data(String value) {
        try {
            return LocalDate.parse(value == null ? "" : value.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Data inválida. Use AAAA-MM-DD.");
        }
    }
}

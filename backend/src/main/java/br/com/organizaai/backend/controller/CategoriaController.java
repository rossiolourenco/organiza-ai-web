package br.com.organizaai.backend.controller;

import br.com.organizaai.backend.model.Categoria;
import br.com.organizaai.backend.model.Usuario;
import br.com.organizaai.backend.repository.CategoriaRepository;
import br.com.organizaai.backend.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import br.com.organizaai.backend.repository.TarefaRepository;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;
    private final TarefaRepository tarefas;

    public CategoriaController(
            CategoriaRepository categoriaRepository,
            UsuarioRepository usuarioRepository, TarefaRepository tarefas) {

        this.categoriaRepository = categoriaRepository;
        this.usuarioRepository = usuarioRepository;
        this.tarefas = tarefas;
    }

    @GetMapping
    public ResponseEntity<List<Categoria>> listar(
            @RequestParam Long usuarioId) {

        return ResponseEntity.ok(
                categoriaRepository.findByUsuarioId(usuarioId)
        );
    }

    @PostMapping
    public ResponseEntity<Categoria> criar(
            @RequestParam Long usuarioId,
            @RequestBody Categoria categoria) {
        validar(categoria);

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElse(null);

        if (usuario == null) {
            return ResponseEntity.notFound().build();
        }

        categoria.setId(null);
        categoria.setUsuario(usuario);

        return ResponseEntity.ok(
                categoriaRepository.save(categoria)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<Categoria> atualizar(
            @PathVariable Long id,
            @RequestParam Long usuarioId,
            @RequestBody Categoria dadosAtualizados) {
        validar(dadosAtualizados);

        return categoriaRepository.findByIdAndUsuarioId(id, usuarioId)
                .map(categoria -> {

                    categoria.setNome(dadosAtualizados.getNome());
                    categoria.setCor(dadosAtualizados.getCor());

                    return ResponseEntity.ok(
                            categoriaRepository.save(categoria)
                    );
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> excluir(
            @PathVariable Long id,
            @RequestParam Long usuarioId) {

        return categoriaRepository.findByIdAndUsuarioId(id, usuarioId)
                .map(categoria -> {
                    tarefas.desassociarCategoria(id);
                    categoriaRepository.delete(categoria);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private void validar(Categoria categoria) {
        if (categoria.getNome() == null || categoria.getNome().isBlank() || categoria.getNome().length() > 100)
            throw new IllegalArgumentException("Nome obrigatório, até 100 caracteres.");
        if (categoria.getCor() != null && !categoria.getCor().matches("#[0-9a-fA-F]{6}"))
            throw new IllegalArgumentException("Cor inválida.");
        categoria.setNome(categoria.getNome().trim());
    }
}

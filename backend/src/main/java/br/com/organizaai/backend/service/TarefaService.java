package br.com.organizaai.backend.service;

import br.com.organizaai.backend.model.Tarefa;
import br.com.organizaai.backend.repository.TarefaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import br.com.organizaai.backend.model.Usuario;
import br.com.organizaai.backend.repository.UsuarioRepository;

import br.com.organizaai.backend.repository.CategoriaRepository;

@Service
public class TarefaService {

    private final TarefaRepository tarefaRepository;
    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;

    public TarefaService(
            TarefaRepository tarefaRepository,
            UsuarioRepository usuarioRepository,
            CategoriaRepository categoriaRepository) {

        this.tarefaRepository = tarefaRepository;
        this.usuarioRepository = usuarioRepository;
        this.categoriaRepository = categoriaRepository;
    }

    public List<Tarefa> listarTodas(Long usuarioId) {
        return tarefaRepository.findByUsuarioId(usuarioId);
    }

    public Optional<Tarefa> buscarPorId(Long id, Long usuarioId) {
        return tarefaRepository.findByIdAndUsuarioId(id, usuarioId);
    }

    public List<Tarefa> listarPorStatus(Long usuarioId, String status) {
        return tarefaRepository.findByUsuarioIdAndStatus(usuarioId, status);
    }

    public List<Tarefa> listarPorPrioridade(Long usuarioId, String prioridade) {
        return tarefaRepository.findByUsuarioIdAndPrioridade(usuarioId, prioridade);
    }

    public Optional<Tarefa> criar(Long usuarioId, Tarefa tarefa) {

        return usuarioRepository.findById(usuarioId)
                .flatMap(usuario -> {

                    tarefa.setUsuario(usuario);

                    if (tarefa.getCategoria() != null &&
                            tarefa.getCategoria().getId() != null) {

                        return categoriaRepository
                                .findByIdAndUsuarioId(
                                        tarefa.getCategoria().getId(),
                                        usuarioId)
                                .map(categoria -> {

                                    tarefa.setCategoria(categoria);
                                    tarefa.setDataAtualizacao(LocalDateTime.now());

                                    return tarefaRepository.save(tarefa);
                                });
                    }

                    tarefa.setCategoria(null);
                    tarefa.setDataAtualizacao(LocalDateTime.now());

                    return Optional.of(tarefaRepository.save(tarefa));
                });
    }

    public Optional<Tarefa> atualizar(
            Long id,
            Long usuarioId,
            Tarefa dadosAtualizados) {

        return tarefaRepository.findByIdAndUsuarioId(id, usuarioId)
                .flatMap(tarefa -> {

                    tarefa.setTitulo(dadosAtualizados.getTitulo());
                    tarefa.setDescricao(dadosAtualizados.getDescricao());
                    tarefa.setPrioridade(dadosAtualizados.getPrioridade());
                    tarefa.setStatus(dadosAtualizados.getStatus());
                    tarefa.setDataPrazo(dadosAtualizados.getDataPrazo());
                    tarefa.setDataAtualizacao(LocalDateTime.now());

                    if (dadosAtualizados.getCategoria() != null &&
                            dadosAtualizados.getCategoria().getId() != null) {

                        return categoriaRepository
                                .findByIdAndUsuarioId(
                                        dadosAtualizados.getCategoria().getId(),
                                        usuarioId)
                                .map(categoria -> {

                                    tarefa.setCategoria(categoria);

                                    return tarefaRepository.save(tarefa);
                                });
                    }

                    tarefa.setCategoria(null);

                    return Optional.of(tarefaRepository.save(tarefa));
                });
    }

    public Optional<Tarefa> concluir(Long id, Long usuarioId) {
        return tarefaRepository.findByIdAndUsuarioId(id, usuarioId)
                .map(tarefa -> {
                    tarefa.setStatus("CONCLUIDA");
                    tarefa.setDataAtualizacao(LocalDateTime.now());

                    return tarefaRepository.save(tarefa);
                });
    }

    public boolean excluir(Long id, Long usuarioId) {

        Optional<Tarefa> tarefa = tarefaRepository.findByIdAndUsuarioId(id, usuarioId);

        if (tarefa.isEmpty()) {
            return false;
        }

        tarefaRepository.delete(tarefa.get());
        return true;
    }
}

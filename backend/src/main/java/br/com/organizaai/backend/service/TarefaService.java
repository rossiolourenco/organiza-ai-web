package br.com.organizaai.backend.service;

import br.com.organizaai.backend.model.Tarefa;
import br.com.organizaai.backend.repository.TarefaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import br.com.organizaai.backend.model.Usuario;
import br.com.organizaai.backend.repository.UsuarioRepository;

@Service
public class TarefaService {

    private final TarefaRepository tarefaRepository;
    private final UsuarioRepository usuarioRepository;

    public TarefaService(
            TarefaRepository tarefaRepository,
            UsuarioRepository usuarioRepository) {

        this.tarefaRepository = tarefaRepository;
        this.usuarioRepository = usuarioRepository;
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
                .map(usuario -> {

                    tarefa.setUsuario(usuario);
                    tarefa.setDataAtualizacao(LocalDateTime.now());

                    return tarefaRepository.save(tarefa);
                });
    }

    public Optional<Tarefa> atualizar(
            Long id,
            Long usuarioId,
            Tarefa dadosAtualizados) {

        return tarefaRepository.findByIdAndUsuarioId(id, usuarioId)
                .map(tarefa -> {

                    tarefa.setTitulo(dadosAtualizados.getTitulo());
                    tarefa.setDescricao(dadosAtualizados.getDescricao());
                    tarefa.setPrioridade(dadosAtualizados.getPrioridade());
                    tarefa.setStatus(dadosAtualizados.getStatus());
                    tarefa.setDataPrazo(dadosAtualizados.getDataPrazo());
                    tarefa.setCategoria(dadosAtualizados.getCategoria());
                    tarefa.setDataAtualizacao(LocalDateTime.now());

                    return tarefaRepository.save(tarefa);
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

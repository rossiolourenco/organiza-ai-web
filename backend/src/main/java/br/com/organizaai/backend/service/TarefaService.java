package br.com.organizaai.backend.service;

import br.com.organizaai.backend.model.Tarefa;
import br.com.organizaai.backend.repository.TarefaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TarefaService {

    private final TarefaRepository tarefaRepository;

    public TarefaService(TarefaRepository tarefaRepository) {
        this.tarefaRepository = tarefaRepository;
    }

    public List<Tarefa> listarTodas() {
        return tarefaRepository.findAll();
    }

    public Optional<Tarefa> buscarPorId(Long id) {
        return tarefaRepository.findById(id);
    }

    public List<Tarefa> listarPorStatus(String status) {
        return tarefaRepository.findByStatus(status);
    }

    public List<Tarefa> listarPorPrioridade(String prioridade) {
        return tarefaRepository.findByPrioridade(prioridade);
    }

    public Tarefa criar(Tarefa tarefa) {
        tarefa.setDataAtualizacao(LocalDateTime.now());
        return tarefaRepository.save(tarefa);
    }

    public Optional<Tarefa> atualizar(Long id, Tarefa dadosAtualizados) {
        return tarefaRepository.findById(id)
                .map(tarefa -> {
                    tarefa.setTitulo(dadosAtualizados.getTitulo());
                    tarefa.setDescricao(dadosAtualizados.getDescricao());
                    tarefa.setPrioridade(dadosAtualizados.getPrioridade());
                    tarefa.setStatus(dadosAtualizados.getStatus());
                    tarefa.setDataPrazo(dadosAtualizados.getDataPrazo());
                    tarefa.setUsuario(dadosAtualizados.getUsuario());
                    tarefa.setCategoria(dadosAtualizados.getCategoria());
                    tarefa.setDataAtualizacao(LocalDateTime.now());

                    return tarefaRepository.save(tarefa);
                });
    }

    public Optional<Tarefa> concluir(Long id) {
        return tarefaRepository.findById(id)
                .map(tarefa -> {
                    tarefa.setStatus("CONCLUIDA");
                    tarefa.setDataAtualizacao(LocalDateTime.now());
                    return tarefaRepository.save(tarefa);
                });
    }

    public void excluir(Long id) {
        tarefaRepository.deleteById(id);
    }
}

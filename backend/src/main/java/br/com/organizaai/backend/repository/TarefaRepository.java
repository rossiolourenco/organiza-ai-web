package br.com.organizaai.backend.repository;

import br.com.organizaai.backend.model.Tarefa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TarefaRepository extends JpaRepository<Tarefa, Long> {

    // Consultas globais existentes
    List<Tarefa> findByStatus(String status);

    List<Tarefa> findByPrioridade(String prioridade);

    // Consultas vinculadas ao usuário
    List<Tarefa> findByUsuarioId(Long usuarioId);

    List<Tarefa> findByUsuarioIdAndStatus(Long usuarioId, String status);

    List<Tarefa> findByUsuarioIdAndPrioridade(Long usuarioId, String prioridade);

    Optional<Tarefa> findByIdAndUsuarioId(Long id, Long usuarioId);
}
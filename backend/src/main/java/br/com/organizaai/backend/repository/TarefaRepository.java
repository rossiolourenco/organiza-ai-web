package br.com.organizaai.backend.repository;

import br.com.organizaai.backend.model.Tarefa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TarefaRepository extends JpaRepository<Tarefa, Long> {

    List<Tarefa> findByStatus(String status);

    List<Tarefa> findByPrioridade(String prioridade);
}

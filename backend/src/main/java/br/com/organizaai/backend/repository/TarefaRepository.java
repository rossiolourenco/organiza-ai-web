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

    // Consultas vinculadas ao usuário. A lixeira é excluída das listagens normais.
    List<Tarefa> findByUsuarioIdAndExcluidaEmIsNull(Long usuarioId);

    List<Tarefa> findByUsuarioIdAndStatusAndExcluidaEmIsNull(Long usuarioId, String status);

    List<Tarefa> findByUsuarioIdAndPrioridadeAndExcluidaEmIsNull(Long usuarioId, String prioridade);

    Optional<Tarefa> findByIdAndUsuarioIdAndExcluidaEmIsNull(Long id, Long usuarioId);

    List<Tarefa> findByUsuarioIdAndExcluidaEmIsNotNullOrderByExcluidaEmDesc(Long usuarioId);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query(
        "select t from Tarefa t where t.id = :id and t.usuario.id = :usuarioId and t.excluidaEm is null")
    Optional<Tarefa> buscarParaAtualizar(Long id, Long usuarioId);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query(
        "select t from Tarefa t where t.id = :id and t.usuario.id = :usuarioId and t.excluidaEm is not null")
    Optional<Tarefa> buscarExcluidaParaAtualizar(Long id, Long usuarioId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("update Tarefa t set t.categoria = null where t.categoria.id = :categoriaId")
    void desassociarCategoria(Long categoriaId);
}

package br.com.organizaai.backend.repository;

import br.com.organizaai.backend.model.AuditoriaEvento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface AuditoriaRepository extends JpaRepository<AuditoriaEvento, Long> {
    @Query("""
        select a from AuditoriaEvento a
        where (:acao is null or a.acao = :acao)
          and (:ator is null or lower(a.atorEmail) like lower(concat('%', :ator, '%')))
          and (:desde is null or a.criadoEm >= :desde)
          and (:ate is null or a.criadoEm <= :ate)
        """)
    Page<AuditoriaEvento> buscar(@Param("acao") String acao, @Param("ator") String ator,
                                 @Param("desde") Instant desde, @Param("ate") Instant ate, Pageable pageable);

    List<AuditoriaEvento> findTop10ByOrderByCriadoEmDesc();
    List<AuditoriaEvento> findTop10ByRecursoAndRecursoIdOrderByCriadoEmDesc(String recurso, String recursoId);
    long countByCriadoEmAfter(Instant desde);
    long countByResultadoAndCriadoEmAfter(String resultado, Instant desde);
}

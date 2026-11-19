package br.com.organizaai.backend.repository;

import br.com.organizaai.backend.model.TentativaLogin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface TentativaLoginRepository extends JpaRepository<TentativaLogin, Long> {
    long countBySucessoFalseAndCriadoEmAfter(Instant inicio);
    long countBySucessoTrueAndCriadoEmAfter(Instant inicio);
    long countByEmailIgnoreCaseAndSucessoFalseAndCriadoEmAfter(String email, Instant inicio);

    @Query("""
        select t from TentativaLogin t
        where (:emailLike is null or lower(t.email) like :emailLike)
          and (:sucesso is null or t.sucesso = :sucesso)
          and t.criadoEm >= :desde
        """)
    Page<TentativaLogin> buscar(@Param("emailLike") String emailLike, @Param("sucesso") Boolean sucesso,
                                @Param("desde") Instant desde, Pageable pageable);
}

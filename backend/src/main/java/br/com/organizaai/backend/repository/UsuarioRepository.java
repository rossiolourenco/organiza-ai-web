package br.com.organizaai.backend.repository;

import br.com.organizaai.backend.model.Papel;
import br.com.organizaai.backend.model.StatusConta;
import br.com.organizaai.backend.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    Optional<Usuario> findByEmailIgnoreCase(String email);

    @Query("""
        select u from Usuario u
        where (:qLike is null or lower(u.nome) like :qLike
               or lower(u.email) like :qLike)
          and (:status is null or u.status = :status)
          and (:papel is null or u.papel = :papel)
        """)
    Page<Usuario> buscar(@Param("qLike") String qLike, @Param("status") StatusConta status,
                         @Param("papel") Papel papel, Pageable pageable);

    long countByStatus(StatusConta status);
    long countByPapelAndStatus(Papel papel, StatusConta status);
    long countByPapelAndStatusAndIdNot(Papel papel, StatusConta status, Long id);
}

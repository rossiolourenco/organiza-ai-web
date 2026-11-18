package br.com.organizaai.backend.repository;

import br.com.organizaai.backend.model.Sessao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;

public interface SessaoRepository extends JpaRepository<Sessao, String> {
    List<Sessao> findByUsuarioIdOrderByCriadoEmDesc(Long usuarioId);
    List<Sessao> findByUsuarioIdAndRevogadaEmIsNullAndExpiraEmAfter(Long usuarioId, Instant agora);
    long countByRevogadaEmIsNullAndExpiraEmAfter(Instant agora);

    @Modifying
    @Query("update Sessao s set s.revogadaEm = :agora where s.usuarioId = :usuarioId and s.revogadaEm is null")
    int revogarDoUsuario(@org.springframework.data.repository.query.Param("usuarioId") Long usuarioId,
                         @org.springframework.data.repository.query.Param("agora") Instant agora);
}

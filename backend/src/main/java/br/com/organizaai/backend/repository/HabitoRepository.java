package br.com.organizaai.backend.repository;

import br.com.organizaai.backend.model.Habito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HabitoRepository extends JpaRepository<Habito, Long> {
    List<Habito> findByUsuarioIdOrderByCriadoEmAsc(Long usuarioId);
    Optional<Habito> findByIdAndUsuarioId(Long id, Long usuarioId);
}

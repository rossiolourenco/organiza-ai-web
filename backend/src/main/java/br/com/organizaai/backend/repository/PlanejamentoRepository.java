package br.com.organizaai.backend.repository;

import br.com.organizaai.backend.model.Planejamento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.Optional;

public interface PlanejamentoRepository extends JpaRepository<Planejamento, Long> {
    Optional<Planejamento> findByUsuarioIdAndData(Long usuarioId, LocalDate data);
}

package br.com.organizaai.backend.repository;

import br.com.organizaai.backend.model.SessaoFoco;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface SessaoFocoRepository extends JpaRepository<SessaoFoco, Long> {
    List<SessaoFoco> findByUsuarioIdAndDataOrderByIniciadaEmDesc(Long usuarioId, LocalDate data);
    List<SessaoFoco> findByUsuarioIdAndDataBetweenOrderByIniciadaEmDesc(Long usuarioId, LocalDate inicio, LocalDate fim);
}

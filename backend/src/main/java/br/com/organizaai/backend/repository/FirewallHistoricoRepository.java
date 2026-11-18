package br.com.organizaai.backend.repository;

import br.com.organizaai.backend.model.FirewallHistorico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FirewallHistoricoRepository extends JpaRepository<FirewallHistorico, Long> {
    List<FirewallHistorico> findTop50ByOrderByCriadoEmDesc();
}

package br.com.organizaai.backend.repository;

import br.com.organizaai.backend.model.FirewallRegra;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FirewallRegraRepository extends JpaRepository<FirewallRegra, Long> {
    List<FirewallRegra> findAllByOrderByPrioridadeAscIdAsc();
}

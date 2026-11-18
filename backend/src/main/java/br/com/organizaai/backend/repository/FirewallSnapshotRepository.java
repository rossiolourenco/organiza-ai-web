package br.com.organizaai.backend.repository;

import br.com.organizaai.backend.model.FirewallSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FirewallSnapshotRepository extends JpaRepository<FirewallSnapshot, Long> {
    Optional<FirewallSnapshot> findTopByOrderByIdDesc();
}

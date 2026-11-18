package br.com.organizaai.backend.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "firewall_estado")
public class FirewallEstado {
    @Id
    private Long id = 1L;

    @Column(name = "aplicado_em")
    private Instant aplicadoEm;

    @Column(name = "snapshot_atual_id")
    private Long snapshotAtualId;

    @Column(name = "atualizado_em")
    private Instant atualizadoEm = Instant.now();

    @Column(name = "atualizado_por", length = 150)
    private String atualizadoPor;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Instant getAplicadoEm() { return aplicadoEm; }
    public void setAplicadoEm(Instant aplicadoEm) { this.aplicadoEm = aplicadoEm; }
    public Long getSnapshotAtualId() { return snapshotAtualId; }
    public void setSnapshotAtualId(Long snapshotAtualId) { this.snapshotAtualId = snapshotAtualId; }
    public Instant getAtualizadoEm() { return atualizadoEm; }
    public void setAtualizadoEm(Instant atualizadoEm) { this.atualizadoEm = atualizadoEm; }
    public String getAtualizadoPor() { return atualizadoPor; }
    public void setAtualizadoPor(String atualizadoPor) { this.atualizadoPor = atualizadoPor; }
}

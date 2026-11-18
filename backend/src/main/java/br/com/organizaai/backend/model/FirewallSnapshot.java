package br.com.organizaai.backend.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "firewall_snapshots")
public class FirewallSnapshot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm = Instant.now();

    @Column(name = "ator_email", length = 150)
    private String atorEmail;

    @Column(nullable = false, length = 300)
    private String motivo;

    @Column(name = "regras_json", nullable = false, columnDefinition = "TEXT")
    private String regrasJson;

    @Column(name = "restaurado_em")
    private Instant restauradoEm;

    public Long getId() { return id; }
    public Instant getCriadoEm() { return criadoEm; }
    public void setCriadoEm(Instant criadoEm) { this.criadoEm = criadoEm; }
    public String getAtorEmail() { return atorEmail; }
    public void setAtorEmail(String atorEmail) { this.atorEmail = atorEmail; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public String getRegrasJson() { return regrasJson; }
    public void setRegrasJson(String regrasJson) { this.regrasJson = regrasJson; }
    public Instant getRestauradoEm() { return restauradoEm; }
    public void setRestauradoEm(Instant restauradoEm) { this.restauradoEm = restauradoEm; }
}

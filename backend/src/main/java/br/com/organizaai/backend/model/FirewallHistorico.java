package br.com.organizaai.backend.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "firewall_historico")
public class FirewallHistorico {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm = Instant.now();

    @Column(name = "ator_email", length = 150)
    private String atorEmail;

    @Column(nullable = false, length = 40)
    private String operacao;

    @Column(name = "regra_id")
    private Long regraId;

    @Column(length = 600)
    private String detalhes;

    @Column(nullable = false, length = 20)
    private String resultado;

    public Long getId() { return id; }
    public Instant getCriadoEm() { return criadoEm; }
    public void setCriadoEm(Instant criadoEm) { this.criadoEm = criadoEm; }
    public String getAtorEmail() { return atorEmail; }
    public void setAtorEmail(String atorEmail) { this.atorEmail = atorEmail; }
    public String getOperacao() { return operacao; }
    public void setOperacao(String operacao) { this.operacao = operacao; }
    public Long getRegraId() { return regraId; }
    public void setRegraId(Long regraId) { this.regraId = regraId; }
    public String getDetalhes() { return detalhes; }
    public void setDetalhes(String detalhes) { this.detalhes = detalhes; }
    public String getResultado() { return resultado; }
    public void setResultado(String resultado) { this.resultado = resultado; }
}

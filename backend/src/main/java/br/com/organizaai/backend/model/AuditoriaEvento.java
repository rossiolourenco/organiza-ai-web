package br.com.organizaai.backend.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "auditoria_admin")
public class AuditoriaEvento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm = Instant.now();

    @Column(name = "ator_id")
    private Long atorId;

    @Column(name = "ator_email", length = 150)
    private String atorEmail;

    @Column(name = "ator_papel", length = 20)
    private String atorPapel;

    @Column(nullable = false, length = 60)
    private String acao;

    @Column(length = 80)
    private String recurso;

    @Column(name = "recurso_id", length = 64)
    private String recursoId;

    @Column(nullable = false, length = 20)
    private String resultado;

    @Column(length = 64)
    private String ip;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @Column(length = 1000)
    private String detalhes;

    @Column(length = 64)
    private String correlacao;

    public Long getId() { return id; }
    public Instant getCriadoEm() { return criadoEm; }
    public void setCriadoEm(Instant criadoEm) { this.criadoEm = criadoEm; }
    public Long getAtorId() { return atorId; }
    public void setAtorId(Long atorId) { this.atorId = atorId; }
    public String getAtorEmail() { return atorEmail; }
    public void setAtorEmail(String atorEmail) { this.atorEmail = atorEmail; }
    public String getAtorPapel() { return atorPapel; }
    public void setAtorPapel(String atorPapel) { this.atorPapel = atorPapel; }
    public String getAcao() { return acao; }
    public void setAcao(String acao) { this.acao = acao; }
    public String getRecurso() { return recurso; }
    public void setRecurso(String recurso) { this.recurso = recurso; }
    public String getRecursoId() { return recursoId; }
    public void setRecursoId(String recursoId) { this.recursoId = recursoId; }
    public String getResultado() { return resultado; }
    public void setResultado(String resultado) { this.resultado = resultado; }
    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    public String getDetalhes() { return detalhes; }
    public void setDetalhes(String detalhes) { this.detalhes = detalhes; }
    public String getCorrelacao() { return correlacao; }
    public void setCorrelacao(String correlacao) { this.correlacao = correlacao; }
}

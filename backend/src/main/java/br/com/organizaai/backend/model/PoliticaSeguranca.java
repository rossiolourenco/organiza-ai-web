package br.com.organizaai.backend.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "politicas_seguranca")
public class PoliticaSeguranca {
    @Id
    private Long id = 1L;

    @Column(name = "senha_minima", nullable = false)
    private int senhaMinima = 8;

    @Column(name = "exigir_numero", nullable = false)
    private boolean exigirNumero;

    @Column(name = "exigir_maiuscula", nullable = false)
    private boolean exigirMaiuscula;

    @Column(name = "max_tentativas", nullable = false)
    private int maxTentativas = 5;

    @Column(name = "janela_minutos", nullable = false)
    private int janelaMinutos = 15;

    @Column(name = "bloqueio_minutos", nullable = false)
    private int bloqueioMinutos = 15;

    @Column(name = "sessao_dias", nullable = false)
    private int sessaoDias = 7;

    @Column(name = "atualizado_em")
    private Instant atualizadoEm = Instant.now();

    @Column(name = "atualizado_por", length = 150)
    private String atualizadoPor;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public int getSenhaMinima() { return senhaMinima; }
    public void setSenhaMinima(int senhaMinima) { this.senhaMinima = senhaMinima; }
    public boolean isExigirNumero() { return exigirNumero; }
    public void setExigirNumero(boolean exigirNumero) { this.exigirNumero = exigirNumero; }
    public boolean isExigirMaiuscula() { return exigirMaiuscula; }
    public void setExigirMaiuscula(boolean exigirMaiuscula) { this.exigirMaiuscula = exigirMaiuscula; }
    public int getMaxTentativas() { return maxTentativas; }
    public void setMaxTentativas(int maxTentativas) { this.maxTentativas = maxTentativas; }
    public int getJanelaMinutos() { return janelaMinutos; }
    public void setJanelaMinutos(int janelaMinutos) { this.janelaMinutos = janelaMinutos; }
    public int getBloqueioMinutos() { return bloqueioMinutos; }
    public void setBloqueioMinutos(int bloqueioMinutos) { this.bloqueioMinutos = bloqueioMinutos; }
    public int getSessaoDias() { return sessaoDias; }
    public void setSessaoDias(int sessaoDias) { this.sessaoDias = sessaoDias; }
    public Instant getAtualizadoEm() { return atualizadoEm; }
    public void setAtualizadoEm(Instant atualizadoEm) { this.atualizadoEm = atualizadoEm; }
    public String getAtualizadoPor() { return atualizadoPor; }
    public void setAtualizadoPor(String atualizadoPor) { this.atualizadoPor = atualizadoPor; }
}

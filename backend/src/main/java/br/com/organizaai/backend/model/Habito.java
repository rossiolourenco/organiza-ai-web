package br.com.organizaai.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "habitos")
public class Habito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(length = 500)
    private String descricao;

    /** DIARIA ou SEMANAL. */
    @Column(nullable = false, length = 20)
    private String frequencia = "DIARIA";

    /** Meta de dias por semana (1 a 7). Para hábitos diários, representa a meta semanal. */
    @Column(name = "meta_semanal", nullable = false)
    private int metaSemanal = 7;

    @Column(length = 20)
    private String cor;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm = Instant.now();

    @JsonIgnore
    @Column(name = "arquivado_em")
    private Instant arquivadoEm;

    @OneToMany(mappedBy = "habito", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("data ASC")
    private java.util.List<HabitoRegistro> registros = new java.util.ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public String getFrequencia() { return frequencia; }
    public void setFrequencia(String frequencia) { this.frequencia = frequencia; }
    public int getMetaSemanal() { return metaSemanal; }
    public void setMetaSemanal(int metaSemanal) { this.metaSemanal = metaSemanal; }
    public String getCor() { return cor; }
    public void setCor(String cor) { this.cor = cor; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
    public Instant getCriadoEm() { return criadoEm; }
    public void setCriadoEm(Instant criadoEm) { this.criadoEm = criadoEm; }
    public Instant getArquivadoEm() { return arquivadoEm; }
    public void setArquivadoEm(Instant arquivadoEm) { this.arquivadoEm = arquivadoEm; }
    public java.util.List<HabitoRegistro> getRegistros() { return registros; }
    public void setRegistros(java.util.List<HabitoRegistro> registros) { this.registros = registros; }
}

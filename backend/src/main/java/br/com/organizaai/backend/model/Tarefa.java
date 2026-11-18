package br.com.organizaai.backend.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import br.com.organizaai.backend.dto.DetalhesTarefa;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "tarefas")
public class Tarefa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(nullable = false, length = 20)
    private String prioridade;

    @Column(nullable = false, length = 20)
    private String status = "PENDENTE";

    @Column(name = "data_prazo")
    private LocalDate dataPrazo;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @Column(name = "data_criacao")
    private LocalDateTime dataCriacao = LocalDateTime.now();

    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;

    @Convert(converter = DetalhesConverter.class)
    @Column(columnDefinition = "TEXT")
    private DetalhesTarefa detalhes;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @ElementCollection
    @CollectionTable(name = "tarefa_conclusoes", joinColumns = @JoinColumn(name = "tarefa_id"))
    @Column(name = "concluida_em", nullable = false)
    private List<Instant> conclusoes = new ArrayList<>();

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(name = "proxima_gerada")
    private boolean proximaGerada;

    /** Exclusão lógica: mantém o registro recuperável na lixeira. */
    @com.fasterxml.jackson.annotation.JsonIgnore
    @Column(name = "excluida_em")
    private Instant excluidaEm;

    public Instant getExcluidaEm() { return excluidaEm; }
    public void setExcluidaEm(Instant value) { excluidaEm = value; }
    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isExcluida() { return excluidaEm != null; }

    public DetalhesTarefa getDetalhes() { return detalhes; }
    public void setDetalhes(DetalhesTarefa value) { detalhes = value; }
    public List<Instant> getConclusoes() { return conclusoes; }
    public boolean isProximaGerada() { return proximaGerada; }
    public void setProximaGerada(boolean value) { proximaGerada = value; }

    public Tarefa() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getPrioridade() {
        return prioridade;
    }

    public void setPrioridade(String prioridade) {
        this.prioridade = prioridade;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getDataPrazo() {
        return dataPrazo;
    }

    public void setDataPrazo(LocalDate dataPrazo) {
        this.dataPrazo = dataPrazo;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }

    public void setDataAtualizacao(LocalDateTime dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }
}

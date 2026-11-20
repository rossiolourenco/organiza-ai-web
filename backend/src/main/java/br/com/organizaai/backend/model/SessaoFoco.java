package br.com.organizaai.backend.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

/** Sessão de foco concluída por um usuário, persistida para sincronizar entre dispositivos. */
@Entity
@Table(name = "sessoes_foco")
public class SessaoFoco {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "tarefa_id")
    private Long tarefaId;

    @Column(nullable = false)
    private Integer minutos;

    @Column(nullable = false)
    private LocalDate data;

    @Column(name = "iniciada_em", nullable = false)
    private Instant iniciadaEm = Instant.now();

    public SessaoFoco() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public Long getTarefaId() { return tarefaId; }
    public void setTarefaId(Long tarefaId) { this.tarefaId = tarefaId; }
    public Integer getMinutos() { return minutos; }
    public void setMinutos(Integer minutos) { this.minutos = minutos; }
    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }
    public Instant getIniciadaEm() { return iniciadaEm; }
    public void setIniciadaEm(Instant iniciadaEm) { this.iniciadaEm = iniciadaEm; }
}

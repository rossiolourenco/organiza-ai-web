package br.com.organizaai.backend.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

/** Plano diário de um usuário. Os itens são persistidos como JSON em TEXT (padrão do `tarefas.detalhes`). */
@Entity
@Table(name = "planejamentos", uniqueConstraints =
    @UniqueConstraint(name = "uk_planejamento_usuario_data", columnNames = {"usuario_id", "data"}))
public class Planejamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(nullable = false)
    private LocalDate data;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String itens;

    @Column(nullable = false)
    private Long versao = 1L;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm = Instant.now();

    public Planejamento() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }
    public String getItens() { return itens; }
    public void setItens(String itens) { this.itens = itens; }
    public Long getVersao() { return versao; }
    public void setVersao(Long versao) { this.versao = versao; }
    public Instant getAtualizadoEm() { return atualizadoEm; }
    public void setAtualizadoEm(Instant atualizadoEm) { this.atualizadoEm = atualizadoEm; }
}

package br.com.organizaai.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "habito_registros", uniqueConstraints =
    @UniqueConstraint(name = "uk_habito_data", columnNames = {"habito_id", "data"}))
public class HabitoRegistro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(optional = false)
    @JoinColumn(name = "habito_id", nullable = false)
    private Habito habito;

    @Column(nullable = false)
    private LocalDate data;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm = Instant.now();

    public HabitoRegistro() {}

    public HabitoRegistro(Habito habito, LocalDate data) {
        this.habito = habito;
        this.data = data;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Habito getHabito() { return habito; }
    public void setHabito(Habito habito) { this.habito = habito; }
    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }
    public Instant getCriadoEm() { return criadoEm; }
    public void setCriadoEm(Instant criadoEm) { this.criadoEm = criadoEm; }
}

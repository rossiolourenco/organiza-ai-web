package br.com.organizaai.backend.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "sessoes")
public class Sessao {
    @Id public String hash;
    @Column(nullable = false) public Long usuarioId;
    @Column(nullable = false) public Instant expiraEm;
    @Column(name = "criado_em", nullable = false) public Instant criadoEm = Instant.now();
    @Column(name = "ultimo_uso_em") public Instant ultimoUsoEm;
    @Column(name = "revogada_em") public Instant revogadaEm;
    @Column(length = 64) public String ip;
    @Column(name = "user_agent", length = 255) public String userAgent;

    public boolean ativa() {
        return revogadaEm == null && expiraEm != null && expiraEm.isAfter(Instant.now());
    }
}

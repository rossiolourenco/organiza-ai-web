package br.com.organizaai.backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false, length = 255)
    private String senha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Papel papel = Papel.USER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusConta status = StatusConta.ATIVO;

    @Column(name = "bloqueado_ate")
    private Instant bloqueadoAte;

    @Column(name = "motivo_bloqueio", length = 255)
    private String motivoBloqueio;

    @Column(name = "ultimo_login_em")
    private Instant ultimoLoginEm;

    @Column(name = "senha_alterada_em")
    private Instant senhaAlteradaEm = Instant.now();

    @Column(name = "deve_trocar_senha", nullable = false)
    private boolean deveTrocarSenha;

    @Column(name = "data_criacao")
    private LocalDateTime dataCriacao = LocalDateTime.now();

    public Usuario() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
    public Papel getPapel() { return papel; }
    public void setPapel(Papel papel) { this.papel = papel == null ? Papel.USER : papel; }
    public StatusConta getStatus() { return status; }
    public void setStatus(StatusConta status) { this.status = status == null ? StatusConta.ATIVO : status; }
    public Instant getBloqueadoAte() { return bloqueadoAte; }
    public void setBloqueadoAte(Instant bloqueadoAte) { this.bloqueadoAte = bloqueadoAte; }
    public String getMotivoBloqueio() { return motivoBloqueio; }
    public void setMotivoBloqueio(String motivoBloqueio) { this.motivoBloqueio = motivoBloqueio; }
    public Instant getUltimoLoginEm() { return ultimoLoginEm; }
    public void setUltimoLoginEm(Instant ultimoLoginEm) { this.ultimoLoginEm = ultimoLoginEm; }
    public Instant getSenhaAlteradaEm() { return senhaAlteradaEm; }
    public void setSenhaAlteradaEm(Instant senhaAlteradaEm) { this.senhaAlteradaEm = senhaAlteradaEm; }
    public boolean isDeveTrocarSenha() { return deveTrocarSenha; }
    public void setDeveTrocarSenha(boolean deveTrocarSenha) { this.deveTrocarSenha = deveTrocarSenha; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public boolean isAdministrador() { return papel != null && papel.administrador(); }
}

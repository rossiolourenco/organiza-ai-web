package br.com.organizaai.backend.controller;

import br.com.organizaai.backend.dto.AlterarSenhaRequest;
import br.com.organizaai.backend.dto.CadastroRequest;
import br.com.organizaai.backend.dto.LoginRequest;
import br.com.organizaai.backend.dto.PerfilRequest;
import br.com.organizaai.backend.dto.PerfilResponse;
import br.com.organizaai.backend.model.Papel;
import br.com.organizaai.backend.model.StatusConta;
import br.com.organizaai.backend.model.Usuario;
import br.com.organizaai.backend.repository.UsuarioRepository;
import br.com.organizaai.backend.security.AuditoriaService;
import br.com.organizaai.backend.security.AutenticacaoService;
import br.com.organizaai.backend.security.PoliticaService;
import br.com.organizaai.backend.security.UsuarioAtual;
import br.com.organizaai.backend.service.SessaoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final SessaoService sessoes;
    private final AutenticacaoService autenticacao;
    private final PoliticaService politica;
    private final UsuarioAtual atual;
    private final AuditoriaService auditoria;

    public UsuarioController(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                             SessaoService sessoes, AutenticacaoService autenticacao,
                             PoliticaService politica, UsuarioAtual atual, AuditoriaService auditoria) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.sessoes = sessoes;
        this.autenticacao = autenticacao;
        this.politica = politica;
        this.atual = atual;
        this.auditoria = auditoria;
    }

    @GetMapping
    public List<Usuario> listar(HttpServletRequest request) {
        return usuarioRepository.findById((Long) request.getAttribute("usuarioAutenticado")).stream().toList();
    }

    /** Perfil do próprio usuário autenticado, com resposta sem hash de senha. */
    @GetMapping("/perfil")
    public PerfilResponse perfil() {
        return PerfilResponse.de(atual.usuario());
    }

    /** Permite editar somente o nome; e-mail e papel não são alteráveis pelo próprio usuário. */
    @PutMapping("/perfil")
    public PerfilResponse atualizarPerfil(@Valid @RequestBody PerfilRequest dados) {
        Usuario usuario = atual.usuario();
        String nome = dados.nome().trim();
        if (nome.length() < 2)
            throw new IllegalArgumentException("O nome deve ter pelo menos 2 caracteres.");
        usuario.setNome(nome);
        Usuario salvo = usuarioRepository.save(usuario);
        auditoria.registrar(salvo, "PERFIL_ATUALIZADO", "usuario", salvo.getId(), "SUCESSO",
            "Nome atualizado pelo próprio usuário.");
        return PerfilResponse.de(salvo);
    }

    @PostMapping
    public ResponseEntity<Usuario> criar(@Valid @RequestBody CadastroRequest dados) {
        politica.validarSenhaUsuario(dados.senha());
        Usuario usuario = new Usuario();
        usuario.setNome(dados.nome().trim());
        usuario.setEmail(dados.email().trim().toLowerCase(Locale.ROOT));
        usuario.setSenha(passwordEncoder.encode(dados.senha()));
        // Papel e status nunca vêm do cliente; cadastro público cria somente USER ativo.
        usuario.setPapel(Papel.USER);
        usuario.setStatus(StatusConta.ATIVO);
        Usuario salvo = usuarioRepository.save(usuario);
        auditoria.registrar(salvo.getId(), salvo.getEmail(), Papel.USER, "CONTA_CADASTRADA",
            "usuario", salvo.getId(), "SUCESSO", "Cadastro público de usuário.");
        return ResponseEntity.ok(salvo);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        var resultado = autenticacao.login(loginRequest.getEmail(), loginRequest.getSenha());
        return ResponseEntity.status(resultado.status()).body(resultado.corpo());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authorization) {
        sessoes.revogar(authorization.substring(7));
        try {
            Usuario usuario = atual.usuario();
            auditoria.registrar(usuario, "LOGOUT", "usuario", usuario.getId(), "SUCESSO", "Sessão revogada.");
        } catch (RuntimeException ignored) {
            // Token já inválido: o logout local continua idempotente.
        }
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/senha")
    public ResponseEntity<Void> alterarSenha(@RequestBody AlterarSenhaRequest request) {
        autenticacao.alterarSenha(atual.usuario(), request.senhaAtual(), request.novaSenha());
        return ResponseEntity.noContent().build();
    }
}

package br.com.organizaai.backend.security;

import br.com.organizaai.backend.model.*;
import br.com.organizaai.backend.repository.UsuarioRepository;
import br.com.organizaai.backend.service.SessaoService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class AutenticacaoService {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final SessaoService sessoes;
    private final PoliticaService politica;
    private final LoginAttemptService tentativas;
    private final AuditoriaService auditoria;
    private final RequisicaoInfo requisicao;

    public AutenticacaoService(UsuarioRepository usuarios, PasswordEncoder encoder, SessaoService sessoes,
                               PoliticaService politica, LoginAttemptService tentativas,
                               AuditoriaService auditoria, RequisicaoInfo requisicao) {
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.sessoes = sessoes;
        this.politica = politica;
        this.tentativas = tentativas;
        this.auditoria = auditoria;
        this.requisicao = requisicao;
    }

    public record Resultado(int status, Map<String, Object> corpo) {}

    @Transactional
    public Resultado login(String emailInformado, String senha) {
        PoliticaSeguranca regra = politica.obter();
        String email = emailInformado == null ? "" : emailInformado.trim().toLowerCase();
        if (email.isBlank() || senha == null || senha.isBlank())
            return erro(401, "E-mail ou senha incorretos.");

        long falhas = tentativas.falhasRecentes(email, regra.getJanelaMinutos());
        if (falhas >= regra.getMaxTentativas()) {
            tentativas.registrar(email, false, "RATE_LIMIT");
            auditoria.registrar(null, email, null, "LOGIN_BLOQUEADO_RATE_LIMIT", "autenticacao", null,
                "BLOQUEADO", "Limite de tentativas excedido.");
            return erro(429, "Muitas tentativas. Aguarde alguns minutos e tente novamente.");
        }

        Optional<Usuario> encontrado = usuarios.findByEmailIgnoreCase(email);
        if (encontrado.isEmpty()) {
            tentativas.registrar(email, false, "CREDENCIAIS");
            auditoria.registrar(null, email, null, "LOGIN_FALHA", "autenticacao", null, "FALHA",
                "Credenciais inválidas.");
            return erro(401, "E-mail ou senha incorretos.");
        }

        Usuario usuario = encontrado.get();
        desbloquearSeExpirado(usuario);

        if (usuario.getStatus() != StatusConta.ATIVO) {
            tentativas.registrar(email, false, "CONTA_INDISPONIVEL");
            auditoria.registrar(usuario, "LOGIN_FALHA", "usuario", usuario.getId(), "FALHA",
                "Conta " + usuario.getStatus() + ".");
            return erro(403, "Conta indisponível. Procure um administrador.");
        }

        if (!encoder.matches(senha, usuario.getSenha())) {
            tentativas.registrar(email, false, "CREDENCIAIS");
            auditoria.registrar(usuario, "LOGIN_FALHA", "usuario", usuario.getId(), "FALHA",
                "Credenciais inválidas.");
            long total = tentativas.falhasRecentes(email, regra.getJanelaMinutos());
            if (total >= regra.getMaxTentativas()) {
                boolean ultimoSuperAdmin = usuario.getPapel() == Papel.SUPER_ADMIN
                    && usuarios.countByPapelAndStatusAndIdNot(Papel.SUPER_ADMIN, StatusConta.ATIVO,
                        usuario.getId()) == 0;
                if (ultimoSuperAdmin) {
                    // O último SUPER_ADMIN não pode ser bloqueado, nem por rate limiting.
                    tentativas.registrar(email, false, "RATE_LIMIT_SUPER_ADMIN");
                    auditoria.registrar(usuario, "LOGIN_RATE_LIMIT_SUPER_ADMIN", "usuario", usuario.getId(),
                        "BLOQUEADO", "Limite de tentativas atingido; status mantido ATIVO por proteção do último SUPER_ADMIN.");
                    return erro(429, "Muitas tentativas. Aguarde alguns minutos e tente novamente.");
                }
                usuario.setStatus(StatusConta.BLOQUEADO);
                usuario.setBloqueadoAte(Instant.now().plus(regra.getBloqueioMinutos(), ChronoUnit.MINUTES));
                usuario.setMotivoBloqueio("Bloqueio automático por tentativas de login");
                usuarios.save(usuario);
                sessoes.revogarDoUsuario(usuario.getId());
                tentativas.registrar(email, false, "BLOQUEIO_TEMPORARIO");
                auditoria.registrar(usuario, "CONTA_BLOQUEADA_AUTOMATICA", "usuario", usuario.getId(),
                    "SUCESSO", "Bloqueio temporário de " + regra.getBloqueioMinutos() + " minutos.");
                return erro(429, "Muitas tentativas. Conta bloqueada temporariamente.");
            }
            return erro(401, "E-mail ou senha incorretos.");
        }

        usuario.setUltimoLoginEm(Instant.now());
        usuario.setBloqueadoAte(null);
        usuario.setMotivoBloqueio(null);
        usuarios.save(usuario);
        String token = sessoes.criar(usuario.getId(), regra.getSessaoDias(),
            requisicao.ip(), requisicao.userAgent());
        tentativas.registrar(email, true, "OK");
        auditoria.registrar(usuario, usuario.getPapel().administrador() ? "LOGIN_ADMIN" : "LOGIN",
            "usuario", usuario.getId(), "SUCESSO", "Sessão criada.");

        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("id", usuario.getId());
        corpo.put("nome", usuario.getNome());
        corpo.put("email", usuario.getEmail());
        corpo.put("token", token);
        corpo.put("papel", usuario.getPapel().name());
        corpo.put("status", usuario.getStatus().name());
        corpo.put("deveTrocarSenha", usuario.isDeveTrocarSenha());
        return new Resultado(200, corpo);
    }

    @Transactional
    public void alterarSenha(Usuario usuario, String senhaAtual, String novaSenha) {
        if (senhaAtual == null || !encoder.matches(senhaAtual, usuario.getSenha()))
            throw new IllegalArgumentException("Senha atual incorreta.");
        politica.validarSenhaUsuario(novaSenha);
        if (encoder.matches(novaSenha, usuario.getSenha()))
            throw new IllegalArgumentException("A nova senha deve ser diferente da senha atual.");
        usuario.setSenha(encoder.encode(novaSenha));
        usuario.setSenhaAlteradaEm(Instant.now());
        usuario.setDeveTrocarSenha(false);
        usuarios.save(usuario);
        sessoes.revogarDoUsuario(usuario.getId());
        auditoria.registrar(usuario, "SENHA_ALTERADA", "usuario", usuario.getId(), "SUCESSO",
            "Senha alterada pelo próprio usuário; sessões revogadas.");
    }

    private void desbloquearSeExpirado(Usuario usuario) {
        if (usuario.getStatus() == StatusConta.BLOQUEADO && usuario.getBloqueadoAte() != null
            && usuario.getBloqueadoAte().isBefore(Instant.now())) {
            usuario.setStatus(StatusConta.ATIVO);
            usuario.setBloqueadoAte(null);
            usuario.setMotivoBloqueio(null);
            usuarios.save(usuario);
        }
    }

    private Resultado erro(int status, String mensagem) {
        return new Resultado(status, Map.of("message", mensagem));
    }
}

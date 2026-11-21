package br.com.organizaai.backend.service;

import br.com.organizaai.backend.model.Papel;
import br.com.organizaai.backend.model.StatusConta;
import br.com.organizaai.backend.model.Usuario;
import br.com.organizaai.backend.repository.AuditoriaRepository;
import br.com.organizaai.backend.repository.TentativaLoginRepository;
import br.com.organizaai.backend.repository.UsuarioRepository;
import br.com.organizaai.backend.security.AuditoriaService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ContaService {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final SessaoService sessoes;
    private final AuditoriaService auditoria;
    private final AuditoriaRepository auditoriaRepository;
    private final TentativaLoginRepository tentativas;

    public ContaService(UsuarioRepository usuarios, PasswordEncoder encoder, SessaoService sessoes,
                        AuditoriaService auditoria, AuditoriaRepository auditoriaRepository,
                        TentativaLoginRepository tentativas) {
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.sessoes = sessoes;
        this.auditoria = auditoria;
        this.auditoriaRepository = auditoriaRepository;
        this.tentativas = tentativas;
    }

    /**
     * Exclui a própria conta: apaga o usuário e, por cascata, tarefas, categorias, hábitos,
     * planejamentos, sessões de foco e sessões. Anonimiza referências de auditoria e remove
     * tentativas de login do e-mail. Exige a senha atual.
     */
    @Transactional
    public void excluir(Long usuarioId, String senha) {
        Usuario usuario = usuarios.findById(usuarioId).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sessão inválida."));

        if (senha == null || !encoder.matches(senha, usuario.getSenha())) {
            throw new IllegalArgumentException("Senha incorreta.");
        }

        if (usuario.getPapel() == Papel.SUPER_ADMIN
            && usuarios.countByPapelAndStatusAndIdNot(Papel.SUPER_ADMIN, StatusConta.ATIVO, usuarioId) == 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "O último SUPER_ADMIN não pode excluir a própria conta.");
        }

        String email = usuario.getEmail();
        auditoria.registrar(usuario, "CONTA_EXCLUIDA_PELO_USUARIO", "usuario", usuarioId, "SUCESSO",
            "Conta e dados associados removidos a pedido do titular.");

        sessoes.revogarDoUsuario(usuarioId);
        tentativas.deleteByEmailIgnoreCase(email);
        auditoriaRepository.anonimizarAtor(usuarioId);
        usuarios.delete(usuario);
    }
}

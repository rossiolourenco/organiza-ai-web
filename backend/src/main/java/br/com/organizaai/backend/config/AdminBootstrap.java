package br.com.organizaai.backend.config;

import br.com.organizaai.backend.model.Papel;
import br.com.organizaai.backend.model.StatusConta;
import br.com.organizaai.backend.model.Usuario;
import br.com.organizaai.backend.repository.UsuarioRepository;
import br.com.organizaai.backend.security.AuditoriaService;
import br.com.organizaai.backend.security.PoliticaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Locale;

/**
 * Provisiona o primeiro SUPER_ADMIN por variáveis de ambiente. Não existe endpoint
 * público para criar administradores e nenhuma senha é fixada no código.
 */
@Component
public class AdminBootstrap implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final PoliticaService politica;
    private final AuditoriaService auditoria;

    @Value("${ADMIN_BOOTSTRAP_EMAIL:}")
    private String email;
    @Value("${ADMIN_BOOTSTRAP_PASSWORD:}")
    private String senha;
    @Value("${ADMIN_BOOTSTRAP_NOME:Administrador}")
    private String nome;

    public AdminBootstrap(UsuarioRepository usuarios, PasswordEncoder encoder, PoliticaService politica,
                          AuditoriaService auditoria) {
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.politica = politica;
        this.auditoria = auditoria;
    }

    @Override
    public void run(ApplicationArguments args) {
        long superAdmins = usuarios.countByPapelAndStatus(Papel.SUPER_ADMIN, StatusConta.ATIVO);
        if (superAdmins > 0) return;
        if (email == null || email.isBlank() || senha == null || senha.isBlank()) {
            log.warn("Nenhum SUPER_ADMIN ativo. Defina ADMIN_BOOTSTRAP_EMAIL e ADMIN_BOOTSTRAP_PASSWORD "
                + "para provisionar o primeiro administrador com segurança.");
            return;
        }
        try {
            politica.validarSenhaAdministrativa(senha);
        } catch (IllegalArgumentException e) {
            log.error("Bootstrap recusado: {}", e.getMessage());
            return;
        }
        String normalizado = email.trim().toLowerCase(Locale.ROOT);
        Usuario usuario = usuarios.findByEmailIgnoreCase(normalizado).orElseGet(Usuario::new);
        boolean novo = usuario.getId() == null;
        usuario.setNome(novo ? (nome == null || nome.isBlank() ? "Administrador" : nome.trim()) : usuario.getNome());
        usuario.setEmail(normalizado);
        usuario.setSenha(encoder.encode(senha));
        usuario.setPapel(Papel.SUPER_ADMIN);
        usuario.setStatus(StatusConta.ATIVO);
        usuario.setBloqueadoAte(null);
        usuario.setMotivoBloqueio(null);
        usuario.setDeveTrocarSenha(false);
        usuario.setSenhaAlteradaEm(Instant.now());
        usuarios.save(usuario);
        auditoria.registrar(usuario.getId(), normalizado, Papel.SUPER_ADMIN, "SUPER_ADMIN_PROVISIONADO",
            "usuario", usuario.getId(), "SUCESSO", "Bootstrap seguro por variáveis de ambiente.");
        log.info("SUPER_ADMIN provisionado para {} (senha não registrada).", normalizado);
    }
}

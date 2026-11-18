package br.com.organizaai.backend.security;

import br.com.organizaai.backend.model.Papel;
import br.com.organizaai.backend.model.StatusConta;
import br.com.organizaai.backend.model.Usuario;
import br.com.organizaai.backend.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class UsuarioAtual {
    private final UsuarioRepository usuarios;

    public UsuarioAtual(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    public Long id() {
        var attributes = org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
        if (attributes instanceof org.springframework.web.context.request.ServletRequestAttributes servlet) {
            Object valor = servlet.getRequest().getAttribute("usuarioAutenticado");
            if (valor instanceof Long id) return id;
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sessão necessária.");
    }

    public Papel papel() {
        var attributes = org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
        if (attributes instanceof org.springframework.web.context.request.ServletRequestAttributes servlet) {
            Object valor = servlet.getRequest().getAttribute("papelAutenticado");
            if (valor instanceof Papel papel) return papel;
            if (valor instanceof String texto) return Papel.valueOf(texto);
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sessão necessária.");
    }

    public String email() {
        var attributes = org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
        if (attributes instanceof org.springframework.web.context.request.ServletRequestAttributes servlet) {
            Object valor = servlet.getRequest().getAttribute("emailAutenticado");
            if (valor instanceof String email) return email;
        }
        return null;
    }

    public Usuario usuario() {
        return usuarios.findById(id()).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sessão inválida."));
    }

    public Usuario requireAdmin() {
        Usuario usuario = usuario();
        if (usuario.getPapel() == null || !usuario.getPapel().administrador()
            || usuario.getStatus() != StatusConta.ATIVO) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso administrativo não permitido.");
        }
        return usuario;
    }

    public Usuario requireSuperAdmin() {
        Usuario usuario = requireAdmin();
        if (usuario.getPapel() != Papel.SUPER_ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Somente SUPER_ADMIN pode executar esta operação.");
        }
        return usuario;
    }
}

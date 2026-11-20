package br.com.organizaai.backend.config;

import br.com.organizaai.backend.model.Usuario;
import br.com.organizaai.backend.repository.UsuarioRepository;
import br.com.organizaai.backend.service.SessaoService;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
public class AutorizacaoFilter extends OncePerRequestFilter {
    private final SessaoService sessoes;
    private final UsuarioRepository usuarios;

    public AutorizacaoFilter(SessaoService sessoes, UsuarioRepository usuarios) {
        this.sessoes = sessoes;
        this.usuarios = usuarios;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        proteger(response);
        String path = request.getRequestURI();
        boolean publico = request.getMethod().equals("POST")
            && (path.equals("/api/usuarios") || path.equals("/api/usuarios/login"));
        if (!path.startsWith("/api/") || publico || request.getMethod().equals("OPTIONS")) {
            chain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");
        String token = header != null && header.startsWith("Bearer ") ? header.substring(7) : null;
        Optional<Long> usuarioId = sessoes.autenticar(token);
        if (usuarioId.isEmpty()) {
            erro(response, 401, "Sessão expirada. Entre novamente.");
            return;
        }

        Optional<Usuario> encontrado = usuarios.findById(usuarioId.get());
        if (encontrado.isEmpty() || encontrado.get().getStatus()
            != br.com.organizaai.backend.model.StatusConta.ATIVO) {
            erro(response, 403, "Conta indisponível. Procure um administrador.");
            return;
        }
        Usuario usuario = encontrado.get();

        String informado = request.getParameter("usuarioId");
        if (informado != null && !informado.equals(usuario.getId().toString())) {
            erro(response, 403, "Acesso não permitido.");
            return;
        }

        if (path.startsWith("/api/admin/") && (usuario.getPapel() == null || !usuario.getPapel().administrador())) {
            erro(response, 403, "Acesso administrativo não permitido.");
            return;
        }

        request.setAttribute("usuarioAutenticado", usuario.getId());
        request.setAttribute("papelAutenticado", usuario.getPapel());
        request.setAttribute("emailAutenticado", usuario.getEmail());
        chain.doFilter(request, response);
    }

    private void proteger(HttpServletResponse response) {
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("Referrer-Policy", "no-referrer");
        response.setHeader("Permissions-Policy", "camera=(), microphone=(), geolocation=()");
        // A API só devolve JSON; negar qualquer carregamento reduz superfície de XSS.
        response.setHeader("Content-Security-Policy", "default-src 'none'; frame-ancestors 'none'");
        response.setHeader("Cache-Control", "no-store");
    }

    private void erro(HttpServletResponse response, int status, String mensagem) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"message\":\"" + mensagem.replace("\"", "'") + "\"}");
    }
}

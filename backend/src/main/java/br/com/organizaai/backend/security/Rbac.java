package br.com.organizaai.backend.security;

import br.com.organizaai.backend.model.Papel;

import java.util.List;

/** Matriz única de autorização. Os controllers e serviços usam estas regras. */
public final class Rbac {
    private Rbac() {}

    public static boolean administrador(Papel papel) {
        return papel != null && papel.administrador();
    }

    public static boolean superAdmin(Papel papel) {
        return papel == Papel.SUPER_ADMIN;
    }

    /** ADMIN só gerencia contas USER. SUPER_ADMIN gerencia ADMIN, USER e outros SUPER_ADMIN. */
    public static boolean podeGerenciarConta(Papel ator, Papel alvo) {
        if (ator == Papel.SUPER_ADMIN) return alvo != null;
        if (ator == Papel.ADMIN) return alvo == Papel.USER;
        return false;
    }

    /** Somente SUPER_ADMIN altera papéis; ninguém altera o próprio papel. */
    public static boolean podeAlterarPapel(Papel ator, Long atorId, Long alvoId, Papel alvoAtual, Papel novo) {
        if (ator != Papel.SUPER_ADMIN) return false;
        if (atorId != null && atorId.equals(alvoId)) return false;
        if (novo == null) return false;
        if (alvoAtual == Papel.SUPER_ADMIN && novo != Papel.SUPER_ADMIN) return true; // protegido pelo serviço
        return alvoAtual == Papel.USER || alvoAtual == Papel.ADMIN || alvoAtual == Papel.SUPER_ADMIN;
    }

    /** ADMIN não pode editar políticas críticas. */
    public static boolean podeEditarPoliticas(Papel papel) {
        return papel == Papel.SUPER_ADMIN;
    }

    public static boolean podeVerAuditoria(Papel papel) {
        return administrador(papel);
    }

    public static boolean podeGerenciarFirewall(Papel papel) {
        return administrador(papel);
    }

    public static List<String> permissoes(Papel papel) {
        if (papel == Papel.SUPER_ADMIN) return List.of("admin.dashboard", "admin.users.read",
            "admin.users.manage", "admin.users.roles", "admin.audit.read", "admin.security.read",
            "admin.security.policy", "admin.firewall.read", "admin.firewall.write",
            "admin.monitor.read", "admin.sessions.revoke");
        if (papel == Papel.ADMIN) return List.of("admin.dashboard", "admin.users.read",
            "admin.users.manage", "admin.audit.read", "admin.security.read",
            "admin.firewall.read", "admin.firewall.write", "admin.monitor.read", "admin.sessions.revoke");
        return List.of("self.read", "self.write");
    }
}

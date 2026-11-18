package br.com.organizaai.backend.service;

import br.com.organizaai.backend.dto.SegurancaOverviewResponse;
import br.com.organizaai.backend.model.Papel;
import br.com.organizaai.backend.model.PoliticaSeguranca;
import br.com.organizaai.backend.model.StatusConta;
import br.com.organizaai.backend.repository.UsuarioRepository;
import br.com.organizaai.backend.security.AuditoriaService;
import br.com.organizaai.backend.security.LoginAttemptService;
import br.com.organizaai.backend.security.PoliticaService;
import br.com.organizaai.backend.security.UsuarioAtual;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class SecurityService {
    private final UsuarioRepository usuarios;
    private final LoginAttemptService tentativas;
    private final SessaoService sessoes;
    private final PoliticaService politica;
    private final AuditoriaService auditoria;
    private final UsuarioAtual atual;

    public SecurityService(UsuarioRepository usuarios, LoginAttemptService tentativas, SessaoService sessoes,
                           PoliticaService politica, AuditoriaService auditoria, UsuarioAtual atual) {
        this.usuarios = usuarios;
        this.tentativas = tentativas;
        this.sessoes = sessoes;
        this.politica = politica;
        this.auditoria = auditoria;
        this.atual = atual;
    }

    @Transactional(readOnly = true)
    public SegurancaOverviewResponse overview() {
        atual.requireAdmin();
        PoliticaSeguranca regra = politica.obter();
        long ativas = usuarios.countByStatus(StatusConta.ATIVO);
        long bloqueadas = usuarios.countByStatus(StatusConta.BLOQUEADO);
        long falhas = tentativas.falhas24h();
        long sucessos = tentativas.sucessos24h();
        long sessoesAtivas = sessoes.contarAtivas();
        long superAdmins = usuarios.countByPapelAndStatus(Papel.SUPER_ADMIN, StatusConta.ATIVO);

        List<SegurancaOverviewResponse.Alerta> alertas = new ArrayList<>();
        if (superAdmins == 0) alertas.add(new SegurancaOverviewResponse.Alerta("CRITICO",
            "Nenhum SUPER_ADMIN ativo", "Provisione um SUPER_ADMIN pelo bootstrap seguro."));
        if (bloqueadas > 0) alertas.add(new SegurancaOverviewResponse.Alerta("ATENCAO",
            bloqueadas + " conta(s) bloqueada(s)", "Revise os bloqueios na central de segurança."));
        if (falhas >= regra.getMaxTentativas() * 2) alertas.add(new SegurancaOverviewResponse.Alerta("ATENCAO",
            "Volume elevado de falhas de login", falhas + " falhas nas últimas 24 horas."));
        if (falhas > 0 && falhas < regra.getMaxTentativas()) alertas.add(new SegurancaOverviewResponse.Alerta("INFO",
            "Falhas de autenticação recentes", falhas + " falha(s) nas últimas 24 horas."));
        if (sessoesAtivas > Math.max(5, ativas * 3)) alertas.add(new SegurancaOverviewResponse.Alerta("INFO",
            "Muitas sessões ativas", sessoesAtivas + " sessões para " + ativas + " contas ativas."));

        return new SegurancaOverviewResponse(ativas, bloqueadas, falhas, sucessos, sessoesAtivas, alertas,
            auditoria.recentes(), politica.toResponse(regra));
    }
}

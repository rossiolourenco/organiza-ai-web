package br.com.organizaai.backend.security;

import br.com.organizaai.backend.dto.PaginaResponse;
import br.com.organizaai.backend.dto.TentativaResponse;
import br.com.organizaai.backend.model.TentativaLogin;
import br.com.organizaai.backend.repository.TentativaLoginRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class LoginAttemptService {
    private static final Instant INICIO = Instant.EPOCH;
    private final TentativaLoginRepository repository;
    private final RequisicaoInfo requisicao;

    public LoginAttemptService(TentativaLoginRepository repository, RequisicaoInfo requisicao) {
        this.repository = repository;
        this.requisicao = requisicao;
    }

    @Transactional
    public void registrar(String email, boolean sucesso, String motivo) {
        TentativaLogin tentativa = new TentativaLogin();
        tentativa.setEmail(RequisicaoInfo.recortar(email == null ? "desconhecido" : email.toLowerCase(), 150));
        tentativa.setSucesso(sucesso);
        tentativa.setMotivo(RequisicaoInfo.recortar(motivo, 120));
        tentativa.setIp(requisicao.ip());
        tentativa.setUserAgent(requisicao.userAgent());
        repository.save(tentativa);
    }

    @Transactional(readOnly = true)
    public long falhasRecentes(String email, int janelaMinutos) {
        return repository.countByEmailIgnoreCaseAndSucessoFalseAndCriadoEmAfter(
            email == null ? "" : email.toLowerCase(),
            Instant.now().minus(Math.max(1, janelaMinutos), ChronoUnit.MINUTES));
    }

    @Transactional(readOnly = true)
    public long falhas24h() {
        return repository.countBySucessoFalseAndCriadoEmAfter(Instant.now().minus(24, ChronoUnit.HOURS));
    }

    @Transactional(readOnly = true)
    public long sucessos24h() {
        return repository.countBySucessoTrueAndCriadoEmAfter(Instant.now().minus(24, ChronoUnit.HOURS));
    }

    @Transactional(readOnly = true)
    public PaginaResponse<TentativaResponse> buscar(String email, Boolean sucesso, Instant desde, int page, int size) {
        var pagina = repository.buscar(like(email), sucesso, desde == null ? INICIO : desde,
            PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size)),
                Sort.by(Sort.Direction.DESC, "criadoEm")));
        return PaginaResponse.de(pagina, t -> new TentativaResponse(t.getId(), t.getEmail(), t.isSucesso(),
            t.getIp(), t.getUserAgent(), t.getMotivo(), t.getCriadoEm()));
    }

    private String like(String valor) {
        if (valor == null || valor.isBlank()) return null;
        return "%" + valor.trim().toLowerCase(java.util.Locale.ROOT) + "%";
    }
}

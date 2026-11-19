package br.com.organizaai.backend.security;

import br.com.organizaai.backend.dto.AuditoriaResponse;
import br.com.organizaai.backend.model.AuditoriaEvento;
import br.com.organizaai.backend.model.Papel;
import br.com.organizaai.backend.model.Usuario;
import br.com.organizaai.backend.repository.AuditoriaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

@Service
public class AuditoriaService {
    private static final Instant INICIO = Instant.EPOCH;
    private static final Instant FIM = Instant.parse("9999-12-31T23:59:59Z");
    private final AuditoriaRepository repository;
    private final RequisicaoInfo requisicao;

    public AuditoriaService(AuditoriaRepository repository, RequisicaoInfo requisicao) {
        this.repository = repository;
        this.requisicao = requisicao;
    }

    @Transactional
    public void registrar(Long atorId, String atorEmail, Papel papel, String acao, String recurso,
                          Object recursoId, String resultado, String detalhes) {
        AuditoriaEvento evento = new AuditoriaEvento();
        evento.setAtorId(atorId);
        evento.setAtorEmail(RequisicaoInfo.recortar(atorEmail, 150));
        evento.setAtorPapel(papel == null ? null : papel.name());
        evento.setAcao(acao);
        evento.setRecurso(recurso);
        evento.setRecursoId(recursoId == null ? null : String.valueOf(recursoId));
        evento.setResultado(resultado == null ? "SUCESSO" : resultado);
        evento.setDetalhes(RequisicaoInfo.recortar(detalhes, 1000));
        evento.setIp(requisicao.ip());
        evento.setUserAgent(requisicao.userAgent());
        evento.setCorrelacao(requisicao.correlacao());
        repository.save(evento);
    }

    @Transactional
    public void registrar(Usuario ator, String acao, String recurso, Object recursoId,
                          String resultado, String detalhes) {
        registrar(ator == null ? null : ator.getId(), ator == null ? null : ator.getEmail(),
            ator == null ? null : ator.getPapel(), acao, recurso, recursoId, resultado, detalhes);
    }

    @Transactional(readOnly = true)
    public Page<AuditoriaResponse> buscar(String acao, String ator, Instant desde, Instant ate,
                                          int page, int size) {
        var pagina = repository.buscar(vazio(acao), like(ator), desde == null ? INICIO : desde, ate == null ? FIM : ate,
            PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size)),
                Sort.by(Sort.Direction.DESC, "criadoEm")));
        return pagina.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<AuditoriaResponse> recentes() {
        return repository.findTop10ByOrderByCriadoEmDesc().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public long totalDesde(Instant desde) {
        return repository.countByCriadoEmAfter(desde);
    }

    public AuditoriaResponse toResponse(AuditoriaEvento evento) {
        return new AuditoriaResponse(evento.getId(), evento.getCriadoEm(), evento.getAtorId(),
            evento.getAtorEmail(), evento.getAtorPapel(), evento.getAcao(), evento.getRecurso(),
            evento.getRecursoId(), evento.getResultado(), evento.getIp(), evento.getUserAgent(),
            evento.getDetalhes(), evento.getCorrelacao());
    }

    private String vazio(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private String like(String valor) {
        String limpo = vazio(valor);
        return limpo == null ? null : "%" + limpo.toLowerCase(Locale.ROOT) + "%";
    }
}

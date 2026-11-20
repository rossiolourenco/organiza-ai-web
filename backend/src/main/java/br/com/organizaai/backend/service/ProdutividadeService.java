package br.com.organizaai.backend.service;

import br.com.organizaai.backend.dto.*;
import br.com.organizaai.backend.model.Planejamento;
import br.com.organizaai.backend.model.SessaoFoco;
import br.com.organizaai.backend.repository.PlanejamentoRepository;
import br.com.organizaai.backend.repository.SessaoFocoRepository;
import br.com.organizaai.backend.repository.TarefaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class ProdutividadeService {
    private final PlanejamentoRepository planos;
    private final SessaoFocoRepository sessoes;
    private final TarefaRepository tarefas;
    private final JsonMapper json = JsonMapper.builder().build();

    public ProdutividadeService(PlanejamentoRepository planos, SessaoFocoRepository sessoes,
                                TarefaRepository tarefas) {
        this.planos = planos;
        this.sessoes = sessoes;
        this.tarefas = tarefas;
    }

    @Transactional(readOnly = true)
    public PlanejamentoResponse obter(Long usuarioId, LocalDate data) {
        return planos.findByUsuarioIdAndData(usuarioId, data)
            .map(this::toResponse)
            .orElse(new PlanejamentoResponse(null, data, List.of(), 0L, null));
    }

    @Transactional
    public PlanejamentoResponse salvar(Long usuarioId, PlanejamentoRequest req) {
        validarItens(usuarioId, req.itens());
        String itensJson = serializar(req.itens());

        Planejamento existente = planos.findByUsuarioIdAndData(usuarioId, req.data()).orElse(null);
        if (existente == null) {
            Planejamento novo = new Planejamento();
            novo.setUsuarioId(usuarioId);
            novo.setData(req.data());
            novo.setItens(itensJson);
            novo.setVersao(1L);
            return toResponse(planos.save(novo));
        }
        if (req.versao() == null || !req.versao().equals(existente.getVersao())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "O plano foi alterado em outro dispositivo. Recarregue e tente novamente.");
        }
        existente.setItens(itensJson);
        existente.setVersao(existente.getVersao() + 1);
        existente.setAtualizadoEm(Instant.now());
        return toResponse(planos.save(existente));
    }

    @Transactional
    public SessaoFocoResponse registrar(Long usuarioId, SessaoFocoRequest req) {
        if (req.tarefaId() != null
            && tarefas.findByIdAndUsuarioIdAndExcluidaEmIsNull(req.tarefaId(), usuarioId).isEmpty()) {
            throw new IllegalArgumentException("Tarefa não encontrada para este usuário.");
        }
        SessaoFoco sessao = new SessaoFoco();
        sessao.setUsuarioId(usuarioId);
        sessao.setTarefaId(req.tarefaId());
        sessao.setMinutos(req.minutos());
        sessao.setData(req.data());
        sessao.setIniciadaEm(Instant.now());
        return toResponse(sessoes.save(sessao));
    }

    @Transactional(readOnly = true)
    public List<SessaoFocoResponse> sessoes(Long usuarioId, LocalDate data) {
        return sessoes.findByUsuarioIdAndDataOrderByIniciadaEmDesc(usuarioId, data).stream()
            .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public FocoResumoResponse resumo(Long usuarioId, LocalDate inicio, LocalDate fim) {
        var lista = sessoes.findByUsuarioIdAndDataBetweenOrderByIniciadaEmDesc(usuarioId, inicio, fim);
        int minutos = lista.stream().mapToInt(SessaoFoco::getMinutos).sum();
        return new FocoResumoResponse(minutos, lista.size());
    }

    private void validarItens(Long usuarioId, List<PlanejamentoItem> itens) {
        for (PlanejamentoItem item : itens) {
            if (tarefas.findByIdAndUsuarioIdAndExcluidaEmIsNull(item.tarefaId(), usuarioId).isEmpty()) {
                throw new IllegalArgumentException("Tarefa não encontrada para este usuário.");
            }
        }
    }

    private String serializar(List<PlanejamentoItem> itens) {
        try {
            return json.writeValueAsString(itens);
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao serializar o plano.", e);
        }
    }

    private List<PlanejamentoItem> desserializar(String itensJson) {
        try {
            return json.readValue(itensJson, new TypeReference<List<PlanejamentoItem>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }

    private PlanejamentoResponse toResponse(Planejamento p) {
        return new PlanejamentoResponse(p.getId(), p.getData(), desserializar(p.getItens()),
            p.getVersao(), p.getAtualizadoEm());
    }

    private SessaoFocoResponse toResponse(SessaoFoco s) {
        return new SessaoFocoResponse(s.getId(), s.getMinutos(), s.getData(), s.getIniciadaEm(),
            s.getTarefaId());
    }
}

package br.com.organizaai.backend.service;

import br.com.organizaai.backend.model.*;
import br.com.organizaai.backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;

@Service
@Transactional
public class TarefaService {
    private final TarefaRepository tarefas;
    private final UsuarioRepository usuarios;
    private final CategoriaRepository categorias;
    public TarefaService(TarefaRepository tarefas, UsuarioRepository usuarios, CategoriaRepository categorias) {
        this.tarefas = tarefas; this.usuarios = usuarios; this.categorias = categorias;
    }
    public List<Tarefa> listarTodas(Long usuarioId) {
        return tarefas.findByUsuarioIdAndExcluidaEmIsNull(usuarioId);
    }
    public List<Tarefa> listarLixeira(Long usuarioId) {
        return tarefas.findByUsuarioIdAndExcluidaEmIsNotNullOrderByExcluidaEmDesc(usuarioId);
    }
    public Optional<Tarefa> buscarPorId(Long id, Long usuarioId) {
        return tarefas.findByIdAndUsuarioIdAndExcluidaEmIsNull(id, usuarioId);
    }
    public List<Tarefa> listarPorStatus(Long usuarioId, String status) {
        return tarefas.findByUsuarioIdAndStatusAndExcluidaEmIsNull(usuarioId, status);
    }
    public List<Tarefa> listarPorPrioridade(Long usuarioId, String prioridade) {
        return tarefas.findByUsuarioIdAndPrioridadeAndExcluidaEmIsNull(usuarioId, prioridade);
    }

    private void validar(Tarefa t) {
        if (t.getTitulo() == null || t.getTitulo().isBlank() || t.getTitulo().length() > 150)
            throw new IllegalArgumentException("Título obrigatório, até 150 caracteres.");
        if (t.getDescricao() != null && t.getDescricao().length() > 10000)
            throw new IllegalArgumentException("Descrição muito longa.");
        if (t.getPrioridade() == null || !Set.of("BAIXA", "MEDIA", "ALTA").contains(t.getPrioridade()))
            throw new IllegalArgumentException("Prioridade inválida.");
        if (t.getStatus() == null || !Set.of("PENDENTE", "EM_ANDAMENTO", "CONCLUIDA").contains(t.getStatus()))
            throw new IllegalArgumentException("Status inválido.");
        if (t.getDetalhes() != null) t.getDetalhes().validar(t.getDataPrazo());
    }
    private Categoria categoria(Tarefa t, Long usuarioId) {
        if (t.getCategoria() == null) return null;
        if (t.getCategoria().getId() == null) throw new IllegalArgumentException("Categoria inválida.");
        return categorias.findByIdAndUsuarioId(t.getCategoria().getId(), usuarioId)
            .orElseThrow(() -> new IllegalArgumentException("Categoria não disponível para este usuário."));
    }
    public Optional<Tarefa> criar(Long usuarioId, Tarefa dados) {
        validar(dados);
        return usuarios.findById(usuarioId).map(u -> {
            Tarefa t = new Tarefa();
            t.setUsuario(u);
            copiar(dados, t, categoria(dados, usuarioId));
            tarefas.save(t);
            concluirSeNecessario(t, "PENDENTE");
            return t;
        });
    }
    public Optional<Tarefa> atualizar(Long id, Long usuarioId, Tarefa dados) {
        return tarefas.buscarParaAtualizar(id, usuarioId).map(t -> {
            if (dados.getDetalhes() == null) dados.setDetalhes(t.getDetalhes());
            validar(dados);
            Categoria categoria = categoria(dados, usuarioId);
            String anterior = t.getStatus();
            copiar(dados, t, categoria);
            concluirSeNecessario(t, anterior);
            return tarefas.save(t);
        });
    }
    private void copiar(Tarefa dados, Tarefa t, Categoria categoria) {
        t.setTitulo(dados.getTitulo().trim());
        t.setDescricao(dados.getDescricao());
        t.setPrioridade(dados.getPrioridade());
        t.setStatus(dados.getStatus());
        t.setDataPrazo(dados.getDataPrazo());
        t.setCategoria(categoria);
        t.setDetalhes(dados.getDetalhes());
        t.setDataAtualizacao(LocalDateTime.now());
    }
    public Optional<Tarefa> concluir(Long id, Long usuarioId) {
        return tarefas.buscarParaAtualizar(id, usuarioId).map(t -> {
            String anterior = t.getStatus();
            t.setStatus("CONCLUIDA");
            concluirSeNecessario(t, anterior);
            t.setDataAtualizacao(LocalDateTime.now());
            return tarefas.save(t);
        });
    }
    private void concluirSeNecessario(Tarefa t, String anterior) {
        if (!t.getStatus().equals("CONCLUIDA") || anterior.equals("CONCLUIDA")) return;
        t.getConclusoes().add(Instant.now());
        var d = t.getDetalhes();
        if (d == null || !d.recorrente() || t.isProximaGerada()) return;
        Tarefa proxima = new Tarefa();
        copiar(t, proxima, t.getCategoria());
        proxima.setUsuario(t.getUsuario());
        proxima.setStatus("PENDENTE");
        proxima.setDetalhes(d.proxima());
        proxima.setDataPrazo(switch (d.recorrencia()) {
            case "DIARIA" -> t.getDataPrazo().plusDays(1);
            case "SEMANAL" -> t.getDataPrazo().plusWeeks(1);
            case "MENSAL" -> t.getDataPrazo().plusMonths(1);
            default -> throw new IllegalArgumentException("Recorrência inválida.");
        });
        tarefas.save(proxima);
        t.setProximaGerada(true);
    }

    /** Exclusão lógica: o registro vai para a lixeira e pode ser restaurado. */
    public boolean excluir(Long id, Long usuarioId) {
        return tarefas.buscarParaAtualizar(id, usuarioId).map(t -> {
            t.setExcluidaEm(Instant.now());
            tarefas.save(t);
            return true;
        }).orElse(false);
    }
    public Optional<Tarefa> restaurar(Long id, Long usuarioId) {
        return tarefas.buscarExcluidaParaAtualizar(id, usuarioId).map(t -> {
            t.setExcluidaEm(null);
            t.setDataAtualizacao(LocalDateTime.now());
            return tarefas.save(t);
        });
    }
    /** Exclusão definitiva, disponível somente para itens que já estão na lixeira. */
    public boolean excluirDefinitivo(Long id, Long usuarioId) {
        return tarefas.buscarExcluidaParaAtualizar(id, usuarioId).map(t -> {
            tarefas.delete(t);
            return true;
        }).orElse(false);
    }
}

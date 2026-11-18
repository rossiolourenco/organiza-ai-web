package br.com.organizaai.backend.dto;

import java.time.*;
import java.util.*;

/** Extensão aditiva: clientes antigos que omitem detalhes preservam seu valor. */
public record DetalhesTarefa(
    String horario, String notas, List<String> etiquetas,
    Boolean favorita, Boolean fixada, Integer ordem,
    List<Item> subtarefas, String recorrencia, String fusoHorario,
    Boolean importante, Boolean urgente, Integer duracaoEstimada
) {
    public record Item(String id, String titulo, boolean concluida) {}
    public void validar(LocalDate prazo) {
        if (horario != null && !horario.isBlank()) {
            if (!horario.matches("\\d{2}:\\d{2}")) throw new IllegalArgumentException("Horário inválido.");
            try { LocalTime.parse(horario); } catch (DateTimeException e) {
                throw new IllegalArgumentException("Horário inválido.");
            }
        }
        if (notas != null && notas.length() > 10000) throw new IllegalArgumentException("Notas muito longas.");
        if (etiquetas != null && (etiquetas.size() > 20 || etiquetas.stream()
            .anyMatch(t -> t == null || t.isBlank() || t.length() > 40)))
            throw new IllegalArgumentException("Use até 20 etiquetas de 40 caracteres.");
        if (subtarefas != null) {
            Set<String> ids = new HashSet<>();
            if (subtarefas.size() > 100) throw new IllegalArgumentException("Limite de 100 subtarefas.");
            for (Item item : subtarefas) {
                if (item == null || item.id() == null || item.id().length() > 80 || !ids.add(item.id())
                    || item.titulo() == null || item.titulo().isBlank() || item.titulo().length() > 150)
                    throw new IllegalArgumentException("Subtarefa inválida ou duplicada.");
            }
        }
        if (recorrencia != null && !Set.of("NENHUMA", "DIARIA", "SEMANAL", "MENSAL").contains(recorrencia))
            throw new IllegalArgumentException("Recorrência inválida.");
        if (duracaoEstimada != null && (duracaoEstimada < 1 || duracaoEstimada > 1440))
            throw new IllegalArgumentException("Duração estimada deve ficar entre 1 e 1440 minutos.");
        if (recorrente()) {
            if (prazo == null) throw new IllegalArgumentException("Recorrência exige prazo.");
            try { ZoneId.of(fusoHorario == null ? "" : fusoHorario); }
            catch (DateTimeException e) { throw new IllegalArgumentException("Fuso horário inválido."); }
        }
    }
    public boolean recorrente() { return recorrencia != null && !recorrencia.equals("NENHUMA"); }
    public DetalhesTarefa proxima() {
        return new DetalhesTarefa(horario, notas, etiquetas, favorita, fixada, ordem,
            subtarefas == null ? List.of() : subtarefas.stream()
                .map(i -> new Item(i.id(), i.titulo(), false)).toList(), recorrencia, fusoHorario,
            importante, urgente, duracaoEstimada);
    }
}

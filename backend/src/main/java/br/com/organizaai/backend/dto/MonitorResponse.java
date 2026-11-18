package br.com.organizaai.backend.dto;

import java.time.Instant;
import java.util.List;

public record MonitorResponse(Instant coletadoEm, long uptimeSegundos, boolean backendDisponivel,
    Banco banco, Cpu cpu, Memoria memoria, Disco disco, Http http, long sessoesAtivas,
    Firewall firewall, long usuariosAtivos, long usuariosBloqueados) {
    public record Banco(boolean disponivel, Long latenciaMs, String detalhe) {}
    public record Cpu(Double cargaSistema, Double cargaProcesso, int nucleos, Double loadAverage) {}
    public record Memoria(long heapUsadoBytes, long heapMaximoBytes, long heapCommitadoBytes,
                          long sistemaTotalBytes, long sistemaLivreBytes) {}
    public record Disco(boolean disponivel, Long totalBytes, Long livreBytes, String caminho) {}
    public record Http(long totalRequisicoes, long erros4xx, long erros5xx, long tempoMedioMs) {}
    public record Firewall(String modo, boolean aplicado, Instant aplicadoEm, int regrasAtivas) {}
}

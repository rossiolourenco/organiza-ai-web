package br.com.organizaai.backend.monitor;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.LongAdder;

@Component
public class MetricsRegistry {
    private final LongAdder requisicoes = new LongAdder();
    private final LongAdder erros4xx = new LongAdder();
    private final LongAdder erros5xx = new LongAdder();
    private final LongAdder nanos = new LongAdder();

    public void registrar(int status, long duracaoNanos) {
        requisicoes.increment();
        nanos.add(duracaoNanos);
        if (status >= 500) erros5xx.increment();
        else if (status >= 400) erros4xx.increment();
    }

    public Snapshot snapshot() {
        long total = requisicoes.sum();
        return new Snapshot(total, erros4xx.sum(), erros5xx.sum(),
            total == 0 ? 0 : nanos.sum() / total / 1_000_000);
    }

    public record Snapshot(long totalRequisicoes, long erros4xx, long erros5xx, long tempoMedioMs) {}
}

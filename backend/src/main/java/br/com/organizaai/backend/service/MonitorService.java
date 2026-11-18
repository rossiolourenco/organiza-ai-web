package br.com.organizaai.backend.service;

import br.com.organizaai.backend.dto.MonitorResponse;
import br.com.organizaai.backend.model.FirewallEstado;
import br.com.organizaai.backend.model.StatusConta;
import br.com.organizaai.backend.monitor.MetricsRegistry;
import br.com.organizaai.backend.repository.FirewallEstadoRepository;
import br.com.organizaai.backend.repository.FirewallRegraRepository;
import br.com.organizaai.backend.repository.UsuarioRepository;
import br.com.organizaai.backend.security.UsuarioAtual;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.sql.Connection;
import java.time.Instant;

@Service
public class MonitorService {
    private final DataSource dataSource;
    private final MetricsRegistry metricas;
    private final SessaoService sessoes;
    private final UsuarioRepository usuarios;
    private final FirewallEstadoRepository firewallEstados;
    private final FirewallRegraRepository firewallRegras;
    private final UsuarioAtual atual;

    @Value("${app.firewall.mode:demo}")
    private String firewallModo;

    public MonitorService(DataSource dataSource, MetricsRegistry metricas, SessaoService sessoes,
                          UsuarioRepository usuarios, FirewallEstadoRepository firewallEstados,
                          FirewallRegraRepository firewallRegras, UsuarioAtual atual) {
        this.dataSource = dataSource;
        this.metricas = metricas;
        this.sessoes = sessoes;
        this.usuarios = usuarios;
        this.firewallEstados = firewallEstados;
        this.firewallRegras = firewallRegras;
        this.atual = atual;
    }

    @Transactional(readOnly = true)
    public MonitorResponse overview() {
        atual.requireAdmin();
        Runtime runtime = Runtime.getRuntime();
        long heapUsado = runtime.totalMemory() - runtime.freeMemory();
        long heapMax = runtime.maxMemory();
        long heapCommitado = runtime.totalMemory();

        Double cargaSistema = null, cargaProcesso = null, loadAverage = null;
        long totalSistema = 0, livreSistema = 0;
        var os = ManagementFactory.getOperatingSystemMXBean();
        if (os instanceof com.sun.management.OperatingSystemMXBean sun) {
            double cpu = sun.getCpuLoad();
            double processo = sun.getProcessCpuLoad();
            cargaSistema = cpu < 0 ? null : Math.round(cpu * 1000) / 10.0;
            cargaProcesso = processo < 0 ? null : Math.round(processo * 1000) / 10.0;
            double load = sun.getSystemLoadAverage();
            loadAverage = load < 0 ? null : Math.round(load * 100) / 100.0;
            totalSistema = sun.getTotalMemorySize();
            livreSistema = sun.getFreeMemorySize();
        }

        MonitorResponse.Banco banco = banco();
        MonitorResponse.Disco disco = disco();
        MetricsRegistry.Snapshot http = metricas.snapshot();
        FirewallEstado estado = firewallEstados.findById(1L).orElseGet(FirewallEstado::new);
        int regrasAtivas = (int) firewallRegras.findAll().stream().filter(r -> r.isAtiva()).count();

        return new MonitorResponse(Instant.now(), ManagementFactory.getRuntimeMXBean().getUptime() / 1000,
            true, banco, new MonitorResponse.Cpu(cargaSistema, cargaProcesso, os.getAvailableProcessors(), loadAverage),
            new MonitorResponse.Memoria(heapUsado, heapMax, heapCommitado, totalSistema, livreSistema),
            disco, new MonitorResponse.Http(http.totalRequisicoes(), http.erros4xx(), http.erros5xx(), http.tempoMedioMs()),
            sessoes.contarAtivas(),
            new MonitorResponse.Firewall("real".equalsIgnoreCase(firewallModo) ? "REAL" : "DEMO",
                estado.getAplicadoEm() != null, estado.getAplicadoEm(), regrasAtivas),
            usuarios.countByStatus(StatusConta.ATIVO), usuarios.countByStatus(StatusConta.BLOQUEADO));
    }

    private MonitorResponse.Banco banco() {
        long inicio = System.nanoTime();
        try (Connection conexao = dataSource.getConnection()) {
            boolean valida = conexao.isValid(2);
            long latencia = (System.nanoTime() - inicio) / 1_000_000;
            return new MonitorResponse.Banco(valida, latencia,
                valida ? conexao.getMetaData().getDatabaseProductName() : "Conexão inválida");
        } catch (Exception e) {
            return new MonitorResponse.Banco(false, null, "Indisponível");
        }
    }

    private MonitorResponse.Disco disco() {
        try {
            File raiz = new File(".").getAbsoluteFile();
            long total = raiz.getTotalSpace(), livre = raiz.getUsableSpace();
            if (total <= 0) return new MonitorResponse.Disco(false, null, null, raiz.getPath());
            return new MonitorResponse.Disco(true, total, livre, raiz.getPath());
        } catch (Exception e) {
            return new MonitorResponse.Disco(false, null, null, null);
        }
    }
}

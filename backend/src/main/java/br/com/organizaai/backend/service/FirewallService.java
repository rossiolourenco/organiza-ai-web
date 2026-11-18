package br.com.organizaai.backend.service;

import br.com.organizaai.backend.dto.*;
import br.com.organizaai.backend.model.*;
import br.com.organizaai.backend.repository.*;
import br.com.organizaai.backend.security.AuditoriaService;
import br.com.organizaai.backend.security.RequisicaoInfo;
import br.com.organizaai.backend.security.UsuarioAtual;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.json.JsonMapper;

import java.net.InetAddress;
import java.time.Instant;
import java.util.*;

@Service
public class FirewallService {
    private static final JsonMapper JSON = JsonMapper.builder().build();
    private final FirewallRegraRepository regras;
    private final FirewallHistoricoRepository historicos;
    private final FirewallSnapshotRepository snapshots;
    private final FirewallEstadoRepository estados;
    private final AuditoriaService auditoria;
    private final UsuarioAtual atual;

    @Value("${app.firewall.mode:demo}")
    private String modo;

    public FirewallService(FirewallRegraRepository regras, FirewallHistoricoRepository historicos,
                           FirewallSnapshotRepository snapshots, FirewallEstadoRepository estados,
                           AuditoriaService auditoria, UsuarioAtual atual) {
        this.regras = regras;
        this.historicos = historicos;
        this.snapshots = snapshots;
        this.estados = estados;
        this.auditoria = auditoria;
        this.atual = atual;
    }

    @Transactional(readOnly = true)
    public List<FirewallRegraResponse> listar() {
        atual.requireAdmin();
        return regras.findAllByOrderByPrioridadeAscIdAsc().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public FirewallEstadoResponse estado() {
        atual.requireAdmin();
        FirewallEstado estado = estados.findById(1L).orElseGet(FirewallEstado::new);
        var todas = regras.findAll();
        return new FirewallEstadoResponse(modoReal() ? "REAL" : "DEMO", !modoReal(), estado.getAplicadoEm() != null,
            estado.getAplicadoEm(), estado.getSnapshotAtualId(), todas.size(),
            (int) todas.stream().filter(FirewallRegra::isAtiva).count());
    }

    @Transactional(readOnly = true)
    public List<FirewallHistoricoResponse> historico() {
        atual.requireAdmin();
        return historicos.findTop50ByOrderByCriadoEmDesc().stream()
            .map(h -> new FirewallHistoricoResponse(h.getId(), h.getCriadoEm(), h.getAtorEmail(),
                h.getOperacao(), h.getRegraId(), h.getDetalhes(), h.getResultado())).toList();
    }

    @Transactional
    public FirewallRegraResponse criar(FirewallRegraRequest request) {
        Usuario ator = exigirDemo();
        validarRequest(request);
        FirewallRegra regra = new FirewallRegra();
        aplicar(regra, request);
        regra.setCriadoPor(ator.getEmail());
        regra.setCriadoEm(Instant.now());
        regras.save(regra);
        registrar(ator, "CRIAR", regra.getId(), "Regra " + regra.getNome() + " criada (simulação).");
        auditoria.registrar(ator, "FIREWALL_REGRA_CRIADA", "firewall", regra.getId(), "SUCESSO",
            regra.getNome() + " " + regra.getDirecao() + " " + regra.getProtocolo() + "/" + regra.getPorta()
                + " " + regra.getIpCidr() + " " + regra.getAcao());
        return toResponse(regra);
    }

    @Transactional
    public FirewallRegraResponse atualizar(Long id, FirewallRegraRequest request) {
        Usuario ator = exigirDemo();
        FirewallRegra regra = buscar(id);
        validarRequest(request);
        aplicar(regra, request);
        regra.setAtualizadoEm(Instant.now());
        regra.setAtualizadoPor(ator.getEmail());
        regras.save(regra);
        registrar(ator, "EDITAR", id, "Regra " + regra.getNome() + " atualizada (simulação).");
        auditoria.registrar(ator, "FIREWALL_REGRA_EDITADA", "firewall", id, "SUCESSO", regra.getNome());
        return toResponse(regra);
    }

    @Transactional
    public FirewallRegraResponse alternar(Long id, boolean ativa) {
        Usuario ator = exigirDemo();
        FirewallRegra regra = buscar(id);
        regra.setAtiva(ativa);
        regra.setAtualizadoEm(Instant.now());
        regra.setAtualizadoPor(ator.getEmail());
        regras.save(regra);
        registrar(ator, ativa ? "ATIVAR" : "DESATIVAR", id, "Regra " + regra.getNome() + " (simulação).");
        auditoria.registrar(ator, ativa ? "FIREWALL_REGRA_ATIVADA" : "FIREWALL_REGRA_DESATIVADA",
            "firewall", id, "SUCESSO", regra.getNome());
        return toResponse(regra);
    }

    @Transactional
    public void excluir(Long id) {
        Usuario ator = exigirDemo();
        FirewallRegra regra = buscar(id);
        String nome = regra.getNome();
        regras.delete(regra);
        registrar(ator, "EXCLUIR", id, "Regra " + nome + " removida (simulação).");
        auditoria.registrar(ator, "FIREWALL_REGRA_EXCLUIDA", "firewall", id, "SUCESSO", nome);
    }

    @Transactional(readOnly = true)
    public FirewallValidacaoResponse validar() {
        atual.requireAdmin();
        return validarRegras();
    }

    @Transactional
    public FirewallEstadoResponse aplicar(FirewallAplicarRequest request) {
        Usuario ator = exigirDemo();
        FirewallValidacaoResponse validacao = validarRegras();
        if (!validacao.valido())
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Existem " + validacao.conflitos().size() + " conflito(s) entre regras ativas. Corrija antes de aplicar.");
        List<FirewallRegra> todas = regras.findAllByOrderByPrioridadeAscIdAsc();
        List<RegraSnapshot> foto = todas.stream().map(r -> new RegraSnapshot(r.getNome(), r.getDescricao(),
            r.getIpCidr(), r.getPorta(), r.getProtocolo(), r.getDirecao(), r.getAcao(),
            r.getPrioridade(), r.isAtiva())).toList();
        FirewallSnapshot snapshot = new FirewallSnapshot();
        snapshot.setAtorEmail(ator.getEmail());
        String motivo = request == null || request.motivo() == null || request.motivo().isBlank()
            ? "Aplicação simulada pela interface" : RequisicaoInfo.recortar(request.motivo(), 300);
        snapshot.setMotivo(motivo);
        snapshot.setRegrasJson(JSON.writeValueAsString(foto));
        snapshots.save(snapshot);

        FirewallEstado estado = estados.findById(1L).orElseGet(FirewallEstado::new);
        estado.setId(1L);
        estado.setAplicadoEm(Instant.now());
        estado.setSnapshotAtualId(snapshot.getId());
        estado.setAtualizadoEm(Instant.now());
        estado.setAtualizadoPor(ator.getEmail());
        estados.save(estado);
        registrar(ator, "APLICAR", null, "Aplicação simulada de " + foto.size() + " regra(s). Snapshot #" + snapshot.getId() + ".");
        auditoria.registrar(ator, "FIREWALL_APLICADO", "firewall", snapshot.getId(), "SUCESSO",
            "Modo DEMO: nenhuma alteração real no sistema operacional. Regras: " + foto.size() + ".");
        return new FirewallEstadoResponse("DEMO", true, true, estado.getAplicadoEm(),
            estado.getSnapshotAtualId(), foto.size(), (int) foto.stream().filter(RegraSnapshot::ativa).count());
    }

    @Transactional
    public FirewallEstadoResponse rollback() {
        Usuario ator = exigirDemo();
        FirewallSnapshot snapshot = snapshots.findTopByOrderByIdDesc().orElseThrow(() ->
            new ResponseStatusException(HttpStatus.CONFLICT, "Não existe snapshot para restaurar."));
        List<RegraSnapshot> foto = JSON.readValue(snapshot.getRegrasJson(),
            JSON.getTypeFactory().constructCollectionType(List.class, RegraSnapshot.class));
        regras.deleteAllInBatch();
        for (RegraSnapshot item : foto) {
            FirewallRegra regra = new FirewallRegra();
            regra.setNome(item.nome());
            regra.setDescricao(item.descricao());
            regra.setIpCidr(item.ipCidr());
            regra.setPorta(item.porta());
            regra.setProtocolo(item.protocolo());
            regra.setDirecao(item.direcao());
            regra.setAcao(item.acao());
            regra.setPrioridade(item.prioridade());
            regra.setAtiva(item.ativa());
            regra.setCriadoPor(ator.getEmail());
            regra.setCriadoEm(Instant.now());
            regras.save(regra);
        }
        snapshot.setRestauradoEm(Instant.now());
        snapshots.save(snapshot);
        FirewallEstado estado = estados.findById(1L).orElseGet(FirewallEstado::new);
        estado.setId(1L);
        estado.setAplicadoEm(null);
        estado.setSnapshotAtualId(null);
        estado.setAtualizadoEm(Instant.now());
        estado.setAtualizadoPor(ator.getEmail());
        estados.save(estado);
        registrar(ator, "ROLLBACK", snapshot.getId(), "Rollback simulado para o snapshot #" + snapshot.getId() + ".");
        auditoria.registrar(ator, "FIREWALL_ROLLBACK", "firewall", snapshot.getId(), "SUCESSO",
            "Modo DEMO: regras restauradas do snapshot. Nenhuma alteração real.");
        return new FirewallEstadoResponse("DEMO", true, false, null, null, foto.size(),
            (int) foto.stream().filter(RegraSnapshot::ativa).count());
    }

    @Transactional
    public FirewallEstadoResponse resetDemo() {
        Usuario ator = exigirDemo();
        regras.deleteAllInBatch();
        List<FirewallRegra> padrao = padraoDemo(ator.getEmail());
        regras.saveAll(padrao);
        FirewallEstado estado = estados.findById(1L).orElseGet(FirewallEstado::new);
        estado.setId(1L);
        estado.setAplicadoEm(null);
        estado.setSnapshotAtualId(null);
        estado.setAtualizadoEm(Instant.now());
        estado.setAtualizadoPor(ator.getEmail());
        estados.save(estado);
        registrar(ator, "RESET", null, "Políticas de demonstração restauradas (" + padrao.size() + " regras).");
        auditoria.registrar(ator, "FIREWALL_RESET_DEMO", "firewall", null, "SUCESSO",
            "Cenário DEMO restaurado com " + padrao.size() + " regras simuladas.");
        return new FirewallEstadoResponse("DEMO", true, false, null, null, padrao.size(), padrao.size());
    }

    private FirewallValidacaoResponse validarRegras() {
        List<FirewallRegra> ativas = regras.findAllByOrderByPrioridadeAscIdAsc().stream()
            .filter(FirewallRegra::isAtiva).toList();
        List<FirewallValidacaoResponse.Conflito> conflitos = new ArrayList<>();
        for (int i = 0; i < ativas.size(); i++) {
            for (int j = i + 1; j < ativas.size(); j++) {
                FirewallRegra a = ativas.get(i), b = ativas.get(j);
                if (!a.getDirecao().equals(b.getDirecao())) continue;
                if (!protocolosCompativeis(a.getProtocolo(), b.getProtocolo())) continue;
                if (!portasCompativeis(a.getPorta(), b.getPorta())) continue;
                if (!cidrsSobrepostos(a.getIpCidr(), b.getIpCidr())) continue;
                if (a.getAcao().equals(b.getAcao())) continue;
                conflitos.add(new FirewallValidacaoResponse.Conflito(a.getId(), a.getNome(), b.getId(), b.getNome(),
                    "Regras ativas sobrepostas com ações opostas na mesma direção/protocolo/porta."));
            }
        }
        return new FirewallValidacaoResponse(conflitos.isEmpty(), conflitos);
    }

    private boolean protocolosCompativeis(String a, String b) {
        return a.equals(b) || a.equals("ANY") || b.equals("ANY");
    }

    private boolean portasCompativeis(Integer a, Integer b) {
        return a == null || b == null || a.equals(b);
    }

    private boolean cidrsSobrepostos(String a, String b) {
        Cidr ca = Cidr.parse(a), cb = Cidr.parse(b);
        if (ca.endereco.length != cb.endereco.length) return false;
        int bits = Math.min(ca.prefixo, cb.prefixo);
        for (int i = 0; i < bits; i++) {
            int indice = i / 8, mascara = 1 << (7 - (i % 8));
            if ((ca.endereco[indice] & mascara) != (cb.endereco[indice] & mascara)) return false;
        }
        return true;
    }

    private void validarRequest(FirewallRegraRequest request) {
        if (request == null) throw new IllegalArgumentException("Regra não informada.");
        if (request.nome() == null || request.nome().isBlank() || request.nome().length() > 100)
            throw new IllegalArgumentException("Nome obrigatório, até 100 caracteres.");
        if (request.descricao() != null && request.descricao().length() > 300)
            throw new IllegalArgumentException("Descrição muito longa.");
        Cidr.parse(request.ipCidr());
        if (request.porta() != null && (request.porta() < 1 || request.porta() > 65535))
            throw new IllegalArgumentException("Porta deve ficar entre 1 e 65535, ou vazia para todas.");
        if (!Set.of("TCP", "UDP", "ANY").contains(normalizar(request.protocolo(), "TCP")))
            throw new IllegalArgumentException("Protocolo inválido.");
        if (!Set.of("INBOUND", "OUTBOUND").contains(normalizar(request.direcao(), "INBOUND")))
            throw new IllegalArgumentException("Direção inválida.");
        if (!Set.of("ALLOW", "DENY").contains(normalizar(request.acao(), "ALLOW")))
            throw new IllegalArgumentException("Ação inválida.");
        if (request.prioridade() != null && (request.prioridade() < 1 || request.prioridade() > 999))
            throw new IllegalArgumentException("Prioridade deve ficar entre 1 e 999.");
        String ip = request.ipCidr().trim();
        if (ip.equals("0.0.0.0/0") && "ALLOW".equals(normalizar(request.acao(), "ALLOW"))
            && (request.porta() == null || request.porta() == 22))
            throw new IllegalArgumentException("Negado permitir SSH de qualquer origem no modo DEMO.");
    }

    private void aplicar(FirewallRegra regra, FirewallRegraRequest request) {
        regra.setNome(request.nome().trim());
        regra.setDescricao(request.descricao());
        regra.setIpCidr(Cidr.parse(request.ipCidr()).texto());
        regra.setPorta(request.porta());
        regra.setProtocolo(normalizar(request.protocolo(), "TCP"));
        regra.setDirecao(normalizar(request.direcao(), "INBOUND"));
        regra.setAcao(normalizar(request.acao(), "ALLOW"));
        regra.setPrioridade(request.prioridade() == null ? 100 : request.prioridade());
        regra.setAtiva(request.ativa() == null || request.ativa());
    }

    private String normalizar(String valor, String padrao) {
        return valor == null || valor.isBlank() ? padrao : valor.trim().toUpperCase(Locale.ROOT);
    }

    private FirewallRegra buscar(Long id) {
        return regras.findById(id).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Regra não encontrada."));
    }

    private Usuario exigirDemo() {
        Usuario ator = atual.requireAdmin();
        if (modoReal())
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Firewall real desabilitado. Use o modo DEMO ou o agente local autorizado.");
        return ator;
    }

    private boolean modoReal() {
        return "real".equalsIgnoreCase(modo);
    }

    private void registrar(Usuario ator, String operacao, Long regraId, String detalhes) {
        FirewallHistorico historico = new FirewallHistorico();
        historico.setAtorEmail(ator.getEmail());
        historico.setOperacao(operacao);
        historico.setRegraId(regraId);
        historico.setDetalhes(RequisicaoInfo.recortar(detalhes, 600));
        historico.setResultado("SIMULADO");
        historicos.save(historico);
    }

    private FirewallRegraResponse toResponse(FirewallRegra r) {
        return new FirewallRegraResponse(r.getId(), r.getNome(), r.getDescricao(), r.getIpCidr(),
            r.getPorta(), r.getProtocolo(), r.getDirecao(), r.getAcao(), r.getPrioridade(),
            r.isAtiva(), r.getCriadoEm(), r.getAtualizadoEm(), r.getCriadoPor(), r.getAtualizadoPor());
    }

    private List<FirewallRegra> padraoDemo(String autor) {
        List<FirewallRegra> lista = new ArrayList<>();
        lista.add(regra("Administração local", "Acesso ao backend apenas via loopback", "127.0.0.1/32",
            8080, "TCP", "INBOUND", "ALLOW", 10, autor));
        lista.add(regra("HTTPS público", "Porta HTTPS exposta (simulação)", "0.0.0.0/0",
            443, "TCP", "INBOUND", "ALLOW", 20, autor));
        lista.add(regra("SSH restrito", "SSH permitido apenas da rede interna", "10.0.0.0/8",
            22, "TCP", "INBOUND", "ALLOW", 30, autor));
        lista.add(regra("Bloquear Telnet", "Serviço legado inseguro", "0.0.0.0/0",
            23, "TCP", "INBOUND", "DENY", 40, autor));
        lista.add(regra("Saída HTTPS", "Tráfego de saída para atualizações", "0.0.0.0/0",
            443, "TCP", "OUTBOUND", "ALLOW", 50, autor));
        return lista;
    }

    private FirewallRegra regra(String nome, String descricao, String cidr, int porta, String protocolo,
                                String direcao, String acao, int prioridade, String autor) {
        FirewallRegra regra = new FirewallRegra();
        regra.setNome(nome);
        regra.setDescricao(descricao);
        regra.setIpCidr(cidr);
        regra.setPorta(porta);
        regra.setProtocolo(protocolo);
        regra.setDirecao(direcao);
        regra.setAcao(acao);
        regra.setPrioridade(prioridade);
        regra.setAtiva(true);
        regra.setCriadoPor(autor);
        regra.setCriadoEm(Instant.now());
        return regra;
    }

    public record RegraSnapshot(String nome, String descricao, String ipCidr, Integer porta,
                                String protocolo, String direcao, String acao, int prioridade, boolean ativa) {}

    private record Cidr(byte[] endereco, int prefixo, String texto) {
        static Cidr parse(String valor) {
            if (valor == null || valor.isBlank()) throw new IllegalArgumentException("Informe um IP ou CIDR.");
            String limpo = valor.trim();
            if (!limpo.matches("^[0-9a-fA-F:.]+(/\\d{1,3})?$"))
                throw new IllegalArgumentException("IP/CIDR inválido: use endereço literal, sem nomes DNS.");
            String[] partes = limpo.split("/");
            String endereco = partes[0];
            boolean ipv4 = endereco.matches("^\\d{1,3}(\\.\\d{1,3}){3}$");
            boolean ipv6 = !ipv4 && endereco.contains(":");
            if (!ipv4 && !ipv6) throw new IllegalArgumentException("IP/CIDR inválido.");
            int max = ipv4 ? 32 : 128;
            int prefixo = partes.length == 2 ? Integer.parseInt(partes[1]) : max;
            if (prefixo < 0 || prefixo > max) throw new IllegalArgumentException("Prefixo CIDR inválido.");
            try {
                byte[] bytes = InetAddress.getByName(endereco).getAddress();
                if ((ipv4 && bytes.length != 4) || (ipv6 && bytes.length != 16))
                    throw new IllegalArgumentException("Família de endereço inválida.");
                Cidr cidr = new Cidr(bytes, prefixo, endereco + "/" + prefixo);
                cidr.validarOctetos(ipv4);
                return cidr;
            } catch (IllegalArgumentException e) {
                throw e;
            } catch (Exception e) {
                throw new IllegalArgumentException("IP/CIDR inválido.");
            }
        }

        private void validarOctetos(boolean ipv4) {
            if (!ipv4) return;
            for (byte b : endereco) {
                int valor = b & 0xFF;
                if (valor > 255) throw new IllegalArgumentException("Octeto IPv4 inválido.");
            }
        }
    }
}

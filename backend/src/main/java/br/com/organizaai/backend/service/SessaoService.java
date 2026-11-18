package br.com.organizaai.backend.service;

import br.com.organizaai.backend.model.Sessao;
import br.com.organizaai.backend.repository.SessaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.*;
import java.time.*;
import java.util.*;

@Service
public class SessaoService {
    private final SessaoRepository repository;
    private final SecureRandom random = new SecureRandom();

    public SessaoService(SessaoRepository repository) { this.repository = repository; }

    public String criar(Long usuarioId) {
        return criar(usuarioId, 7, null, null);
    }

    @Transactional
    public String criar(Long usuarioId, int dias, String ip, String userAgent) {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        Sessao sessao = new Sessao();
        sessao.hash = hash(token);
        sessao.usuarioId = usuarioId;
        sessao.expiraEm = Instant.now().plus(Duration.ofDays(Math.max(1, Math.min(30, dias))));
        sessao.criadoEm = Instant.now();
        sessao.ip = ip;
        sessao.userAgent = userAgent;
        repository.save(sessao);
        return token;
    }

    @Transactional
    public Optional<Long> autenticar(String token) {
        if (token == null || token.length() != 43) return Optional.empty();
        Optional<Sessao> encontrada = repository.findById(hash(token))
            .filter(s -> s.revogadaEm == null)
            .filter(s -> s.expiraEm.isAfter(Instant.now()));
        encontrada.ifPresent(s -> {
            if (s.ultimoUsoEm == null || s.ultimoUsoEm.isBefore(Instant.now().minusSeconds(60))) {
                s.ultimoUsoEm = Instant.now();
                repository.save(s);
            }
        });
        return encontrada.map(s -> s.usuarioId);
    }

    @Transactional
    public void revogar(String token) {
        if (token == null || token.length() != 43) return;
        repository.findById(hash(token)).ifPresent(s -> {
            s.revogadaEm = Instant.now();
            repository.save(s);
        });
    }

    @Transactional
    public int revogarDoUsuario(Long usuarioId) {
        return repository.revogarDoUsuario(usuarioId, Instant.now());
    }

    @Transactional(readOnly = true)
    public long contarAtivas(Long usuarioId) {
        return repository.findByUsuarioIdAndRevogadaEmIsNullAndExpiraEmAfter(usuarioId, Instant.now()).size();
    }

    @Transactional(readOnly = true)
    public long contarAtivas() {
        return repository.countByRevogadaEmIsNullAndExpiraEmAfter(Instant.now());
    }

    @Transactional(readOnly = true)
    public List<Sessao> ativasDoUsuario(Long usuarioId) {
        return repository.findByUsuarioIdAndRevogadaEmIsNullAndExpiraEmAfter(usuarioId, Instant.now());
    }

    private String hash(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(token.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }
}

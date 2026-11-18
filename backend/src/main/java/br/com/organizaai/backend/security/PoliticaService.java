package br.com.organizaai.backend.security;

import br.com.organizaai.backend.dto.PoliticaRequest;
import br.com.organizaai.backend.dto.PoliticaResponse;
import br.com.organizaai.backend.model.PoliticaSeguranca;
import br.com.organizaai.backend.model.Usuario;
import br.com.organizaai.backend.repository.PoliticaSegurancaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class PoliticaService {
    private final PoliticaSegurancaRepository repository;

    public PoliticaService(PoliticaSegurancaRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public PoliticaSeguranca obter() {
        return repository.findById(1L).orElseGet(() -> repository.save(new PoliticaSeguranca()));
    }

    @Transactional
    public PoliticaSeguranca atualizar(PoliticaRequest request, Usuario ator) {
        if (request == null) throw new IllegalArgumentException("Política não informada.");
        PoliticaSeguranca politica = obter();
        if (request.senhaMinima() != null) politica.setSenhaMinima(limite(request.senhaMinima(), 8, 72, "senha mínima"));
        if (request.maxTentativas() != null) politica.setMaxTentativas(limite(request.maxTentativas(), 3, 20, "tentativas"));
        if (request.janelaMinutos() != null) politica.setJanelaMinutos(limite(request.janelaMinutos(), 1, 1440, "janela"));
        if (request.bloqueioMinutos() != null) politica.setBloqueioMinutos(limite(request.bloqueioMinutos(), 1, 1440, "bloqueio"));
        if (request.sessaoDias() != null) politica.setSessaoDias(limite(request.sessaoDias(), 1, 30, "sessão"));
        if (request.exigirNumero() != null) politica.setExigirNumero(request.exigirNumero());
        if (request.exigirMaiuscula() != null) politica.setExigirMaiuscula(request.exigirMaiuscula());
        politica.setAtualizadoEm(Instant.now());
        politica.setAtualizadoPor(ator.getEmail());
        return repository.save(politica);
    }

    public void validarSenhaUsuario(String senha) {
        PoliticaSeguranca politica = obter();
        if (senha == null || senha.length() < politica.getSenhaMinima())
            throw new IllegalArgumentException("A senha deve ter pelo menos " + politica.getSenhaMinima() + " caracteres.");
        if (senha.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72)
            throw new IllegalArgumentException("A senha deve ter até 72 bytes em UTF-8.");
        if (politica.isExigirNumero() && !senha.matches(".*\\d.*"))
            throw new IllegalArgumentException("A senha deve conter pelo menos um número.");
        if (politica.isExigirMaiuscula() && !senha.matches(".*[A-Z].*"))
            throw new IllegalArgumentException("A senha deve conter pelo menos uma letra maiúscula.");
    }

    /** Contas administrativas exigem política reforçada, independente da política de usuários. */
    public void validarSenhaAdministrativa(String senha) {
        if (senha == null || senha.length() < 12)
            throw new IllegalArgumentException("A senha administrativa deve ter pelo menos 12 caracteres.");
        if (!senha.matches(".*[A-Z].*") || !senha.matches(".*[a-z].*") || !senha.matches(".*\\d.*"))
            throw new IllegalArgumentException("A senha administrativa deve conter maiúscula, minúscula e número.");
        if (senha.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72)
            throw new IllegalArgumentException("A senha administrativa deve ter até 72 bytes em UTF-8.");
    }

    public PoliticaResponse toResponse(PoliticaSeguranca politica) {
        return new PoliticaResponse(politica.getSenhaMinima(), politica.isExigirNumero(),
            politica.isExigirMaiuscula(), politica.getMaxTentativas(), politica.getJanelaMinutos(),
            politica.getBloqueioMinutos(), politica.getSessaoDias(), politica.getAtualizadoEm(),
            politica.getAtualizadoPor());
    }

    private int limite(int valor, int minimo, int maximo, String campo) {
        if (valor < minimo || valor > maximo)
            throw new IllegalArgumentException("Valor inválido para " + campo + " (" + minimo + " a " + maximo + ").");
        return valor;
    }
}

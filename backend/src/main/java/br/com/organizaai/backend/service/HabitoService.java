package br.com.organizaai.backend.service;

import br.com.organizaai.backend.model.*;
import br.com.organizaai.backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;

@Service
@Transactional
public class HabitoService {

    private final HabitoRepository habitos;
    private final UsuarioRepository usuarios;

    public HabitoService(HabitoRepository habitos, UsuarioRepository usuarios) {
        this.habitos = habitos;
        this.usuarios = usuarios;
    }

    public List<Habito> listar(Long usuarioId) {
        return habitos.findByUsuarioIdOrderByCriadoEmAsc(usuarioId);
    }

    public Optional<Habito> criar(Long usuarioId, Habito dados) {
        validar(dados);
        return usuarios.findById(usuarioId).map(usuario -> {
            Habito habito = new Habito();
            habito.setUsuario(usuario);
            copiar(dados, habito);
            return habitos.save(habito);
        });
    }

    public Optional<Habito> atualizar(Long id, Long usuarioId, Habito dados) {
        validar(dados);
        return habitos.findByIdAndUsuarioId(id, usuarioId).map(habito -> {
            copiar(dados, habito);
            return habitos.save(habito);
        });
    }

    public boolean excluir(Long id, Long usuarioId) {
        return habitos.findByIdAndUsuarioId(id, usuarioId).map(habito -> {
            habitos.delete(habito);
            return true;
        }).orElse(false);
    }

    /** Registro idempotente: repetir a mesma data não cria duplicidade. */
    public Optional<Habito> registrar(Long id, Long usuarioId, LocalDate data) {
        if (data == null) throw new IllegalArgumentException("Informe a data do registro.");
        return habitos.findByIdAndUsuarioId(id, usuarioId).map(habito -> {
            boolean existe = habito.getRegistros().stream().anyMatch(r -> r.getData().equals(data));
            if (!existe) {
                habito.getRegistros().add(new HabitoRegistro(habito, data));
                habitos.save(habito);
            }
            return habito;
        });
    }

    public Optional<Habito> removerRegistro(Long id, Long usuarioId, LocalDate data) {
        if (data == null) throw new IllegalArgumentException("Informe a data do registro.");
        return habitos.findByIdAndUsuarioId(id, usuarioId).map(habito -> {
            habito.getRegistros().removeIf(r -> r.getData().equals(data));
            return habitos.save(habito);
        });
    }

    private void copiar(Habito dados, Habito habito) {
        habito.setNome(dados.getNome().trim());
        habito.setDescricao(dados.getDescricao());
        habito.setFrequencia(dados.getFrequencia());
        habito.setMetaSemanal(dados.getMetaSemanal());
        habito.setCor(dados.getCor());
        habito.setAtivo(dados.isAtivo());
    }

    private void validar(Habito dados) {
        if (dados.getNome() == null || dados.getNome().isBlank() || dados.getNome().length() > 100)
            throw new IllegalArgumentException("Nome do hábito obrigatório, até 100 caracteres.");
        if (dados.getDescricao() != null && dados.getDescricao().length() > 500)
            throw new IllegalArgumentException("Descrição do hábito muito longa.");
        if (dados.getFrequencia() == null || !Set.of("DIARIA", "SEMANAL").contains(dados.getFrequencia()))
            throw new IllegalArgumentException("Frequência inválida. Use DIARIA ou SEMANAL.");
        if (dados.getMetaSemanal() < 1 || dados.getMetaSemanal() > 7)
            throw new IllegalArgumentException("A meta deve ficar entre 1 e 7 dias por semana.");
        if (dados.getCor() != null && !dados.getCor().isBlank() && !dados.getCor().matches("#[0-9a-fA-F]{6}"))
            throw new IllegalArgumentException("Cor inválida.");
    }
}

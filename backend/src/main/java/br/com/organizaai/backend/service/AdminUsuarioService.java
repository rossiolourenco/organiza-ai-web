package br.com.organizaai.backend.service;

import br.com.organizaai.backend.dto.*;
import br.com.organizaai.backend.model.*;
import br.com.organizaai.backend.repository.AuditoriaRepository;
import br.com.organizaai.backend.repository.UsuarioRepository;
import br.com.organizaai.backend.security.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class AdminUsuarioService {
    private final UsuarioRepository usuarios;
    private final AuditoriaRepository auditorias;
    private final AuditoriaService auditoria;
    private final SessaoService sessoes;
    private final SenhaService senhas;
    private final PasswordEncoder encoder;
    private final PoliticaService politica;
    private final UsuarioAtual atual;

    public AdminUsuarioService(UsuarioRepository usuarios, AuditoriaRepository auditorias,
                               AuditoriaService auditoria, SessaoService sessoes, SenhaService senhas,
                               PasswordEncoder encoder, PoliticaService politica, UsuarioAtual atual) {
        this.usuarios = usuarios;
        this.auditorias = auditorias;
        this.auditoria = auditoria;
        this.sessoes = sessoes;
        this.senhas = senhas;
        this.encoder = encoder;
        this.politica = politica;
        this.atual = atual;
    }

    @Transactional(readOnly = true)
    public AdminMeResponse me() {
        Usuario usuario = atual.requireAdmin();
        return new AdminMeResponse(usuario.getId(), usuario.getNome(), usuario.getEmail(),
            usuario.getPapel().name(), Rbac.permissoes(usuario.getPapel()));
    }

    @Transactional(readOnly = true)
    public PaginaResponse<UsuarioAdminResponse> listar(String q, String status, String papel, int page, int size) {
        atual.requireAdmin();
        StatusConta statusFiltro = enumOuNull(StatusConta.class, status);
        Papel papelFiltro = enumOuNull(Papel.class, papel);
        var pagina = usuarios.buscar(like(q), statusFiltro, papelFiltro,
            PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size)),
                Sort.by(Sort.Direction.ASC, "id")));
        return PaginaResponse.de(pagina, this::toResponse);
    }

    @Transactional(readOnly = true)
    public UsuarioAdminDetalheResponse detalhar(Long id) {
        Usuario ator = atual.requireAdmin();
        Usuario alvo = buscar(id);
        if (!ator.getId().equals(id) && !Rbac.podeGerenciarConta(ator.getPapel(), alvo.getPapel()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sem permissão para ver esta conta.");
        var sessoesAtivas = sessoes.ativasDoUsuario(id).stream()
            .map(s -> new UsuarioAdminDetalheResponse.SessaoResumoResponse(s.criadoEm, s.ultimoUsoEm,
                s.expiraEm, s.ip, s.userAgent))
            .toList();
        var eventos = auditorias.findTop10ByRecursoAndRecursoIdOrderByCriadoEmDesc("usuario", String.valueOf(id))
            .stream().map(auditoria::toResponse).toList();
        return new UsuarioAdminDetalheResponse(toResponse(alvo), sessoesAtivas, eventos);
    }

    @Transactional
    public UsuarioAdminResponse alterarStatus(Long id, AtualizarStatusRequest request) {
        Usuario ator = atual.requireAdmin();
        Usuario alvo = buscar(id);
        if (request == null || request.status() == null)
            throw new IllegalArgumentException("Informe o novo status.");
        if (ator.getId().equals(id))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Você não pode alterar o próprio status.");
        if (!Rbac.podeGerenciarConta(ator.getPapel(), alvo.getPapel()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sem permissão para gerenciar esta conta.");

        StatusConta novo;
        try { novo = StatusConta.valueOf(request == null ? "" : request.status().toUpperCase(Locale.ROOT)); }
        catch (Exception e) { throw new IllegalArgumentException("Status inválido."); }
        if (novo == StatusConta.ANONIMIZADO)
            throw new IllegalArgumentException("Use a anonimização para remover uma conta.");
        if (alvo.getStatus() == novo)
            throw new IllegalArgumentException("A conta já está neste status.");

        if (novo != StatusConta.ATIVO && alvo.getPapel() == Papel.SUPER_ADMIN && ultimoSuperAdmin(alvo.getId()))
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "O último SUPER_ADMIN ativo não pode ser bloqueado ou desativado.");

        StatusConta anterior = alvo.getStatus();
        alvo.setStatus(novo);
        alvo.setBloqueadoAte(null);
        alvo.setMotivoBloqueio(novo == StatusConta.ATIVO ? null : motivo(request.motivo()));
        usuarios.save(alvo);
        int revogadas = novo == StatusConta.ATIVO ? 0 : sessoes.revogarDoUsuario(id);

        String acao = novo == StatusConta.ATIVO
            ? (anterior == StatusConta.BLOQUEADO ? "CONTA_DESBLOQUEADA" : "CONTA_REATIVADA")
            : novo == StatusConta.BLOQUEADO ? "CONTA_BLOQUEADA" : "CONTA_DESATIVADA";
        auditoria.registrar(ator, acao, "usuario", id, "SUCESSO",
            acao + " (status anterior " + anterior + "). Sessões revogadas: " + revogadas + ".");
        return toResponse(alvo);
    }

    @Transactional
    public UsuarioAdminResponse alterarPapel(Long id, AtualizarPapelRequest request) {
        Usuario ator = atual.requireAdmin();
        if (!Rbac.superAdmin(ator.getPapel()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Somente SUPER_ADMIN altera papéis.");
        Usuario alvo = buscar(id);
        if (ator.getId().equals(id))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Você não pode alterar o próprio papel.");
        Papel novo;
        try { novo = Papel.valueOf(request == null ? "" : request.papel().toUpperCase(Locale.ROOT)); }
        catch (Exception e) { throw new IllegalArgumentException("Papel inválido."); }
        if (novo == alvo.getPapel())
            throw new IllegalArgumentException("A conta já possui este papel.");
        if (alvo.getPapel() == Papel.SUPER_ADMIN && novo != Papel.SUPER_ADMIN && ultimoSuperAdmin(alvo.getId()))
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "O último SUPER_ADMIN ativo não pode ser rebaixado.");

        Papel anterior = alvo.getPapel();
        alvo.setPapel(novo);
        usuarios.save(alvo);
        auditoria.registrar(ator, "PAPEL_ALTERADO", "usuario", id, "SUCESSO",
            "Papel alterado de " + anterior + " para " + novo + ".");
        return toResponse(alvo);
    }

    @Transactional
    public int revogarSessoes(Long id) {
        Usuario ator = atual.requireAdmin();
        Usuario alvo = buscar(id);
        if (ator.getId().equals(id))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Use o logout para encerrar sua própria sessão.");
        if (!Rbac.podeGerenciarConta(ator.getPapel(), alvo.getPapel()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sem permissão para revogar estas sessões.");
        int total = sessoes.revogarDoUsuario(id);
        auditoria.registrar(ator, "SESSOES_REVOGADAS", "usuario", id, "SUCESSO",
            "Sessões revogadas: " + total + ".");
        return total;
    }

    @Transactional
    public SenhaTemporariaResponse redefinirSenha(Long id) {
        Usuario ator = atual.requireAdmin();
        Usuario alvo = buscar(id);
        if (ator.getId().equals(id))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Altere sua própria senha no perfil; a redefinição administrativa é para terceiros.");
        if (!Rbac.podeGerenciarConta(ator.getPapel(), alvo.getPapel()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sem permissão para redefinir esta senha.");
        String temporaria = senhas.temporaria();
        if (alvo.getPapel() == Papel.ADMIN || alvo.getPapel() == Papel.SUPER_ADMIN)
            politica.validarSenhaAdministrativa(temporaria);
        alvo.setSenha(encoder.encode(temporaria));
        alvo.setSenhaAlteradaEm(Instant.now());
        alvo.setDeveTrocarSenha(true);
        usuarios.save(alvo);
        sessoes.revogarDoUsuario(id);
        auditoria.registrar(ator, "SENHA_REDEFINIDA", "usuario", id, "SUCESSO",
            "Senha temporária emitida e sessões revogadas. Conteúdo da senha não registrado.");
        return new SenhaTemporariaResponse(alvo.getId(), alvo.getEmail(), temporaria,
            "Exiba esta senha uma única vez. O usuário deve alterá-la após entrar.");
    }

    @Transactional
    public SenhaTemporariaResponse criar(CriarUsuarioAdminRequest request) {
        Usuario ator = atual.requireAdmin();
        if (request == null || request.nome() == null || request.nome().isBlank()
            || request.nome().trim().length() > 100)
            throw new IllegalArgumentException("Nome obrigatório, até 100 caracteres.");
        if (request.email() == null || !request.email().contains("@") || request.email().length() > 150)
            throw new IllegalArgumentException("E-mail inválido.");
        Papel papel;
        try { papel = request.papel() == null || request.papel().isBlank() ? Papel.USER
            : Papel.valueOf(request.papel().toUpperCase(Locale.ROOT)); }
        catch (Exception e) { throw new IllegalArgumentException("Papel inválido."); }
        if (papel == Papel.SUPER_ADMIN)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "SUPER_ADMIN só pode ser provisionado pelo bootstrap seguro.");
        if (papel == Papel.ADMIN && ator.getPapel() != Papel.SUPER_ADMIN)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Somente SUPER_ADMIN cria administradores.");

        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (usuarios.findByEmailIgnoreCase(email).isPresent())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe uma conta com este e-mail.");

        String temporaria = senhas.temporaria();
        if (papel == Papel.ADMIN) politica.validarSenhaAdministrativa(temporaria);
        Usuario novo = new Usuario();
        novo.setNome(request.nome().trim());
        novo.setEmail(email);
        novo.setSenha(encoder.encode(temporaria));
        novo.setPapel(papel);
        novo.setStatus(StatusConta.ATIVO);
        novo.setDeveTrocarSenha(true);
        novo.setSenhaAlteradaEm(Instant.now());
        usuarios.save(novo);
        auditoria.registrar(ator, "CONTA_CRIADA", "usuario", novo.getId(), "SUCESSO",
            "Conta criada com papel " + papel + ". Senha temporária não registrada.");
        return new SenhaTemporariaResponse(novo.getId(), novo.getEmail(), temporaria,
            "Exiba esta senha uma única vez. O usuário deve alterá-la após entrar.");
    }

    @Transactional
    public void anonimizar(Long id) {
        Usuario ator = atual.requireAdmin();
        Usuario alvo = buscar(id);
        if (ator.getId().equals(id))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Você não pode anonimizar a própria conta.");
        if (!Rbac.podeGerenciarConta(ator.getPapel(), alvo.getPapel()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sem permissão para anonimizar esta conta.");
        if (alvo.getPapel() == Papel.SUPER_ADMIN && ultimoSuperAdmin(alvo.getId()))
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "O último SUPER_ADMIN ativo não pode ser anonimizado.");

        alvo.setNome("Conta anonimizada #" + id);
        alvo.setEmail("anonimizado+" + id + "+" + UUID.randomUUID() + "@anon.invalid");
        alvo.setSenha(encoder.encode(senhas.temporaria()));
        alvo.setPapel(Papel.USER);
        alvo.setStatus(StatusConta.ANONIMIZADO);
        alvo.setMotivoBloqueio("Conta anonimizada por política administrativa");
        alvo.setDeveTrocarSenha(true);
        usuarios.save(alvo);
        sessoes.revogarDoUsuario(id);
        auditoria.registrar(ator, "CONTA_ANONIMIZADA", "usuario", id, "SUCESSO",
            "Dados de identificação removidos; tarefas e categorias mantidas por integridade.");
    }

    public UsuarioAdminResponse toResponse(Usuario usuario) {
        return new UsuarioAdminResponse(usuario.getId(), usuario.getNome(), usuario.getEmail(),
            usuario.getPapel().name(), usuario.getStatus().name(), usuario.getDataCriacao(),
            usuario.getUltimoLoginEm(), usuario.getBloqueadoAte(), usuario.getMotivoBloqueio(),
            usuario.isDeveTrocarSenha(), sessoes.contarAtivas(usuario.getId()));
    }

    private Usuario buscar(Long id) {
        return usuarios.findById(id).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Conta não encontrada."));
    }

    private boolean ultimoSuperAdmin(Long id) {
        return usuarios.countByPapelAndStatusAndIdNot(Papel.SUPER_ADMIN, StatusConta.ATIVO, id) == 0;
    }

    private String motivo(String valor) {
        String limpo = valor == null || valor.isBlank() ? "Ação administrativa" : valor.trim();
        return RequisicaoInfo.recortar(limpo, 255);
    }

    private <E extends Enum<E>> E enumOuNull(Class<E> tipo, String valor) {
        if (valor == null || valor.isBlank()) return null;
        try { return Enum.valueOf(tipo, valor.trim().toUpperCase(Locale.ROOT)); }
        catch (Exception e) { throw new IllegalArgumentException("Filtro inválido: " + valor); }
    }

    private String like(String valor) {
        if (valor == null || valor.isBlank()) return null;
        return "%" + valor.trim().toLowerCase(java.util.Locale.ROOT) + "%";
    }
}

package br.com.organizaai.backend;

import br.com.organizaai.backend.model.*;
import br.com.organizaai.backend.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.net.URI;
import java.net.http.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "spring.datasource.url=jdbc:h2:mem:admin_tests;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
    "spring.jpa.show-sql=false",
    "app.firewall.mode=demo"
})
@ActiveProfiles("local")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AdminApiTests {
    @Value("${local.server.port}") int port;
    @Autowired UsuarioRepository usuarios;
    @Autowired TarefaRepository tarefas;
    @Autowired PasswordEncoder encoder;
    final HttpClient client = HttpClient.newHttpClient();
    final JsonMapper json = JsonMapper.builder().build();
    final String senha = "Admin-password-2026";

    record Conta(long id, String email, String token) {}
    record Resultado(int status, JsonNode body) {}

    Resultado call(String method, String path, Object body, String token) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
            .header("Content-Type", "application/json");
        if (token != null) builder.header("Authorization", "Bearer " + token);
        var response = client.send(builder.method(method, body == null ? HttpRequest.BodyPublishers.noBody()
            : HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body))).build(),
            HttpResponse.BodyHandlers.ofString());
        return new Resultado(response.statusCode(), response.body().isBlank() ? null : json.readTree(response.body()));
    }

    Conta criar(Papel papel, StatusConta status, String senha) {
        Usuario usuario = new Usuario();
        usuario.setNome("DEMO " + papel + " " + UUID.randomUUID());
        usuario.setEmail(UUID.randomUUID() + "@demo.invalid");
        usuario.setSenha(encoder.encode(senha));
        usuario.setPapel(papel);
        usuario.setStatus(status);
        usuarios.save(usuario);
        return new Conta(usuario.getId(), usuario.getEmail(), null);
    }

    Conta login(Conta conta, String senha) throws Exception {
        var resultado = call("POST", "/api/usuarios/login",
            Map.of("email", conta.email, "senha", senha), null);
        assertEquals(200, resultado.status, String.valueOf(resultado.body));
        return new Conta(conta.id, conta.email, resultado.body.get("token").asString());
    }

    Map<String, Object> tarefa() {
        Map<String, Object> data = new HashMap<>();
        data.put("titulo", "DEMO Tarefa admin");
        data.put("prioridade", "MEDIA");
        data.put("status", "PENDENTE");
        data.put("dataPrazo", "2026-11-20");
        data.put("categoria", null);
        return data;
    }

    @Test void userCannotAccessAdminSurface() throws Exception {
        Conta user = login(criar(Papel.USER, StatusConta.ATIVO, senha), senha);
        assertEquals(403, call("GET", "/api/admin/usuarios", null, user.token).status);
        assertEquals(403, call("GET", "/api/admin/usuarios/me", null, user.token).status);
        assertEquals(403, call("GET", "/api/admin/seguranca/overview", null, user.token).status);
        assertEquals(403, call("GET", "/api/admin/auditoria", null, user.token).status);
        assertEquals(403, call("GET", "/api/admin/firewall/regras", null, user.token).status);
        assertEquals(403, call("GET", "/api/admin/monitoramento/overview", null, user.token).status);
        assertEquals(401, call("GET", "/api/admin/usuarios", null, null).status);
    }

    @Test void adminManagesUsersButNotAdminsOrRoles() throws Exception {
        Conta superAdmin = login(criar(Papel.SUPER_ADMIN, StatusConta.ATIVO, senha), senha);
        Conta admin = login(criar(Papel.ADMIN, StatusConta.ATIVO, senha), senha);
        Conta user = criar(Papel.USER, StatusConta.ATIVO, senha);

        var lista = call("GET", "/api/admin/usuarios?q=" + user.email, null, admin.token);
        assertEquals(200, lista.status);
        assertEquals(1, lista.body.get("content").size());
        assertFalse(lista.body.get("content").get(0).has("senha"));

        assertEquals(400, call("PATCH", "/api/admin/usuarios/" + admin.id + "/status",
            Map.of("status", "BLOQUEADO", "motivo", "teste"), admin.token).status);
        assertEquals(403, call("GET", "/api/admin/usuarios/" + superAdmin.id, null, admin.token).status);
        assertEquals(403, call("PATCH", "/api/admin/usuarios/" + user.id + "/papel",
            Map.of("papel", "ADMIN"), admin.token).status);
        assertEquals(403, call("POST", "/api/admin/usuarios",
            Map.of("nome", "DEMO", "email", "novo-admin-" + UUID.randomUUID() + "@demo.invalid", "papel", "ADMIN"),
            admin.token).status);

        var bloqueado = call("PATCH", "/api/admin/usuarios/" + user.id + "/status",
            Map.of("status", "BLOQUEADO", "motivo", "Auditoria de teste"), admin.token);
        assertEquals(200, bloqueado.status);
        assertEquals("BLOQUEADO", bloqueado.body.get("status").asString());
        assertEquals(403, call("POST", "/api/usuarios/login",
            Map.of("email", user.email, "senha", senha), null).status);

        assertEquals(200, call("PATCH", "/api/admin/usuarios/" + user.id + "/status",
            Map.of("status", "ATIVO"), admin.token).status);
        Conta userAtivo = login(user, senha);

        var reset = call("POST", "/api/admin/usuarios/" + user.id + "/redefinir-senha", null, admin.token);
        assertEquals(200, reset.status);
        String temporaria = reset.body.get("senhaTemporaria").asString();
        assertTrue(temporaria.length() >= 12);
        var loginTemporario = call("POST", "/api/usuarios/login",
            Map.of("email", user.email, "senha", temporaria), null);
        assertEquals(200, loginTemporario.status);
        assertTrue(loginTemporario.body.get("deveTrocarSenha").asBoolean());
        String tokenTemporario = loginTemporario.body.get("token").asString();

        var alterar = call("PUT", "/api/usuarios/senha",
            Map.of("senhaAtual", temporaria, "novaSenha", "Nova-senha-2026"), tokenTemporario);
        assertEquals(204, alterar.status);
        assertEquals(401, call("GET", "/api/tarefas?usuarioId=" + user.id, null, tokenTemporario).status);
        var novaSessao = call("POST", "/api/usuarios/login",
            Map.of("email", user.email, "senha", "Nova-senha-2026"), null);
        assertEquals(200, novaSessao.status);
        assertFalse(novaSessao.body.get("deveTrocarSenha").asBoolean());
        assertEquals(401, call("POST", "/api/usuarios/login",
            Map.of("email", user.email, "senha", temporaria), null).status);

        String tokenNovo = novaSessao.body.get("token").asString();
        assertEquals(200, call("POST", "/api/admin/usuarios/" + user.id + "/revogar-sessoes", null, admin.token).status);
        assertEquals(401, call("GET", "/api/tarefas?usuarioId=" + user.id, null, tokenNovo).status);
        assertEquals(401, call("GET", "/api/tarefas?usuarioId=" + user.id, null, userAtivo.token).status);
    }

    @Test void superAdminManagesRolesAndKeepsPrivilegeInvariants() throws Exception {
        Conta super1 = login(criar(Papel.SUPER_ADMIN, StatusConta.ATIVO, senha), senha);
        Conta super2 = login(criar(Papel.SUPER_ADMIN, StatusConta.ATIVO, senha), senha);
        Conta admin = criar(Papel.ADMIN, StatusConta.ATIVO, senha);
        Conta user = criar(Papel.USER, StatusConta.ATIVO, senha);

        assertEquals(200, call("PATCH", "/api/admin/usuarios/" + user.id + "/papel",
            Map.of("papel", "ADMIN"), super1.token).status);
        assertEquals(200, call("PATCH", "/api/admin/usuarios/" + admin.id + "/papel",
            Map.of("papel", "USER"), super1.token).status);
        assertEquals(200, call("PATCH", "/api/admin/usuarios/" + user.id + "/papel",
            Map.of("papel", "SUPER_ADMIN"), super1.token).status);

        assertEquals(400, call("PATCH", "/api/admin/usuarios/" + super1.id + "/papel",
            Map.of("papel", "ADMIN"), super1.token).status);
        assertEquals(400, call("PATCH", "/api/admin/usuarios/" + super1.id + "/status",
            Map.of("status", "BLOQUEADO"), super1.token).status);
        assertEquals(400, call("POST", "/api/admin/usuarios/" + super1.id + "/revogar-sessoes", null, super1.token).status);
        assertEquals(400, call("DELETE", "/api/admin/usuarios/" + super1.id, null, super1.token).status);

        assertEquals(200, call("PATCH", "/api/admin/usuarios/" + super2.id + "/status",
            Map.of("status", "BLOQUEADO", "motivo", "Teste de sessão"), super1.token).status);
        assertEquals(401, call("GET", "/api/admin/usuarios", null, super2.token).status);
        assertEquals(200, call("PATCH", "/api/admin/usuarios/" + super2.id + "/status",
            Map.of("status", "ATIVO"), super1.token).status);

        var criado = call("POST", "/api/admin/usuarios",
            Map.of("nome", "DEMO Gestor", "email", "gestor-" + UUID.randomUUID() + "@demo.invalid", "papel", "ADMIN"),
            super1.token);
        assertEquals(200, criado.status);
        assertTrue(criado.body.get("senhaTemporaria").asString().length() >= 12);
    }

    @Test void rateLimitingBlocksBruteForceAndAuditKeepsTrail() throws Exception {
        Conta superAdmin = login(criar(Papel.SUPER_ADMIN, StatusConta.ATIVO, senha), senha);
        Conta user = criar(Papel.USER, StatusConta.ATIVO, senha);
        for (int i = 1; i <= 4; i++) {
            assertEquals(401, call("POST", "/api/usuarios/login",
                Map.of("email", user.email, "senha", "errada"), null).status);
        }
        assertEquals(429, call("POST", "/api/usuarios/login",
            Map.of("email", user.email, "senha", "errada"), null).status);
        assertEquals(StatusConta.BLOQUEADO, usuarios.findById(user.id).orElseThrow().getStatus());
        assertEquals(429, call("POST", "/api/usuarios/login",
            Map.of("email", user.email, "senha", senha), null).status);

        var auditoria = call("GET", "/api/admin/auditoria?acao=CONTA_BLOQUEADA_AUTOMATICA", null, superAdmin.token);
        assertEquals(200, auditoria.status);
        assertTrue(auditoria.body.get("content").size() >= 1);
        assertEquals("SUCESSO", auditoria.body.get("content").get(0).get("resultado").asString());

        var tentativas = call("GET", "/api/admin/seguranca/tentativas?email=" + user.email, null, superAdmin.token);
        assertEquals(200, tentativas.status);
        assertTrue(tentativas.body.get("content").size() >= 5);
    }

    @Test void firewallDemoValidatesConflictsAppliesAndRollsBack() throws Exception {
        Conta superAdmin = login(criar(Papel.SUPER_ADMIN, StatusConta.ATIVO, senha), senha);
        var reset = call("POST", "/api/admin/firewall/reset-demo", null, superAdmin.token);
        assertEquals(200, reset.status);
        assertEquals(5, reset.body.get("totalRegras").asInt());
        assertTrue(call("GET", "/api/admin/firewall/validar", null, superAdmin.token).body.get("valido").asBoolean());

        Map<String, Object> conflito = new HashMap<>();
        conflito.put("nome", "DEMO Bloquear HTTPS");
        conflito.put("ipCidr", "0.0.0.0/0");
        conflito.put("porta", 443);
        conflito.put("protocolo", "TCP");
        conflito.put("direcao", "INBOUND");
        conflito.put("acao", "DENY");
        conflito.put("prioridade", 5);
        conflito.put("ativa", true);
        var criado = call("POST", "/api/admin/firewall/regras", conflito, superAdmin.token);
        assertEquals(200, criado.status);
        long conflitoId = criado.body.get("id").asLong();
        var validacao = call("GET", "/api/admin/firewall/validar", null, superAdmin.token);
        assertFalse(validacao.body.get("valido").asBoolean());
        assertTrue(validacao.body.get("conflitos").size() >= 1);
        assertEquals(409, call("POST", "/api/admin/firewall/aplicar", Map.of("motivo", "teste"), superAdmin.token).status);

        assertEquals(204, call("DELETE", "/api/admin/firewall/regras/" + conflitoId, null, superAdmin.token).status);
        Map<String, Object> valida = new HashMap<>(conflito);
        valida.put("nome", "DEMO Bloquear SMTP");
        valida.put("porta", 25);
        assertEquals(200, call("POST", "/api/admin/firewall/regras", valida, superAdmin.token).status);
        assertEquals(200, call("GET", "/api/admin/firewall/validar", null, superAdmin.token).status);

        var aplicado = call("POST", "/api/admin/firewall/aplicar", Map.of("motivo", "Aplicação de teste"),
            superAdmin.token);
        assertEquals(200, aplicado.status);
        assertTrue(aplicado.body.get("aplicado").asBoolean());
        var rollback = call("POST", "/api/admin/firewall/rollback", null, superAdmin.token);
        assertEquals(200, rollback.status);
        assertFalse(rollback.body.get("aplicado").asBoolean());

        conflito.put("ipCidr", "999.1.1.1/24");
        assertEquals(400, call("POST", "/api/admin/firewall/regras", conflito, superAdmin.token).status);
        var historico = call("GET", "/api/admin/firewall/historico", null, superAdmin.token);
        assertEquals(200, historico.status);
        assertTrue(historico.body.size() >= 4);
    }

    @Test void lastSuperAdminIsNotBlockedByAutomaticRateLimit() throws Exception {
        Conta superAdmin = login(criar(Papel.SUPER_ADMIN, StatusConta.ATIVO, senha), senha);
        for (int i = 1; i <= 4; i++) {
            assertEquals(401, call("POST", "/api/usuarios/login",
                Map.of("email", superAdmin.email, "senha", "errada"), null).status);
        }
        assertEquals(429, call("POST", "/api/usuarios/login",
            Map.of("email", superAdmin.email, "senha", "errada"), null).status);
        Usuario preservado = usuarios.findById(superAdmin.id).orElseThrow();
        assertEquals(StatusConta.ATIVO, preservado.getStatus());
        assertNull(preservado.getBloqueadoAte());
    }

    @Test void monitoringUsesRealMetricsAndIsAdminOnly() throws Exception {
        Conta superAdmin = login(criar(Papel.SUPER_ADMIN, StatusConta.ATIVO, senha), senha);
        Conta user = login(criar(Papel.USER, StatusConta.ATIVO, senha), senha);
        assertEquals(403, call("GET", "/api/admin/monitoramento/overview", null, user.token).status);
        var monitor = call("GET", "/api/admin/monitoramento/overview", null, superAdmin.token);
        assertEquals(200, monitor.status);
        assertTrue(monitor.body.get("backendDisponivel").asBoolean());
        assertTrue(monitor.body.get("banco").get("disponivel").asBoolean());
        assertTrue(monitor.body.get("memoria").get("heapMaximoBytes").asLong() > 0);
        assertEquals("DEMO", monitor.body.get("firewall").get("modo").asString());
        assertTrue(monitor.body.get("http").get("totalRequisicoes").asLong() >= 1);
        assertTrue(monitor.body.get("uptimeSegundos").asLong() >= 0);
    }

    @Test void anonymizationRemovesIdentityButPreservesTasks() throws Exception {
        Conta superAdmin = login(criar(Papel.SUPER_ADMIN, StatusConta.ATIVO, senha), senha);
        Conta user = login(criar(Papel.USER, StatusConta.ATIVO, senha), senha);
        assertEquals(200, call("POST", "/api/tarefas?usuarioId=" + user.id, tarefa(), user.token).status);
        assertEquals(1, tarefas.findByUsuarioIdAndExcluidaEmIsNull(user.id).size());

        assertEquals(204, call("DELETE", "/api/admin/usuarios/" + user.id, null, superAdmin.token).status);
        Usuario anonimo = usuarios.findById(user.id).orElseThrow();
        assertEquals(StatusConta.ANONIMIZADO, anonimo.getStatus());
        assertTrue(anonimo.getEmail().contains("@anon.invalid"));
        assertTrue(anonimo.getNome().contains("anonimizada"));
        assertEquals(1, tarefas.findByUsuarioIdAndExcluidaEmIsNull(user.id).size());
        assertEquals(401, call("GET", "/api/tarefas?usuarioId=" + user.id, null, user.token).status);
        assertEquals(401, call("POST", "/api/usuarios/login",
            Map.of("email", user.email, "senha", senha), null).status);
    }

    @Test void adminListQueriesWorkWithAndWithoutFilters() throws Exception {
        Conta admin = login(criar(Papel.ADMIN, StatusConta.ATIVO, senha), senha);
        String[] paths = {
            "/api/admin/usuarios?page=0&size=10",
            "/api/admin/usuarios?q=DEMO&page=0&size=10",
            "/api/admin/auditoria?page=0&size=10",
            "/api/admin/auditoria?ator=admin&page=0&size=10",
            "/api/admin/seguranca/tentativas?page=0&size=10",
            "/api/admin/seguranca/tentativas?email=demo&page=0&size=10",
        };
        for (String path : paths) {
            var r = call("GET", path, null, admin.token);
            assertEquals(200, r.status, path + " -> " + r.body);
        }
    }
}

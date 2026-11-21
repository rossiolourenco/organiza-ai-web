package br.com.organizaai.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "spring.datasource.url=jdbc:h2:mem:conta_tests;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
    "spring.jpa.show-sql=false"
})
@ActiveProfiles("local")
class ContaApiTests {
    @Value("${local.server.port}") int port;
    final HttpClient client = HttpClient.newHttpClient();
    final JsonMapper json = JsonMapper.builder().build();

    record Conta(long id, String email, String token) {}
    record Resultado(int status, JsonNode body) {}

    Resultado call(String method, String path, Object body, String token) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api" + path))
            .header("Content-Type", "application/json");
        if (token != null) builder.header("Authorization", "Bearer " + token);
        var response = client.send(builder.method(method, body == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body))).build(),
            HttpResponse.BodyHandlers.ofString());
        return new Resultado(response.statusCode(), response.body().isBlank() ? null : json.readTree(response.body()));
    }

    Conta conta(String senha) throws Exception {
        String email = UUID.randomUUID() + "@demo.invalid";
        var criada = call("POST", "/usuarios",
            Map.of("nome", "DEMO Conta", "email", email, "senha", senha), null);
        assertEquals(200, criada.status);
        assertFalse(criada.body.has("senha"), "O cadastro não pode devolver o hash da senha.");
        var login = call("POST", "/usuarios/login", Map.of("email", email, "senha", senha), null);
        assertEquals(200, login.status);
        return new Conta(login.body.get("id").asLong(), email, login.body.get("token").asString());
    }

    @Test
    void perfilRetornaDadosDoProprioUsuarioESemHash() throws Exception {
        var a = conta("Senha-atual-2026");
        var perfil = call("GET", "/usuarios/perfil", null, a.token);
        assertEquals(200, perfil.status);
        assertEquals(a.email, perfil.body.get("email").asString());
        assertEquals("DEMO Conta", perfil.body.get("nome").asString());
        assertFalse(perfil.body.has("senha"));
        assertEquals(401, call("GET", "/usuarios/perfil", null, null).status);
    }

    @Test
    void atualizaSomenteONomeEPersiste() throws Exception {
        var a = conta("Senha-atual-2026");
        var atualizado = call("PUT", "/usuarios/perfil", Map.of("nome", "  Nome Atualizado  "), a.token);
        assertEquals(200, atualizado.status);
        assertEquals("Nome Atualizado", atualizado.body.get("nome").asString());
        assertEquals("Nome Atualizado",
            call("GET", "/usuarios/perfil", null, a.token).body.get("nome").asString());

        assertEquals(400, call("PUT", "/usuarios/perfil", Map.of("nome", " "), a.token).status);
        assertEquals(400, call("PUT", "/usuarios/perfil", Map.of("nome", "A"), a.token).status);
    }

    @Test
    void naoPermiteEditarPerfilDeOutroUsuarioPeloParametro() throws Exception {
        var a = conta("Senha-atual-2026");
        var b = conta("Outra-senha-2026");
        assertEquals(403, call("PUT", "/usuarios/perfil?usuarioId=" + b.id,
            Map.of("nome", "Invasor"), a.token).status);
        assertEquals("DEMO Conta",
            call("GET", "/usuarios/perfil", null, b.token).body.get("nome").asString());
    }

    @Test
    void alterarSenhaValidaConfereAtualAplicaPoliticaERevogaSessoes() throws Exception {
        var a = conta("Senha-atual-2026");

        assertEquals(400, call("PUT", "/usuarios/senha",
            Map.of("senhaAtual", "errada", "novaSenha", "Nova-senha-2026"), a.token).status);
        assertEquals(400, call("PUT", "/usuarios/senha",
            Map.of("senhaAtual", "Senha-atual-2026", "novaSenha", "123"), a.token).status);
        assertEquals(400, call("PUT", "/usuarios/senha",
            Map.of("senhaAtual", "Senha-atual-2026", "novaSenha", "Senha-atual-2026"), a.token).status);

        assertEquals(204, call("PUT", "/usuarios/senha",
            Map.of("senhaAtual", "Senha-atual-2026", "novaSenha", "Nova-senha-2026"), a.token).status);

        // A sessão usada na troca é revogada, como todas as demais do usuário.
        assertEquals(401, call("GET", "/usuarios/perfil", null, a.token).status);
        assertEquals(401, call("POST", "/usuarios/login",
            Map.of("email", a.email, "senha", "Senha-atual-2026"), null).status);
        var novoLogin = call("POST", "/usuarios/login",
            Map.of("email", a.email, "senha", "Nova-senha-2026"), null);
        assertEquals(200, novoLogin.status);
        assertFalse(novoLogin.body.has("senha"));
        assertEquals(200, call("GET", "/usuarios/perfil", null,
            novoLogin.body.get("token").asString()).status);
    }

    @Test
    void trocaDeSenhaNaoRevogaSessaoDeOutroUsuario() throws Exception {
        var a = conta("Senha-atual-2026");
        var b = conta("Outra-senha-2026");
        assertEquals(204, call("PUT", "/usuarios/senha",
            Map.of("senhaAtual", "Senha-atual-2026", "novaSenha", "Nova-senha-2026"), a.token).status);
        assertEquals(200, call("GET", "/usuarios/perfil", null, b.token).status);

        var novoA = call("POST", "/usuarios/login",
            Map.of("email", a.email, "senha", "Nova-senha-2026"), null);
        assertEquals(200, novoA.status);
        assertEquals(403, call("PUT", "/usuarios/senha?usuarioId=" + b.id,
            Map.of("senhaAtual", "Outra-senha-2026", "novaSenha", "Outra-nova-2026"),
            novoA.body.get("token").asString()).status);
        assertEquals(200, call("POST", "/usuarios/login",
            Map.of("email", b.email, "senha", "Outra-senha-2026"), null).status);
    }

    @Test
    void alterarSenhaExigeAutenticacao() throws Exception {
        assertEquals(401, call("PUT", "/usuarios/senha",
            Map.of("senhaAtual", "x", "novaSenha", "Nova-senha-2026"), null).status);
    }

    @Test
    void excluirContaExigeSenhaERemoveAcesso() throws Exception {
        var a = conta("Senha-atual-2026");
        assertEquals(400, call("DELETE", "/usuarios/conta", Map.of("senha", "errada"), a.token).status);
        assertEquals(204, call("DELETE", "/usuarios/conta", Map.of("senha", "Senha-atual-2026"), a.token).status);
        assertEquals(401, call("GET", "/usuarios/perfil", null, a.token).status);
        assertEquals(401, call("POST", "/usuarios/login",
            Map.of("email", a.email, "senha", "Senha-atual-2026"), null).status);
        assertEquals(401, call("DELETE", "/usuarios/conta", Map.of("senha", "x"), a.token).status);
    }

    @Test
    void excluirContaNaoAfetaOutroUsuario() throws Exception {
        var a = conta("Senha-atual-2026");
        var b = conta("Senha-atual-2026");
        assertEquals(204, call("DELETE", "/usuarios/conta", Map.of("senha", "Senha-atual-2026"), b.token).status);
        assertEquals(200, call("GET", "/usuarios/perfil", null, a.token).status);
        assertEquals(200, call("POST", "/usuarios/login",
            Map.of("email", a.email, "senha", "Senha-atual-2026"), null).status);
    }
}

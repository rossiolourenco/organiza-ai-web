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
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "spring.datasource.url=jdbc:h2:mem:produtividade_tests;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
    "spring.jpa.show-sql=false"
})
@ActiveProfiles("local")
class ProdutividadeApiTests {
    @Value("${local.server.port}") int port;
    final HttpClient client = HttpClient.newHttpClient();
    final JsonMapper json = JsonMapper.builder().build();

    record Resultado(int status, JsonNode body) {}

    Resultado call(String method, String path, Object body, String token) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api" + path))
            .header("Content-Type", "application/json");
        if (token != null) builder.header("Authorization", "Bearer " + token);
        var response = client.send(builder.method(method, body == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body))).build(),
            HttpResponse.BodyHandlers.ofString());
        return new Resultado(response.statusCode(),
            response.body() == null || response.body().isBlank() ? null : json.readTree(response.body()));
    }

    record Conta(long id, String token) {}

    Conta conta() throws Exception {
        String email = UUID.randomUUID() + "@demo.invalid";
        assertEquals(200, call("POST", "/usuarios",
            Map.of("nome", "DEMO Prod", "email", email, "senha", "Senha-teste-2026"), null).status);
        var login = call("POST", "/usuarios/login", Map.of("email", email, "senha", "Senha-teste-2026"), null);
        return new Conta(login.body.get("id").asLong(), login.body.get("token").asString());
    }

    long tarefa(Conta conta) throws Exception {
        var criada = call("POST", "/tarefas?usuarioId=" + conta.id,
            Map.of("titulo", "Foco", "prioridade", "MEDIA", "status", "PENDENTE"), conta.token);
        return criada.body.get("id").asLong();
    }

    @Test
    void planejamentoComControleOtimistaEIsolamento() throws Exception {
        var a = conta();
        var b = conta();
        long tarefaA = tarefa(a);

        var salvar = call("PUT", "/produtividade/planejamento", Map.of(
            "data", "2026-11-20",
            "itens", List.of(Map.of("tarefaId", tarefaA, "ordem", 0, "minutosEstimados", 30)),
            "versao", 0), a.token);
        assertEquals(200, salvar.status);
        assertEquals(1L, salvar.body.get("versao").asLong());

        assertEquals(409, call("PUT", "/produtividade/planejamento", Map.of(
            "data", "2026-11-20",
            "itens", List.of(Map.of("tarefaId", tarefaA, "ordem", 0, "minutosEstimados", 40)),
            "versao", 0), a.token).status);

        var outro = call("GET", "/produtividade/planejamento?data=2026-11-20", null, b.token);
        assertEquals(0, outro.body.get("itens").size(), "plano de A não pode vazar para B");

        // Tarefa de outro usuário no plano do próprio usuário -> 400.
        var invalido = call("PUT", "/produtividade/planejamento", Map.of(
            "data", "2026-11-21",
            "itens", List.of(Map.of("tarefaId", 999999L, "ordem", 0, "minutosEstimados", 30)),
            "versao", 0), a.token);
        assertEquals(400, invalido.status);
    }

    @Test
    void focoRegistraEResume() throws Exception {
        var a = conta();
        long tarefaA = tarefa(a);

        var sessao = call("POST", "/produtividade/foco",
            Map.of("minutos", 25, "tarefaId", tarefaA, "data", "2026-11-20"), a.token);
        assertEquals(200, sessao.status);

        var resumo = call("GET", "/produtividade/foco/resumo?inicio=2026-11-20&fim=2026-11-20", null, a.token);
        assertEquals(25, resumo.body.get("minutos").asInt());
        assertEquals(1, resumo.body.get("sessoes").asInt());

        assertEquals(400, call("POST", "/produtividade/foco",
            Map.of("minutos", 0, "data", "2026-11-20"), a.token).status);
    }
}

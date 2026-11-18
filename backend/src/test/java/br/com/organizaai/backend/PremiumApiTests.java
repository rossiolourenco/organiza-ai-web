package br.com.organizaai.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.beans.factory.annotation.Value;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import java.net.*;
import java.net.http.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "spring.datasource.url=jdbc:h2:mem:premium_tests;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
    "spring.jpa.show-sql=false"
})
@ActiveProfiles("local")
class PremiumApiTests {
    @Value("${local.server.port}") int port;
    final HttpClient client = HttpClient.newHttpClient();
    final JsonMapper json = JsonMapper.builder().build();
    record Account(long id, String token) {}
    record Result(int status, JsonNode body) {}

    Result call(String method, String path, Object body, String token) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api" + path))
            .header("Content-Type", "application/json");
        if (token != null) builder.header("Authorization", "Bearer " + token);
        var response = client.send(builder.method(method, body == null ? HttpRequest.BodyPublishers.noBody() :
            HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body))).build(),
            HttpResponse.BodyHandlers.ofString());
        return new Result(response.statusCode(), response.body().isBlank() ? null : json.readTree(response.body()));
    }

    Account account() throws Exception {
        String email = UUID.randomUUID() + "@demo.invalid";
        assertEquals(200, call("POST", "/usuarios",
            Map.of("nome", "DEMO Premium", "email", email, "senha", "test-password"), null).status);
        var login = call("POST", "/usuarios/login", Map.of("email", email, "senha", "test-password"), null);
        assertEquals(200, login.status);
        return new Account(login.body.get("id").asLong(), login.body.get("token").asString());
    }

    Map<String, Object> task() {
        Map<String, Object> data = new HashMap<>();
        data.put("titulo", "DEMO Tarefa premium");
        data.put("prioridade", "ALTA");
        data.put("status", "PENDENTE");
        data.put("dataPrazo", "2026-11-20");
        data.put("categoria", null);
        return data;
    }

    String path(Account a) { return "/tarefas?usuarioId=" + a.id; }
    String path(long id, Account a) { return "/tarefas/" + id + "?usuarioId=" + a.id; }

    @Test void trashRestoresAndKeepsTasksRecoverable() throws Exception {
        var a = account();
        long id = call("POST", path(a), task(), a.token).body.get("id").asLong();
        assertEquals(204, call("DELETE", path(id, a), null, a.token).status);
        assertEquals(0, call("GET", path(a), null, a.token).body.size());
        assertEquals(404, call("GET", path(id, a), null, a.token).status);
        var trash = call("GET", "/tarefas/lixeira?usuarioId=" + a.id, null, a.token).body;
        assertEquals(1, trash.size());
        assertEquals(id, trash.get(0).get("id").asLong());
        assertEquals(200, call("POST", "/tarefas/" + id + "/restaurar?usuarioId=" + a.id, null, a.token).status);
        assertEquals(1, call("GET", path(a), null, a.token).body.size());
        assertEquals(0, call("GET", "/tarefas/lixeira?usuarioId=" + a.id, null, a.token).body.size());
        assertEquals(204, call("DELETE", path(id, a), null, a.token).status);
        assertEquals(204, call("DELETE", "/tarefas/" + id + "/definitivo?usuarioId=" + a.id, null, a.token).status);
        assertEquals(0, call("GET", "/tarefas/lixeira?usuarioId=" + a.id, null, a.token).body.size());
    }

    @Test void trashEndpointsRespectOwnership() throws Exception {
        var a = account();
        var b = account();
        long id = call("POST", path(a), task(), a.token).body.get("id").asLong();
        call("DELETE", path(id, a), null, a.token);
        assertEquals(404, call("POST", "/tarefas/" + id + "/restaurar?usuarioId=" + b.id, null, b.token).status);
        assertEquals(404, call("DELETE", "/tarefas/" + id + "/definitivo?usuarioId=" + b.id, null, b.token).status);
        assertEquals(403, call("GET", "/tarefas/lixeira?usuarioId=" + a.id, null, b.token).status);
        assertEquals(1, call("GET", "/tarefas/lixeira?usuarioId=" + a.id, null, a.token).body.size());
    }

    @Test void deletingAlreadyDeletedTaskIsNotFound() throws Exception {
        var a = account();
        long id = call("POST", path(a), task(), a.token).body.get("id").asLong();
        assertEquals(204, call("DELETE", path(id, a), null, a.token).status);
        assertEquals(404, call("DELETE", path(id, a), null, a.token).status);
        assertEquals(404, call("PUT", path(id, a), task(), a.token).status);
        assertEquals(404, call("PATCH", "/tarefas/" + id + "/concluir?usuarioId=" + a.id, null, a.token).status);
    }

    Map<String, Object> habit(String nome, String frequency, int goal) {
        return Map.of("nome", nome, "descricao", "Hábito de demonstração", "frequencia", frequency,
            "metaSemanal", goal, "cor", "#635BFF", "ativo", true);
    }

    @Test void habitsAreIdempotentPerDateAndPersistRealRecords() throws Exception {
        var a = account();
        String base = "/habitos?usuarioId=" + a.id;
        long id = call("POST", base, habit("DEMO Ler", "DIARIA", 7), a.token).body.get("id").asLong();

        var first = call("POST", "/habitos/" + id + "/registros?usuarioId=" + a.id,
            Map.of("data", "2026-11-17"), a.token);
        assertEquals(200, first.status);
        assertEquals(1, first.body.get("registros").size());

        var repeated = call("POST", "/habitos/" + id + "/registros?usuarioId=" + a.id,
            Map.of("data", "2026-11-17"), a.token);
        assertEquals(200, repeated.status);
        assertEquals(1, repeated.body.get("registros").size());

        call("POST", "/habitos/" + id + "/registros?usuarioId=" + a.id, Map.of("data", "2026-11-18"), a.token);
        var removed = call("DELETE", "/habitos/" + id + "/registros?usuarioId=" + a.id + "&data=2026-11-18", null, a.token);
        assertEquals(200, removed.status);
        assertEquals(1, removed.body.get("registros").size());

        var list = call("GET", base, null, a.token).body;
        assertEquals(1, list.size());
        assertEquals("DEMO Ler", list.get(0).get("nome").asString());
        assertEquals("2026-11-17", list.get(0).get("registros").get(0).get("data").asString());
        assertFalse(list.get(0).has("usuario"));
    }

    @Test void habitsValidateFrequencyGoalColorAndDates() throws Exception {
        var a = account();
        String base = "/habitos?usuarioId=" + a.id;
        assertEquals(400, call("POST", base, habit(" ", "DIARIA", 7), a.token).status);
        assertEquals(400, call("POST", base, habit("DEMO", "MENSAL", 7), a.token).status);
        assertEquals(400, call("POST", base, habit("DEMO", "DIARIA", 9), a.token).status);
        var badColor = new HashMap<>(habit("DEMO", "DIARIA", 3));
        badColor.put("cor", "azul");
        assertEquals(400, call("POST", base, badColor, a.token).status);
        long id = call("POST", base, habit("DEMO", "SEMANAL", 3), a.token).body.get("id").asLong();
        assertEquals(400, call("POST", "/habitos/" + id + "/registros?usuarioId=" + a.id,
            Map.of("data", "2026-02-30"), a.token).status);
    }

    @Test void habitsAreIsolatedBetweenUsers() throws Exception {
        var a = account();
        var b = account();
        long id = call("POST", "/habitos?usuarioId=" + a.id, habit("DEMO Privado", "DIARIA", 7), a.token)
            .body.get("id").asLong();
        assertEquals(0, call("GET", "/habitos?usuarioId=" + b.id, null, b.token).body.size());
        assertEquals(404, call("PUT", "/habitos/" + id + "?usuarioId=" + b.id,
            habit("Invasão", "DIARIA", 7), b.token).status);
        assertEquals(404, call("DELETE", "/habitos/" + id + "?usuarioId=" + b.id, null, b.token).status);
        assertEquals(404, call("POST", "/habitos/" + id + "/registros?usuarioId=" + b.id,
            Map.of("data", "2026-11-18"), b.token).status);
        assertEquals(403, call("GET", "/habitos?usuarioId=" + a.id, null, b.token).status);
        assertEquals(1, call("GET", "/habitos?usuarioId=" + a.id, null, a.token).body.size());
    }

    @Test void taskDetailsKeepEisenhowerAndEstimatedDuration() throws Exception {
        var a = account();
        var data = task();
        data.put("detalhes", Map.of("importante", true, "urgente", false, "duracaoEstimada", 45,
            "recorrencia", "NENHUMA", "fusoHorario", "America/Sao_Paulo"));
        var created = call("POST", path(a), data, a.token);
        assertEquals(200, created.status);
        assertTrue(created.body.get("detalhes").get("importante").asBoolean());
        assertFalse(created.body.get("detalhes").get("urgente").asBoolean());
        assertEquals(45, created.body.get("detalhes").get("duracaoEstimada").asInt());
        data.put("detalhes", Map.of("duracaoEstimada", 0));
        assertEquals(400, call("POST", path(a), data, a.token).status);
    }
}

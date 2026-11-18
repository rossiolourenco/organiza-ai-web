package br.com.organizaai.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import br.com.organizaai.backend.repository.SessaoRepository;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import java.net.*;
import java.net.http.*;
import java.time.Instant;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "spring.datasource.url=jdbc:h2:mem:mobile_tests;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
    "spring.jpa.show-sql=false"
})
@ActiveProfiles("local")
class MobileApiTests {
    @Value("${local.server.port}") int port;
    @Autowired SessaoRepository sessoes;
    final HttpClient client = HttpClient.newHttpClient();
    final JsonMapper json = JsonMapper.builder().build();
    record Account(long id, String token) {}
    record Result(int status, JsonNode body) {}
    Result call(String method, String path, Object body, String token) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api" + path))
            .header("Content-Type", "application/json");
        if (token != null) builder.header("Authorization", "Bearer " + token);
        var response = client.send(builder.method(method, body == null ? HttpRequest.BodyPublishers.noBody() :
            HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body))).build(), HttpResponse.BodyHandlers.ofString());
        return new Result(response.statusCode(), response.body().isBlank() ? null : json.readTree(response.body()));
    }
    Account account() throws Exception {
        String email = UUID.randomUUID() + "@demo.invalid";
        var result = call("POST", "/usuarios", Map.of("nome", "DEMO Teste", "email", email, "senha", "test-password"), null);
        assertEquals(200, result.status);
        assertFalse(result.body.has("senha"));
        result = call("POST", "/usuarios/login", Map.of("email", email, "senha", "test-password"), null);
        assertEquals(200, result.status);
        return new Account(result.body.get("id").asLong(), result.body.get("token").asString());
    }
    Map<String, Object> task() {
        Map<String, Object> data = new HashMap<>();
        data.put("titulo", "DEMO Entregar projeto"); data.put("descricao", "Descrição");
        data.put("prioridade", "ALTA"); data.put("status", "PENDENTE");
        data.put("dataPrazo", "2026-01-31"); data.put("categoria", null);
        return data;
    }
    String path(Account a) { return "/tarefas?usuarioId=" + a.id; }
    String path(long id, Account a) { return "/tarefas/" + id + "?usuarioId=" + a.id; }
    long create(Account a, Map<String, Object> input) throws Exception {
        var result = call("POST", path(a), input, a.token);
        assertEquals(200, result.status);
        return result.body.get("id").asLong();
    }
    Map<String, Object> details(String recurrence) {
        return Map.of("horario", "09:30", "notas", "Nota real", "etiquetas", List.of("estudo"),
            "favorita", true, "fixada", true, "ordem", 2,
            "subtarefas", List.of(Map.of("id", "etapa-1", "titulo", "Revisar", "concluida", true)),
            "recorrencia", recurrence, "fusoHorario", "America/Sao_Paulo");
    }
    @Test void crudAndKanban() throws Exception {
        var a = account();
        long id = create(a, task());
        var data = task(); data.put("titulo", "Editada"); data.put("status", "EM_ANDAMENTO");
        assertEquals("EM_ANDAMENTO", call("PUT", path(id, a), data, a.token).body.get("status").asString());
        var done = call("PATCH", "/tarefas/" + id + "/concluir?usuarioId=" + a.id, null, a.token);
        assertEquals("CONCLUIDA", done.body.get("status").asString());
        assertEquals(1, done.body.get("conclusoes").size());
        assertEquals(1, call("PATCH", "/tarefas/" + id + "/concluir?usuarioId=" + a.id, null, a.token).body.get("conclusoes").size());
        assertEquals(204, call("DELETE", path(id, a), null, a.token).status);
        assertEquals(404, call("GET", path(id, a), null, a.token).status);
    }
    @Test void categoryCrudPreservesTasks() throws Exception {
        var a = account();
        String categories = "/categorias?usuarioId=" + a.id;
        long category = call("POST", categories, Map.of("nome", "Estudos", "cor", "#635BFF"), a.token).body.get("id").asLong();
        var data = task(); data.put("categoria", Map.of("id", category));
        long id = create(a, data);
        String categoryPath = "/categorias/" + category + "?usuarioId=" + a.id;
        assertEquals(200, call("PUT", categoryPath, Map.of("nome", "Faculdade", "cor", "#16A34A"), a.token).status);
        assertEquals("Faculdade", call("GET", path(id, a), null, a.token).body.get("categoria").get("nome").asString());
        assertEquals(204, call("DELETE", categoryPath, null, a.token).status);
        var preserved = call("GET", path(id, a), null, a.token);
        assertEquals(200, preserved.status); assertTrue(preserved.body.get("categoria").isNull());
        assertEquals("DEMO Entregar projeto", preserved.body.get("titulo").asString());
    }
    @Test void cannotImpersonateOrAccessAnotherUser() throws Exception {
        var a = account(); var b = account();
        long id = create(a, task());
        assertEquals(401, call("GET", path(a), null, null).status);
        assertEquals(403, call("GET", path(a), null, b.token).status);
        assertEquals(404, call("GET", path(id, b), null, b.token).status);
        assertEquals(404, call("PUT", path(id, b), task(), b.token).status);
        assertEquals(404, call("DELETE", path(id, b), null, b.token).status);
        assertEquals(404, call("PATCH", "/tarefas/" + id + "/concluir?usuarioId=" + b.id, null, b.token).status);
        long category = call("POST", "/categorias?usuarioId=" + a.id, Map.of("nome", "Privada"), a.token).body.get("id").asLong();
        assertEquals(404, call("DELETE", "/categorias/" + category + "?usuarioId=" + b.id, null, b.token).status);
        var data = task(); data.put("categoria", Map.of("id", category));
        assertEquals(400, call("POST", path(b), data, b.token).status);
        assertEquals(1, call("GET", "/usuarios", null, b.token).body.size());
    }
    @Test void rejectsInvalidInputWithoutPartialUpdate() throws Exception {
        var a = account(); long id = create(a, task());
        var data = task(); data.put("titulo", ""); assertEquals(400, call("PUT", path(id, a), data, a.token).status);
        data = task(); data.put("status", "INVALIDO"); assertEquals(400, call("PUT", path(id, a), data, a.token).status);
        data = task(); data.put("dataPrazo", "2026-02-30"); assertEquals(400, call("PUT", path(id, a), data, a.token).status);
        data = task(); data.put("titulo", "Não persistir"); data.put("categoria", Map.of("id", 999999));
        assertEquals(400, call("PUT", path(id, a), data, a.token).status);
        assertEquals("DEMO Entregar projeto", call("GET", path(id, a), null, a.token).body.get("titulo").asString());
        assertEquals(400, call("POST", "/categorias?usuarioId=" + a.id, Map.of("nome", " ", "cor", "red"), a.token).status);
    }
    @Test void legacyWebUpdatePreservesDetailsAndChecklist() throws Exception {
        var a = account(); var data = task(); data.put("detalhes", details("NENHUMA"));
        long id = create(a, data);
        var oldWeb = task(); oldWeb.put("titulo", "Atualizada pelo Web");
        var updated = call("PUT", path(id, a), oldWeb, a.token);
        assertEquals(200, updated.status);
        assertTrue(updated.body.get("detalhes").get("favorita").asBoolean());
        assertTrue(updated.body.get("detalhes").get("subtarefas").get(0).get("concluida").asBoolean());
        assertEquals("Nota real", updated.body.get("detalhes").get("notas").asString());
    }
    @Test void recurrenceIsAtomicAndIdempotentOnRepeatedCompletion() throws Exception {
        var a = account(); var data = task(); data.put("detalhes", details("MENSAL"));
        long id = create(a, data);
        String conclude = "/tarefas/" + id + "/concluir?usuarioId=" + a.id;
        try (var pool = java.util.concurrent.Executors.newFixedThreadPool(2)) {
            var first = pool.submit(() -> call("PATCH", conclude, null, a.token));
            var second = pool.submit(() -> call("PATCH", conclude, null, a.token));
            assertEquals(200, first.get().status); assertEquals(200, second.get().status);
        }
        var all = call("GET", path(a), null, a.token).body;
        assertEquals(2, all.size());
        JsonNode next = all.get(0).get("id").asLong() == id ? all.get(1) : all.get(0);
        assertEquals("2026-02-28", next.get("dataPrazo").asString());
        assertFalse(next.get("detalhes").get("subtarefas").get(0).get("concluida").asBoolean());
        assertEquals(0, next.get("conclusoes").size());
        data.put("status", "PENDENTE"); call("PUT", path(id, a), data, a.token);
        call("PATCH", conclude, null, a.token);
        assertEquals(2, call("GET", path(a), null, a.token).body.size());
    }
    @Test void invalidRecurrenceAndSubtasksAreRejected() throws Exception {
        var a = account(); var data = task();
        var detail = new HashMap<>(details("DIARIA")); detail.put("fusoHorario", "inventado");
        data.put("detalhes", detail); assertEquals(400, call("POST", path(a), data, a.token).status);
        detail.put("fusoHorario", "UTC"); data.put("dataPrazo", null);
        assertEquals(400, call("POST", path(a), data, a.token).status);
        data.put("dataPrazo", "2026-01-01");
        detail.put("subtarefas", List.of(Map.of("id", "1", "titulo", "", "concluida", false)));
        assertEquals(400, call("POST", path(a), data, a.token).status);
    }
    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.CsvSource({"DIARIA,2026-02-01", "SEMANAL,2026-02-07"})
    void recurrenceUsesCivilDeadline(String recurrence, String expected) throws Exception {
        var a = account(); var data = task(); data.put("detalhes", details(recurrence));
        long id = create(a, data);
        call("PATCH", "/tarefas/" + id + "/concluir?usuarioId=" + a.id, null, a.token);
        var all = call("GET", path(a), null, a.token).body;
        assertEquals(2, all.size());
        JsonNode next = all.get(0).get("id").asLong() == id ? all.get(1) : all.get(0);
        assertEquals(expected, next.get("dataPrazo").asString());
    }
    @Test void expiredAndRevokedSessionsAreDenied() throws Exception {
        var a = account();
        assertEquals(204, call("POST", "/usuarios/logout", null, a.token).status);
        assertEquals(401, call("GET", path(a), null, a.token).status);
        var b = account();
        var session = sessoes.findAll().stream().filter(s -> s.usuarioId.equals(b.id)).findFirst().orElseThrow();
        session.expiraEm = Instant.now().minusSeconds(1); sessoes.save(session);
        assertEquals(401, call("GET", path(b), null, b.token).status);
    }
}

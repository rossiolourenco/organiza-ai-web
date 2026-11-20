package br.com.organizaai.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Homologação do backend contra PostgreSQL real (mesma major 17 usada em produção).
 *
 * <p>O container é inicializado com `database/schema.sql` e as migrations V002–V004, exatamente
 * como o runbook de produção. O Hibernate sobe com `ddl-auto=validate`, portanto qualquer
 * divergência entre entidade e schema real quebra o teste. H2 continua sendo usado nos testes
 * rápidos; este teste é a validação de paridade exigida para o release.</p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("postgres-it")
@Testcontainers(disabledWithoutDocker = true)
class PostgresIntegrationTests {

    private static final Path DATABASE = scriptDir();

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(
        DockerImageName.parse("postgres:17.7-alpine"))
        .withDatabaseName("organizaai")
        .withUsername("organizaai")
        .withPassword("organizaai")
        .withCopyFileToContainer(script("schema.sql"), "/docker-entrypoint-initdb.d/01-schema.sql")
        .withCopyFileToContainer(script("migrations/V002__mobile_produtividade.sql"),
            "/docker-entrypoint-initdb.d/02-v002.sql")
        .withCopyFileToContainer(script("migrations/V003__premium_produtividade.sql"),
            "/docker-entrypoint-initdb.d/03-v003.sql")
        .withCopyFileToContainer(script("migrations/V004__admin_rbac_seguranca_firewall.sql"),
            "/docker-entrypoint-initdb.d/04-v004.sql")
        .withCopyFileToContainer(script("migrations/V005__planejamento_foco.sql"),
            "/docker-entrypoint-initdb.d/05-v005.sql");

    @Value("${local.server.port}")
    int port;

    final HttpClient client = HttpClient.newHttpClient();
    final JsonMapper json = JsonMapper.builder().build();

    @org.springframework.beans.factory.annotation.Autowired
    JdbcTemplate jdbc;

    record Resultado(int status, JsonNode body, HttpResponse<String> response) {
        boolean temCampo(String campo) {
            return body != null && body.has(campo);
        }
    }

    record Conta(long id, String email, String token) {}

    Resultado call(String method, String path, Object body, String token) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api" + path))
            .header("Content-Type", "application/json");
        if (token != null) builder.header("Authorization", "Bearer " + token);
        var response = client.send(builder.method(method, body == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body))).build(),
            HttpResponse.BodyHandlers.ofString());
        return new Resultado(response.statusCode(),
            response.body() == null || response.body().isBlank() ? null : json.readTree(response.body()),
            response);
    }

    Conta conta(String senha) throws Exception {
        String email = UUID.randomUUID() + "@postgres.invalid";
        var criada = call("POST", "/usuarios",
            Map.of("nome", "DEMO PostgreSQL", "email", email, "senha", senha), null);
        assertEquals(200, criada.status, "cadastro deve funcionar no PostgreSQL real");
        var login = call("POST", "/usuarios/login", Map.of("email", email, "senha", senha), null);
        assertEquals(200, login.status);
        return new Conta(login.body.get("id").asLong(), email, login.body.get("token").asString());
    }

    @Test
    void cadastroLoginPerfilETrocaDeSenhaNoPostgreSQL() throws Exception {
        String email = UUID.randomUUID() + "@postgres.invalid";
        var criada = call("POST", "/usuarios",
            Map.of("nome", "DEMO PostgreSQL", "email", email, "senha", "Senha-real-2026"), null);
        assertEquals(200, criada.status);
        // DTO na borda: nada de hash nem metadados internos de segurança.
        assertFalse(criada.temCampo("senha"), "cadastro não pode devolver hash de senha");
        assertFalse(criada.temCampo("bloqueadoAte"), "cadastro não pode expor bloqueio");
        assertFalse(criada.temCampo("motivoBloqueio"), "cadastro não pode expor motivo de bloqueio");
        assertFalse(criada.temCampo("ultimoLoginEm"), "cadastro não pode expor último login");
        assertEquals("USER", criada.body.get("papel").asString());

        assertEquals(401, call("GET", "/usuarios", null, null).status, "listagem exige sessão");

        var login = call("POST", "/usuarios/login", Map.of("email", email, "senha", "Senha-real-2026"), null);
        assertEquals(200, login.status);
        String token = login.body.get("token").asString();

        var listagem = call("GET", "/usuarios", null, token);
        assertEquals(200, listagem.status);
        assertEquals(1, listagem.body.size(), "só o próprio usuário é listado");
        assertFalse(listagem.temCampo("senha"));
        assertEquals(email, listagem.body.get(0).get("email").asString());

        var perfil = call("GET", "/usuarios/perfil", null, token);
        assertEquals(200, perfil.status);
        assertFalse(perfil.temCampo("senha"));

        var atualizado = call("PUT", "/usuarios/perfil", Map.of("nome", "Nome PostgreSQL"), token);
        assertEquals(200, atualizado.status);
        assertEquals("Nome PostgreSQL", atualizado.body.get("nome").asString());

        assertEquals(204, call("PUT", "/usuarios/senha",
            Map.of("senhaAtual", "Senha-real-2026", "novaSenha", "Outra-senha-2026"), token).status);
        assertEquals(401, call("GET", "/usuarios/perfil", null, token).status,
            "troca de senha revoga todas as sessões");
        assertEquals(401, call("POST", "/usuarios/login",
            Map.of("email", email, "senha", "Senha-real-2026"), null).status);
        assertEquals(200, call("POST", "/usuarios/login",
            Map.of("email", email, "senha", "Outra-senha-2026"), null).status);
    }

    @Test
    void tarefasCategoriasEIsolamentoEntreUsuarios() throws Exception {
        var a = conta("Senha-teste-2026");
        var b = conta("Senha-teste-2026");

        assertEquals(401, call("GET", "/tarefas?usuarioId=" + a.id, null, null).status);
        assertEquals(403, call("GET", "/tarefas?usuarioId=" + a.id, null, b.token).status,
            "não é permitido consultar dados de outro usuário");
        assertEquals(403, call("GET", "/categorias?usuarioId=" + a.id, null, b.token).status);

        var categoria = call("POST", "/categorias?usuarioId=" + a.id,
            Map.of("nome", "Trabalho", "cor", "#4F46E5"), a.token);
        assertEquals(200, categoria.status);
        long categoriaId = categoria.body.get("id").asLong();

        var invalida = call("POST", "/tarefas?usuarioId=" + a.id,
            Map.of("titulo", "Inválida", "prioridade", "URGENTE", "status", "PENDENTE"), a.token);
        assertEquals(400, invalida.status, "prioridade fora do domínio deve falhar");

        var tarefa = call("POST", "/tarefas?usuarioId=" + a.id,
            Map.of("titulo", "Entregar RC", "descricao", "Validar PostgreSQL",
                "prioridade", "ALTA", "status", "PENDENTE",
                "categoria", Map.of("id", categoriaId)), a.token);
        assertEquals(200, tarefa.status);
        long tarefaId = tarefa.body.get("id").asLong();

        assertEquals(200, call("GET", "/tarefas?usuarioId=" + a.id, null, a.token).status);
        assertEquals(1, call("GET", "/tarefas?usuarioId=" + a.id, null, a.token).body.size());
        assertEquals("ALTA",
            call("GET", "/tarefas/prioridade/ALTA?usuarioId=" + a.id, null, a.token).body.get(0)
                .get("prioridade").asString());

        // Outro usuário não acessa nem apaga recurso alheio.
        assertEquals(404, call("GET", "/tarefas/" + tarefaId + "?usuarioId=" + b.id, null, b.token).status);
        assertEquals(404, call("DELETE", "/tarefas/" + tarefaId + "?usuarioId=" + b.id, null, b.token).status);

        assertEquals(200, call("PATCH", "/tarefas/" + tarefaId + "/concluir?usuarioId=" + a.id,
            null, a.token).status);
        assertEquals(204, call("DELETE", "/tarefas/" + tarefaId + "?usuarioId=" + a.id, null, a.token).status);
        assertEquals(1, call("GET", "/tarefas/lixeira?usuarioId=" + a.id, null, a.token).body.size());
        assertEquals(0, call("GET", "/tarefas?usuarioId=" + a.id, null, a.token).body.size());
        assertEquals(200, call("POST", "/tarefas/" + tarefaId + "/restaurar?usuarioId=" + a.id,
            null, a.token).status);
        assertEquals(1, call("GET", "/tarefas?usuarioId=" + a.id, null, a.token).body.size());
        assertEquals(204, call("DELETE", "/tarefas/" + tarefaId + "?usuarioId=" + a.id, null, a.token).status);
        assertEquals(204, call("DELETE", "/tarefas/" + tarefaId + "/definitivo?usuarioId=" + a.id,
            null, a.token).status);
        assertEquals(0, call("GET", "/tarefas/lixeira?usuarioId=" + a.id, null, a.token).body.size());
    }

    @Test
    void cabeccosDeSegurancaEAcessoAdministrativo() throws Exception {
        var usuario = conta("Senha-teste-2026");

        var perfil = call("GET", "/usuarios/perfil", null, usuario.token);
        assertEquals("nosniff", perfil.response.headers().firstValue("X-Content-Type-Options").orElse(null));
        assertEquals("DENY", perfil.response.headers().firstValue("X-Frame-Options").orElse(null));
        assertEquals("no-store", perfil.response.headers().firstValue("Cache-Control").orElse(null));
        assertTrue(perfil.response.headers().firstValue("Content-Security-Policy").isPresent());

        assertEquals(401, call("GET", "/admin/usuarios", null, null).status);
        assertEquals(403, call("GET", "/admin/usuarios", null, usuario.token).status,
            "USER não acessa console administrativo");
    }

    @Test
    void migrationsSaoReproduziveisEIntegridadeReferencialNoPostgreSQL() {
        assertEquals(1, jdbc.queryForObject("SELECT count(*) FROM politicas_seguranca WHERE id = 1",
            Integer.class), "V004 deve criar a política singleton");
        assertEquals(1, jdbc.queryForObject("SELECT count(*) FROM firewall_estado WHERE id = 1",
            Integer.class), "V004 deve criar o estado de firewall singleton");

        for (String tabela : List.of("usuarios", "categorias", "tarefas", "sessoes", "tarefa_conclusoes",
            "habitos", "habito_registros", "tentativas_login", "auditoria_admin", "firewall_regras",
            "firewall_historico", "firewall_snapshots", "firewall_estado", "politicas_seguranca",
            "planejamentos", "sessoes_foco")) {
            assertEquals(1, jdbc.queryForObject(
                "SELECT count(*) FROM information_schema.tables WHERE table_schema = 'public' AND table_name = ?",
                Integer.class, tabela), "tabela ausente após migrations: " + tabela);
        }

        String email = UUID.randomUUID() + "@postgres.invalid";
        jdbc.update("INSERT INTO usuarios(nome,email,senha) VALUES (?,?,?)", "DEMO FK", email, "hash");
        Long usuarioId = jdbc.queryForObject("SELECT id FROM usuarios WHERE email = ?", Long.class, email);
        jdbc.update("INSERT INTO categorias(nome,usuario_id) VALUES ('FK',?)", usuarioId);
        Long categoriaId = jdbc.queryForObject("SELECT id FROM categorias WHERE usuario_id = ?",
            Long.class, usuarioId);
        jdbc.update("INSERT INTO tarefas(titulo,prioridade,usuario_id,categoria_id) VALUES ('FK','MEDIA',?,?)",
            usuarioId, categoriaId);
        jdbc.update("INSERT INTO habitos(nome,usuario_id,frequencia,meta_semanal) VALUES ('FK',?,'DIARIA',7)",
            usuarioId);

        // ON DELETE SET NULL preserva a tarefa ao remover a categoria.
        jdbc.update("DELETE FROM categorias WHERE id = ?", categoriaId);
        assertEquals(1, jdbc.queryForObject(
            "SELECT count(*) FROM tarefas WHERE usuario_id = ? AND categoria_id IS NULL",
            Integer.class, usuarioId));

        // ON DELETE CASCADE remove tarefas e hábitos quando o usuário é removido.
        jdbc.update("DELETE FROM usuarios WHERE id = ?", usuarioId);
        assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM tarefas WHERE usuario_id = ?",
            Integer.class, usuarioId));
        assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM habitos WHERE usuario_id = ?",
            Integer.class, usuarioId));
    }

    @Test
    void planejamentoEFocoPersistemEntreRequisicoes() throws Exception {
        var a = conta("Senha-teste-2026");

        var tarefa = call("POST", "/tarefas?usuarioId=" + a.id,
            Map.of("titulo", "Foco persistente", "prioridade", "MEDIA", "status", "PENDENTE",
                "dataPrazo", "2026-11-21"), a.token);
        long tarefaId = tarefa.body.get("id").asLong();

        // Planejamento: criar e reler.
        var salvar = call("PUT", "/produtividade/planejamento", Map.of(
            "data", "2026-11-20",
            "itens", List.of(Map.of("tarefaId", tarefaId, "ordem", 0, "minutosEstimados", 30)),
            "versao", 0), a.token);
        assertEquals(200, salvar.status);
        assertEquals(1L, salvar.body.get("versao").asLong());

        var ler = call("GET", "/produtividade/planejamento?data=2026-11-20", null, a.token);
        assertEquals(200, ler.status);
        assertEquals(1, ler.body.get("itens").size());
        assertEquals(tarefaId, ler.body.get("itens").get(0).get("tarefaId").asLong());

        // Controle otimista: versão antiga gera 409.
        var conflito = call("PUT", "/produtividade/planejamento", Map.of(
            "data", "2026-11-20",
            "itens", List.of(Map.of("tarefaId", tarefaId, "ordem", 0, "minutosEstimados", 45)),
            "versao", 0), a.token);
        assertEquals(409, conflito.status);

        // Atualização com a versão correta.
        var atualizar = call("PUT", "/produtividade/planejamento", Map.of(
            "data", "2026-11-20",
            "itens", List.of(Map.of("tarefaId", tarefaId, "ordem", 0, "minutosEstimados", 45)),
            "versao", 1), a.token);
        assertEquals(200, atualizar.status);
        assertEquals(2L, atualizar.body.get("versao").asLong());

        // Foco: registrar e resumir.
        var sessao = call("POST", "/produtividade/foco",
            Map.of("minutos", 25, "tarefaId", tarefaId, "data", "2026-11-20"), a.token);
        assertEquals(200, sessao.status);
        assertEquals(25, sessao.body.get("minutos").asInt());

        var listaFoco = call("GET", "/produtividade/foco?data=2026-11-20", null, a.token);
        assertEquals(200, listaFoco.status);
        assertEquals(1, listaFoco.body.size());

        var resumo = call("GET", "/produtividade/foco/resumo?inicio=2026-11-16&fim=2026-11-22", null, a.token);
        assertEquals(200, resumo.status);
        assertEquals(25, resumo.body.get("minutos").asInt());
        assertEquals(1, resumo.body.get("sessoes").asInt());

        // Isolamento: outro usuário não vê o plano nem o foco do primeiro.
        var b = conta("Senha-teste-2026");
        var planoB = call("GET", "/produtividade/planejamento?data=2026-11-20", null, b.token);
        assertEquals(200, planoB.status);
        assertEquals(0, planoB.body.get("itens").size());
        var focoB = call("GET", "/produtividade/foco?data=2026-11-20", null, b.token);
        assertEquals(0, focoB.body.size());

        // IDOR: registrar foco apontando tarefa de outro usuário deve falhar.
        var idor = call("POST", "/produtividade/foco",
            Map.of("minutos", 25, "tarefaId", tarefaId, "data", "2026-11-20"), b.token);
        assertEquals(400, idor.status);
    }

    private static MountableFile script(String relative) {
        return MountableFile.forHostPath(DATABASE.resolve(relative).toAbsolutePath());
    }

    /** Resolve ../database tanto rodando de `backend/` quanto da raiz do repositório. */
    private static Path scriptDir() {
        Path backendLocal = Path.of("..", "database").normalize();
        if (Files.isDirectory(backendLocal)) return backendLocal;
        Path rootLocal = Path.of("database");
        if (Files.isDirectory(rootLocal)) return rootLocal;
        throw new IllegalStateException("diretório database/ não encontrado a partir de " +
            Path.of("").toAbsolutePath());
    }
}

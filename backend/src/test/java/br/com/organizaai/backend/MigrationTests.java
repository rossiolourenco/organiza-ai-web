package br.com.organizaai.backend;

import org.junit.jupiter.api.Test;
import java.nio.file.*;
import java.sql.*;
import static org.junit.jupiter.api.Assertions.*;

class MigrationTests {
    @Test void additiveMigrationPreservesExistingRowsAndCategoryDeletion() throws Exception {
        try (Connection connection = DriverManager.getConnection(
                "jdbc:h2:mem:migration_test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE", "sa", "");
             Statement statement = connection.createStatement()) {
            statement.execute(Files.readString(Path.of("../database/schema.sql")));
            statement.execute("INSERT INTO usuarios(nome,email,senha) VALUES ('DEMO','demo@test.invalid','hash-test')");
            statement.execute("INSERT INTO categorias(nome,usuario_id) VALUES ('DEMO',1)");
            statement.execute("INSERT INTO tarefas(titulo,prioridade,usuario_id,categoria_id) VALUES ('DEMO preservada','MEDIA',1,1)");
            statement.execute(Files.readString(Path.of("../database/migrations/V002__mobile_produtividade.sql")));
            statement.execute(Files.readString(Path.of("../database/migrations/V003__premium_produtividade.sql")));
            statement.execute(Files.readString(Path.of("../database/migrations/V004__admin_rbac_seguranca_firewall.sql")));
            statement.execute(Files.readString(Path.of("../database/migrations/V005__planejamento_foco.sql")));
            statement.execute("INSERT INTO habitos(nome,usuario_id,frequencia,meta_semanal) VALUES ('DEMO Hábito',1,'DIARIA',7)");
            statement.execute("INSERT INTO habito_registros(habito_id,data) VALUES (1,'2026-11-17')");
            try (ResultSet rows = statement.executeQuery(
                    "SELECT h.nome, r.data, t.excluida_em FROM habitos h " +
                    "JOIN habito_registros r ON r.habito_id = h.id, tarefas t")) {
                assertTrue(rows.next());
                assertEquals("DEMO Hábito", rows.getString(1));
                assertEquals("2026-11-17", rows.getObject(2).toString());
                assertNull(rows.getObject(3));
            }
            try (ResultSet rows = statement.executeQuery(
                    "SELECT papel, status, deve_trocar_senha FROM usuarios WHERE id=1")) {
                assertTrue(rows.next());
                assertEquals("USER", rows.getString(1));
                assertEquals("ATIVO", rows.getString(2));
                assertFalse(rows.getBoolean(3));
            }
            statement.execute("INSERT INTO firewall_regras(nome,ip_cidr,porta,protocolo,direcao,acao) " +
                "VALUES ('DEMO SSH','10.0.0.0/8',22,'TCP','INBOUND','ALLOW')");
            statement.execute("INSERT INTO auditoria_admin(acao,resultado) VALUES ('DEMO_TESTE','SUCESSO')");
            statement.execute("INSERT INTO planejamentos(usuario_id,data,itens) VALUES (1,'2026-11-20','[{\"tarefaId\":1,\"ordem\":0,\"minutosEstimados\":30}]')");
            statement.execute("INSERT INTO sessoes_foco(usuario_id,tarefa_id,minutos,data) VALUES (1,1,25,'2026-11-20')");
            try (ResultSet rows = statement.executeQuery(
                    "SELECT f.nome, a.acao, p.senha_minima, pl.itens, sf.minutos " +
                    "FROM firewall_regras f, auditoria_admin a, politicas_seguranca p, planejamentos pl, sessoes_foco sf")) {
                assertTrue(rows.next());
                assertEquals("DEMO SSH", rows.getString(1));
                assertEquals("DEMO_TESTE", rows.getString(2));
                assertEquals(8, rows.getInt(3));
                assertTrue(rows.getString(4).contains("tarefaId"));
                assertEquals(25, rows.getInt(5));
            }
            statement.execute("DELETE FROM categorias WHERE id=1");
            try (ResultSet rows = statement.executeQuery("SELECT titulo,categoria_id,detalhes,proxima_gerada FROM tarefas")) {
                assertTrue(rows.next());
                assertEquals("DEMO preservada", rows.getString(1));
                assertNull(rows.getObject(2));
                assertNull(rows.getObject(3));
                assertFalse(rows.getBoolean(4));
            }
        }
    }
}

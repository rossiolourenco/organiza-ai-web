# Database Migration Plan — Organiza Aí 3.3 RC

- **SGBD:** PostgreSQL 17.12 (produção), 17.7 (container de homologação)
- **Banco:** `organiza_ai` · **Usuário da aplicação:** `organiza_ai` · **Escuta:** `127.0.0.1:5432`
- **Ferramenta:** SQL versionado manual (`database/migrations/`), sem Flyway/Liquibase
- **Modo do Hibernate em produção:** `spring.jpa.hibernate.ddl-auto=validate`

## 1. Inventário

| Arquivo | Conteúdo | Aditivo | Já aplicado em produção |
|---|---|---|---|
| `database/schema.sql` | Estrutura base (`usuarios`, `categorias`, `tarefas`) | — | Sim (versão 1.x/3.0) |
| `V002__mobile_produtividade.sql` | `sessoes`, `tarefa_conclusoes`, `tarefas.detalhes`, `tarefas.proxima_gerada`, índices | Sim | Sim (3.0) |
| `V003__premium_produtividade.sql` | `tarefas.excluida_em`, `habitos`, `habito_registros`, índices | Sim (`IF NOT EXISTS`) | Sim (3.1/3.2) |
| `V004__admin_rbac_seguranca_firewall.sql` | RBAC, status, bloqueio, auditoria, tentativas, política, firewall DEMO, monitoramento | Sim (`IF NOT EXISTS`) | Sim (3.2) |

**O 3.3 RC não introduz nenhuma migration nova.** Nenhum `ALTER`, `DROP`, `DELETE` ou backfill
é executado nesta release. A release é compatível com o schema já existente em produção.

## 2. O que foi validado

O teste `PostgresIntegrationTests` (Testcontainers) executa, em PostgreSQL 17 real e isolado:

1. `schema.sql`;
2. `V002`, `V003`, `V004` na ordem correta;
3. o backend com `ddl-auto=validate` — qualquer divergência entre entidade e schema real
   impede a aplicação de subir e quebra o teste;
4. criação de usuário, login, perfil, troca de senha, tarefas, categorias, lixeira e
   restauração;
5. integridade referencial: `ON DELETE SET NULL` em `tarefas.categoria_id` e
   `ON DELETE CASCADE` em tarefas/hábitos ao remover o usuário;
6. presença das 14 tabelas e dos singletons `politicas_seguranca(1)` e `firewall_estado(1)`.

Resultado: **4/4 testes verdes, 0 ignorados** (`web-postgres` no CI).

### Reproductibilidade

- Os scripts usam `IF NOT EXISTS` em V003/V004; V002 é aplicada uma única vez.
- A ordem é fixa (`V002` → `V003` → `V004`) e validada pelo teste.
- O histórico de produção já contém todas as migrations; o RC não reaplica nada.
- O teste roda a partir das **mesmas** versões de arquivo versionadas no Git (não há cópia
  paralela do SQL).

## 3. Procedimento pré-deploy (Prompt 2)

Executar na VM, com janela de manutenção e **antes** de trocar o `current`:

```bash
# 1. Backup obrigatório (padrão já usado pela VM)
systemctl start organiza-ai-backup.service
ls -lh /var/backups/organiza-ai/ | tail -3
pg_restore -l /var/backups/organiza-ai/<dump-mais-recente>.dump >/dev/null && echo "dump íntegro"

# 2. Backup extra identificado pelo release
sudo -u postgres pg_dump -Fc organiza_ai \
  -f /var/backups/organiza-ai/organiza_ai-pre-3.3-$(date +%Y%m%d-%H%M).dump

# 3. Fotografia do schema para comparação
sudo -u postgres pg_dump --schema-only organiza_ai \
  > /var/backups/organiza-ai/schema-pre-3.3.sql
```

## 4. Execução

**Nenhuma migration é necessária para o 3.3 RC.**

Se, e somente se, o `validate` do backend apontar divergência em produção (não esperado, pois a
3.2 já roda com o mesmo schema):

1. Parar o backend.
2. Aplicar o script faltante **como o usuário da aplicação** (nunca como `postgres`, para não
   repetir o incidente de owner documentado em `DEPLOY-VM-INSTALLATION.md`):

```bash
PGPASSWORD="<segredo>" psql -h 127.0.0.1 -U organiza_ai -d organiza_ai \
  -v ON_ERROR_STOP=1 -f database/migrations/<script>.sql
```

3. Conferir owner e permissões:

```sql
SELECT tablename, tableowner FROM pg_tables WHERE schemaname = 'public' ORDER BY tablename;
```

Todos os objetos devem pertencer a `organiza_ai`.

## 5. Verificação pós-migration

```sql
-- 14 tabelas esperadas
SELECT count(*) FROM information_schema.tables
WHERE table_schema = 'public'
  AND table_name IN ('usuarios','categorias','tarefas','sessoes','tarefa_conclusoes',
                     'habitos','habito_registros','tentativas_login','auditoria_admin',
                     'firewall_regras','firewall_historico','firewall_snapshots',
                     'firewall_estado','politicas_seguranca');

-- singletons
SELECT count(*) FROM politicas_seguranca WHERE id = 1;
SELECT count(*) FROM firewall_estado WHERE id = 1;

-- nenhuma coluna obrigatória nula inesperada
SELECT count(*) FROM usuarios WHERE papel IS NULL OR status IS NULL;
```

Depois, subir o backend e confirmar que `ddl-auto=validate` não falha.

## 6. Rollback de banco

- **Não existe rollback destrutivo** para V002–V004: as colunas são aditivas e os dados novos
  (sessões, hábitos, lixeira, auditoria) devem ser preservados.
- Rollback de **aplicação** mantém o schema atual (ver `ROLLBACK-PLAN.md`).
- Restauração de dump só em caso de corrupção comprovada, com autorização explícita:

```bash
systemctl stop organiza-ai-backend
sudo -u postgres pg_restore -d organiza_ai --clean --if-exists <dump>
systemctl start organiza-ai-backend
```

`--clean` remove objetos existentes; é uma operação destrutiva e exige backup do estado atual.

## 7. Riscos e mitigações

| Risco | Mitigação |
|---|---|
| Divergência entidade × schema | `ddl-auto=validate` + teste PostgreSQL real no CI |
| Owner incorreto após migration manual | Aplicar como `organiza_ai` e conferir `pg_tables` |
| Migration aplicada duas vezes | V003/V004 idempotentes; V002 aplicada uma única vez e já presente |
| Perda de dados em rollback | Schema aditivo, backup obrigatório e restauração só com autorização |
| Ausência de Flyway | Dívida registrada (P2): adotar Flyway ou validar migrations automaticamente no deploy |

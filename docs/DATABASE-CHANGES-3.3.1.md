# Alterações de banco — 3.3.1

## 1. Resultado

Uma migration aditiva `V005__planejamento_foco.sql` cria duas tabelas para centralizar
planejamento diário e sessões de foco. Nenhuma coluna/índice existente foi removido, nenhum DDL
destrutivo e nenhum backfill.

## 2. Migration V005

- `planejamentos(id, usuario_id, data, itens TEXT, versao, atualizado_em)` com
  `UNIQUE (usuario_id, data)` — plano diário com controle otimista por versão.
- `sessoes_foco(id, usuario_id, tarefa_id FK SET NULL, minutos, data, iniciada_em)` — sessões de foco
  concluídas.
- Índices em `(usuario_id, data)`.
- **Aditiva e idempotente** (`IF NOT EXISTS`); aplicada apenas em ensaio via Testcontainers, nunca em produção.

## 3. Compatibilidade

- Backend `3.3.1-RC2` continua com `ddl-auto=validate`.
- Sem remoção de coluna/índice/constraint. A migration é cumulativa após V002–V004.
- Banco de produção não foi tocado nesta rodada.

## 4. Migrations

`schema.sql`, `V002`, `V003`, `V004` (inalteradas) e **`V005` (nova)**.

## 5. Rollback de banco

As tabelas são aditivas e podem ser mantidas em caso de rollback de aplicação. Ver `ROLLBACK-3.3.1.md`.

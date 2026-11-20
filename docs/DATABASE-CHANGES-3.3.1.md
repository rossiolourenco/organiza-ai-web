# Alterações de banco — 3.3.1

## 1. Resultado

**Nenhuma alteração de schema.** Nenhuma migration nova, nenhum DDL, nenhum backfill.

## 2. Justificativa

Todas as funcionalidades adicionadas à Web consomem endpoints já existentes:

- tarefas/lixeira/restauração → `tarefas` (V002/V003);
- categorias → `categorias`;
- hábitos/registros → `habitos`/`habito_registros` (V003);
- perfil/senha → `usuarios`/`sessoes` (V004).

Planejamento e foco são preferências do dispositivo e não exigem persistência central.

## 3. Compatibilidade

- Backend `3.3.1-RC1` continua com `ddl-auto=validate` sobre o schema da 3.3.0.
- Nenhuma coluna, índice ou constraint foi removido.
- Banco de produção não foi tocado nesta rodada.

## 4. Migrations existentes (inalteradas)

`V002__mobile_produtividade.sql`, `V003__premium_produtividade.sql`,
`V004__admin_rbac_seguranca_firewall.sql` e `schema.sql`.

## 5. Rollback de banco

Não aplicável: não houve alteração. Ver `ROLLBACK-3.3.1.md`.

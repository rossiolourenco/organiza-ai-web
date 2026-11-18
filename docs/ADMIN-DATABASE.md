# Admin 1.0 — Banco de dados

## 1. Migrações

Ordem obrigatória:

1. `database/schema.sql` (base original)
2. `V002__mobile_produtividade.sql`
3. `V003__premium_produtividade.sql`
4. `V004__admin_rbac_seguranca_firewall.sql`

Todas são aditivas. O backend usa `ddl-auto=validate` em produção; o H2 local usa `create`.
O teste `MigrationTests` executa schema + V002 + V003 + V004 em H2 modo PostgreSQL.

## 2. Alterações em `usuarios`

| Coluna | Tipo | Regra |
|---|---|---|
| `papel` | VARCHAR(20) | `USER` (default), `ADMIN`, `SUPER_ADMIN` |
| `status` | VARCHAR(20) | `ATIVO` (default), `BLOQUEADO`, `DESATIVADO`, `ANONIMIZADO` |
| `bloqueado_ate` | TIMESTAMPTZ | bloqueio temporário automático |
| `motivo_bloqueio` | VARCHAR(255) | motivo exibido na gestão |
| `ultimo_login_em` | TIMESTAMPTZ | último login bem-sucedido |
| `senha_alterada_em` | TIMESTAMPTZ | auditoria de credencial |
| `deve_trocar_senha` | BOOLEAN | senha temporária emitida |

Índice: `idx_usuarios_papel_status (papel, status)`.

## 3. `sessoes` (alterada)

| Coluna | Tipo |
|---|---|
| `criado_em` | TIMESTAMPTZ NOT NULL DEFAULT NOW() |
| `ultimo_uso_em` | TIMESTAMPTZ |
| `revogada_em` | TIMESTAMPTZ |
| `ip` | VARCHAR(64) |
| `user_agent` | VARCHAR(255) |

A autenticação exige `revogada_em IS NULL` e `expira_em > NOW()`.
Índice: `idx_sessoes_usuario (usuario_id, revogada_em)`.

## 4. Novas tabelas

### `tentativas_login`

`id`, `email`, `sucesso`, `ip`, `user_agent`, `motivo`, `criado_em`.
Índices por e-mail/data e sucesso/data. Alimenta rate limiting e a central de segurança.

### `auditoria_admin`

`id`, `criado_em`, `ator_id`, `ator_email`, `ator_papel`, `acao`, `recurso`, `recurso_id`,
`resultado`, `ip`, `user_agent`, `detalhes`, `correlacao`.

Índices: data, ação, ator e recurso. Sem FK obrigatória para preservar histórico mesmo após
anonimização. Não há endpoint de alteração/exclusão.

### `politicas_seguranca`

Linha única `id=1`: `senha_minima`, `exigir_numero`, `exigir_maiuscula`, `max_tentativas`,
`janela_minutos`, `bloqueio_minutos`, `sessao_dias`, `atualizado_em`, `atualizado_por`.

### `firewall_regras`

`id`, `nome`, `descricao`, `ip_cidr`, `porta`, `protocolo`, `direcao`, `acao`, `prioridade`,
`ativa`, `criado_em`, `atualizado_em`, `criado_por`, `atualizado_por`.
Índice `(prioridade, id)`.

### `firewall_historico`

`id`, `criado_em`, `ator_email`, `operacao`, `regra_id`, `detalhes`, `resultado`.
Últimas 50 operações exibidas.

### `firewall_snapshots`

`id`, `criado_em`, `ator_email`, `motivo`, `regras_json` (TEXT), `restaurado_em`.
Snapshot é a fonte do rollback simulado.

### `firewall_estado`

Linha única `id=1`: `aplicado_em`, `snapshot_atual_id`, `atualizado_em`, `atualizado_por`.

## 5. Compatibilidade preservada

- `usuarios.senha`, `nome`, `email`, `data_criacao` mantidos.
- Tarefas, categorias, hábitos e sessões antigas continuam válidas.
- Novos campos têm default, portanto registros antigos passam a `USER/ATIVO`.
- Nenhuma coluna foi removida ou renomeada.
- Nenhum dado pessoal foi apagado pela migração.
- `tarefas`, `categorias` e `habitos` não foram alteradas pela V004.

## 6. Reversão

Não há rollback destrutivo. Para retornar à versão anterior:

1. Restaure o backend/Web anteriores coordenados.
2. Mantenha as tabelas/colunas aditivas para não perder auditoria, sessões e firewall.
3. Se for indispensável remover estruturas, faça backup completo e remova apenas em ambiente de
   homologação, na ordem inversa, com autorização explícita.

## 7. Integridade e anonimização

Anonimizar uma conta altera nome/e-mail/senha/status e revoga sessões, mas mantém o `id`.
Tarefas, categorias e hábitos permanecem com a mesma FK, preservando histórico e métricas.
O e-mail anonimizado usa domínio reservado `@anon.invalid`.

## 8. Ambiente local

H2 em memória (`jdbc:h2:mem:organizaai;MODE=PostgreSQL`) recria o schema a cada reinício.
Para demonstração, reiniciar o backend exige rodar novamente:

```bash
node scripts/seed-admin-demo.mjs --reset
node scripts/seed-demo.mjs --reset
```

PostgreSQL/Supabase de produção exige backup e aplicação das migrações antes do novo backend.

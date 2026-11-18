# Migrações — Organiza Aí 3.0

O backend permanece com `ddl-auto=validate`. Nenhuma migração é executada automaticamente e nenhuma conexão com o Supabase foi feita neste trabalho.

## Ordem de aplicação

1. Fazer backup do ambiente de destino e ensaiar em cópia PostgreSQL.
2. Executar **uma vez** `V002__mobile_produtividade.sql` (se ainda não aplicada).
3. Executar **uma vez** `V003__premium_produtividade.sql`.
4. Executar **uma vez** `V004__admin_rbac_seguranca_firewall.sql`.
5. Implantar backend e Web atualizados juntos. O login mantém `id/nome/email` e acrescenta `token`;
   as requisições exigem `Authorization: Bearer ...`. Sessões antigas exigem novo login.
6. Provisionar o primeiro SUPER_ADMIN com `ADMIN_BOOTSTRAP_EMAIL` e `ADMIN_BOOTSTRAP_PASSWORD`.
7. Validar cadastro, login, CRUD, lixeira, hábitos, exclusão de categoria, recorrência e console `/admin`.

## V002 — sessões, detalhes e histórico

Adiciona `sessoes`, `tarefa_conclusoes` e os campos `tarefas.detalhes` / `tarefas.proxima_gerada`.
Não há renomeação, exclusão ou backfill de dados pessoais. `detalhes` é JSON validado pelo serviço,
armazenado em TEXT para manter compatibilidade PostgreSQL/H2. O Web antigo não envia esse campo e o
serviço preserva seu valor.

## V003 — lixeira e hábitos

- `tarefas.excluida_em`: exclusão lógica reversível. Registros antigos permanecem com `NULL` e continuam visíveis.
  A listagem normal filtra `excluida_em IS NULL`; a lixeira lista os demais.
- `habitos`: hábitos por usuário (`DIARIA`/`SEMANAL`, meta de 1 a 7 dias por semana, cor, ativo).
- `habito_registros`: um registro por hábito e data civil, garantido por `UNIQUE (habito_id, data)`;
  repetir a mesma data é idempotente. Excluir o hábito remove seus registros por cascata.
- Índices adicionais para lixeira e consultas de hábitos.

O script é aditivo, usa `IF NOT EXISTS` e não remove dados. O teste `MigrationTests` executa
`schema.sql`, V002 e V003 em **H2 modo PostgreSQL**, insere e lê hábitos, preserva uma tarefa anterior e
verifica `ON DELETE SET NULL`. Isso não substitui ensaio em PostgreSQL real.

## Reversão

Para reverter a aplicação, mantenha as estruturas aditivas no banco e restaure as versões coordenadas dos
clientes/backend. Isso preserva os novos dados. Não é fornecido rollback que apague sessões, detalhes,
histórico, lixeira ou hábitos. Voltar ao backend 1.x restaura a confiança indevida em `usuarioId`.

## Lixeira

- `DELETE /api/tarefas/{id}` move para a lixeira e retorna 204.
- `GET /api/tarefas/lixeira` lista apenas itens removidos do usuário autenticado.
- `POST /api/tarefas/{id}/restaurar` devolve a tarefa à listagem.
- `DELETE /api/tarefas/{id}/definitivo` só funciona para item já na lixeira e apaga também checklist,
  etiquetas, histórico e recorrência associada.
- Itens na lixeira não aparecem em listagens, filtros, indicadores ou no Kanban.
- A restauração preserva o estado anterior (status, prazo e detalhes). A interface oferece "Desfazer" após excluir.

## Hábitos

- `GET/POST /api/habitos`, `PUT/DELETE /api/habitos/{id}`.
- `POST /api/habitos/{id}/registros` com `{ "data": "AAAA-MM-DD" }` é idempotente.
- `DELETE /api/habitos/{id}/registros?data=AAAA-MM-DD` remove o registro.
- Datas são civis, sem conversão UTC. A sequência é calculada no cliente, sempre a partir dessas datas.
- A meta semanal é a referência para hábitos diários e semanais; a frequência altera a unidade da sequência.

## Recorrência e histórico

- A primeira transição para CONCLUIDA de uma ocorrência recorrente gera exatamente uma próxima tarefa na mesma transação.
- Bloqueio pessimista e `proxima_gerada` persistido impedem duas gerações concorrentes.
- Prazo é DATE civil. Fuso IANA é obrigatório na recorrência e identifica o contexto do horário opcional.
- Próximo prazo = prazo anterior + 1 dia / 1 semana / 1 mês, com ajuste ao último dia válido.
- Excluir uma tarefa (para a lixeira ou definitivamente) não apaga a ocorrência seguinte já gerada.
- Cada transição não-concluída → concluída acrescenta um instante UTC ao histórico; repetir enquanto concluída não duplica.
- Tarefas na lixeira mantêm o histórico até a exclusão definitiva.

## Sessão

Tokens aleatórios de 256 bits, somente hash SHA-256 armazenado, validade de sete dias, revogação em logout
online. Em produção, usar HTTPS. Não registrar tokens.


## V004 — Admin 1.0

Adiciona RBAC, status de conta, bloqueio temporário, auditoria, tentativas de login, política de
segurança, firewall DEMO e estado de monitoramento persistido.

- `usuarios`: `papel`, `status`, `bloqueado_ate`, `motivo_bloqueio`, `ultimo_login_em`,
  `senha_alterada_em`, `deve_trocar_senha`.
- `sessoes`: criação, último uso, revogação, IP e user agent.
- Novas tabelas: `tentativas_login`, `auditoria_admin`, `politicas_seguranca`,
  `firewall_regras`, `firewall_historico`, `firewall_snapshots`, `firewall_estado`.
- Linhas singletons: `politicas_seguranca(1)` e `firewall_estado(1)`.
- Nenhuma coluna existente é removida. Usuários antigos passam a `USER/ATIVO`.

O teste `MigrationTests` aplica V004 após V002/V003 e valida papel/status, firewall, auditoria e
política. O console exige essa migração antes de iniciar com `ddl-auto=validate`.

### Provisionamento do primeiro SUPER_ADMIN

Não existe endpoint público. Use variáveis de ambiente no backend:

```bash
ADMIN_BOOTSTRAP_EMAIL=admin@seudominio.com
ADMIN_BOOTSTRAP_PASSWORD=...   # 12+, maiúscula, minúscula e número
ADMIN_BOOTSTRAP_NOME="Administrador"
```

O bootstrap só age se não existir SUPER_ADMIN ativo. Remova a senha do ambiente após o primeiro acesso.

### Rollback

Mantenha as estruturas aditivas. A reversão de aplicação deve restaurar backend/Web anteriores
coordenados; não há script destrutivo de rollback de V004.

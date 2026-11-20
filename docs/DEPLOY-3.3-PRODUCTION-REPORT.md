# Deploy 3.3 em produção — registro

- **Data/hora:** 2026-11-20 16:09–16:16 (America/Sao_Paulo)
- **Versão publicada:** `3.3.0-rc.1` (backend `3.3.0-RC1`)
- **Commit de `main` publicado:** `48154eea036c1dfef4be04c8ed32d618b5257cca`
- **PR:** https://github.com/rossiolourenco/organiza-ai-web/pull/2 (squash merge)
- **Release anterior:** `/opt/organiza-ai/releases/20261119-func32c` (3.2)
- **Release nova:** `/opt/organiza-ai/releases/20261120-3.3-rc1`
- **Symlink ativo:** `/opt/organiza-ai/current -> 20261120-3.3-rc1`

## 1. Gate e merge

- PR #2: `CLEAN`/`MERGEABLE`, HEAD `1d45207`.
- Checks verdes: `web-angular`, `web-backend`, `web-postgres`, `web-admin`.
- Squash merge em `main` = `48154ee`; árvore idêntica à branch do RC (0 arquivos de diferença).
- CI de `main` verde: run 44799397987.

## 2. Backups

Diretório: `/var/backups/organiza-ai/pre-3.3-20261120-160935` (700 root).

| Item | Valor |
|---|---|
| Dump do banco | `organiza_ai-pre-3.3-20261120-160935.dump` (83.580 bytes) |
| SHA-256 do dump | `a885cd51678e212017526cc103a78f7b3763ed7495c8be9f02bbbfcb1c3067b5` |
| Verificação | `pg_restore -l` OK |
| Schema-only | `schema-pre-3.3-20261120-160935.sql` (22.775 bytes) |
| Release anterior | `release-20261119-func32c.tar.gz` (49 MB) |
| SHA-256 do tar | `604288f6c7c40c297ad52e750971e130408906e9c6b7d0eda634b47e5f53f3cd` |
| Configs | `organiza-ai` (nginx), `organiza-ai-backend.service`, `backend.env` (0600) |

## 3. Artefatos publicados

| Artefato | SHA-256 |
|---|---|
| `organiza-ai-web-3.3.0-rc.1.tar.gz` | `59456a53bca7abd1fd2cacf8f0a0400e41c171631f1f024f41d03b2636ee1f95` |
| `organiza-ai-backend-3.3.0-rc.1.jar` | `17c13123c556cbf2818b50a3ec9121b3dfc51f17be7b4ac8598ec122016e25f6` |
| `organiza-ai-database-3.3.0-rc.1.tar.gz` | `29291d179f8b3831b14042ba570a65911b048581c3e6ee6dd35e67ba73051bd4` |

Checksums conferidos no servidor antes da ativação.

## 4. Banco

- PostgreSQL 17.12, base `organiza_ai`, 14 tabelas, owners `organiza_ai`.
- **Nenhuma migration aplicada** (3.3 não introduz DDL); `ddl-auto=validate` passou na subida.
- Contagens antes → depois do deploy: usuários 4 → 4; tarefas 5 → 5; categorias 0 → 0; hábitos 0 → 0.
- Conta de homologação criada para smoke test foi removida ao final (cascade).

## 5. Ativação

- `ln -sfn 20261120-3.3-rc1 /opt/organiza-ai/current`
- `systemctl restart organiza-ai-backend` (único serviço reiniciado; Nginx e PostgreSQL não foram reiniciados)
- Backend: `v3.3.0-RC1`, Java 21.0.13, Tomcat 8080, PID 45405, `NRestarts=0`.

## 6. Smoke tests

| Verificação | Resultado |
|---|---|
| `/`, `/admin`, `/login` | 200 |
| `/api/usuarios` sem token | 401 |
| `/api/admin/usuarios` sem token | 401 |
| Headers (CSP, nosniff, DENY, no-store) | presentes |
| Cadastro (DTO, papel USER, sem hash) | OK |
| Login (token 43 chars) | OK |
| Perfil do próprio usuário | OK |
| Admin com token USER | 403 |
| IDOR (`usuarioId` de terceiro) | 403 |
| Categoria + tarefa (criar/listar/concluir/excluir/lixeira/definitivo) | OK |
| Troca de senha (204), revogação de sessão | OK |
| Senha antiga rejeitada / nova aceita | 401 / 200 |
| Logout (204) e token inválido depois | OK |
| SPA em navegador real (login) | renderiza, 0 erros de console |
| Guards: `/admin` → `/login?admin=1`; `/home`,`/perfil` → `/login` | OK |

## 7. Monitoramento (4min32s)

- Serviço `active (running)` em 10/10 amostras; 0 restarts.
- RSS do backend 458 → 460 MB; memória disponível estável (~2,9 GB).
- 0 respostas 5xx no Nginx desde a ativação.
- 0 erros no journal do backend.

## 8. Incidente e correção

- **16:10:** páginas `/`, `/admin`, `/login` retornaram 403 por permissão `750` no diretório da release (Nginx/`www-data` sem acesso).
- **16:10:40:** `chmod 755` na release e em `public/` (arquivos 644); imediatamente voltou a 200.
- Sem impacto em API (o backend já respondia) e sem perda de dados. Rollback não foi necessário.

## 9. Pendências

- Mobile: PR #2 continua aberto, sem merge e sem publicação; faltam build assinado e teste em aparelho físico.
- Itens P0/P1 do roadmap (MFA, refresh token, recuperação de senha, upgrade Angular/Expo).

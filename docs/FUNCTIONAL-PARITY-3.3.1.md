# Matriz de paridade funcional — 3.3.1

- **Backend auditado:** 54 endpoints (28 de negócio/autenticação + 26 administrativos)
- **Data:** 2026-11-20
- **Regra:** só é `PARITY_OK` com evidência de funcionamento nas duas plataformas.

Legenda: **PARITY_OK** · **WEB_MISSING** · **MOBILE_MISSING** · **PARTIAL** · **BACKEND_GAP** ·
**ROLE_RESTRICTED** · **NOT_APPLICABLE**

## 1. Conta e autenticação

| Recurso | Backend | Web | Mobile | Permissão | Teste | Status |
|---|---|---|---|---|---|---|
| Cadastro | `POST /api/usuarios` | `/cadastro` | `/cadastro` | pública | `ContaApiTests`, `PostgresIntegrationTests`, browser E2E, Jest | PARITY_OK |
| Login | `POST /api/usuarios/login` | `/login` | `/` | pública | idem | PARITY_OK |
| Logout | `POST /api/usuarios/logout` | shell (Sair) | Perfil/Segurança | autenticado | browser E2E, Jest | PARITY_OK |
| Perfil (ler) | `GET /api/usuarios/perfil` | `/perfil` | aba Perfil | autenticado | browser E2E, Jest | PARITY_OK |
| Perfil (editar nome) | `PUT /api/usuarios/perfil` | `/perfil` | Perfil | autenticado | browser E2E, Jest | PARITY_OK |
| Alterar senha | `PUT /api/usuarios/senha` | `/perfil` | `/seguranca` | autenticado | browser E2E, Jest | PARITY_OK |
| Listar o próprio usuário | `GET /api/usuarios` | — | — | autenticado | `ContaApiTests` | NOT_APPLICABLE (nenhum cliente precisa; o perfil cobre) |

## 2. Tarefas

| Recurso | Backend | Web | Mobile | Permissão | Teste | Status |
|---|---|---|---|---|---|---|
| Listar | `GET /api/tarefas` | `/tarefas`, `/home` | aba Tarefas/Home | dono | Postgres IT, browser E2E, Jest | PARITY_OK |
| Criar | `POST /api/tarefas` | `/tarefas/nova` | `/novaTarefa` | dono | Postgres IT, browser E2E, Jest | PARITY_OK |
| Editar | `PUT /api/tarefas/{id}` | `/tarefas/editar/:id` | `/editarTarefa` | dono | Postgres IT, Jest | PARITY_OK |
| Concluir | `PATCH /api/tarefas/{id}/concluir` | `/tarefas`, `/home` | Tarefas/Home | dono | Postgres IT, browser E2E, Jest | PARITY_OK |
| Excluir (lixeira) | `DELETE /api/tarefas/{id}` | `/tarefas` | Tarefas | dono | Postgres IT, browser E2E, Jest | PARITY_OK |
| Lixeira | `GET /api/tarefas/lixeira` | `/lixeira` | `/lixeira` | dono | Postgres IT, browser E2E, Jest | PARITY_OK |
| Restaurar | `POST /api/tarefas/{id}/restaurar` | `/lixeira` | `/lixeira` | dono | Postgres IT, browser E2E, Jest | PARITY_OK |
| Exclusão definitiva | `DELETE /api/tarefas/{id}/definitivo` | `/lixeira` | `/lixeira` | dono | Postgres IT, Jest | PARITY_OK |
| Filtrar por status | `GET /api/tarefas/status/{s}` | filtro local | filtro local | dono | `PremiumApiTests`, Jest | NOT_APPLICABLE (filtro no cliente; endpoint disponível) |
| Filtrar por prioridade | `GET /api/tarefas/prioridade/{p}` | filtro local | filtro local | dono | `PremiumApiTests`, Jest | NOT_APPLICABLE (filtro no cliente) |
| Buscar por id | `GET /api/tarefas/{id}` | — | — | dono | `PremiumApiTests` | NOT_APPLICABLE (listas já trazem o item) |

## 3. Categorias

| Recurso | Backend | Web | Mobile | Permissão | Teste | Status |
|---|---|---|---|---|---|---|
| Listar | `GET /api/categorias` | `/categorias`, formulário | `/categorias` | dono | Postgres IT, browser E2E, Jest | PARITY_OK |
| Criar | `POST /api/categorias` | `/categorias` | `/categorias` | dono | Postgres IT, browser E2E, Jest | PARITY_OK |
| Editar | `PUT /api/categorias/{id}` | `/categorias` | `/categorias` | dono | `PremiumApiTests`, Jest | PARITY_OK |
| Excluir | `DELETE /api/categorias/{id}` | `/categorias` | `/categorias` | dono | Postgres IT (SET NULL), Jest | PARITY_OK |

## 4. Hábitos

| Recurso | Backend | Web | Mobile | Permissão | Teste | Status |
|---|---|---|---|---|---|---|
| Listar | `GET /api/habitos` | `/habitos` | `/habitos` | dono | Postgres IT, browser E2E, Jest | PARITY_OK |
| Criar | `POST /api/habitos` | `/habitos` | `/habitos` | dono | Postgres IT, browser E2E, Jest | PARITY_OK |
| Editar | `PUT /api/habitos/{id}` | `/habitos` | `/habitos` | dono | `PremiumApiTests`, Jest | PARITY_OK |
| Excluir | `DELETE /api/habitos/{id}` | `/habitos` | `/habitos` | dono | `PremiumApiTests`, Jest | PARITY_OK |
| Registrar dia | `POST /api/habitos/{id}/registros` | `/habitos` | `/habitos` | dono | Postgres IT, browser E2E, Jest | PARITY_OK |
| Remover registro | `DELETE /api/habitos/{id}/registros` | `/habitos` | `/habitos` | dono | `PremiumApiTests`, Jest | PARITY_OK |

## 5. Produtividade derivada e preferências de dispositivo

| Recurso | Backend | Web | Mobile | Permissão | Teste | Status |
|---|---|---|---|---|---|---|
| Dashboard/indicadores | derivado de `/api/tarefas` | `/home` | aba Home | dono | `dashboard` (browser) + Jest | PARITY_OK |
| Progresso do dia | derivado | `/home` | Home | dono | `productivity.spec.ts`, Jest | PARITY_OK |
| Próxima melhor ação | derivado | `/home` | Home | dono | `productivity.spec.ts`, Jest | PARITY_OK |
| Planejamento diário | `/api/produtividade/planejamento` | `/planejamento` | aba Planejamento | dono | Postgres IT, `ProdutividadeApiTests`, `productivity.spec.ts`, Jest | PARITY_OK |
| Revisão semanal | derivado | `/revisao` | `/revisao` | dono | `productivity.spec.ts`, Jest | PARITY_OK |
| Foco/Pomodoro | `/api/produtividade/foco` (sessões concluídas) | `/foco` | aba Foco | dono | Postgres IT, `ProdutividadeApiTests`, `focus-sync.test.ts`, Jest | PARITY_OK (cronômetro local; histórico no backend) |
| Notificações/lembretes | — (sem backend) | — | `expo-notifications` (local) | dono | Jest/config | NOT_APPLICABLE (notificação nativa; sem backend de notificações) |

## 6. Administração (restrita à Web)

| Recurso | Backend | Web | Mobile | Permissão | Teste | Status |
|---|---|---|---|---|---|---|
| Console e dashboard | `/api/admin/*` | `/admin/*` | — | ADMIN/SUPER_ADMIN | `AdminApiTests`, browser E2E | ROLE_RESTRICTED |
| Gestão de usuários | `GET/POST/PATCH/DELETE /api/admin/usuarios` | `/admin/usuarios` | — | ADMIN/SUPER_ADMIN | `AdminApiTests` | ROLE_RESTRICTED |
| Segurança/política | `/api/admin/seguranca/*` | `/admin/seguranca` | — | ADMIN/SUPER_ADMIN | `AdminApiTests` | ROLE_RESTRICTED |
| Auditoria | `GET /api/admin/auditoria` | `/admin/auditoria` | — | ADMIN/SUPER_ADMIN | `AdminApiTests` | ROLE_RESTRICTED |
| Firewall DEMO | `/api/admin/firewall/*` | `/admin/firewall` | — | ADMIN/SUPER_ADMIN | `AdminApiTests` | ROLE_RESTRICTED |
| Monitoramento | `GET /api/admin/monitoramento/overview` | `/admin/monitoramento` | — | ADMIN/SUPER_ADMIN | `AdminApiTests` | ROLE_RESTRICTED |

## 7. Resumo

| Status | Quantidade |
|---|---|
| PARITY_OK | 30 |
| NOT_APPLICABLE | 5 (filtro local/endpoints redundantes + notificações nativas) |
| ROLE_RESTRICTED | 6 grupos administrativos (26 endpoints) |
| WEB_MISSING | **0** |
| MOBILE_MISSING | **0** |
| PARTIAL | **0** |
| BACKEND_GAP | **0** |

Planejamento e histórico de foco agora são **dados de negócio centralizados** (níveis 2 e 3 de paridade).

Nenhuma funcionalidade de negócio destinada ao usuário ficou ausente em uma das plataformas.

## 8. Exclusão de conta (exigência das lojas)

| Item | Backend | Web | Mobile |
|---|---|---|---|
| `DELETE /api/usuarios/conta` (senha atual) | ✅ | ✅ | ✅ |
| Confirmação por senha + digitar `EXCLUIR` | — | ✅ | ✅ |
| Página pública `/excluir-conta` | — | ✅ | — |
| Remoção em cascata + revogação de sessões | ✅ | — | — |
| Auditoria anonimizada | ✅ | — | — |

Cobertura: `ContaApiTests` (H2), `PostgresIntegrationTests` (cascata em PostgreSQL 17) e
validação manual no navegador.

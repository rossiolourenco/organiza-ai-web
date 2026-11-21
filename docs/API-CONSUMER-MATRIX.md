# Matriz de consumidores da API — 3.3.1

- **Fonte:** controllers Spring Boot (`backend/src/main/java/.../controller`)
- **Total:** 59 endpoints (33 de negócio/autenticação + 26 administrativos)
- **Data:** 2026-11-20

## 1. Conta e autenticação (7)

| Método | Endpoint | Controller | DTO entrada | DTO saída | Auth | Web | Mobile |
|---|---|---|---|---|---|---|---|
| GET | `/api/usuarios` | Usuario | — | `PerfilResponse` | Bearer | — | — |
| GET | `/api/usuarios/perfil` | Usuario | — | `PerfilResponse` | Bearer | ✅ | ✅ |
| PUT | `/api/usuarios/perfil` | Usuario | `PerfilRequest` | `PerfilResponse` | Bearer | ✅ | ✅ |
| POST | `/api/usuarios` | Usuario | `CadastroRequest` | `PerfilResponse` | pública | ✅ | ✅ |
| POST | `/api/usuarios/login` | Usuario | `LoginRequest` | sessão/token | pública | ✅ | ✅ |
| POST | `/api/usuarios/logout` | Usuario | — | 204 | Bearer | ✅ | ✅ |
| PUT | `/api/usuarios/senha` | Usuario | `AlterarSenhaRequest` | 204 | Bearer | ✅ | ✅ |
| DELETE | `/api/usuarios/conta` | Usuario | `ExcluirContaRequest` | 204 | Bearer | ✅ | ✅ |

## 2. Tarefas (11)

| Método | Endpoint | Controller | DTO entrada | DTO saída | Auth | Web | Mobile |
|---|---|---|---|---|---|---|---|
| GET | `/api/tarefas` | Tarefa | — | `Tarefa[]` | Bearer + escopo | ✅ | ✅ |
| GET | `/api/tarefas/lixeira` | Tarefa | — | `Tarefa[]` | Bearer + escopo | ✅ | ✅ |
| POST | `/api/tarefas/{id}/restaurar` | Tarefa | — | `Tarefa` | Bearer + escopo | ✅ | ✅ |
| DELETE | `/api/tarefas/{id}/definitivo` | Tarefa | — | 204 | Bearer + escopo | ✅ | ✅ |
| GET | `/api/tarefas/status/{status}` | Tarefa | — | `Tarefa[]` | Bearer + escopo | — (filtro local) | — (filtro local) |
| GET | `/api/tarefas/prioridade/{prioridade}` | Tarefa | — | `Tarefa[]` | Bearer + escopo | — (filtro local) | — (filtro local) |
| GET | `/api/tarefas/{id}` | Tarefa | — | `Tarefa` | Bearer + escopo | — | — |
| POST | `/api/tarefas` | Tarefa | `Tarefa` | `Tarefa` | Bearer + escopo | ✅ | ✅ |
| PUT | `/api/tarefas/{id}` | Tarefa | `Tarefa` | `Tarefa` | Bearer + escopo | ✅ | ✅ |
| PATCH | `/api/tarefas/{id}/concluir` | Tarefa | — | `Tarefa` | Bearer + escopo | ✅ | ✅ |
| DELETE | `/api/tarefas/{id}` | Tarefa | — | 204 | Bearer + escopo | ✅ | ✅ |

## 3. Categorias (4)

| Método | Endpoint | Controller | DTO entrada | DTO saída | Auth | Web | Mobile |
|---|---|---|---|---|---|---|---|
| GET | `/api/categorias` | Categoria | — | `Categoria[]` | Bearer + escopo | ✅ | ✅ |
| POST | `/api/categorias` | Categoria | `Categoria` | `Categoria` | Bearer + escopo | ✅ | ✅ |
| PUT | `/api/categorias/{id}` | Categoria | `Categoria` | `Categoria` | Bearer + escopo | ✅ | ✅ |
| DELETE | `/api/categorias/{id}` | Categoria | — | 204 | Bearer + escopo | ✅ | ✅ |

## 4. Hábitos (6)

| Método | Endpoint | Controller | DTO entrada | DTO saída | Auth | Web | Mobile |
|---|---|---|---|---|---|---|---|
| GET | `/api/habitos` | Habito | — | `Habito[]` | Bearer + escopo | ✅ | ✅ |
| POST | `/api/habitos` | Habito | `Habito` | `Habito` | Bearer + escopo | ✅ | ✅ |
| PUT | `/api/habitos/{id}` | Habito | `Habito` | `Habito` | Bearer + escopo | ✅ | ✅ |
| DELETE | `/api/habitos/{id}` | Habito | — | 204 | Bearer + escopo | ✅ | ✅ |
| POST | `/api/habitos/{id}/registros` | Habito | `RegistroHabitoRequest` | `Habito` | Bearer + escopo | ✅ | ✅ |
| DELETE | `/api/habitos/{id}/registros` | Habito | query `data` | `Habito` | Bearer + escopo | ✅ | ✅ |

## 5. Produtividade (5) — sincronização entre dispositivos

| Método | Endpoint | Controller | DTO entrada | DTO saída | Auth | Web | Mobile |
|---|---|---|---|---|---|---|---|
| GET | `/api/produtividade/planejamento` | Produtividade | query `data` | `PlanejamentoResponse` | Bearer (sessão) | ✅ | ✅ |
| PUT | `/api/produtividade/planejamento` | Produtividade | `PlanejamentoRequest` | `PlanejamentoResponse` | Bearer (sessão) | ✅ | ✅ |
| GET | `/api/produtividade/foco` | Produtividade | query `data` | `SessaoFocoResponse[]` | Bearer (sessão) | ✅ | ✅ |
| POST | `/api/produtividade/foco` | Produtividade | `SessaoFocoRequest` | `SessaoFocoResponse` | Bearer (sessão) | ✅ | ✅ |
| GET | `/api/produtividade/foco/resumo` | Produtividade | query `inicio`,`fim` | `FocoResumoResponse` | Bearer (sessão) | ✅ | — |

> Os endpoints de produtividade derivam o usuário da sessão (não usam `usuarioId` na query), o que
> impede IDOR; a posse da tarefa vinculada é validada no serviço.

## 6. Administração (26) — apenas Web, RBAC

| Método | Endpoint | Controller | Web | Mobile |
|---|---|---|---|---|
| GET | `/api/admin/auditoria` | AdminAuditoria | ✅ | — |
| GET | `/api/admin/firewall/estado` | AdminFirewall | ✅ | — |
| GET | `/api/admin/firewall/regras` | AdminFirewall | ✅ | — |
| POST | `/api/admin/firewall/regras` | AdminFirewall | ✅ | — |
| PUT | `/api/admin/firewall/regras/{id}` | AdminFirewall | ✅ | — |
| PATCH | `/api/admin/firewall/regras/{id}/ativa` | AdminFirewall | ✅ | — |
| DELETE | `/api/admin/firewall/regras/{id}` | AdminFirewall | ✅ | — |
| GET | `/api/admin/firewall/validar` | AdminFirewall | ✅ | — |
| POST | `/api/admin/firewall/aplicar` | AdminFirewall | ✅ | — |
| POST | `/api/admin/firewall/rollback` | AdminFirewall | ✅ | — |
| POST | `/api/admin/firewall/reset-demo` | AdminFirewall | ✅ | — |
| GET | `/api/admin/firewall/historico` | AdminFirewall | ✅ | — |
| GET | `/api/admin/monitoramento/overview` | AdminMonitor | ✅ | — |
| GET | `/api/admin/seguranca/overview` | AdminSeguranca | ✅ | — |
| GET | `/api/admin/seguranca/tentativas` | AdminSeguranca | ✅ | — |
| GET | `/api/admin/seguranca/politica` | AdminSeguranca | ✅ | — |
| PUT | `/api/admin/seguranca/politica` | AdminSeguranca | ✅ | — |
| GET | `/api/admin/usuarios/me` | AdminUsuario | ✅ | — |
| GET | `/api/admin/usuarios` | AdminUsuario | ✅ | — |
| GET | `/api/admin/usuarios/{id}` | AdminUsuario | ✅ | — |
| POST | `/api/admin/usuarios` | AdminUsuario | ✅ | — |
| PATCH | `/api/admin/usuarios/{id}/status` | AdminUsuario | ✅ | — |
| PATCH | `/api/admin/usuarios/{id}/papel` | AdminUsuario | ✅ | — |
| POST | `/api/admin/usuarios/{id}/revogar-sessoes` | AdminUsuario | ✅ | — |
| POST | `/api/admin/usuarios/{id}/redefinir-senha` | AdminUsuario | ✅ | — |
| DELETE | `/api/admin/usuarios/{id}` | AdminUsuario | ✅ | — |

## 7. Observações de contrato

- Todos os endpoints de negócio exigem `Authorization: Bearer` e escopo por `usuarioId` (403/404 para
  recurso de terceiro). Endpoints administrativos exigem papel `ADMIN`/`SUPER_ADMIN`.
- `/api/usuarios` (GET), `/api/tarefas/{id}`, `/status` e `/prioridade` não têm consumidor direto:
  as listas já trazem os dados e os filtros são locais. Mantidos por compatibilidade.
- O 3.3.1 adiciona 5 endpoints de produtividade (planejamento e foco) sem alterar contratos existentes.

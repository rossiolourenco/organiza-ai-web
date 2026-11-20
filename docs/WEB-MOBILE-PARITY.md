# Web × Mobile × API — Matriz de Paridade (3.3 RC)

Legenda: **✅ implementado** · **◐ parcial** · **— não implementado** · **API** = endpoint
existente no backend compartilhado.

## 1. Autenticação e conta

| Funcionalidade | Web/Admin | Mobile | API |
|---|---|---|---|
| Cadastro | ✅ `/cadastro` | ✅ `/cadastro` | ✅ `POST /api/usuarios` |
| Login | ✅ `/login` | ✅ `/` | ✅ `POST /api/usuarios/login` |
| Logout | ✅ header | ✅ Perfil/Segurança | ✅ `POST /api/usuarios/logout` |
| Sessão persistente | ✅ `localStorage` | ✅ `expo-secure-store` | Token opaco com hash |
| Perfil (ver) | ✅ `/perfil` | ✅ aba Perfil | ✅ `GET /api/usuarios/perfil` |
| Perfil (editar nome) | ✅ | ✅ | ✅ `PUT /api/usuarios/perfil` |
| Alterar senha | ✅ `/perfil` | ✅ `/seguranca` | ✅ `PUT /api/usuarios/senha` |
| Redefinição por admin | ✅ console | — | ✅ `POST /api/admin/usuarios/{id}/redefinir-senha` |
| Recuperação self-service | — | — | — (roadmap P1) |
| MFA | — | — | — (roadmap P1) |
| Refresh token | — | — | — (roadmap P1) |

## 2. Produtividade

| Funcionalidade | Web | Mobile | API |
|---|---|---|---|
| Dashboard/Home com indicadores | ✅ `/home` | ✅ aba Home | ✅ `GET /api/tarefas` |
| Listar tarefas | ✅ | ✅ | ✅ `GET /api/tarefas?usuarioId` |
| Criar/editar tarefa | ✅ `/tarefas/nova`, `/tarefas/editar/:id` | ✅ `/novaTarefa`, `/editarTarefa` | ✅ `POST`/`PUT /api/tarefas` |
| Concluir tarefa | ✅ | ✅ | ✅ `PATCH /api/tarefas/{id}/concluir` |
| Excluir (lixeira) | ✅ modal de confirmação | ✅ | ✅ `DELETE /api/tarefas/{id}` |
| Restaurar da lixeira | — | ✅ `/lixeira` | ✅ `POST /api/tarefas/{id}/restaurar` |
| Exclusão definitiva | — | ✅ `/lixeira` | ✅ `DELETE /api/tarefas/{id}/definitivo` |
| Filtros (status/prioridade/prazo) | ✅ | ✅ | ✅ `/status/{s}`, `/prioridade/{p}` |
| Busca e ordenação | — | ✅ | ◐ filtro por status/prioridade no servidor |
| Kanban | — | ✅ | ✅ via `status` |
| Categorias | ✅ no formulário | ✅ `/categorias` | ✅ `GET/POST/PUT/DELETE /api/categorias` |
| Check-list/detalhes da tarefa | — | ✅ “Checklist e notas” | ✅ campo `detalhes` |
| Recorrência | — | ✅ “Repetição” | ✅ geração da próxima ocorrência |
| Histórico de conclusões | — | ✅ | ✅ `tarefa_conclusoes` |
| Hábitos | — | ✅ `/habitos` | ✅ `GET/POST/PUT/DELETE /api/habitos` |
| Planejamento semanal | — | ✅ aba Planejamento | ◐ derivado de tarefas |
| Revisão semanal | — | ✅ `/revisao` | ◐ derivado de tarefas |
| Foco/Pomodoro | — | ✅ aba Foco + `/concentracao` | — (estado local) |
| Matriz de Eisenhower | — | ✅ em Planejamento | ◐ derivado de prioridade |
| Notificações/lembretes | — | ✅ `expo-notifications` | — (local) |
| Exportar dados | — | ✅ (local no app) | ◐ dados via API |

## 3. Administração (Web/Admin apenas)

| Funcionalidade | Web/Admin | API |
|---|---|---|
| Login administrativo e RBAC | ✅ `adminGuard` + `Rbac` | ✅ filtro + serviços |
| Dashboard com indicadores | ✅ | ✅ `GET /api/admin/monitoramento/overview` |
| Gestão de usuários | ✅ `/admin/usuarios` | ✅ `/api/admin/usuarios` |
| Status, papel e sessões | ✅ conforme RBAC | ✅ `PATCH status`, `PATCH papel`, `POST revogar-sessoes` |
| Segurança e política | ✅ `/admin/seguranca` | ✅ `GET/PUT /api/admin/seguranca/politica` |
| Auditoria | ✅ `/admin/auditoria` | ✅ `GET /api/admin/auditoria` |
| Firewall DEMO | ✅ `/admin/firewall` | ✅ regras, validar, aplicar, rollback, histórico |
| Monitoramento | ✅ `/admin/monitoramento` | ✅ `GET /api/admin/monitoramento/overview` |
| Configurações/ambiente | ✅ `/admin/configuracoes` | ✅ política + metadados |
| Console administrativo no Mobile | — (por decisão de produto) | — |

## 4. Diferenças intencionais

- **Mobile** concentra produtividade pessoal (hábitos, foco, planejamento, revisão, lixeira).
- **Web** concentra gestão de tarefas e o **Admin 1.0**.
- O **Mobile não tem Admin** por decisão de escopo; a API administrativa exige RBAC no backend.
- Nenhuma funcionalidade existente do Mobile ou do Web foi removida nesta release.

## 5. Sincronização Web ↔ Mobile

Cenário validado na 3.2 (backend local, mesma conta): tarefa criada na Web apareceu no Mobile,
foi concluída no Mobile e a Web passou a exibir `CONCLUÍDA`; categoria criada no Mobile apareceu
no formulário da Web.

No RC 3.3 **nenhum endpoint, verbo ou payload de tarefas/categorias foi alterado**; a única
mudança de contrato foi a remoção de campos internos do próprio usuário em `/api/usuarios`, não
consumidos por nenhum dos clientes. O teste de integração com PostgreSQL real reexecutou CRUD,
filtros, conclusão, lixeira e categorias na mesma API usada pelas duas plataformas.

## 6. Lacunas registradas (roadmap 3.3/3.4)

| Lacuna | Impacto | Prioridade |
|---|---|---|
| Lixeira sem UI no Web | Tarefa excluída no Web fica preservada no banco, mas só é recuperável pelo Mobile ou pelo Admin | P2 |
| Check-list/recorrência sem UI no Web | Recurso disponível apenas no Mobile | P2 |
| Hábitos no Web | Usuário Web não gerencia hábitos | P3 |
| Planejamento/Foco no Web | Recurso exclusivo do Mobile | P3 |
| Recuperação de senha | Suporte manual | P1 |
| MFA e refresh token | Endurecimento de sessão | P1 |
| Tela de sessões ativas no Web | Gestão pelo Admin | P2 |

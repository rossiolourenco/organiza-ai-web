# Organiza Aí 3.2 — Matriz funcional

Legenda: ✅ funcional e validada · 🟡 funcional com ressalvas · 🟠 incompleta · ❌ ausente ·
➖ não aplicável · 🔒 bloqueada por dependência externa.

Evidências: suítes `ContaApiTests`, `MobileApiTests`, `PremiumApiTests`, `AdminApiTests`,
testes Angular e Mobile, e cenários ponta a ponta descritos no relatório de testes.

## 0. Matriz resumida (colunas obrigatórias)

| Módulo | Funcionalidade | Web | Mobile | Backend | Perfil | Comportamento esperado | Estado encontrado | Problema | Prioridade | Correção | Teste | Resultado |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| Conta | Login | ✅ | ✅ | ✅ | público | Autenticar e criar sessão | Funcional | — | — | — | E2E | Aprovado |
| Conta | Logout | ✅ | ✅ | ✅ | autenticado | Revogar sessão e limpar cliente | Funcional | — | — | — | E2E | Aprovado |
| Conta | Alterar senha | ✅ | ✅ | ✅ | autenticado | Confirmar atual, validar política, revogar sessões | **Ausente na UI** | Interface não expunha endpoint | P1 | Perfil → Segurança | ContaApiTests + E2E | Aprovado |
| Conta | Perfil (ver) | ✅ | ✅ | ✅ | autenticado | Exibir nome/e-mail sem hash | **Ausente na UI** | Sem tela/contrato de leitura | P1 | GET /usuarios/perfil + tela | ContaApiTests | Aprovado |
| Conta | Perfil (editar nome) | ✅ | ✅ | ✅ | autenticado | Persistir nome e refletir na sessão | **Ausente** | Sem endpoint/tela | P1 | PUT /usuarios/perfil | ContaApiTests + E2E | Aprovado |
| Conta | Recuperar senha | ❌ | ❌ | ❌ | público | Enviar token por e-mail | Ausente | Depende de SMTP | P2 | Não simulado | — | Pendente |
| Tarefas | Criar | ✅ | ✅ | ✅ | dono | Persistir e listar | Funcional | — | — | — | E2E Web→Mobile | Aprovado |
| Tarefas | Concluir | ✅ | ✅ | ✅ | dono | Status CONCLUIDA e conclusão registrada | Funcional | — | — | — | E2E Mobile→Web | Aprovado |
| Tarefas | Reabrir | ✅ | ✅ | ✅ | dono | Voltar para PENDENTE | **Ausente na Web** | Sem ação | P2 | Botão Reabrir | E2E | Aprovado |
| Tarefas | Status Em andamento | ✅ | ✅ | ✅ | dono | Persistir status | **Ausente na Web** | Sem select | P2 | Select no formulário | E2E | Aprovado |
| Tarefas | Excluir | ✅ | ✅ | ✅ | dono | Exclusão lógica + lixeira | Funcional | Web sem lixeira | P2 | Paridade futura | PremiumApiTests | Aprovado com ressalva |
| Categorias | CRUD | ✅ | ✅ | ✅ | dono | Criar/editar/excluir sem afetar terceiros | Funcional | Web sem página dedicada | P2 | Formulário Web | E2E Mobile→Web | Aprovado com ressalva |
| Segurança | Isolamento A/B | ✅ | ✅ | ✅ | dono | A não acessa B | Funcional | — | — | — | MobileApiTests | Aprovado |
| Admin | RBAC | ✅ | ➖ | ✅ | ADMIN/SUPER | USER não acessa Admin | Funcional | — | — | — | AdminApiTests | Aprovado |
| Sincronização | Tarefa Web→Mobile | ✅ | ✅ | ✅ | dono | Mesmo dado nas duas plataformas | Funcional | — | — | — | E2E | Aprovado |
| Sincronização | Categoria Mobile→Web | ✅ | ✅ | ✅ | dono | Mesma categoria nas duas | Funcional | — | — | — | E2E | Aprovado |

## 1. Autenticação e conta

| Funcionalidade | Web | Mobile | Backend | Perfil | Estado | Problema | Prioridade | Correção | Teste |
|---|---|---|---|---|---|---|---|---|---|
| Cadastro | ✅ | ✅ | ✅ | público | ✅ | — | — | — | MobileApiTests / Angular |
| Login | ✅ | ✅ | ✅ | público | ✅ | — | — | — | E2E + testes |
| Logout | ✅ | ✅ | ✅ | autenticado | ✅ | — | — | — | E2E |
| Sessão persistente | ✅ | ✅ | ✅ | autenticado | ✅ | — | — | — | Mobile + Web |
| Expiração/revogação | ✅ | ✅ | ✅ | autenticado | ✅ | — | — | — | ContaApiTests |
| Alterar senha | ✅ | ✅ | ✅ | autenticado | ✅ | **Ausente antes da 3.2** | P1 | Página Perfil → Segurança | ContaApiTests + E2E |
| Política de senha | ✅ | ✅ | ✅ | autenticado | ✅ | Validação backend | — | — | ContaApiTests |
| Revogar sessões na troca | ✅ | ✅ | ✅ | autenticado | ✅ | — | — | — | ContaApiTests + E2E |
| Ver perfil | ✅ | ✅ | ✅ | autenticado | ✅ | **Ausente antes da 3.2** | P1 | GET /usuarios/perfil | ContaApiTests + E2E |
| Editar nome | ✅ | ✅ | ✅ | autenticado | ✅ | **Ausente antes da 3.2** | P1 | PUT /usuarios/perfil | ContaApiTests + E2E |
| Editar e-mail | ❌ | ❌ | ❌ | autenticado | ❌ | Falta contrato e verificação | P2 | Não implementado | — |
| Recuperar senha | ❌ | ❌ | ❌ | público | 🔒 | Depende de SMTP/produto | P2 | Não simulado | — |
| Bloqueio por tentativas | ✅ | ✅ | ✅ | público | ✅ | — | — | — | AdminApiTests |
| Isolamento entre contas | ✅ | ✅ | ✅ | autenticado | ✅ | — | — | — | MobileApiTests |
| RBAC Admin | ✅ | ➖ | ✅ | ADMIN/SUPER | ✅ | — | — | — | AdminApiTests |

## 2. Tarefas

| Funcionalidade | Web | Mobile | Backend | Perfil | Estado | Problema | Prioridade | Correção | Teste |
|---|---|---|---|---|---|---|---|---|---|
| Criar | ✅ | ✅ | ✅ | USER | ✅ | — | — | — | E2E Web→Mobile |
| Listar | ✅ | ✅ | ✅ | USER | ✅ | — | — | — | E2E |
| Detalhar | ✅ | ✅ | ✅ | dono | ✅ | — | — | — | MobileApiTests |
| Editar | ✅ | ✅ | ✅ | dono | ✅ | — | — | — | E2E |
| Prioridade | ✅ | ✅ | ✅ | dono | ✅ | — | — | — | E2E |
| Status Em andamento | ✅ | ✅ | ✅ | dono | ✅ | **Web não tinha** | P2 | Select no formulário | E2E |
| Concluir | ✅ | ✅ | ✅ | dono | ✅ | — | — | — | E2E |
| Reabrir | ✅ | ✅ | ✅ | dono | ✅ | **Web não tinha** | P2 | Botão Reabrir | E2E |
| Excluir (lixeira) | ✅ | ✅ | ✅ | dono | ✅ | — | — | — | PremiumApiTests |
| Restaurar | ❌ | ✅ | ✅ | dono | 🟡 | Web sem lixeira | P2 | Paridade futura | PremiumApiTests |
| Excluir definitivo | ❌ | ✅ | ✅ | dono | 🟡 | Web sem lixeira | P2 | Paridade futura | PremiumApiTests |
| Filtrar status/prioridade | ✅ | ✅ | ✅ | dono | ✅ | — | — | — | Angular spec |
| Filtrar período | ✅ | ✅ | ✅ | dono | ✅ | — | — | — | Angular spec |
| Buscar | ❌ | ✅ | ✅ | dono | 🟡 | Web não tem busca | P2 | Paridade futura | Mobile |
| Ordenar | ❌ | ✅ | ✅ | dono | 🟡 | Web ordem padrão | P3 | Paridade futura | Mobile |
| Recorrência | ❌ | ✅ | ✅ | dono | 🟡 | Web sem campo | P2 | Paridade futura | MobileApiTests |
| Subtarefas/checklist | ❌ | ✅ | ✅ | dono | 🟡 | Web sem campo | P2 | Paridade futura | MobileApiTests |
| Notas/etiquetas | ❌ | ✅ | ✅ | dono | 🟡 | Web sem campo | P2 | Paridade futura | MobileApiTests |
| Timezone/fuso | ✅ | ✅ | ✅ | dono | ✅ | Datas LocaleDate | — | — | Testes |

## 3. Categorias

| Funcionalidade | Web | Mobile | Backend | Perfil | Estado | Problema | Prioridade | Correção | Teste |
|---|---|---|---|---|---|---|---|---|---|
| Listar | ✅ | ✅ | ✅ | dono | ✅ | Web lista no formulário | — | — | E2E Mobile→Web |
| Criar | ✅ | ✅ | ✅ | dono | ✅ | Web cria no formulário | — | — | E2E |
| Editar | ❌ | ✅ | ✅ | dono | 🟡 | Web só cria/exclui | P2 | Paridade futura | MobileApiTests |
| Excluir | ✅ | ✅ | ✅ | dono | ✅ | Preserva tarefas | — | — | E2E |
| Associar tarefa | ✅ | ✅ | ✅ | dono | ✅ | — | — | — | MobileApiTests |
| Isolamento | ✅ | ✅ | ✅ | dono | ✅ | — | — | — | MobileApiTests |
| Página dedicada | ❌ | ✅ | ✅ | dono | 🟡 | Web usa formulário | P2 | Paridade futura | Mobile |

## 4. Hábitos

| Funcionalidade | Web | Mobile | Backend | Perfil | Estado | Problema | Prioridade | Correção | Teste |
|---|---|---|---|---|---|---|---|---|---|
| CRUD | ❌ | ✅ | ✅ | dono | 🟡 | Módulo só Mobile | P2 | Paridade futura | MobileApiTests |
| Registro por data | ❌ | ✅ | ✅ | dono | 🟡 | Módulo só Mobile | P2 | Paridade futura | MobileApiTests |
| Sequência/estatísticas | ❌ | ✅ | ✅ | dono | 🟡 | Cálculo local | P2 | Paridade futura | testes Mobile |
| Isolamento | ➖ | ✅ | ✅ | dono | ✅ | — | — | — | MobileApiTests |

## 5. Planejamento, Kanban e foco

| Funcionalidade | Web | Mobile | Backend | Perfil | Estado | Problema | Prioridade | Correção | Teste |
|---|---|---|---|---|---|---|---|---|---|
| Calendário | ❌ | ✅ | ✅ | dono | 🟡 | Só Mobile | P2 | Paridade futura | Mobile |
| Navegação por datas | ❌ | ✅ | ✅ | dono | 🟡 | Só Mobile | P2 | Paridade futura | testes Mobile |
| Kanban | ❌ | ✅ | ✅ | dono | 🟡 | Status via API | — | Status Web corrigido | MobileApiTests |
| Mover entre colunas | ❌ | ✅ | ✅ | dono | 🟡 | Só Mobile | P2 | Paridade futura | MobileApiTests |
| Pomodoro/foco | ❌ | ✅ | ➖ | dono | 🟡 | Estado local | — | — | testes Mobile |
| Revisão semanal | ❌ | ✅ | ➖ | dono | 🟡 | Só Mobile | P2 | Paridade futura | testes Mobile |

## 6. Perfil, preferências e dados

| Funcionalidade | Web | Mobile | Backend | Perfil | Estado | Problema | Prioridade | Correção | Teste |
|---|---|---|---|---|---|---|---|---|---|
| Ver nome/e-mail | ✅ | ✅ | ✅ | dono | ✅ | — | — | — | E2E |
| Editar nome | ✅ | ✅ | ✅ | dono | ✅ | **Ausente antes** | P1 | PUT perfil | ContaApiTests + E2E |
| Alterar senha | ✅ | ✅ | ✅ | dono | ✅ | **Ausente antes** | P1 | Perfil → Segurança | ContaApiTests + E2E |
| Tema claro/escuro | ✅ | ✅ | ➖ | dono | ✅ | Preferência local | — | — | visual |
| Notificações locais | ❌ | ✅ | ➖ | dono | 🟡 | Só nativo | P3 | — | Mobile |
| Exportar dados | ❌ | ✅ | ➖ | dono | 🟡 | Só Mobile | P3 | Paridade futura | Mobile |
| Sair | ✅ | ✅ | ✅ | dono | ✅ | — | — | — | E2E |
| Excluir conta | ❌ | ❌ | ❌ | dono | ❌ | Não existe | P2 | Produto | — |

## 7. Admin

| Funcionalidade | Web | Mobile | Backend | Perfil | Estado | Problema | Prioridade | Correção | Teste |
|---|---|---|---|---|---|---|---|---|---|
| Dashboard | ✅ | ➖ | ✅ | ADMIN+ | ✅ | — | — | — | AdminApiTests + visual |
| Listar usuários | ✅ | ➖ | ✅ | ADMIN+ | ✅ | — | — | — | AdminApiTests |
| Detalhar usuário | ✅ | ➖ | ✅ | ADMIN+ | ✅ | — | — | — | AdminApiTests |
| Criar usuário | ✅ | ➖ | ✅ | SUPER | ✅ | — | — | — | AdminApiTests |
| Bloquear/desbloquear | ✅ | ➖ | ✅ | ADMIN+ | ✅ | — | — | — | AdminApiTests |
| Revogar sessões | ✅ | ➖ | ✅ | ADMIN+ | ✅ | — | — | — | AdminApiTests |
| Redefinir senha | ✅ | ➖ | ✅ | ADMIN+ | ✅ | — | — | — | AdminApiTests |
| Alterar papel | ✅ | ➖ | ✅ | SUPER | ✅ | — | — | — | AdminApiTests |
| Auditoria | ✅ | ➖ | ✅ | ADMIN+ | ✅ | — | — | — | AdminApiTests |
| Auditoria com filtros | ✅ | ➖ | ✅ | ADMIN+ | ✅ | Bug PostgreSQL corrigido | P1 | `func32c` | AdminApiTests + produção |
| Segurança/política | ✅ | ➖ | ✅ | ADMIN/SUPER | ✅ | — | — | — | AdminApiTests |
| Tentativas com filtros | ✅ | ➖ | ✅ | ADMIN+ | ✅ | Bug PostgreSQL corrigido | P1 | `func32c` | AdminApiTests + produção |
| Firewall DEMO | ✅ | ➖ | ✅ | ADMIN+ | ✅ | Sem integração real | — | — | AdminApiTests |
| Monitoramento | ✅ | ➖ | ✅ | ADMIN+ | ✅ | — | — | — | AdminApiTests |
| Configurações | ✅ | ➖ | ✅ | SUPER | ✅ | — | — | — | AdminApiTests |
| Isolamento RBAC | ✅ | ➖ | ✅ | todos | ✅ | — | — | — | AdminApiTests |

## 8. Sincronização

| Cenário | Resultado | Evidência |
|---|---|---|
| Tarefa criada na Web aparece no Mobile | ✅ | E2E Web → Mobile |
| Tarefa concluída no Mobile reflete na Web | ✅ | E2E Mobile → Web |
| Categoria criada no Mobile aparece na Web | ✅ | E2E Mobile → Web |
| Senha alterada na Web invalida senha antiga | ✅ | E2E + API |
| Senha alterada no Mobile invalida senha antiga | ✅ | E2E + API |
| Sessão de outro usuário permanece válida | ✅ | ContaApiTests |

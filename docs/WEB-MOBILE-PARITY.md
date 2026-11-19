# Organiza Aí — Paridade Web + Mobile (3.2)

## 1. Resumo

As duas plataformas compartilham o mesmo backend e os mesmos contratos de tarefas, categorias,
hábitos e conta. A Web tem foco em tarefas e administração; o Mobile é o produto completo de
produtividade. A 3.2 aproximou as plataformas em conta/segurança, mas não criou módulos Web novos.

## 2. Comparativo

| Recurso | Web | Mobile | Observação |
|---|---|---|---|
| Cadastro/login/logout | ✅ | ✅ | Mesmo endpoint |
| Sessão e expiração | ✅ | ✅ | Token Bearer |
| Alterar senha | ✅ | ✅ | Mesmo contrato |
| Ver/editar nome | ✅ | ✅ | `GET/PUT /usuarios/perfil` |
| E-mail editável | ❌ | ❌ | Identidade de login |
| Recuperação de senha | ❌ | ❌ | Depende de SMTP |
| Dashboard/visão geral | ✅ | ✅ | Web com indicadores de tarefas |
| CRUD de tarefas | ✅ | ✅ | Sincronizado |
| Status/reabrir | ✅ | ✅ | Web ganhou na 3.2 |
| Busca/ordenação | ❌ | ✅ | Paridade pendente |
| Recorrência/checklist/notas | ❌ | ✅ | Backend preserva na Web |
| Lixeira | ❌ | ✅ | Paridade pendente |
| Categorias | ✅ (no formulário) | ✅ (tela dedicada) | Mesma API |
| Hábitos | ❌ | ✅ | Paridade pendente |
| Calendário/planejamento | ❌ | ✅ | Paridade pendente |
| Kanban | ❌ | ✅ | Status via API |
| Pomodoro/foco | ❌ | ✅ | Estado local no Mobile |
| Revisão semanal | ❌ | ✅ | Paridade pendente |
| Tema claro/escuro | ✅ | ✅ | Local por plataforma |
| Notificações locais | ❌ | ✅ | Nativo |
| Exportar dados | ❌ | ✅ | Local no Mobile |
| Admin | ✅ | ❌ | Intencional |

## 3. Sincronização validada

Cenário executado em backend local com a mesma conta:

1. Tarefa “Reabrir 3.2” criada na Web.
2. Tarefa visível no Mobile após novo login/refresh.
3. Tarefa concluída no Mobile.
4. Web recarregada exibiu `CONCLUÍDA` e os indicadores atualizados.
5. Categoria “Sincronismo 3.2” criada no Mobile.
6. Formulário de tarefa da Web exibiu a categoria no select.

Resultado: **sincronização Web ↔ Mobile funcional** via API.

## 4. Diferenças intencionais

- Admin existe apenas na Web.
- Notificações locais, foco e metas locais existem apenas no Mobile.
- A Web não tem modo offline; o Mobile também não tem offline completo.
- A sessão Web é mantida em `localStorage`; no Mobile nativo, em SecureStore.

## 5. Próximos passos de paridade

1. Lixeira/restauração na Web.
2. Busca e ordenação.
3. Página dedicada de categorias.
4. Calendário e revisão semanal.
5. Formulário avançado (recorrência, checklist, notas).
6. Hábitos e foco na Web.

# Web Premium 3.3.1 — o que foi construído

## 1. Estrutura

- `app/shell/app-shell.component.*` — shell autenticado com sidebar retrátil (desktop ≥ 980 px) e
  navegação inferior de 5 itens (mobile), além de topbar com usuário, tema, perfil e sair.
- Rotas sob o shell: `/home`, `/tarefas`, `/tarefas/nova`, `/tarefas/editar/:id`, `/planejamento`,
  `/foco`, `/habitos`, `/revisao`, `/categorias`, `/lixeira`, `/perfil`.
- `/admin` permanece fora do shell, com o layout próprio do console.

## 2. Novas telas

| Rota | Componente | Função |
|---|---|---|
| `/home` | `DashboardComponent` | Hero “Seu dia, com mais intenção”, anel de progresso, 4 indicadores, próxima melhor ação, tarefas de hoje, gráfico da semana e atalhos |
| `/planejamento` | `PlanejamentoComponent` | Seleção de tarefas dos próximos 7 dias, estimativa de tempo e plano salvo por usuário/dia |
| `/revisao` | `RevisaoComponent` | Concluídas por dia, taxa, atrasadas, sequência e desempenho por categoria |
| `/foco` | `FocoComponent` | Pomodoro 25/5, sessões por usuário/dia e vínculo opcional com tarefa |
| `/habitos` | `HabitosComponent` | CRUD de hábitos, registro diário, sequência e últimos 7 dias |
| `/categorias` | `CategoriasComponent` | CRUD completo com cor e confirmação de exclusão |
| `/lixeira` | `LixeiraComponent` | Listar, restaurar e excluir definitivamente |

`/tarefas` reutiliza a Home Premium 3.1 (topbar removida; o shell assume a navegação).

## 3. Identidade visual

- Mesmos tokens do design system (`--primary`, `--surface`, `--sidebar`, `--radius`).
- Sidebar escura com item ativo em pílula, tooltips e estado recolhido persistido.
- Mobile: barra inferior com blur, ícones e item ativo destacado.
- Tema claro/escuro preservado em todas as telas.

## 4. Evidências

- `docs/assets/3.3.1-web-dashboard.jpg` — dashboard em 1440 px com sidebar.
- `docs/assets/3.3.1-web-habitos.jpg` — hábitos com registro do dia e sequência.
- Testes: `home.premium.spec.ts` (7), `app-shell.component.spec.ts` (5),
  `productivity.spec.ts` (8) e os 19 existentes = **39**.

## 5. Acessibilidade e responsividade

- Navegação com `aria-label`, `aria-current` via `routerLinkActive`, botões com rótulo.
- Modal de exclusão com `role="alertdialog"`, `aria-modal` e Escape.
- Grids colapsam em 1 coluna < 560 px; sidebar vira barra inferior < 980 px.
- Formulários com `<label>` associado e foco visível.

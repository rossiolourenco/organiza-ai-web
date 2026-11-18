# Inventário de arquivos — Organiza Aí 3.0

Todos os caminhos são relativos aos dois repositórios. Alterações da 2.0 e da 3.0 permanecem no
mesmo conjunto local, não commitado.

## Mobile (`../organiza-ai`)

### Rotas e navegação
- `app/_layout.tsx` — providers globais, stack protegido, tema e status bar.
- `app/index.tsx`, `app/cadastro.tsx` — entrada e cadastro.
- `app/(main)/_layout.tsx` — dados, preferências, foco, lembretes e pilha.
- `app/(main)/(tabs)/_layout.tsx` — cinco abas premium.
- `app/(main)/(tabs)/{home,tarefas,planejamento,foco,perfil}.tsx`.
- `app/(main)/{novaTarefa,editarTarefa,categorias,habitos,revisao,lixeira,concentracao}.tsx`.
- Removidos: `app/*.js` e `servicos/api.js` (migração para TSX/serviços tipados; histórico no Git).

### Design system e tema
- `src/theme/ThemeProvider.tsx` — paleta clara/escura, tipografia, espaços, raios e preferência.
- `src/components/ui.tsx` — biblioteca de componentes e estados.
- `src/components/DataFeedback.tsx` — erro, retry e faixa de conexão.

### Domínio puro
- `src/domain/dates.ts` — datas civis, semanas, meses e horários.
- `src/domain/tasks.ts` — filtros, ordenação, validação, indicadores e agrupamento.
- `src/domain/focus.ts` — timer, recuperação, pausa, encerramento antecipado e estatísticas.
- `src/domain/planning.ts` — pontuação, timeline e alocação determinística.
- `src/domain/eisenhower.ts` — quadrantes e regra de sugestão.
- `src/domain/habits.ts` — normalização, sequência, consistência e validação.
- `src/domain/review.ts` — revisão semanal e recomendações por regras.

### Serviços
- `src/services/api.ts` — HTTP tipado, erros, timeout, endpoints de tarefas, categorias e hábitos.
- `src/services/sessionStorage.ts` — SecureStore nativo e memória na Web.
- `src/services/reminders.ts` — stub Web de notificações.
- `src/services/reminders.native.ts` — lembretes locais Android/iOS.
- `src/services/export.ts` — JSON por download (Web) ou compartilhamento (nativo).

### Estado
- `src/state/AuthProvider.tsx` — login, logout, expiração e sessão.
- `src/state/DataProvider.tsx` — tarefas, categorias, hábitos, conexão, mutações, toast e desfazer.
- `src/state/FocusProvider.tsx` — persistência local do foco.
- `src/state/SettingsProvider.tsx` — disponibilidade, notificações e plano aceito.

### Telas
- `src/features/auth/AuthScreen.tsx`.
- `src/features/dashboard/DashboardScreen.tsx`.
- `src/features/tasks/{TasksScreen,TaskCard,TaskFormScreen}.tsx`.
- `src/features/categories/CategoriesScreen.tsx`.
- `src/features/planning/PlanningScreen.tsx`.
- `src/features/focus/{FocusScreen,ConcentrationScreen}.tsx`.
- `src/features/habits/HabitsScreen.tsx`.
- `src/features/review/WeeklyReviewScreen.tsx`.
- `src/features/trash/TrashScreen.tsx`.
- `src/features/profile/ProfileScreen.tsx`.
- `src/features/notifications/ReminderSync.tsx`.
- `src/features/analytics/WeeklyChart.tsx`.

### Tipos, testes e configuração
- `src/types/models.ts`.
- `tests/{api,tasks,focus,focusStorage,theme,navigation,planning,eisenhower,habits,review}.test.*`.
- `jest.config.js`, `eslint.config.js`, `tsconfig.json`, `app.json`, `package.json`, `.env.example`.

### Documentação
- `docs/MOBILE-3.0.md`.
- `docs/MOBILE-3.0-AUDITORIA.md`.
- `docs/MOBILE-2.0.md`, `docs/MOBILE-2.0-AUDITORIA.md` (histórico).

## Web/backend (`organiza-ai-web`)

### Backend — modificados
- `config/CorsConfig.java` — CORS e `PasswordEncoder`.
- `controller/{TarefaController,CategoriaController,UsuarioController}.java`.
- `model/Tarefa.java` — exclusão lógica e detalhes.
- `repository/TarefaRepository.java` — consultas sem lixeira e bloqueio pessimista.
- `service/TarefaService.java` — lixeira, restauração, exclusão definitiva e recorrência.
- `resources/{application.properties,application-local.properties}`.

### Backend — novos
- `config/{AutorizacaoFilter,ErrosApi}.java`.
- `controller/HabitoController.java`.
- `dto/{CadastroRequest,DetalhesTarefa,RegistroHabitoRequest}.java`.
- `model/{DetalhesConverter,Sessao,Habito,HabitoRegistro}.java`.
- `repository/{SessaoRepository,HabitoRepository}.java`.
- `service/{SessaoService,HabitoService}.java`.
- `test/.../{MobileApiTests,PremiumApiTests,MigrationTests}.java`.

### Banco e scripts
- `database/migrations/V002__mobile_produtividade.sql` (2.0).
- `database/migrations/V003__premium_produtividade.sql` (3.0).
- `database/migrations/README.md`.
- `scripts/seed-demo.mjs` — seed idempotente com `--reset`.

### Frontend Angular
- `services/{auth.guard,auth.interceptor,usuario.service}.ts`.
- `app.config.ts`, `app.routes.ts`.
- `pages/home/home.component.ts` — autenticação e correção da recursão preexistente.
- Specs atualizados para Bearer e novos contratos.

### Documentação
- `docs/MOBILE-3.0-ENTREGA.md`.
- `docs/DEMONSTRACAO-MOBILE-3.0.md`.
- `docs/MOBILE-2.0-ENTREGA.md`, `docs/DEMONSTRACAO-MOBILE-2.0.md`,
  `docs/ARQUIVOS-MOBILE-2.0.md`, `docs/MOBILE-2.0-AUDITORIA.md` (histórico).

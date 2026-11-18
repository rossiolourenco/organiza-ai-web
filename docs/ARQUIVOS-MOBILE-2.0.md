# Inventário de arquivos da entrega

## Mobile — `../organiza-ai`

Configuração modificada: `.gitignore`, `README.md`, `app.json`, `package.json`, `package-lock.json`.

Novos arquivos de configuração: `.env.example`, `tsconfig.json`, `eslint.config.js`, `jest.config.js`.

Rotas migradas: `app/_layout.js`, `index.js`, `cadastro.js`, `home.js`, `novaTarefa.js`, `editarTarefa.js`, `categorias.js` deixam de ser arquivos de rota JS; substituídas por:

```text
app/_layout.tsx
app/index.tsx
app/cadastro.tsx
app/(main)/_layout.tsx
app/(main)/novaTarefa.tsx
app/(main)/editarTarefa.tsx
app/(main)/categorias.tsx
app/(main)/(tabs)/_layout.tsx
app/(main)/(tabs)/home.tsx
app/(main)/(tabs)/tarefas.tsx
app/(main)/(tabs)/planejamento.tsx
app/(main)/(tabs)/foco.tsx
app/(main)/(tabs)/perfil.tsx
```

`servicos/api.js` migra para os serviços tipados abaixo.

```text
src/types/models.ts
src/theme/ThemeProvider.tsx
src/services/api.ts
src/services/sessionStorage.ts
src/state/AuthProvider.tsx
src/state/DataProvider.tsx
src/state/FocusProvider.tsx
src/domain/dates.ts
src/domain/tasks.ts
src/domain/focus.ts
src/components/ui.tsx
src/components/DataFeedback.tsx
src/features/auth/AuthScreen.tsx
src/features/dashboard/DashboardScreen.tsx
src/features/tasks/TasksScreen.tsx
src/features/tasks/TaskCard.tsx
src/features/tasks/TaskFormScreen.tsx
src/features/categories/CategoriesScreen.tsx
src/features/planning/PlanningScreen.tsx
src/features/focus/FocusScreen.tsx
src/features/profile/ProfileScreen.tsx
src/features/analytics/WeeklyChart.tsx
tests/setup.ts
tests/tasks.test.ts
tests/api.test.ts
tests/focus.test.ts
tests/focusStorage.test.tsx
tests/navigation.test.tsx
tests/theme.test.tsx
docs/MOBILE-2.0.md
```

## Backend — `backend/src/main/java/br/com/organizaai/backend`

Modificados:

```text
config/CorsConfig.java
controller/CategoriaController.java
controller/UsuarioController.java
model/Tarefa.java
repository/TarefaRepository.java
service/TarefaService.java
```

Criados:

```text
config/AutorizacaoFilter.java
config/ErrosApi.java
dto/CadastroRequest.java
dto/DetalhesTarefa.java
model/DetalhesConverter.java
model/Sessao.java
repository/SessaoRepository.java
service/SessaoService.java
```

Outros:

```text
backend/src/main/resources/application-local.properties           modificado: CORS da prévia local
backend/src/test/java/br/com/organizaai/backend/MobileApiTests.java novo
backend/src/test/java/br/com/organizaai/backend/MigrationTests.java novo
database/migrations/V002__mobile_produtividade.sql                 novo
database/migrations/README.md                                     novo
```

`application.properties` de produção e `pom.xml` foram preservados.

## Web — `frontend/src/app`

```text
app.config.ts                                      interceptor HTTP
app.routes.ts                                      guard de sessão
services/usuario.service.ts                        separar token da identidade no login
services/auth.interceptor.ts                       novo
services/auth.guard.ts                             novo
pages/home/home.component.ts                       fim da recursão; limpar token no logout
app.component.spec.ts                              corrigir expectativa do template antigo
pages/login/login.component.spec.ts                 providers de teste
pages/cadastro/cadastro.component.spec.ts            providers de teste
pages/home/home.component.spec.ts                   providers e regressão dos filtros
pages/tarefa-form/tarefa-form.component.spec.ts       providers e sessão de teste
services/tarefa.service.spec.ts                     providers de teste
services/usuario.service.spec.ts                    teste de autenticação e contrato
```

## Documentação e demonstração — Web

```text
README.md
docs/MOBILE-2.0-AUDITORIA.md
docs/MOBILE-2.0-ENTREGA.md
docs/ARQUIVOS-MOBILE-2.0.md
docs/DEMONSTRACAO-MOBILE-2.0.md
scripts/seed-demo.mjs
```

`dist/`, `backend/target/`, caches Expo e `node_modules/` são resultados locais ignorados, não arquivos de entrega versionáveis.

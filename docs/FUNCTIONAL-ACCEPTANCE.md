# Functional Acceptance — Organiza Aí 3.3 RC

> Somente o que foi **executado e observado**. Testes automatizados, teste de integração com
> PostgreSQL real e sessão de navegador headless no build do RC.

- **Build:** `3.3.0-rc.1`
- **Data:** 2026-11-20
- **Ambiente da homologação Web:** build de produção em `http://localhost:4200`,
  API em `http://localhost:8080` (perfil `local`, H2)
- **Ambiente da homologação de API:** PostgreSQL 17.7 em container isolado

## 1. Resultado dos testes automatizados

| Suíte | Resultado |
|---|---|
| Angular (Karma/ChromeHeadless) | **19/19** |
| Admin/RBAC (`adminGuard.spec.ts`) | **4/4** |
| Backend H2 (6 classes) | **34/34** |
| Backend PostgreSQL real (Testcontainers) | **4/4** |
| **Backend total** | **38/38** |
| Mobile Jest (11 suítes) | **50/50** |
| Mobile typecheck | OK |
| Mobile lint | OK |
| Expo Doctor | **18/18** |
| Export Web/iOS/Android | OK |

## 2. Web — fluxos verificados em navegador real

| Fluxo | Ação executada | Resultado observado | Status |
|---|---|---|---|
| Cadastro | Preencher nome/e-mail/senha em `/cadastro` e enviar | Redirecionou para `/login?cadastro=1` com “Conta criada com sucesso” | ✅ |
| Login | Credenciais válidas em `/login` | Redirecionou para `/home` | ✅ |
| Dashboard | Abrir `/home` | “PAINEL PESSOAL”, saudação, contadores e filtros renderizados | ✅ |
| Criar tarefa | `/tarefas/nova`, título/descrição/prioridade ALTA/prazo, enviar | `POST /api/tarefas` 200, retorno a `/home`, item listado como `PENDENTE`/`ALTA`/`2026-12-01` | ✅ |
| Perfil | Abrir `/perfil` | Nome, e-mail, papel, status, “membro desde”, sessão e formulários | ✅ |
| Editar nome | Alterar nome e salvar | Nome atualizado e persistido | ✅ |
| Alterar senha | Senha atual + nova + confirmação | Redirecionou para `/login?senha=alterada`, token removido do navegador | ✅ |
| Revogação de sessão | Reutilizar senha antiga / token anterior | Senha antiga rejeitada; sessão anterior inválida (401) | ✅ |
| Logout | Botão “Sair” | Voltou para `/login`, `localStorage` limpo | ✅ |
| Proteção de rota | Abrir `/admin` como `USER` | Redirecionou para `/login?admin=1` com aviso administrativo | ✅ |
| Erros de formulário | Validação de campos e credenciais | Mensagens em português, sem stack trace | ✅ |
| Console do navegador | Navegação pelas telas | **0 erros** | ✅ |

## 3. Admin — fluxos verificados em navegador real

| Página | Marcador observado | Console | Status |
|---|---|---|---|
| `/admin/dashboard` | “Security Console”, “Dashboard administrativo” | 0 erros | ✅ |
| `/admin/usuarios` | Lista com 2 contas, papel, status, último login, sessões e ações | 0 erros | ✅ |
| `/admin/seguranca` | “Política em vigor”, “Eventos recentes” | 0 erros | ✅ |
| `/admin/firewall` | “Histórico simulado”, “Validação de conflitos”, “Firewall real” | 0 erros | ✅ |
| `/admin/monitoramento` | “CPU e carga”, “Memória”, “Disco” | 0 erros | ✅ |
| `/admin/auditoria` | “Trilha administrativa” com eventos reais (`LOGIN`, `LOGIN_ADMIN`, `LOGIN_FALHA`) | 0 erros | ✅ |
| `/admin/configuracoes` | “Política de autenticação”, “Sessão administrativa”, “Ambiente e limites” | 0 erros | ✅ |

RBAC observado: `ADMIN` visualiza contas, mas ações de papel são restritas a `SUPER_ADMIN`; a
conta `USER` não entra no console.

## 4. API — fluxos verificados contra PostgreSQL 17 real

| Cenário | Verificação | Status |
|---|---|---|
| Cadastro | 200, sem `senha`/`bloqueadoAte`/`motivoBloqueio`/`ultimoLoginEm`, papel `USER` | ✅ |
| Login | 200 com token opaco | ✅ |
| Listagem `/api/usuarios` | 401 sem token; com token retorna apenas o próprio usuário | ✅ |
| Perfil | `GET`/`PUT` com nome persistido | ✅ |
| Senha | 204; sessões revogadas; senha antiga 401; nova senha 200 | ✅ |
| Tarefas | CRUD, filtro por prioridade, concluir, lixeira, restaurar, exclusão definitiva | ✅ |
| Categorias | Criar e associar a tarefa | ✅ |
| Validação | Prioridade inválida → 400 | ✅ |
| IDOR | `usuarioId` de terceiro → 403; recurso de terceiro → 404 | ✅ |
| RBAC | `/api/admin/**` sem token 401; com `USER` 403 | ✅ |
| Headers | `nosniff`, `DENY`, `no-store`, CSP presentes | ✅ |
| Integridade referencial | Categoria removida → `categoria_id` nulo; usuário removido → tarefas/hábitos removidos | ✅ |
| Migrations | 14 tabelas presentes; singletons de política e firewall criados | ✅ |

## 5. Mobile — verificado por automação

| Item | Verificação | Status |
|---|---|---|
| Login/cadastro | Testes Jest de `AuthProvider`/validação | ✅ |
| Persistência de sessão | `SecureStore` coberto por teste | ✅ |
| Tarefas/planejamento/foco | Testes de `tasks`, `planning`, `focus`, `eisenhower` | ✅ |
| Configuração de API | Teste de origem HTTPS e montagem de URL | ✅ |
| Tema e revisão | Testes de `config` e `review` | ✅ |
| Export nativo | Bundles Web/iOS/Android gerados e verificados | ✅ |

**Não executado:** teste em aparelho físico iOS/Android e build assinado (requer credenciais e
hardware; fora do escopo desta rodada). Ver `WEB-MOBILE-PARITY.md` e `RELEASE-HANDOFF.md`.

## 6. Regressões

Nenhuma regressão funcional observada. As únicas mudanças de contrato são restritivas
(remoção de campos internos do próprio usuário), sem impacto em Web ou Mobile — ambos não
consomem esses campos.

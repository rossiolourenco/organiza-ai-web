# Organiza Aí 3.3.1 — Release Candidate

- **Versão:** `3.3.1-rc.2` (`VERSION`)
- **Backend:** `3.3.1-RC2` · **Web/Admin:** `3.3.1-rc.2`
- **Mobile:** `3.3.1-rc.2` (app/loja `3.3.1`, build 34)
- **Base publicada:** 3.3.0-rc.1 (`48154ee`)
- **Branches:** `feature/3.3.1-functional-parity-web` · `feature/3.3.1-functional-parity-mobile`
- **Status:** RC preparado; **sem merge e sem deploy**

## 1. Escopo

Unificação funcional Web + Mobile: a Web passa a oferecer todas as capacidades de negócio já
disponíveis no backend e no Mobile, com a mesma persistência e as mesmas regras. O Mobile recebe
revalidação ao retomar o app.

## 2. Entregas

### Web
- Shell com sidebar retrátil e navegação inferior.
- Dashboard Premium (hero, anel de progresso, indicadores, próxima melhor ação, semana).
- Planejamento diário, Revisão, Foco (Pomodoro), Hábitos, Categorias e Lixeira.
- Tarefas mantidas sobre o redesign 3.1.

### Mobile
- `RevalidateOnActive` (retomada) com throttle e teste.
- Versão 3.3.1.

### Backend
- 5 endpoints novos em `/api/produtividade/*` (planejamento e foco) com controle otimista por versão.
- Migration aditiva `V005__planejamento_foco.sql` (não aplicada em produção).
- Nenhum contrato existente alterado.

## 3. Matriz de paridade

Ver `FUNCTIONAL-PARITY-3.3.1.md`: 30 recursos `PARITY_OK`, 0 `WEB_MISSING`, 0 `MOBILE_MISSING`,
0 `BACKEND_GAP`. Recursos administrativos são `ROLE_RESTRICTED` (Web) por decisão de produto.

## 4. Testes

| Suíte | Resultado |
|---|---|
| Angular | **39/39** |
| Backend H2 | **34/34** |
| Backend PostgreSQL 17 (Testcontainers) | **4/4** |
| Mobile Jest | **53/53** |
| Typecheck / Lint / Expo Doctor | OK / OK / 18/18 |
| Export Web/iOS/Android | OK |
| E2E em navegador real (Web) | 8 rotas, CRUD, lixeira, hábitos, categorias — 0 erros de console |

## 5. Riscos e limitações

- Cronômetro em execução é estado transitório local; **sessões concluídas e plano diário são
  centralizados** no backend.
- Sessão concluída offline é enviada na próxima execução do app.
- Notificações continuam exclusivas do Mobile (sem backend de notificações).
- Admin continua exclusivo da Web.
- Homologação manual em dois dispositivos pendente (`CROSS-PLATFORM-TESTS.md` §6).
- Sem build assinado/teste físico do Mobile.

## 6. Artefatos

Gerados por `scripts/build-release.sh` em `dist-release/3.3.1-rc.1/` (fora do Git):

| Artefato | SHA-256 |
|---|---|
| `organiza-ai-web-3.3.1-rc.2.tar.gz` | `a5d6ceff0bffbdd7777386458174f704fa82c1f4ba42febd0b4ce1d830394f70` |
| `organiza-ai-backend-3.3.1-rc.2.jar` | `a0af67b462305de672cd23562e2e8e4d794a74f6d979be92a7bb715d9edaa8c3` |
| `organiza-ai-database-3.3.1-rc.2.tar.gz` | `1457801ec481df42e134f098532e33a8eaef81ae57ca2d126a87e9a0e1a4c1c3` |

Mobile: `organiza-ai-mobile-3.3.1-rc.2.tar.gz` =
`6f4a9316d6e95a2377e74bc7f4d28d7568b1d23fa92ebaf41170854c765dc99e`.

O JAR é construído com `mvn clean package` e validado por `Implementation-Version: 3.3.1-RC2`.

## 7. Publicação

Seguir `docs/PRODUCTION-DEPLOYMENT.md` com o commit do merge; banco sem migration; rollback em
`ROLLBACK-3.3.1.md`.

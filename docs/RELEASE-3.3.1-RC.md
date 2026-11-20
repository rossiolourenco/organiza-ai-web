# Organiza Aí 3.3.1 — Release Candidate

- **Versão:** `3.3.1-rc.1` (`VERSION`)
- **Backend:** `3.3.1-RC1` · **Web/Admin:** `3.3.1-rc.1`
- **Mobile:** `3.3.1-rc.1` (app/loja `3.3.1`, build 34)
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
- Nenhum endpoint novo, nenhum contrato alterado, nenhuma migration.

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

- Planejamento e foco são preferências **por dispositivo** (mesma decisão do Mobile); não sincronizam
  entre aparelhos.
- Notificações continuam exclusivas do Mobile.
- Admin continua exclusivo da Web.
- Homologação manual em dois dispositivos pendente (`CROSS-PLATFORM-TESTS.md` §6).
- Sem build assinado/teste físico do Mobile.

## 6. Artefatos

Gerados por `scripts/build-release.sh` em `dist-release/3.3.1-rc.1/` (fora do Git):
web, backend (`3.3.1-RC1`), database e `SHA256SUMS`. Checksums conferidos no servidor antes do deploy.

## 7. Publicação

Seguir `docs/PRODUCTION-DEPLOYMENT.md` com o commit do merge; banco sem migration; rollback em
`ROLLBACK-3.3.1.md`.

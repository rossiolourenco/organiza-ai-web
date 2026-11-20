# Testes cross-platform — 3.3.1

- **Data:** 2026-11-20
- **Ambiente:** backend local (H2) + UI Web de produção local + app Mobile no simulador iOS 18.1
- **Conta de teste:** `paridade.*@organizaai.invalid` (criada e usada apenas no ambiente local)

## 1. Cenários executados

| # | Cenário | Como foi executado | Resultado |
|---|---|---|---|
| 1 | Criar tarefa em um cliente e ver na Web | `POST /api/tarefas` (2º cliente) → reload de `/tarefas` | ✅ apareceu |
| 2 | Editar tarefa em outro cliente e ver na Web | `PUT /api/tarefas/{id}` → reload | ✅ título atualizado |
| 3 | Excluir na Web e ver na lixeira | UI Web (modal) → `/lixeira` | ✅ listada |
| 4 | Restaurar na Web e ver na lista | `/lixeira` → Restaurar → `/tarefas` | ✅ voltou |
| 5 | Criar tarefa na Web e ver na API | UI `/tarefas/nova` → `GET /api/tarefas` | ✅ presente |
| 6 | Criar categoria na Web e ver na API | UI `/categorias` → `GET /api/categorias` | ✅ presente |
| 7 | Criar hábito na Web e ver na API | UI `/habitos` → `GET /api/habitos` | ✅ presente |
| 8 | Criar hábito em outro cliente e ver na Web | `POST /api/habitos` → `/habitos` | ✅ apareceu |
| 9 | Marcar hábito hoje | UI → `POST /api/habitos/{id}/registros` | ✅ badge 1 dia e botão “Desmarcar” |
| 10 | Dashboard refletir dados de outro cliente | `POST /api/tarefas` → `/home` | ✅ “Próxima melhor ação” e “Hoje” |
| 11 | Retomada do app Mobile | `AppState -> active` com throttle | ✅ `RevalidateOnActive` + teste unitário |
| 12 | Expiração de sessão | token inválido → 401 | ✅ clientes limpam sessão (backend + `Api`) |

## 2. Estado observado na API após os cenários

```
tarefas=2  Lixeira E2E | Sync editado pelo Mobile
categorias=1  Paridade
habitos=2  Beber água(1 reg) | Sync hábito(0 reg)
```

Os mesmos registros foram vistos na UI Web (tarefas, hábitos e categorias) sem divergência.

## 3. Consistência de regras

- Status `PENDENTE` = “em aberto” nas duas plataformas.
- Datas civis `AAAA-MM-DD` sem deslocamento de UTC (`dayKey`).
- Filtros de status/prioridade são locais nos dois clientes, sobre a mesma lista da API.
- Planejamento e foco são **preferências do dispositivo** (localStorage no Web, AsyncStorage no Mobile),
  indexadas por usuário + data; não são dados de negócio compartilhados.
- Nenhuma regra de negócio foi duplicada no cliente: CRUD e persistência ficam no backend.

## 4. Sincronização e cache

- Web: cada rota recarrega ao entrar; operações chamam a API e atualizam a lista.
- Mobile: `refresh()` no login, em cada `mutate` (CRUD) e ao retomar o app (`RevalidateOnActive`,
  janela de 10 s para evitar loops).
- Não há cache persistente de tarefas no Web/Mobile; a fonte é sempre a API.
- Consistência é **forte por requisição**; não há realtime/push entre dispositivos.

## 5. Limitação declarada

- O teste visual completo em **dois dispositivos simultâneos** (UI Mobile ↔ UI Web) não foi
  automatizado nesta rodada. A consistência foi comprovada no contrato/estado (2º cliente via API ↔
  UI Web) e a UI Mobile por Jest + reload no simulador.
- Antes da publicação, executar o checklist manual de 10 minutos em dois aparelhos (seção 6).

## 6. Checklist manual pendente (pré-deploy)

1. Criar tarefa na Web e abrir Tarefas no app.
2. Editar no app e recarregar a Web.
3. Concluir na Web e abrir o app.
4. Excluir no app e conferir a lixeira na Web.
5. Restaurar na Web e conferir no app.
6. Criar hábito na Web e marcar no app.
7. Salvar plano no app e abrir Planejamento na Web (preferências independentes — conferir aviso).
8. Alterar nome no app e recarregar a Web.
9. Revogar sessão pelo Admin e confirmar que o app volta ao login.

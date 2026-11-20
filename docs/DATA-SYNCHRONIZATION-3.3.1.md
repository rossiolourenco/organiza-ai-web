# Sincronização de dados — 3.3.1

## 1. Persistência por recurso

| Recurso | Criação | Armazenamento | Recuperação | Usuário | Outro dispositivo | Local | Endpoint |
|---|---|---|---|---|---|---|---|
| Tarefas | Web/Mobile | PostgreSQL | API | sim | sim | — | `/api/tarefas` |
| Categorias | Web/Mobile | PostgreSQL | API | sim | sim | — | `/api/categorias` |
| Hábitos | Web/Mobile | PostgreSQL | API | sim | sim | — | `/api/habitos` |
| Registros de hábito | Web/Mobile | PostgreSQL | API | sim | sim | — | `/api/habitos/{id}/registros` |
| Lixeira/restauração | Web/Mobile | PostgreSQL | API | sim | sim | — | `/api/tarefas/lixeira` etc. |
| Perfil/senha | Web/Mobile | PostgreSQL | API | sim | sim | — | `/api/usuarios` |
| **Planejamento diário** | Web/Mobile | PostgreSQL (`planejamentos`) | API | sim | **sim** | — | `/api/produtividade/planejamento` |
| **Sessões de foco** | Web/Mobile | PostgreSQL (`sessoes_foco`) | API | sim | **sim** | — | `/api/produtividade/foco` |
| Revisão semanal | derivado | derivado de tarefas | API | sim | sim | — | — |
| Cronômetro em execução | Web/Mobile | estado transitório | — | sim | não | sim | — |
| Preferências (tema, metas) | Web/Mobile | dispositivo | — | sim | não | sim | — |
| Descarte de sugestão | Mobile | dispositivo | — | sim | não | sim | — |

> A revisão semanal é **derivada** das tarefas (mesma regra nas duas plataformas), por isso não
> precisa de persistência própria. O cronômetro em andamento é estado transitório e permanece local.

## 2. Estratégia de cache e revalidação

- **Web:** cada rota busca da API ao entrar; operações recarregam a lista. Não há cache persistente.
- **Mobile:** `refresh()` no login, em cada `mutate` e ao retomar o app (`RevalidateOnActive`, 10 s).
- Sem polling contínuo; sem realtime. Consistência forte por requisição.
- Invalidação por recurso: tarefas/categorias/hábitos atualizados em bloco no `refresh`; foco e
  planejamento são lidos sob demanda ao abrir a tela.

## 3. Concorrência e conflito (planejamento)

- O `planejamentos` usa **controle otimista por versão** (`versao` + `UNIQUE (usuario_id, data)`).
- Uma atualização com versão antiga recebe **HTTP 409** e o cliente recarrega o plano antes de editar.
- Nunca há sobrescrita silenciosa de uma edição concorrente de outro dispositivo.
- As sessões de foco são **somente adição** (idempotentes por natureza), sem conflito de escrita.

## 4. Sincronização

- Planejamento: Web e Mobile gravam no backend; ao abrir, cada cliente lê o plano do dia e o aplica.
- Foco: ao concluir uma sessão, o cliente a envia ao backend; ao abrir, mescla as sessões remotas do dia
  (deduplicadas por tarefa + minutos + minuto de conclusão) com as locais.
- Se o dispositivo estiver offline ao concluir uma sessão, ela permanece local e é enviada na próxima
  inicialização (a reenvio é disparado por mudança em `state.sessions`).

## 5. Notificações

- **Não há backend de notificações.** As notificações existentes no Mobile são locais
  (`expo-notifications`), específicas da plataforma, e não representam evento de negócio persistente.
- A Web não reproduz notificação nativa de iOS/Android (decisão de plataforma).
- Nenhuma infraestrutura de push foi criada; se o produto exigir lembretes centralizados no futuro,
  isso deve ser uma entrega separada com endpoint e permissões próprias.

## 6. Limitações

- Preferências (tema, metas, descarte de sugestão) continuam por dispositivo.
- Sessão de foco concluída offline só chega ao servidor na próxima execução do app.
- Teste visual em dois dispositivos simultâneos é PENDENTE (checklist em `CROSS-PLATFORM-TESTS.md`).

## 7. Testes

- `PostgresIntegrationTests` (planejamento/foco: criar, ler, 409, isolamento, IDOR).
- `ProdutividadeApiTests` (H2).
- `MigrationTests` (V005).
- Web: `productivity.spec.ts` (regras puras); componentes validados em navegador real.
- Mobile: `tests/focus-sync.test.ts` (mescla de sessões); `tests/revalidate.test.ts`.

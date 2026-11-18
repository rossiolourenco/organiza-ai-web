# Entrega — Organiza Aí 3.0 Premium Experience

Data: 18/11/2026. Trabalho nos **dois repositórios existentes**, sem commit, push ou merge.
Este documento substitui o relatório da 2.0 como referência da versão atual.

## 1. Resumo executivo

O Mobile foi reformulado sobre a base 2.0: design system próprio, cinco abas e sete telas de pilha,
planejamento determinístico, matriz de Eisenhower, hábitos persistidos no backend, revisão semanal,
modo concentração, lixeira reversível, notificações locais, exportação e modo exposição isolado.
O backend Spring Boot recebeu endpoints de lixeira e hábitos, campos explícitos de classificação e
duração estimada, mantendo o contrato anterior. A migração V003 é aditiva.

Foram executados **45 testes Mobile**, **19 testes backend** e **11 testes Angular**, além de
typecheck, lint, Expo Doctor, checagem de dependências, export dos três bundles e smoke real no
navegador contra o backend local.

## 2. Arquitetura auditada e final

Antes: 5 abas funcionais, porém sem hábitos, revisão, Eisenhower, planejamento, concentração,
lixeira, notificações, conexão ou exposição. O backend tinha CRUD, sessão Bearer, recorrência e
migração V002 não aplicada.

Depois: `Expo Router → providers → serviços HTTP → Spring Boot → PostgreSQL/H2`, com regras puras em
`src/domain` e design system único. Detalhes em [MOBILE-3.0.md](../../organiza-ai/docs/MOBILE-3.0.md).

## 3. Inventário de funcionalidades

- Dashboard com progresso diário, quatro métricas, prioridade sugerida, agenda, hábitos, semana,
  metas, próximos sete dias e atalhos.
- Tarefas com busca, filtros combinados, agrupamento, ordenação, checklist e ações rápidas.
- Cadastro com divulgação progressiva, estimativa, etiquetas, recorrência e Eisenhower.
- Calendário mês/semana/dia com atrasos, contagens e criação por data.
- Kanban com três colunas e persistência por menu acessível.
- Pomodoro com anel, pausa/retomada por timestamps, encerramento com tempo real, histórico e metas.
- Modo concentração com tarefa, objetivo, checklist, cronômetro e conclusão.
- Hábitos com CRUD, meta, registros idempotentes, sequência, consistência e arquivamento.
- Planejamento diário com pontuação publicada, timeline, aceite/ignorar e overflow.
- Matriz de Eisenhower com quatro quadrantes, filtros por quadrante e sugestão confirmada pelo usuário.
- Revisão semanal com comparação, foco, categorias, hábitos, metas e recomendações por regras.
- Perfil com tema, notificações, metas, exportação, acessibilidade e modo exposição.
- Lixeira com restauração, exclusão definitiva e “Desfazer” após excluir.

## 4. Alterações de layout

Paleta premium clara/escura, escala tipográfica, cards compactos, hierarquia por seções, anel de
progresso, cabeçalho único, abas com ícones, FAB, chips, sheets, skeletons, estados vazios e toast
com ação. Nenhuma tela declara cor literal. Capturas de dashboard e tarefas foram conferidas nos
dois temas.

## 5. Componentes criados

`AppHeader`, `PremiumCard`, `MetricCard`, `ProgressRing`, `Progress`, `StatusBadge`, `PriorityBadge`,
`FilterChip`, `Segmented`, `QuickAction`, `FloatingActionButton`, `SearchBar`, `EmptyState`,
`ErrorState`, `LoadingSkeleton`, `AppBottomSheet`, `ConfirmDialog`, `DateSelector`, `CalendarGrid`,
`ThemeToggle`, `ToggleRow`, `SectionHeader`, `SmartSuggestionCard` e `Toast`.

## 6. Funcionalidades implementadas (detalhe)

- Planejamento: pesos +50/+30/+15/+8, prioridade +24/+12/+4, importante +10, urgente +12,
  favorita +5, fixada +4, bônus curto +6; duração padrão 30 min; ordem determinística; alocação gulosa.
- Eisenhower: atributos explícitos anuláveis; sugestão documentada; nunca automática.
- Hábitos: `UNIQUE (habito_id, data)`; sequência diária/semanal; datas civis; normalização de duplicados.
- Revisão: taxa = concluídas com prazo ÷ prazos da semana; comparação com semana anterior completa;
  seis regras de recomendação.
- Concentração: integrada ao mesmo timer e histórico do Pomodoro.
- Lixeira: exclusão lógica, restauração, definitiva apenas a partir da lixeira.

## 7. Mudanças no backend

- `Tarefa`: `excluida_em`; consultas filtram lixeira; `buscarParaAtualizar` ignora excluídas.
- `TarefaService`: exclusão lógica, restauração e exclusão definitiva.
- `TarefaController`: `GET /lixeira`, `POST /{id}/restaurar`, `DELETE /{id}/definitivo`.
- `DetalhesTarefa`: `importante`, `urgente`, `duracaoEstimada`; booleanos agora anuláveis para
  preservar compatibilidade de clientes parciais; validação de duração 1–1440.
- Novos: `Habito`, `HabitoRegistro`, `HabitoRepository`, `HabitoService`, `HabitoController`.
- Testes: `PremiumApiTests` (7 casos) cobrindo lixeira, isolamento, hábitos e detalhes estendidos.
- Web Angular não precisou de alteração de contrato; os campos novos são aditivos.

## 8. Migrações

`database/migrations/V003__premium_produtividade.sql`: `tarefas.excluida_em`, tabelas `habitos` e
`habito_registros`, índices e unicidade. Aditiva, com `IF NOT EXISTS`. **Não aplicada em produção.**
`MigrationTests` executa schema + V002 + V003 em H2 modo PostgreSQL e valida preservação.

## 9. Testes executados

| Verificação | Resultado |
|---|---|
| Mobile `npm test` | **45 testes, 10 suítes, todas aprovadas** |
| Mobile `npx tsc --noEmit` | **Sem erros** |
| Mobile `npx eslint app src tests` | **Sem erros ou avisos** |
| Backend `./mvnw package -Dspring.profiles.active=local` | **19 testes, BUILD SUCCESS, JAR gerado** |
| Angular `npm test -- --watch=false --browsers=ChromeHeadless` | **11 testes aprovados** |
| Angular `npm run build` | **Passou**; aviso preexistente de orçamento CSS (6,25 kB / 4 kB) |
| `npx expo-doctor` | **18/18** |
| `npx expo install --check` | **Dependências compatíveis** |
| `npm run export` | **Web, Android/Hermes e iOS/Hermes exportados** |
| `npm audit` | **22 alertas: 6 altos, 16 moderados, 0 críticos** |
| Migração V003 | Executada em H2 modo PostgreSQL, com hábitos e preservação |

## 10. Resultados verificáveis

- Bundles: Web **1,2 MB**, Android **3,1 MB**, iOS **3,1 MB**.
- Smoke com backend real em `localhost:8080` e Expo Web em `localhost:8081`:
  login DEMO; dashboard com dados reais; criação/leitura de hábitos; marcar hábito hoje;
  classificação Eisenhower persistida; checklist do modo concentração persistido (100%);
  Pomodoro iniciado/pausado/descartado; plano aceito com timeline 08:00–09:00;
  Kanban e matriz renderizados; exclusão com toast e “Desfazer” restaurando a tarefa;
  lixeira restaurando item; revisão semanal com recomendações reais; tema claro e escuro.
- Verificação via API após o smoke: `importante=true`, `urgente=true` e checklist concluído
  persistidos no backend.
- Seed `--reset` executado duas vezes: contagens permaneceram 8 tarefas, 3 categorias e 3 hábitos.

## 11. Pendências e limitações

- Ensaiar e aplicar V002/V003 em PostgreSQL de homologação antes do banco compartilhado.
- Validar Android/iOS físicos: notificações, permissões, teclado, leitor de tela, fontes grandes,
  SecureStore, suspensão/retorno e processo encerrado.
- Gerar build nativo assinado se a exposição exigir instalação fora do Expo Go.
- Sem offline completo, fila de sincronização, resolução de conflitos ou drag-and-drop.
- Dependências entre tarefas não existem no contrato atual.
- Foco, metas e preferências são locais por conta/aparelho; não sincronizam.
- Listas são paginadas, mas ainda não usam virtualização (`FlatList`); evolução indicada para volume alto.
- Web não recebe notificações locais; comportamento documentado na UI.

## 12. Comandos de execução

```bash
# Backend isolado
cd organiza-ai-web/backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=local

# Cenário DEMO (somente local)
cd organiza-ai-web
export DEMO_PASSWORD='defina-uma-senha-forte'
export DEMO_EMAIL='demo@exposicao.invalid'
node scripts/seed-demo.mjs --reset

# Mobile
cd ../organiza-ai
npm ci
npm start        # ou npm run web

# Web Angular
cd ../organiza-ai-web/frontend
npm ci
npm start
```

## 13. Roteiro de exposição

Ver [DEMONSTRACAO-MOBILE-3.0.md](DEMONSTRACAO-MOBILE-3.0.md): roteiro em 15 passos, com restauração
idempotente, cenário de lixeira e classificação Eisenhower.

## 14. Estado do Git

- Mobile: branch `main`, base `bf9ba5c`. Alterações locais da 2.0 e 3.0, não commitadas.
- Web/backend: branch `main`, base `c5302ee`. Alterações locais da 2.0 e 3.0, não commitadas.
- Nenhum commit, push, merge, reset ou operação em banco de produção.
- `git diff --check` executado nos dois repositórios.

## 15. Riscos

- Implantar backend/Web/Mobile juntos: a API exige Bearer e a listagem normal ignora a lixeira.
- Aplicar V003 antes de subir o backend com `ddl-auto=validate`; caso contrário, a inicialização falha.
- Notificações e background variam por fabricante/versão; não foram validadas em aparelho.
- Auditoria npm tem 22 alertas transitivos de desenvolvimento/CLI; não foi usado `--force`.
- HTTP local é aceitável na exposição, mas produção exige HTTPS.
- Relógio manual do aparelho afeta Pomodoro e lembretes.

# Entrega técnica — Organiza Aí Mobile 2.0

> Documento histórico. A versão atual é a 3.0: [MOBILE-3.0-ENTREGA.md](MOBILE-3.0-ENTREGA.md).

Data: 17/11/2026. Trabalho realizado nos **dois repositórios existentes**, sem commit/push.

## 1. Resumo executivo

O Mobile foi evoluído de sete telas JavaScript para uma aplicação Expo Router/TypeScript com cinco áreas, componentes compartilhados, temas, planejamento, tarefas enriquecidas e foco local. O backend Spring Boot existente recebeu sessão autenticada, validações, persistência adicional e testes de integração. O cliente Angular foi adaptado para autenticação e teve corrigida uma recursão no carregamento de tarefas.

O ambiente local demonstra dados reais da conta DEMO persistidos pela API. Não foi feita implantação no Supabase ou validação física Android/iOS.

## 2. Arquitetura final

`React Native/Expo → serviços HTTP tipados → Spring Boot/JPA → PostgreSQL`.

No Mobile, AuthProvider gerencia identidade, DataProvider coordena listas/mutações e FocusProvider grava foco/metas por servidor/usuário. Regras puras em `src/domain`; UI em `src/components` e `src/features`. Tokens de tema centralizados. Router preservado, agora com Stack protegido e Tabs.

No backend, login emite token aleatório com validade de sete dias; só o hash é armazenado. Filtro verifica token e rejeita usuarioId diferente do autenticado. Serviços ainda verificam propriedade de cada tarefa/categoria. DTOs de cadastro/detalhes, converter JSON e transações compõem a extensão.

## 3. Implementado

- Login/cadastro, sessão SecureStore Android/iOS, tratamento de expiração e logout.
- CRUD e conclusão de tarefas, três status, prioridades, categorias, prazo/horário.
- Subtarefas/checklist, notas, etiquetas, favoritos, fixação, duplicação, ordem numérica, busca e histórico de conclusões.
- Recorrência diária/semanal/mensal, geração ao concluir e proteção contra geração concorrente/repetida.
- CRUD de categorias com cor e contagem; exclusão transacional preserva tarefas.
- Filtros inclusivos e combinados por status, prioridade, categoria, prazo, atraso, favoritas e busca.
- Dashboard, gráficos locais, distribuições, metas e sequência produtiva com fórmulas documentadas.
- Calendário mês/semana/dia, criação na data, Kanban com movimentação persistida por menu acessível.
- Pomodoro configurável, pausar/retomar, histórico, estatísticas e recuperação por timestamps.
- Claro/escuro/sistema, toasts de operações, carregamento, estados vazios/erro, confirmação destrutiva e bloqueio de operações simultâneas.

Não há dados demonstrativos embutidos nas telas. O seed é explícito, cria uma nova conta DEMO e só aceita backend local.

## 4. Arquivos

Inventário completo por responsabilidade: [ARQUIVOS-MOBILE-2.0.md](ARQUIVOS-MOBILE-2.0.md).

Principais pontos de entrada:

- Mobile: `../organiza-ai/app/_layout.tsx`, `app/(main)/`, `src/features/`, `src/state/`, `src/domain/`.
- Backend: `AutorizacaoFilter`, `SessaoService`, `TarefaService`, `CategoriaController`, `UsuarioController`, `DetalhesTarefa`.
- Web: `auth.interceptor.ts`, `auth.guard.ts`, configuração HTTP, serviço de usuário e Home.

## 5. Migração e compatibilidade

`database/migrations/V002__mobile_produtividade.sql` adiciona sessões, detalhes e histórico; não apaga registros. **Não aplicada em produção.**

Procedimento, reversão sem perda, recorrência e fuso: [migrations/README.md](../database/migrations/README.md).

Campos básicos e endpoints do Web continuam disponíveis. PUT antigo preserva detalhes adicionais. **A autorização agora é obrigatória:** implantar o Web adaptado e o backend juntos; fazer novo login. Clientes antigos sem Bearer receberão 401. Isso corrige a confiança indevida em usuarioId.

## 6. Execução

Backend local isolado:

```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

Mobile:

```bash
cd ../organiza-ai
npm ci
npm start
# prévia de navegador:
npm run web
```

Web Angular:

```bash
cd frontend
npm ci
npm start
```

No telefone, usar IP LAN do computador. URLs HTTPS e portas explícitas também são aceitas. `.env.example` contém somente configuração pública opcional.

## 7. Evidências de qualidade

| Verificação | Resultado |
|---|---|
| Backend `./mvnw package -Dspring.profiles.active=local -Dspring.jpa.show-sql=false` | **12 testes passaram; JAR gerado** |
| Angular `npm test -- --watch=false --browsers=ChromeHeadless` | **11 testes passaram** |
| Angular `npm run build` | **Passou**, aviso preexistente de orçamento CSS Home (6,25 kB / limite 4 kB) |
| Mobile `npm test` | **26 testes passaram em 6 suítes** |
| Mobile `npm run typecheck` | **Passou, sem erros TypeScript** |
| Mobile `npm run lint` | **Passou, sem erros ou avisos** |
| `npx expo-doctor` | **18/18 verificações passaram** |
| `npx expo install --check` | Dependências compatíveis |
| `expo export --platform all` | Bundles Android/Hermes, iOS/Hermes e Web gerados |
| Migração | Schema anterior + V002 executados em H2 modo PostgreSQL, preservação verificada |
| Git `diff --check` nos dois repositórios | Passou |

Backend cobre HTTP real: CRUD, Kanban, categorias, preservação de tarefas, impersonação/isolamento, validação sem atualização parcial, compatibilidade do PUT Web, subtarefas, recorrências, concorrência, expiração e revogação.

Mobile cobre filtros/datas/indicadores, falhas HTTP, formulário com checklist, rotas protegidas, navegação, persistência de tema, foco por timestamps, retomada, deduplicação, separação de contas e falha/corrupção de armazenamento.

Smoke na prévia Expo Web conectada ao backend H2:

- Login com conta DEMO, dashboard e tarefas carregados.
- Criação de “DEMO · Validação pelo navegador” com etapa de checklist e prazo; confirmação após resposta HTTP.
- Navegação em tarefas, perfil e foco; alternância para tema claro; captura do dashboard escuro.
- Sessão de foco de **60 segundos reais**, `timer: null` ao concluir e uma sessão de duração 60000 registrada no armazenamento local.
- Consulta de console sem erros na validação de login/criação.

São validações de navegador e testes automatizados, **não testes em aparelhos**. Nenhum APK/AAB/IPA assinado foi gerado.

Na última rodada, os três bundles foram regenerados após as correções finais:
Web 1,14 MB, Android 2,71 MB e iOS 2,72 MB. Importação direta de Feather evita incluir todas as famílias de ícones.

## 8. Pendências e limitações

- Ensaiar/aplicar V002 em PostgreSQL de homologação antes do banco compartilhado de produção.
- Validar Android/iOS físicos: teclado, leitor de tela, tamanhos de fonte, rede HTTPS/HTTP local, SecureStore, suspensão/retorno e processo encerrado.
- Preparar build nativo assinado se a exposição exigir instalação independente do Expo Go.
- Foco/metas são locais, sem sincronização entre aparelhos/Web. Tema é preferência do aparelho.
- Sem notificações, drag-and-drop, fila offline ou resolução de conflitos. Kanban usa menu; recorrência gera na conclusão, não em agendamento automático.
- Checklist é uma lista de etapas dentro da tarefa; não são tarefas-filhas independentes com prazo próprio.
- Ordem personalizada usa número editável; não há arraste manual de itens.

## 9. Riscos conhecidos

- `npm audit` após correções compatíveis: **75 alertas (65 altos, 10 moderados; zero críticos)** na árvore Mobile, incluindo dependências de desenvolvimento/CLI e do SDK antigo. A correção crítica `shell-quote` foi aplicada. Não se executou `audit fix --force` nem se atualizou Expo/RN incompativelmente. Há trabalho de atualização de dependências a planejar separadamente.
- HTTP local mantém a compatibilidade acadêmica, mas produção deve usar HTTPS. O transporte em binários nativos precisa ser ensaiado.
- Edições simultâneas em dispositivos distintos não têm resolução de conflitos; não há garantia de idempotência de um novo POST repetido manualmente após timeout. A interface bloqueia disparos concorrentes e orienta atualizar antes de repetir.
- Mudança manual no relógio pode afetar Pomodoro; não há relógio externo.
- Logout Mobile sem conexão limpa a sessão local, mas o token remoto só será revogado quando possível ou ao expirar. Logout Angular limpa a credencial da aba; expiração do servidor continua em sete dias.
- Histórico antigo sem eventos não é reconstruído. Excluir tarefa remove seus eventos dos indicadores.
- Perfil local H2 é descartável. Dados DEMO não representam persistência de produção.

## 10. Apresentação

[Roteiro de exposição](DEMONSTRACAO-MOBILE-2.0.md), com os doze passos solicitados e cenário de exclusão de categoria sem perda.

Seed: `scripts/seed-demo.mjs`, senha via `DEMO_PASSWORD`. Nunca sobrescreve registros existentes.

## 11. Git

- Web/backend: branch `main`, base `c5302ee`.
- Mobile: branch `main`, base `bf9ba5c`.
- Ambos estavam limpos no início. Agora contêm somente as alterações desta execução, não commitadas.
- Sem commit, push, reset ou operação em banco de produção.
- As remoções de arquivos JS do Mobile correspondem à migração para rotas TSX/serviços compartilhados; as versões originais permanecem no histórico Git.

Detalhes de implementação e fórmulas: [documentação Mobile](../../organiza-ai/docs/MOBILE-2.0.md).

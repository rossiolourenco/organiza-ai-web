# Auditoria — Organiza Aí 3.0

Data: 18/11/2026. Auditoria dos dois repositórios antes das alterações da versão 3.0.

## 1. Estado encontrado

### Mobile (`../organiza-ai`, branch `main`, base `bf9ba5c`)
- Expo SDK 54 / React Native 0.81.5 / Expo Router 6, TypeScript estrito.
- 5 abas (Início, Tarefas, Planejar, Foco, Perfil) e telas de pilha para tarefa e categorias.
- CRUD de tarefas e categorias real via HTTP, com Bearer.
- Domínio testável em `src/domain` (datas, tarefas, foco); 6 suítes Jest.
- Foco e metas locais via AsyncStorage por servidor/usuário; tema por aparelho.
- Nenhuma dependência de serviço externo, IA ou Firebase.
- Não havia: hábitos, revisão semanal, matriz de Eisenhower, planejamento inteligente,
  modo concentração, lixeira, notificações, indicador de conexão, modo exposição.

### Web/backend (`organiza-ai-web`, branch `main`, base `c5302ee`)
- Spring Boot 4.1.1, Java 21, JPA, H2 local, PostgreSQL/Supabase em produção.
- Endpoints `/api/tarefas`, `/api/categorias`, `/api/usuarios`; autorização por token opaco
  de 256 bits com hash SHA-256 e validade de 7 dias.
- `DetalhesTarefa` persistido como JSON em coluna TEXT; recorrência transacional.
- Migração `V002__mobile_produtividade.sql` ainda **não aplicada** em produção.
- Angular adaptado ao Bearer; build e testes aprovados na versão 2.0.

## 2. Contratos preservados

- `GET/POST/PUT/DELETE /api/tarefas`, `PATCH /{id}/concluir` mantidos.
- `GET/POST/PUT/DELETE /api/categorias` mantidos.
- `POST /api/usuarios`, `/login`, `/logout` mantidos.
- Campos básicos da tarefa inalterados. Campos novos são aditivos e opcionais.
- Clientes antigos sem Bearer continuam recebendo 401 (comportamento da 2.0).

## 3. Lacunas identificadas e tratamento na 3.0

| Lacuna | Tratamento |
|---|---|
| Sem lixeira | Exclusão lógica com `excluida_em`, listagem, restauração e exclusão definitiva |
| Sem hábitos | Tabelas `habitos` e `habito_registros`, CRUD e registro idempotente por data |
| Sem Eisenhower | Campos explícitos `importante`/`urgente` em `detalhes`, com sugestão documentada |
| Sem duração estimada | Campo opcional `duracaoEstimada` (minutos) em `detalhes` |
| Sem planejamento | Algoritmo determinístico no cliente, com pesos publicados |
| Foco só local | Mantido local, por decisão documentada de escopo e sem sincronização |
| Sem notificações | Serviço local opcional por plataforma, com preferências e limites |
| Sem modo exposição | Seed isolado e restauração local restrita à conta DEMO |

## 4. Riscos mantidos

- Migração PostgreSQL real continua pendente de ensaio em homologação.
- Validação em aparelhos físicos e builds assinados não fazem parte desta execução.
- A árvore npm mantém alertas de dependências de desenvolvimento do SDK 54.

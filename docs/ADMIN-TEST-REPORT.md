# Admin 1.0 — Relatório de testes

Data: 18/11/2026. Resultados reais desta execução, sem valores simulados nos testes.

## 1. Backend

Comando:

```bash
cd backend
./mvnw test -Dspring.profiles.active=local -Dspring.jpa.show-sql=false
```

Resultado: **26 testes, 0 falhas, 0 erros — BUILD SUCCESS**.

| Classe | Testes | Cobertura funcional |
|---|---:|---|
| `MobileApiTests` | 10 | CRUD, Kanban, categorias, autorização/IDOR, validação, PUT legado, recorrência concorrente, sessões expiradas |
| `PremiumApiTests` | 7 | lixeira/restauração/definitivo, isolamento, hábitos idempotentes, validação, Eisenhower/duração |
| `AdminApiTests` | 7 | RBAC, gestão de contas, papéis, bloqueio, revogação, senha, rate limit, auditoria, firewall DEMO, monitoramento, anonimização |
| `MigrationTests` | 1 | schema + V002 + V003 + V004 em H2 modo PostgreSQL, inclusão de hábitos/firewall/auditoria e preservação de tarefas |
| `OrganizaAiBackendApplicationTests` | 1 | contexto Spring Boot |

### Casos administrativos verificados

- USER recebe 403 em `/api/admin/**`; sem token recebe 401.
- ADMIN gerencia USER e recebe 403 para ADMIN/SUPER_ADMIN.
- ADMIN não altera papéis nem cria ADMIN.
- SUPER_ADMIN promove/rebaixa e cria ADMIN; não cria SUPER_ADMIN pela API.
- Autoalteração de status/papel/sessão/anonymização retorna 400.
- Bloqueio revoga sessões e invalida tokens.
- Redefinição emite senha temporária, marca troca pendente e revoga sessões.
- Troca de senha pelo usuário invalida o token anterior.
- Quatro falhas retornam 401; a quinta bloqueia e retorna 429.
- Auditoria registra `CONTA_BLOQUEADA_AUTOMATICA`, `SESSOES_REVOGADAS`, firewall etc.
- Conflito de firewall impede aplicação (409); rollback limpa estado aplicado.
- IP/CIDR inválido retorna 400.
- Monitoramento retorna banco disponível, memória e HTTP reais; USER recebe 403.
- Anonimização remove identidade e preserva a tarefa.

## 2. Frontend Angular

Build:

```bash
cd frontend
npm run build
```

Resultado: **sucesso**. Único aviso: orçamento CSS preexistente da Home (6,25 kB / limite 4 kB).
Nenhum erro de template ou TypeScript.

Testes:

```bash
npm test -- --watch=false --browsers=ChromeHeadless
```

Resultado: **11 testes, 11 sucessos**.

## 3. Mobile

```bash
cd organiza-ai
npm test
npx tsc --noEmit
npx eslint app src tests
```

Resultado anterior mantido: **45 testes/10 suítes**, typecheck e lint limpos. O contrato do Mobile não
foi alterado; login agora inclui `papel/status`, campos ignorados pelo app.

## 4. Smoke no navegador (ambiente real local)

Backend `:8080` (H2), Angular `:4200`, Expo Web `:8081`.

| Verificação | Resultado |
|---|---|
| Login SUPER_ADMIN e redirecionamento `/admin/dashboard` | OK |
| Dashboard com contas, sessões, falhas, alertas, banco, CPU e eventos | OK |
| Lista de 6 contas com filtros e paginação | OK |
| Detalhe de conta com sessões e eventos | OK |
| Revogação de sessões com confirmação | OK; sessões caíram para 0 e auditoria registrou |
| Firewall DEMO com 6 regras e estado aplicado | OK |
| Validação sem conflitos | OK |
| Monitoramento com CPU, memória, disco, HTTP e banco reais | OK |
| Auditoria somente leitura | OK |
| Configurações com política | OK |
| Alternância de tema claro/escuro | OK (`data-theme` alterado) |
| Console do navegador | Sem erros |

## 5. Limitações de teste

- Não houve teste em aparelho físico, emulador Android/iOS ou build assinado.
- O firewall real **não foi implementado nem testado**; somente o DEMO.
- A migração V004 foi validada em H2 modo PostgreSQL, não em PostgreSQL real.
- Não houve teste de carga, penetração ou failover.
- O monitoramento não inclui métricas de processos externos ou rede.
- A automação de UI foi feita por DOM no navegador, não por framework E2E.

## 6. Como repetir

```bash
# backend
cd backend && ./mvnw test -Dspring.profiles.active=local

# angular
cd frontend && npm test -- --watch=false --browsers=ChromeHeadless
npm run build

# mobile
cd ../organiza-ai && npm test && npm run typecheck && npm run lint
```

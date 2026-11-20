# Conformidade com o dev-standards — Web/Admin

Data: 2026-11-20. Classificação: **Conforme**, **Parcialmente conforme**, **Não conforme**,
**Não aplicável**. Nenhuma refatoração ampla foi feita nesta intervenção.

## 1. Resumo

| Camada | Classificação | Evidência |
|---|---|---|
| Angular/TypeScript | Parcialmente conforme | `strict`, componentes/rotas, testes 15/15; falta lint/Prettier |
| Spring Boot/Java | Parcialmente conforme | camadas, DTOs de perfil/senha, testes 35/35; há entidades expostas e H2 |
| PostgreSQL | Conforme | migrations versionadas, índices, constraints; teste usa H2 |
| API REST | Parcialmente conforme | recursos e status coerentes; envelope de erro e versionamento a evoluir |
| Segurança | Parcialmente conforme | BCrypt, sessões revogáveis, RBAC, HTTPS/HSTS/CSP; token Web em localStorage |
| Admin/RBAC | Conforme | RBAC, auditoria, firewall DEMO explícito, testes |
| Testes | Conforme com ressalvas | 35 backend, 15 Angular; falta Testcontainers |
| Documentação | Conforme | docs 3.2, ADR, integração, conformidade, workflow |
| Versionamento | Conforme | branches `chore/`, Conventional Commits, SemVer documentado |

## 2. Prioridades

### P0 — Crítico

Nenhum achado.

### P1 — Alto

Nenhuma falha funcional no código atual. A divergência visual observada era ambiental:

- cópia acadêmica `organiza-ai - cópia` em `5bb727f`, com app 1.0.0 e login por IP;
- cache/aba antiga em produção, embora o servidor entregue o bundle 3.2 (`main-NXMTECUN.js`).

O uso dos repositórios oficiais e a limpeza de cache eliminam o sintoma; nenhuma tela foi
redesenhada.

### P2 — Médio (não corrigido nesta fase)

1. Testes de backend usam H2, não Testcontainers/PostgreSQL — pode esconder divergência SQL.
2. `UsuarioController` ainda retorna entidades JPA em `GET /usuarios` e `POST /usuarios`; o correto
   é DTO de resposta.
3. Sem Checkstyle/Spotless e sem análise estática no backend.
4. Sem ESLint/Prettier/script de lint no frontend Angular.
5. API sem versionamento por path (`/v1`) e sem OpenAPI.
6. Envelope de erro atual é `{ "message": ... }`, não o formato `{ "error": { code, message } }`
   previsto no framework.
7. Token Web em `localStorage`, sem refresh token de curta duração; é um risco XSS conhecido.
8. Sem MFA para contas administrativas.
9. Recuperação de senha autoatendida ausente (depende de SMTP/produto).

### P3 — Baixo

- `confirm()` nativo na exclusão de categoria.
- README contém seções históricas da entrega acadêmica; os docs 3.2 complementam.
- Cobertura de testes não medida formalmente.

## 3. Correções realizadas

- Integração do framework canônico, com fingerprint e escopo.
- `AGENTS.md`, `STACK.md` e `opencode.json` carregados pelo OpenCode.
- `.editorconfig`, PR template, ADR e gate local.
- Documentação de integração/conformidade/workflow/regressão/execução/validação.
- Nenhuma alteração de UI, backend, banco ou contrato.

## 4. Plano separado para P2/P3

1. Adicionar Testcontainers e rodar os testes de integração no PostgreSQL.
2. Introduzir DTOs de resposta no `UsuarioController` e testes de contrato.
3. Adicionar Spotless/Checkstyle e ESLint/Prettier com gates no CI.
4. Padronizar envelope de erro e versionar API (`/v1`) com OpenAPI.
5. Avaliar refresh token/expiração curta e MFA administrativo.
6. Implementar recuperação de senha com SMTP real e token de uso único.

Cada item deve ser um PR próprio, com teste e ADR quando mudar contrato/arquitetura.

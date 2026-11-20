# STACK.md — Organiza Aí Web/Admin

- **Projeto:** Organiza Aí Web + Admin 1.0
- **Data da decisão:** 2026-11-20
- **Responsável:** decisão do usuário no prompt 3.2 (projeto existente)
- **Situação:** projeto em desenvolvimento; stack detectada e preservada. Release Candidate 3.3
  (`3.3.0-rc.1`) em preparação para publicação.

## Stack

| Camada | Escolha | Justificativa |
|---|---|---|
| Frontend | Angular 19.2 + TypeScript 5.7 (`strict: true`) | SPA existente, rotas e Admin |
| Backend | Java 21 + Spring Boot 4.1.1 | API existente, RBAC, auditoria |
| Banco | PostgreSQL 17 em produção; H2 em testes locais | banco existente; testes rápidos |
| API | REST sob `/api` | contrato atual do Web e Mobile |
| Autenticação | Bearer token opaco, hash SHA-256 no banco, BCrypt para senha | implementação existente |
| Infraestrutura | VM Debian 13, Nginx, HTTPS, systemd | produção atual |
| Testes | JUnit 5 + Spring Boot Test + **Testcontainers/PostgreSQL 17**; Karma/Jasmine no Angular | suíte existente; paridade real de banco no CI |
| Admin | Angular com RBAC USER/ADMIN/SUPER_ADMIN e firewall DEMO | Admin 1.0 |

## Alternativas consideradas

Não aplicável: o projeto já existe e a stack foi preservada. Não foi introduzida uma segunda
linguagem, framework ou banco.

## Confirmação

- [x] Stack existente apresentada e confirmada pelo usuário
- [x] Nenhuma migração de tecnologia nesta intervenção

## Observações

- O Mobile compartilha o mesmo backend e a mesma API.
- Testes rápidos de backend usam H2; a paridade com PostgreSQL é validada por
  `PostgresIntegrationTests` (Testcontainers + PostgreSQL 17 + `ddl-auto=validate`) no CI
  (`web-postgres`). A dívida de paridade foi encerrada no RC 3.3.
- O 3.3 RC não altera o schema: nenhuma migration nova. As migrations V002–V004 continuam
  manuais e são validadas em PostgreSQL real.
- `spring.jpa.show-sql=false` por padrão (produção não registra SQL/parâmetros).
- O frontend ainda não possui script de lint/Prettier; dívida P2.
- A API atual não tem versionamento por path (`/v1`); dívida P2 de contrato.
- 3.3.1: Web com shell (sidebar/bottom nav) e telas de dashboard, planejamento, revisão, foco,
  hábitos, categorias e lixeira; paridade funcional com o Mobile documentada em
  `docs/FUNCTIONAL-PARITY-3.3.1.md`.
- Versão do release centralizada em `VERSION`, validada contra `frontend/package.json` e
  `backend/pom.xml` pelo gate.

## Referências

- `dev-standards/AI-GUIDELINES.md`
- `dev-standards/PROJECT-SCOPE.md`
- `docs/DEV-STANDARDS-COMPLIANCE.md`

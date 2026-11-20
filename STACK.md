# STACK.md — Organiza Aí Web/Admin

- **Projeto:** Organiza Aí Web + Admin 1.0
- **Data da decisão:** 2026-11-20
- **Responsável:** decisão do usuário no prompt 3.2 (projeto existente)
- **Situação:** projeto já em desenvolvimento; stack detectada e preservada

## Stack

| Camada | Escolha | Justificativa |
|---|---|---|
| Frontend | Angular 19.2 + TypeScript 5.7 (`strict: true`) | SPA existente, rotas e Admin |
| Backend | Java 21 + Spring Boot 4.1.1 | API existente, RBAC, auditoria |
| Banco | PostgreSQL 17 em produção; H2 em testes locais | banco existente; testes rápidos |
| API | REST sob `/api` | contrato atual do Web e Mobile |
| Autenticação | Bearer token opaco, hash SHA-256 no banco, BCrypt para senha | implementação existente |
| Infraestrutura | VM Debian 13, Nginx, HTTPS, systemd | produção atual |
| Testes | JUnit 5 + Spring Boot Test; Karma/Jasmine no Angular | suíte existente |
| Admin | Angular com RBAC USER/ADMIN/SUPER_ADMIN e firewall DEMO | Admin 1.0 |

## Alternativas consideradas

Não aplicável: o projeto já existe e a stack foi preservada. Não foi introduzida uma segunda
linguagem, framework ou banco.

## Confirmação

- [x] Stack existente apresentada e confirmada pelo usuário
- [x] Nenhuma migração de tecnologia nesta intervenção

## Observações

- O Mobile compartilha o mesmo backend e a mesma API.
- Testes de backend usam H2; a paridade com PostgreSQL é uma dívida P2 registrada no relatório de
  conformidade.
- O frontend ainda não possui script de lint/Prettier; dívida P2.
- A API atual não tem versionamento por path (`/v1`); dívida P2 de contrato.
- Não há alteração de banco nesta intervenção.

## Referências

- `dev-standards/AI-GUIDELINES.md`
- `dev-standards/PROJECT-SCOPE.md`
- `docs/DEV-STANDARDS-COMPLIANCE.md`

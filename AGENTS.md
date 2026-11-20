# Organiza Aí Web/Admin — instruções para agentes

> Este arquivo é carregado pelo OpenCode. O `dev-standards` é obrigatório neste repositório.
> Antes de alterar código, leia `dev-standards/AI-GUIDELINES.md`, `STACK.md` e o escopo em
> `dev-standards/PROJECT-SCOPE.md`.

## Antes de implementar

1. Detecte a stack existente: Angular 19.2, TypeScript 5.7, Java 21, Spring Boot 4.1.1 e PostgreSQL 17.
2. Leia os padrões aplicáveis em `dev-standards/` (Java/Spring, TypeScript, PostgreSQL, REST, segurança,
   arquitetura, testes e versionamento).
3. Preserve funcionalidades, contratos de API, RBAC e compatibilidade com o Mobile.
4. Mudanças amplas, de schema ou de arquitetura exigem plano/ADR separado e autorização.
5. Não faça deploy, commit ou push sem autorização explícita. Não altere banco de produção, firewall ou SSH.

## Comandos

```bash
# Backend local (H2)
cd backend && ./mvnw spring-boot:run -Dspring-boot.run.profiles=local

# Frontend local
cd frontend && npm start -- --port 4200

# Testes
cd backend && ./mvnw test -Dspring.profiles.active=local
cd frontend && npm test -- --watch=false --browsers=ChromeHeadless

# Build
cd frontend && npm run build
```

## Estrutura

- `frontend/src/app/pages/` — login, cadastro, home, tarefas, perfil.
- `frontend/src/app/admin/` — Admin 1.0, RBAC, auditoria, firewall DEMO.
- `frontend/src/app/services/` — API, sessão, guards e interceptor.
- `backend/src/main/java/.../controller|service|repository|dto|model` — API e domínio.
- `database/migrations/` — migrations versionadas.

## Regras de qualidade

- Toda mudança de comportamento exige teste; bugfix exige teste de regressão.
- Use DTOs na borda, validação `jakarta.validation` e erros sem vazamento de detalhe interno.
- Não reintroduza IP/servidor no login; a API é centralizada.
- Atualize `docs/`, `STACK.md` e o relatório de conformidade quando a decisão mudar.
- O framework é uma cópia controlada; a origem e o fingerprint estão em `dev-standards/SOURCE.md`.
  Não edite as regras locais; atualize a partir da fonte canônica.
- Gate local: `bash scripts/verify-premium.sh`.

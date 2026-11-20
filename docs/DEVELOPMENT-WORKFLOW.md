# Workflow de desenvolvimento — Web/Admin

## 1. Antes de implementar

1. Ler `AGENTS.md`, `STACK.md` e `dev-standards/PROJECT-SCOPE.md`.
2. Identificar a stack existente (Angular 19.2, Spring Boot 4.1.1, PostgreSQL 17).
3. Para feature nova, percorrer `dev-standards/00-bootstrap/discovery-checklist.md`.
4. Confirmar critérios de aceite e impacto em API/banco/Mobile.
5. Não trocar tecnologia nem redesenhar telas fora do escopo.

## 2. Branch

```bash
git switch -c feature/nome-curto
git switch -c fix/nome-curto
git switch -c chore/nome-curto
```

Branches curtas, baseadas em `main`. Não commitar direto em `main`; não fazer push/merge sem
autorização.

## 3. Implementação

- Menor mudança possível.
- Preservar contratos, RBAC e compatibilidade Web/Mobile.
- Validar entradas na borda; usar DTOs; não expor entidade JPA.
- Não logar token/senha; não commitar segredo.
- Atualizar teste junto com a mudança.

## 4. Testes

```bash
cd backend && ./mvnw test -Dspring.profiles.active=local
cd frontend && npm test -- --watch=false --browsers=ChromeHeadless
cd frontend && npm run build
```

Para o gate completo:

```bash
bash scripts/verify-premium.sh
```

## 5. Segurança

Revisar `dev-standards/04-security/security-checklist.md` antes de considerar pronto. Em especial:
autorização no backend, IDOR, hash de senha, sessão/revogação, HTTPS, headers, rate limit e logs.

## 6. Documentação

- README/uso local: `docs/LOCAL-EXECUTION.md`.
- Decisão arquitetural: `docs/adr/NNNN-*.md`.
- Integração/conformidade: `docs/DEV-STANDARDS-*.md`.
- Mudança de contrato: atualizar docs de API e ADR.

## 7. Commit e PR

Conventional Commits, ex.:

```text
feat(perfil): adiciona alteração de senha
fix(auth): corrige expiração de sessão
docs(dev-standards): registra integração
```

PR pequeno, com o “porquê”, checklist do template, testes verdes e revisão. Sem `--force` em
branch compartilhada.

## 8. Release

- Build Angular + testes.
- Backend JAR + testes.
- Validar `/`, `/admin`, `/api/usuarios` (401), `/api/usuarios/perfil` (401).
- Backup e rollback antes de publicar.
- Deploy somente com autorização explícita.
- Tag SemVer após publicar.

# Escopo de aplicação do dev-standards — Web/Admin

O framework completo é mantido para reprodutibilidade. A seleção abaixo define o que é carregado
pelo `opencode.json` e consultado nas tarefas deste projeto.

| Arquivo | Aplicação |
|---|---|
| `AI-GUIDELINES.md` | Obrigatório |
| `00-bootstrap/*` | Obrigatório em feature nova |
| `01-code-standards/general-principles.md` | Obrigatório |
| `01-code-standards/nodejs-typescript.md` | Obrigatório (Angular/TS) |
| `01-code-standards/java-spring.md` | Obrigatório (backend) |
| `01-code-standards/dotnet-csharp.md`, `python.md` | Não aplicável |
| `02-database/general-principles.md`, `postgresql.md` | Obrigatório |
| `02-database/mongodb.md`, `mysql.md`, `sqlserver.md` | Não aplicável |
| `03-api-design/rest-api-standards.md` | Obrigatório |
| `03-api-design/microservices-standards.md` | Não aplicável (monólito modular) |
| `04-security/security-checklist.md` | Obrigatório |
| `05-architecture/architecture-guidelines.md` | Obrigatório |
| `05-architecture/mobile-architecture.md` | Não aplicável neste repositório |
| `06-testing/testing-strategy.md` | Obrigatório |
| `07-documentation/*` | Obrigatório ao documentar |
| `08-versioning/*` | Obrigatório ao versionar |
| `09-prompts/*` | Referência de trabalho |
| `10-boilerplate/*` | Referência; `.editorconfig` e PR template já copiados |

## Dívidas conhecidas (não corrigidas nesta intervenção)

- Testes de backend usam H2 em vez de Testcontainers/PostgreSQL (P2).
- Sem Checkstyle/Spotless no backend (P2).
- Sem script de lint/Prettier no frontend (P2).
- Entidades JPA ainda retornadas diretamente em `UsuarioController` (P2).
- API sem versionamento por path (`/v1`) e sem OpenAPI (P2).
- `confirm()` nativo na exclusão de categoria (P3).

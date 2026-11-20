# Integração do dev-standards — Web/Admin

## 1. Origem

- **Fonte canônica:** `/Users/rossiolourenco/Framework-Projetos/dev-standards`
- **Integração:** 2026-11-20
- **Fingerprint SHA-256 (sem `.DS_Store`):**
  `37a59b23899aa564e9cf67e351f73c6dda7198b6dff2ffc3bc2fc2361e7fa32f`
- **Cópia no projeto:** `dev-standards/`, idêntica à fonte canônica.
- **Metadados:** `dev-standards/SOURCE.md` e `dev-standards/PROJECT-SCOPE.md`.

O framework é **metodológico**: não é dependência de runtime do Angular, do Spring Boot ou do banco.

## 2. Estrutura adotada

```text
organiza-ai-web/
├── AGENTS.md
├── STACK.md
├── opencode.json
├── .editorconfig
├── .github/pull_request_template.md
├── dev-standards/            # cópia canônica + SOURCE/PROJECT-SCOPE
├── docs/
│   ├── adr/0001-adopt-dev-standards.md
│   ├── DEV-STANDARDS-INTEGRATION.md
│   ├── DEV-STANDARDS-COMPLIANCE.md
│   ├── DEVELOPMENT-WORKFLOW.md
│   ├── REGRESSION-PREVENTION.md
│   ├── LOCAL-EXECUTION.md
│   └── VALIDATION-REPORT.md
├── scripts/verify-premium.sh
├── frontend/
└── backend/
```

Não foi usado submódulo Git: o framework é composto por regras/documentação e a cópia versionada
com fingerprint é reproduzível e simples de auditar.

## 3. Configuração do OpenCode

- `AGENTS.md` na raiz é descoberto automaticamente.
- `opencode.json` declara `instructions` adicionais:
  `STACK.md`, `AI-GUIDELINES.md`, general principles, TypeScript, Java/Spring, PostgreSQL, REST,
  security, architecture, testing, ADR template, commits e git workflow.
- O arquivo `opencode.json` é específico deste repositório; não altera a configuração global.

### Evidência de carregamento

Nesta sessão, o OpenCode aplicou automaticamente, via `system-update`, os seguintes arquivos:

`AGENTS.md`, `STACK.md`, `dev-standards/AI-GUIDELINES.md`, `general-principles.md`,
`nodejs-typescript.md`, `java-spring.md`, `postgresql.md`, `rest-api-standards.md`,
`security-checklist.md`, `architecture-guidelines.md`, `testing-strategy.md`, `adr-template.md`,
`commit-convention.md` e `git-workflow.md`.

Isso confirma que a configuração não é apenas uma pasta copiada: as regras foram efetivamente
carregadas pelo agente. Além disso, uma nova sessão `opencode run` no diretório do projeto
respondeu `WEB_STACK_OK`, confirmando o carregamento em sessão limpa.

## 4. Escopo

O `PROJECT-SCOPE.md` marca o que se aplica. Permanecem no repositório os arquivos de linguagens/banco
não usados (para preservar a fonte), mas eles não são carregados pelo `opencode.json`.

## 5. Atualização

```bash
# 1. atualizar a fonte canônica
cd /Users/rossiolourenco/Framework-Projetos/dev-standards

# 2. comparar com o projeto
diff -rq --exclude=.DS_Store \
  /Users/rossiolourenco/Framework-Projetos/dev-standards \
  /Users/rossiolourenco/ProjetosFaama/organiza-ai-web/dev-standards

# 3. atualizar SOURCE.md e o fingerprint
# 4. rodar o gate
cd /Users/rossiolourenco/ProjetosFaama/organiza-ai-web
bash scripts/verify-premium.sh
```

Não edite as regras diretamente na cópia do projeto. Adaptações vão em `STACK.md`, `AGENTS.md`,
`PROJECT-SCOPE.md` ou `docs/`.

## 6. Limitações

- O framework não tem tag/versão semântica; o fingerprint é a referência.
- A cópia é manual, portanto a atualização exige diff/revisão.
- A integração não altera o comportamento da aplicação.

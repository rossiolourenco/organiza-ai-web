# Proteção contra regressões — Web/Admin

## 1. Gate local

```bash
cd /Users/rossiolourenco/ProjetosFaama/organiza-ai-web
bash scripts/verify-premium.sh
```

O script falha imediatamente se qualquer verificação essencial quebrar. Ele cobre:

1. commit e branch atuais;
2. presença de `dev-standards`, `STACK.md`, `AGENTS.md` e `opencode.json` válido;
3. integridade dos arquivos do framework contra a fonte canônica;
4. existência de `PerfilComponent`, `HomeComponent` e Admin;
5. marcadores `PAINEL PESSOAL`, `Meu perfil` e `Security Console`;
6. rota `/perfil` em `app.routes.ts`;
7. ausência de marcadores do layout acadêmico no código ativo;
8. `git diff --check`;
9. build Angular;
10. testes Angular (15);
11. testes Spring Boot (35).

## 2. O que o gate não cobre

- Teste visual real em navegador/dispositivo.
- Teste E2E de browser; é necessário um runner (Playwright/Cypress) em fase futura.
- Teste de carga e de acessibilidade com leitor de tela.
- Deploy/produção.
- Paridade Web completa com o Mobile.

## 3. Verificações manuais obrigatórias antes de release

- Login premium sem IP.
- Home com `PAINEL PESSOAL`, indicadores e filtros.
- `/perfil` com nome, e-mail e segurança.
- Alteração de senha → login com a nova → senha antiga rejeitada.
- `/admin` com RBAC; auditoria/segurança carregando dados.
- 360 px, 768 px e 1440 px sem overflow.
- Console sem erro.

## 4. CI futuro

Proposta de pipeline:

```text
checkout → setup Java 21/Node → npm ci → mvn verify → ng build → ng test → verify-premium.sh
```

Adicionar ao CI quando o repositório tiver pipeline. O gate já é compatível com execução local e
com um job de CI que tenha Chrome/Java/Node.

## 5. GitHub Actions

O workflow `CI Web` executa em PR/push para `main` e sob demanda:

- `web-angular` — gate `bash scripts/verify-premium.sh --frontend`;
- `web-backend` — gate `bash scripts/verify-premium.sh --backend`;
- `web-admin` — gate `bash scripts/verify-premium.sh --admin`.

Os gates usam o manifesto `dev-standards/MANIFEST.sha256` e o `FINGERPRINT`, portanto funcionam
sem caminhos absolutos no runner. A branch `main` deve exigir esses três checks, PR e bloquear
force push/exclusão.

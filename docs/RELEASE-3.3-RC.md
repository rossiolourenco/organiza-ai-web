# Organiza Aí 3.3 — Release Candidate

- **Versão:** `3.3.0-rc.1` (arquivo `VERSION`)
- **Backend Maven:** `3.3.0-RC1`
- **Frontend Web/Admin:** `3.3.0-rc.1`
- **Mobile (package):** `3.3.0-rc.1` · **Mobile (app.json):** `3.3.0` · iOS build `33` · Android `versionCode` `33`
- **Base de produção:** 3.2 (`20261119-func32c`)
- **Status:** Release Candidate preparado, **não publicado**
- **Branches:** `release/3.3-rc` (Web/Admin e Mobile)

## 1. Escopo desta entrega

O 3.3 RC **não reescreve** o produto. Ele fecha as pendências de segurança, banco real,
qualidade e rastreabilidade necessárias para publicar com segurança:

| Área | Entrega |
|---|---|
| Segurança | DTO na borda do `UsuarioController`, remoção de metadados internos da resposta, `CSP` nas respostas da API, `show-sql` desativado em produção |
| Banco real | Testcontainers com **PostgreSQL 17**, `ddl-auto=validate`, `schema.sql` + migrations V002–V004 aplicadas no container |
| Qualidade | Testes de integração cobrindo cadastro, login, perfil, senha, tarefas, categorias, IDOR, RBAC, headers e integridade referencial |
| CI | Novo check obrigatório `web-postgres`; gates passam a validar versão única do release |
| Release | `VERSION`, artefatos com checksum, plano de deploy, rollback e validação pós-deploy |
| Documentação | 10 documentos de release (lista em `docs/RELEASE-HANDOFF.md`) |

## 2. Funcionalidades preservadas

Nenhuma rota, tela, endpoint ou fluxo existente foi removido. O contrato da API permanece
compatível: o cadastro continua respondendo `200` com `id`, `nome`, `email`, `papel`, `status`,
`deveTrocarSenha`, `dataCriacao` e `senhaAlteradaEm` — apenas deixa de serializar a entidade JPA
e, com ela, `bloqueadoAte`, `motivoBloqueio` e `ultimoLoginEm` do próprio usuário.

Web e Mobile não consomem esses três campos (verificado em `frontend/src/app/services` e
`src/state`/`src/features` do Mobile).

## 3. O que mudou no código

### Backend

- `UsuarioController.listar` e `criar` retornam `PerfilResponse` (DTO) em vez de `Usuario`.
- `AutorizacaoFilter` adiciona `Content-Security-Policy: default-src 'none'; frame-ancestors 'none'`.
- `application.properties`: `spring.jpa.show-sql=false` e `format_sql=false` (log sem SQL/parâmetros).
- `pom.xml`: versão `3.3.0-RC1` e dependências de teste `spring-boot-testcontainers`,
  `testcontainers-postgresql`, `testcontainers-junit-jupiter`.
- Novo `PostgresIntegrationTests` (4 testes) e perfil `postgres-it`.

### Ferramentas

- `scripts/verify-premium.sh` (Web) ganhou os modos `--postgres` e `--release` e valida
  `VERSION` × `frontend/package.json` × `backend/pom.xml`.
- `scripts/verify-premium.sh` (Mobile) valida `VERSION` × `package.json` × `app.json`.
- `CI Web` ganhou o job `web-postgres`, que falha se o teste PostgreSQL for ignorado.
- `CI Mobile` mantém `mobile-quality` e `mobile-bundle`.

## 4. Artefatos

Gerados por `scripts/build-release.sh` em `dist-release/3.3.0-rc.1/` (fora do Git):

| Artefato | Origem |
|---|---|
| `organiza-ai-web-3.3.0-rc.1.tar.gz` | `frontend/dist/frontend/browser` |
| `organiza-ai-backend-3.3.0-rc.1.jar` | `backend/target/backend-3.3.0-RC1.jar` |
| `organiza-ai-mobile-3.3.0-rc.1.tar.gz` | `dist/` do export Expo |
| `SHA256SUMS` | checksums dos artefatos acima |

Os checksums publicados em `docs/RELEASE-HANDOFF.md` são os do build homologado.

## 5. Critérios de aceitação do RC

1. `bash scripts/verify-premium.sh --release` verde (Web) — inclui PostgreSQL real.
2. `bash scripts/verify-premium.sh` verde (Mobile).
3. CI remoto verde em `web-angular`, `web-backend`, `web-postgres`, `web-admin`,
   `mobile-quality`, `mobile-bundle` — **confirmado** (runs 44764549186 e 44764548796).
4. Nenhuma vulnerabilidade crítica/alta conhecida sem mitigação documentada.
5. Migrations aditivas validadas em PostgreSQL 17.
6. Backup, deploy, rollback e smoke tests documentados e executáveis.

## 6. Limitações conhecidas (não bloqueantes)

- Sem MFA, refresh token e recuperação de senha — roadmap 3.3 P1, com mitigações descritas em
  `SECURITY-REVIEW.md`.
- Token Web em `localStorage` — risco aceito e documentado até a migração para cookie `HttpOnly`.
- Sem teste em aparelho físico iOS/Android nesta rodada.
- Lint/format Java e Angular ainda não configurados (P2).
- Migrations continuam manuais (sem Flyway/Liquibase); o ensaio automatizado usa PostgreSQL real
  via Testcontainers.

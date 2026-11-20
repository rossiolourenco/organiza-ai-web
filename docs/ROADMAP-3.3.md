# Roadmap 3.3 — Web/Admin

Status: **execução parcial no RC 3.3**. Itens de segurança de release, DTOs e PostgreSQL real
foram concluídos; os demais seguem planejados para 3.3/3.4.
Base: 3.2 em produção; dev-standards integrado; CI em `.github/workflows/ci.yml`.

## Guardrails

- Sem deploy, migration, alteração de banco, SSH ou firewall sem autorização.
- Toda mudança em branch + PR; `main` protegida com checks.
- Testes e documentação junto com o código.
- Não remover funcionalidades; não trocar stack sem ADR.

## Paridade 3.3.1 — concluída

- Web passou a cobrir todas as funcionalidades de negócio do backend: shell com sidebar, dashboard,
  planejamento, revisão, foco, hábitos, categorias e lixeira.
- Mobile ganhou revalidação ao retomar o app.
- Matriz: `docs/FUNCTIONAL-PARITY-3.3.1.md`. Nenhuma lacuna `WEB_MISSING`/`MOBILE_MISSING`.

## Status de execução (RC 3.3)

**Concluído neste RC:**

- DTOs na borda do `UsuarioController` (`PerfilResponse`), sem entidade JPA na resposta.
- `PostgresIntegrationTests` com Testcontainers + PostgreSQL 17 e `ddl-auto=validate`.
- Job obrigatório `web-postgres` no CI.
- `spring.jpa.show-sql=false` em produção e `Content-Security-Policy` nas respostas da API.
- Versão única (`VERSION`), artefatos com checksum e 10 documentos de release.

**Pendente (ordem recomendada):** scanner de dependências no CI (P0), migração Angular 20+ e
Expo SDK (P0/P1 de plataforma), refresh token/MFA/recuperação (P1), limite de tentativas em
cadastro/troca de senha (P1), Spotless/ESLint/Prettier (P2), OpenAPI/`/v1` e envelope de erro
(P2), lixeira/check-list no Web e acessibilidade (P3).

## P0 — Segurança crítica

| Item | Justificativa | Impacto / risco | Dependências | Testes / aceite | Ordem |
|---|---|---|---|---|---|
| Auditoria de dependências e segredos | Detectar CVEs e segredos antes de novas features | Alto impacto; risco de exploração | `npm audit`, `mvn dependency-check`/OSV, revisão de logs | Zero segredo no git; CVEs críticas/altas tratadas ou aceitas formalmente | 1 |
| Revisão de autorização/IDOR | Garantir que A não acessa B em todos os endpoints | Alto; vazamento de dados | Testes de API com duas contas | Testes de isolamento verdes em tarefas, categorias, hábitos, perfil | 2 |
| Sessões/revogação e rate limit | Reduzir janela de token comprometido | Alto; sequestro de sessão | Modelo de sessão atual | Token revogado retorna 401; brute force limitado | 3 |

## P1 — Autenticação e confiabilidade

| Item | Justificativa | Impacto / risco | Dependências | Testes / aceite | Ordem |
|---|---|---|---|---|---|
| Armazenamento de token Web | `localStorage` é vulnerável a XSS | Alto; roubo de sessão | Decisão cookie HttpOnly x token curto/refresh | XSS simulado não expõe token; login/logout/refresh funcionam | 1 |
| Refresh token / expiração curta | Reduzir validade de token roubado | Médio/alto; complexidade de rotação | Endpoint de refresh, revogação, CORS | Rotação, revogação e expiração testadas | 2 |
| MFA administrativo | Proteger ADMIN/SUPER_ADMIN | Alto; locked-out se mal implementado | TOTP/WebAuthn, recovery codes | Login admin exige segundo fator; recovery testado | 3 |
| Recuperação de senha | Falta fluxo autoatendido | Médio; suporte sobrecarregado | SMTP real, token de uso único, rate limit | Token expira, é único, senha antiga é rejeitada | 4 |
| Proteção de sessões | Sessões ativas, IP/UA, alertas | Médio; falso positivo | Auditoria e tela de sessões | Sessão revogada no servidor; usuário vê sessões | 5 |

## P2 — Qualidade e arquitetura

| Item | Justificativa | Impacto / risco | Dependências | Testes / aceite | Ordem |
|---|---|---|---|---|---|
| ~~Testcontainers PostgreSQL~~ **feito no RC** | H2 esconde divergências SQL | Médio; CI mais lento | Docker no CI | Testes de integração contra PostgreSQL | 1 |
| ~~DTOs no `UsuarioController`~~ **feito no RC** | Não expor entidade JPA | Médio; contrato mais seguro | Mapeador/testes de contrato | Respostas sem campos internos; testes atualizados | 2 |
| Spotless/Checkstyle | Padronizar Java | Baixo/médio; reformatação grande | Configuração e baseline | `mvn verify` com formatação/lint | 3 |
| ESLint/Prettier Angular | Padronizar TS/HTML | Baixo; mudanças massivas | Config e baseline | CI com lint/format sem erros | 4 |
| OpenAPI + versionamento `/v1` | Contrato documentado e evolutivo | Médio; impacto em clients | Decisão de versão e compatibilidade | OpenAPI gerado; clients ajustados; smoke tests | 5 |
| Envelope de erro padrão | Consistência de erros | Baixo/médio; ajuste de clients | Novo contrato + testes | `{error:{code,message}}` em toda API | 6 |

## P3 — Experiência e manutenção

| Item | Justificativa | Impacto / risco | Dependências | Testes / aceite | Ordem |
|---|---|---|---|---|---|
| Acessibilidade Web/Admin | Uso por teclado/leitores | Baixo; ajustes de foco/contraste | Auditoria | Foco visível, labels, contraste AA | 1 |
| Remover diálogo nativo de categoria | UX consistente | Baixo | Componente de modal | Exclusão via modal acessível | 2 |
| E2E Playwright | Proteger login/tarefas/perfil | Médio; manutenção | Runner/CI | Golden path verde | 3 |
| Documentação operacional | Reduzir dependência de conhecimento local | Baixo | Docs | Runbook/rollback atualizados | 4 |

## Critério de saída da 3.3

- P0/P1 concluídos com testes e ADRs.
- CI obrigatório verde em PR e `main`.
- Nenhuma regressão de autenticação, RBAC, tarefas ou Admin.
- Produção só recebe deploy após autorização, backup e smoke test.

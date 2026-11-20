# Security Review — Organiza Aí 3.3 RC

- **Data:** 2026-11-20
- **Escopo:** Web, Admin, API (Spring Boot), Mobile (Expo) e configuração de produção
- **Método:** revisão de código, teste de integração com PostgreSQL real, `npm audit`,
  inspeção de configuração e verificação de headers em execução
- **Resultado:** nenhuma vulnerabilidade **crítica** explorável identificada; 1 correção de
  exposição de dados, 1 correção de log, 1 reforço de header. Riscos residuais conhecidos
  documentados com mitigação e item de roadmap.

## 1. Correções aplicadas neste RC

| ID | Severidade | Achado | Correção | Evidência |
|---|---|---|---|---|
| SEC-01 | Média | `UsuarioController` devolvia a entidade JPA `Usuario` em `GET /api/usuarios` e `POST /api/usuarios`, expondo `bloqueadoAte`, `motivoBloqueio` e `ultimoLoginEm` do próprio usuário. O hash de senha já era `WRITE_ONLY`. | Respostas passam a usar `PerfilResponse` (DTO). | `ContaApiTests` e `PostgresIntegrationTests.cadastroLoginPerfilETrocaDeSenhaNoPostgreSQL` |
| SEC-02 | Média | `spring.jpa.show-sql=true` no `application.properties`, herdado por produção, gravava SQL e parâmetros no log (`/var/log/organiza-ai/backend.log`). | `show-sql=false` e `format_sql=false` por padrão; `local` mantém SQL para desenvolvimento. | `application.properties`; produção já define `SPRING_JPA_SHOW_SQL=false` em `/etc/organiza-ai/backend.env` (defesa em profundidade) |
| SEC-03 | Baixa | API não enviava `Content-Security-Policy`. | `Content-Security-Policy: default-src 'none'; frame-ancestors 'none'` em todas as respostas `/api/**`. | `AutorizacaoFilter` + teste `cabeccosDeSegurancaEAcessoAdministrativo` + verificação por `curl` |

## 2. Controles verificados (sem alteração)

| Controle | Situação | Evidência |
|---|---|---|
| Senha com BCrypt | OK | `CorsConfig.passwordEncoder()` → `BCryptPasswordEncoder` |
| Token opaco de 256 bits | OK | `SessaoService.criar` (`SecureRandom`, 32 bytes, Base64 URL) |
| Apenas hash SHA-256 do token no banco | OK | `SessaoService.hash`; tabela `sessoes.hash` |
| Expiração de sessão | OK | 7 dias por política (`sessao_dias`), revogação em logout e troca de senha |
| Rate limiting de login | OK | `LoginAttemptService` + política (5 tentativas / 15 min, bloqueio 15 min) |
| Bloqueio temporário de conta | OK | `AutenticacaoService`, com proteção do último SUPER_ADMIN |
| Autorização por recurso (IDOR) | OK | `AutorizacaoFilter` compara `usuarioId` da query com a sessão → 403; repositórios consultam por `usuarioId`; teste com dois usuários |
| RBAC administrativo | OK | `Rbac` + `UsuarioAtual.requireAdmin/requireSuperAdmin`; `USER` recebe 403 em `/api/admin/**` |
| Rotas administrativas no SPA | OK | `adminGuard` (4 testes) e redirecionamento verificado em navegador |
| Cadastro não permite escalar papel | OK | `UsuarioController.criar` fixa `USER/ATIVO` |
| Mass assignment em tarefas | OK | `TarefaService.copiar` só aceita campos permitidos; dono vem da sessão |
| CORS | OK | Origens explícitas por ambiente; sem `*` e sem `allowCredentials` |
| CSRF | Não aplicável | Autenticação por `Authorization: Bearer`, sem cookie de sessão |
| Headers de segurança | OK | `nosniff`, `DENY`, `Referrer-Policy`, `Permissions-Policy`, `no-store`, CSP |
| Erros sem vazamento | OK | `ErrosApi` devolve mensagem genérica; `include-stacktrace=never`, `include-message=never` |
| Segredos no repositório | OK | Nenhum segredo encontrado; configuração por variáveis de ambiente |
| Escuta de rede | OK | Backend e PostgreSQL apenas em loopback; Nginx publica 443 |
| Firewall da aplicação | OK | Modo `DEMO`; não altera nftables/UFW |

## 3. Auditoria de dependências

### Web/Admin (`npm audit`)

- Produção (`--omit=dev`): 0 críticas, 4 altas, 3 moderadas — todas em `@angular/*` 19.2.25.
- Desenvolvimento: 4 críticas e 24 altas em ferramentas de build (`@angular/build`,
  `@angular/cli`, `vite`, `webpack-dev-middleware`, `tar`, `shell-quote`, `piscina`,
  `proxy-addr`).

**Análise das altas de produção (Angular 19.2.25):**

| Advisory | Vetor | Aplicável? | Mitigação |
|---|---|---|---|
| GHSA-rgjc-h3x7-9mwg | Client hydration / SSR cache poisoning | **Não** — a aplicação é SPA pura, sem `provideClientHydration` e sem build de servidor (`angular.json` usa `browser`, sem `outputMode: server`) | Não há superfície de hidratação |
| GHSA-jj27-h5hq-8x99 | XSS via i18n event-handler attributes | **Não** — o projeto não usa runtime de i18n do Angular (`$localize`/`i18n` ausentes) | Textos são literais nos templates |
| GHSA-ff3f-86qr-9cv3, GHSA-62vg-58rm-qff7 | DoS no SSR (matrix params / empty-path outlets) | **Não** — sem SSR | Servidor não renderiza Angular |
| GHSA-39pv-4j6c-2g6v, GHSA-jhpw-976m-542j, GHSA-p297-fm68-3q8c | `HttpTransferCache` (cache poisoning / bypass) | **Não** — `HttpTransferCache` só existe com hidratação | — |
| GHSA-jfgx-762w-99vr | Vazamento de token XSRF via parsing de URL | **Não** — o cliente usa Bearer token; não há cookie XSRF (`withXsrfConfiguration` ausente) | Token não é enviado em cookie |
| GHSA-48r7-hpm6-gfxm | DoS (OOM) em `formatDate` | **Parcial** — datas exibidas vêm da própria API e do próprio usuário | Impacto restrito ao próprio navegador; sem execução remota de código |

Não existe versão corrigida dentro do major 19 (19.2.25 é o último LTS). A correção exige subir
para Angular ≥ 20, o que é uma migração de framework incompatível com o escopo de um RC.
**Item P0 do roadmap 3.4:** migração para Angular 20/21 com suíte completa e teste visual.

### Mobile (`npm audit`)

- Produção: 0 críticas, 6 altas (todas DoS), 15 moderadas.
- Altas: `decode-uri-component`/`query-string` (via `expo-router`), `brace-expansion`,
  `image-size`, `postcss`.

**Análise:**

| Pacote | Vetor | Aplicável? | Mitigação |
|---|---|---|---|
| `decode-uri-component` / `query-string` | DoS ao decodificar percent-encoding malformado | Sim, via deep link | Sem exposição de dados; falha resulta em app lento/reiniciado. O app não processa deep links com payloads não confiáveis além do roteamento |
| `brace-expansion`, `postcss` | DoS / leitura de arquivos no build | Não no runtime do app | Dependências de Metro/CLI usadas apenas em build |
| `image-size` | Loop infinito ao parsear JXL/HEIF/ICNS | **Não** — o app não recebe imagens do usuário | Assets são empacotados no build |

Correção definitiva exige atualização do Expo SDK (major). **Item P0 do roadmap 3.4.**

### Backend (Java)

- `pom.xml` sem dependência vulnerável conhecida nas versões gerenciadas pelo Spring Boot 4.1.1.
- Não há `OWASP Dependency-Check`/OSV no CI. **Item P0 do roadmap 3.4:** adicionar scanner de
  dependências ao pipeline.

## 4. Riscos residuais aceitos para o RC

| Risco | Severidade | Por que não foi corrigido agora | Mitigação / próximo passo |
|---|---|---|---|
| Token Web em `localStorage` | Alta se houver XSS | Migrar para cookie `HttpOnly` muda a arquitetura de autenticação dos três clientes | SPA sem `innerHTML` de conteúdo de usuário, sem i18n dinâmico e CSP da API; item P1 do roadmap |
| Sem refresh token | Média | Exige novo endpoint, rotação e revogação | Token de 7 dias, revogável, revogado em logout/troca de senha; item P1 |
| Sem MFA administrativo | Média | Exige TOTP/WebAuthn + recovery codes + migração | Senha 12+ para contas administrativas, rate limit, bloqueio, auditoria de login; item P1 |
| Sem recuperação de senha | Baixa (funcionalidade) | Exige SMTP e token de uso único | Redefinição administrativa com senha temporária e `deveTrocarSenha`; item P1 |
| Rate limit só no login | Baixa | Cadastro/troca de senha exigem limite por IP | Cadastro público é idempotente por e-mail único; item P1 |
| Sessões sem tela de gestão no Web | Baixa | UI nova | Admin revoga sessões por conta; Mobile tem tela de segurança |

## 5. Conclusão

- Nenhuma falha **crítica** ou **alta explorável** identificada no código da aplicação.
- As altas de dependências são de DoS/cenários inexistentes na arquitetura (SSR/hidratação/i18n)
  ou restritas ao próprio cliente, com mitigação documentada.
- O RC está apto a receber deploy **com** os itens P0/P1 do roadmap como dívida registrada.

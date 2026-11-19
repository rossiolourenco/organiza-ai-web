# Admin 1.0 — Segurança

## 1. Autenticação

- Login por e-mail e senha; senha armazenada com BCrypt.
- Token opaco de 256 bits (`SecureRandom`), retornado uma única vez.
- Banco armazena apenas SHA-256 do token; o token não é registrado em logs.
- Validade configurável (1–30 dias, padrão 7).
- Sessão registra criação, último uso, expiração, IP e user agent.
- Logout e revogação marcam `revogada_em`; a autenticação passa a falhar imediatamente.
- Conta com status diferente de `ATIVO` não acessa nenhuma API, mesmo com token válido.

## 2. Autorização

- `AutorizacaoFilter` bloqueia `/api/admin/**` para `USER`.
- Serviços revalidam papel e regras específicas; esconder botões não é proteção.
- ADMIN gerencia apenas USER; SUPER_ADMIN gerencia ADMIN/USER/SUPER_ADMIN.
- Autoelevação, auto-bloqueio, auto-revogação e auto-anonimização são recusados.
- O último SUPER_ADMIN ativo é protegido por contagem no banco.
- `usuarioId` enviado na query continua sendo comparado ao usuário do token (anti-IDOR).

## 3. Rate limiting e força bruta

- Tentativas falhas são contadas por e-mail na janela configurada (padrão 15 min).
- Ao atingir o limite (padrão 5), a conta é marcada `BLOQUEADO` por N minutos (padrão 15),
  todas as sessões são revogadas e um evento é auditado. Exceção: o último SUPER_ADMIN ativo
  não é bloqueado pelo rate limit; recebe 429, mas mantém status `ATIVO`.
- Enquanto o limite está excedido, o login retorna 429 sem revelar se a senha está correta.
- Depois do prazo, o próximo login reativa automaticamente a conta e tenta autenticar.
- Bloqueios manuais não expiram automaticamente.

## 4. Política de senhas

Configurável por SUPER_ADMIN:

- comprimento mínimo de 8 a 72;
- exigência opcional de número e maiúscula;
- aplicada a cadastro público e redefinições;
- contas administrativas têm regra reforçada fixa: 12+ caracteres, maiúscula, minúscula e número;
- redefinição administrativa gera 16 caracteres com todas as classes e marca `deveTrocarSenha`.

Troca pelo próprio usuário: `PUT /api/usuarios/senha`, exigindo senha atual; revoga as sessões.

## 5. Sessões e revogação

- Revogação administrativa atinge todas as sessões ativas do alvo.
- O ator não pode revogar as próprias sessões pelo console; deve usar logout.
- Sessões não expõem hash/token na API administrativa.
- Tokens expirados ou revogados resultam em 401.

## 6. Cabeçalhos e transporte

Respostas de API incluem:

- `X-Content-Type-Options: nosniff`
- `X-Frame-Options: DENY`
- `Referrer-Policy: no-referrer`
- `Permissions-Policy: camera=(), microphone=(), geolocation=()`
- `Cache-Control: no-store`

CORS usa lista explícita de origens (`app.cors-origins`). Em produção, use HTTPS e restrinja portas.

## 7. CSRF

A API usa `Authorization: Bearer`, sem cookie de sessão. Portanto CSRF clássico por cookie não se
aplica. Se no futuro houver cookie, será necessário token CSRF e `SameSite`.

## 8. Auditoria

- Registro append-only pela aplicação; não há endpoint de edição/exclusão.
- Ações críticas: login administrativo, falhas, bloqueios, desbloqueios, papéis, sessões, senha,
  anonimização, políticas e firewall.
- Dados sensíveis nunca são gravados: sem senhas, tokens, hashes ou conteúdo de tarefas.
- IP vem de `X-Forwarded-For` (primeiro valor) ou do socket; user agent é truncado.
- Correlação usa `X-Correlation-Id` ou UUID.
- Leitura restrita a ADMIN/SUPER_ADMIN.

## 9. Firewall

- O modo real está desabilitado por configuração.
- O backend não executa `nft`, `ufw`, `iptables` ou shell.
- O frontend não envia comandos; apenas estrutura de regra validada.
- Regras, aplicação e rollback são simulações persistidas.
- O agente real futuro deve ser separado, com socket local, allowlist e rollback temporizado.

## 10. Erros e vazamento de informação

- `ErrosApi` retorna mensagens controladas; stack traces não são enviadas.
- `server.error.include-message=never` e `include-stacktrace=never`.
- 401/403/404/409/429 têm mensagens distintas sem expor detalhes internos.
- Diagnósticos e monitoramento exigem administrador.

## 11. Bootstrap do primeiro SUPER_ADMIN

Variáveis:

```bash
ADMIN_BOOTSTRAP_EMAIL=...
ADMIN_BOOTSTRAP_PASSWORD=...   # 12+, maiúscula, minúscula e número
ADMIN_BOOTSTRAP_NOME="Administrador"
```

O bootstrap só age quando não existe SUPER_ADMIN ativo. A senha não é impressa nem registrada.
Depois do primeiro acesso, recomenda-se rotacionar a senha e manter as variáveis fora do serviço.

## 12. Riscos residuais

- Não há MFA.
- Rate limiting é por e-mail, não distribuído entre réplicas.
- Logs de auditoria residem no mesmo banco; não há WORM externo.
- Firewall real não foi implementado/testado.
- Testes em VM, HTTPS e proxy reverso ainda precisam ser executados na implantação.

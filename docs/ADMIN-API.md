# Admin 1.0 — API

Base: `http://localhost:8080/api` (produção: `https://SEU_DOMINIO/api`).

Todas as rotas administrativas exigem:

```http
Authorization: Bearer <token>
```

`USER` recebe **403**. Sem token, **401**. Erros retornam `{ "message": "..." }`.

## 1. Autenticação e conta

### `POST /usuarios`

Cadastro público. Sempre cria `USER/ATIVO`; papel e status do corpo são ignorados.

```json
{ "nome": "Ana", "email": "ana@exemplo.com", "senha": "Senha-forte-2026" }
```

Resposta `200` com o usuário (sem senha). `400` para política/validação; `409` para e-mail duplicado.

### `POST /usuarios/login`

```json
{ "email": "ana@exemplo.com", "senha": "Senha-forte-2026" }
```

`200`:

```json
{
  "id": 10, "nome": "Ana", "email": "ana@exemplo.com",
  "token": "…", "papel": "USER", "status": "ATIVO", "deveTrocarSenha": false
}
```

`401` credenciais inválidas; `403` conta indisponível; `429` limite de tentativas.

### `POST /usuarios/logout`

Revoga o token atual. `204`.

### `PUT /usuarios/senha`

```json
{ "senhaAtual": "…", "novaSenha": "Nova-senha-2026" }
```

`204` e revoga todas as sessões. `400` senha atual/política.

## 2. Console — identidade

### `GET /admin/usuarios/me`

```json
{ "id": 1, "nome": "Administrador", "email": "admin@…", "papel": "SUPER_ADMIN",
  "permissoes": ["admin.dashboard", "admin.users.read", "..."] }
```

## 3. Contas

### `GET /admin/usuarios`

Query: `q`, `status` (`ATIVO|BLOQUEADO|DESATIVADO|ANONIMIZADO`), `papel`, `page` (0+), `size` (1–100).

```json
{ "content": [{ "id": 2, "nome": "…", "email": "…", "papel": "USER", "status": "ATIVO",
  "dataCriacao": "…", "ultimoLoginEm": null, "bloqueadoAte": null, "motivoBloqueio": null,
  "deveTrocarSenha": false, "sessoesAtivas": 1 }],
  "page": 0, "size": 20, "total": 1, "totalPages": 1 }
```

### `GET /admin/usuarios/{id}`

Retorna `{ usuario, sessoes[], eventosRecentes[] }`. Sessões não expõem token/hash.
ADMIN só acessa USER; SUPER_ADMIN acessa qualquer conta.

### `POST /admin/usuarios`

```json
{ "nome": "Novo", "email": "novo@exemplo.com", "papel": "USER" }
```

`200`:

```json
{ "usuarioId": 12, "email": "novo@exemplo.com", "senhaTemporaria": "…", "aviso": "…" }
```

ADMIN cria apenas USER; SUPER_ADMIN pode criar ADMIN. `SUPER_ADMIN` é recusado (`403`).

### `PATCH /admin/usuarios/{id}/status`

```json
{ "status": "BLOQUEADO", "motivo": "Revisão de segurança" }
```

Aceita `ATIVO`, `BLOQUEADO`, `DESATIVADO`. Bloquear/desativar revoga sessões.
`400` autoalteração/status inválido; `409` proteção do último SUPER_ADMIN.

### `PATCH /admin/usuarios/{id}/papel`

Somente SUPER_ADMIN.

```json
{ "papel": "ADMIN" }
```

`403` para ADMIN; `400` autoalteração; `409` último SUPER_ADMIN.

### `POST /admin/usuarios/{id}/revogar-sessoes`

```json
{ "revogadas": 3 }
```

`400` para a própria conta; `403` alvo acima do papel.

### `POST /admin/usuarios/{id}/redefinir-senha`

`200` com `senhaTemporaria` (uma única vez). A senha não é auditada. Revoga sessões e marca
`deveTrocarSenha`.

### `DELETE /admin/usuarios/{id}`

Anonimiza a conta (nome/e-mail/senha), status `ANONIMIZADO`, papel `USER`, revoga sessões.
Tarefas/categorias/hábitos são preservados. `204`; `400` autoanonimização; `409` último SUPER_ADMIN.

## 4. Central de segurança

### `GET /admin/seguranca/overview`

```json
{
  "contasAtivas": 3, "contasBloqueadas": 1, "falhas24h": 5, "sucessos24h": 2,
  "sessoesAtivas": 4,
  "alertas": [{ "nivel": "ATENCAO", "titulo": "…", "detalhe": "…" }],
  "ultimosEventos": [{ "id": 10, "acao": "CONTA_BLOQUEADA", "…": "…" }],
  "politica": { "senhaMinima": 8, "maxTentativas": 5, "janelaMinutos": 15,
                "bloqueioMinutos": 15, "sessaoDias": 7, "…": "…" }
}
```

### `GET /admin/seguranca/tentativas`

Query: `email`, `sucesso` (`true|false`), `desde` (`AAAA-MM-DD` ou ISO), `page`, `size`.
Resposta paginada com `email`, `sucesso`, `ip`, `userAgent`, `motivo`, `criadoEm`.

### `GET /admin/seguranca/politica`

Retorna a política vigente.

### `PUT /admin/seguranca/politica`

Somente SUPER_ADMIN.

```json
{ "senhaMinima": 10, "exigirNumero": true, "exigirMaiuscula": true,
  "maxTentativas": 5, "janelaMinutos": 15, "bloqueioMinutos": 30, "sessaoDias": 7 }
```

Limites: senha 8–72; tentativas 3–20; janela/bloqueio 1–1440; sessão 1–30.

## 5. Auditoria

### `GET /admin/auditoria`

Query: `acao`, `ator`, `desde`, `ate`, `page`, `size`. Somente leitura.
Retorna eventos com ator, papel, ação, recurso, resultado, IP, user agent, detalhes e correlação.

## 6. Firewall DEMO

### `GET /admin/firewall/estado`

```json
{ "modo": "DEMO", "demo": true, "aplicado": true, "aplicadoEm": "…",
  "snapshotAtualId": 1, "totalRegras": 6, "regrasAtivas": 6 }
```

### `GET /admin/firewall/regras`

Lista ordenada por prioridade e id.

### `POST /admin/firewall/regras`

```json
{ "nome": "Bloquear SMTP", "descricao": "…", "ipCidr": "0.0.0.0/0", "porta": 25,
  "protocolo": "TCP", "direcao": "INBOUND", "acao": "DENY",
  "prioridade": 60, "ativa": true }
```

Validação: IP/CIDR literal, porta 1–65535 ou nula, protocolo TCP/UDP/ANY, direção
INBOUND/OUTBOUND, ação ALLOW/DENY, prioridade 1–999. `400` inválido.

### `PUT /admin/firewall/regras/{id}`

Mesmo corpo. `404` inexistente.

### `PATCH /admin/firewall/regras/{id}/ativa?ativa=true|false`

Ativa/desativa regra.

### `DELETE /admin/firewall/regras/{id}`

`204`.

### `GET /admin/firewall/validar`

```json
{ "valido": false, "conflitos": [
  { "regraA": 2, "nomeA": "HTTPS", "regraB": 7, "nomeB": "Bloquear HTTPS",
    "motivo": "Regras ativas sobrepostas com ações opostas…" }
] }
```

### `POST /admin/firewall/aplicar`

```json
{ "motivo": "Aplicação pela interface" }
```

Cria snapshot e marca estado. Conflitos ativos → `409`.
**Não executa firewall real.**

### `POST /admin/firewall/rollback`

Restaura o último snapshot; `409` se não existir. Marca estado não aplicado.

### `POST /admin/firewall/reset-demo`

Recria as cinco regras padrão. `200` com estado.

### `GET /admin/firewall/historico`

Últimas 50 operações simuladas.

## 7. Monitoramento

### `GET /admin/monitoramento/overview`

```json
{
  "coletadoEm": "…", "uptimeSegundos": 123, "backendDisponivel": true,
  "banco": { "disponivel": true, "latenciaMs": 2, "detalhe": "H2" },
  "cpu": { "cargaSistema": 4.2, "cargaProcesso": 1.1, "nucleos": 8, "loadAverage": 1.4 },
  "memoria": { "heapUsadoBytes": 0, "heapMaximoBytes": 0, "…": 0 },
  "disco": { "disponivel": true, "totalBytes": 0, "livreBytes": 0, "caminho": "…" },
  "http": { "totalRequisicoes": 42, "erros4xx": 2, "erros5xx": 0, "tempoMedioMs": 18 },
  "sessoesAtivas": 3,
  "firewall": { "modo": "DEMO", "aplicado": true, "aplicadoEm": "…", "regrasAtivas": 6 },
  "usuariosAtivos": 3, "usuariosBloqueados": 1
}
```

Campos indisponíveis podem ser `null`; jamais são inventados.

## 8. Códigos comuns

| Código | Significado |
|---|---|
| 200 | Sucesso com corpo |
| 204 | Sucesso sem corpo |
| 400 | Entrada inválida/regra de negócio |
| 401 | Token ausente/expirado/revogado |
| 403 | Papel ou status sem permissão |
| 404 | Recurso inexistente |
| 409 | Conflito/proteção do último SUPER_ADMIN |
| 429 | Rate limit de login |

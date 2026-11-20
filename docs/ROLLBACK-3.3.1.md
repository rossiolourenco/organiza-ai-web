# Rollback — 3.3.1

## 1. Banco

Nenhuma alteração de schema. Rollback de banco **não é necessário**; manter os dados.

## 2. Aplicação

A 3.3.1 muda o frontend (novas rotas/bundle) e o backend apenas na versão. Rollback:

```bash
readlink -f /opt/organiza-ai/current
ANTERIOR=/opt/organiza-ai/releases/20261120-3.3-rc1   # 3.3.0-rc.1
ln -sfn "$ANTERIOR" /opt/organiza-ai/current
systemctl restart organiza-ai-backend
systemctl reload nginx
```

Validar `/`, `/admin`, `/api/usuarios` (401) e login.

## 3. Compatibilidade

- API inalterada: clientes antigos continuam funcionando.
- O bundle antigo `main-NXMTECUN.js` volta a ser servido pela release anterior.
- Preferências locais (plano/foco) não são perdidas: ficam no navegador/aparelho.

## 4. Mobile

A publicação em loja não faz parte desta rodada. Se um build 3.3.1 for publicado e precisar de
rollback, manter a API compatível (não há migration) e republicar o build anterior.

## 5. Critério

Rollback se: página branca, erro 5xx sistêmico, login quebrado, CRUD falhando ou perda de dados.

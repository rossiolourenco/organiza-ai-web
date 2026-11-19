# Organiza Aí 3.2 — Preparação de release

## 1. Estado

A versão **3.2.0** está implementada, testada e pronta para homologação/publicação. **Não foi
publicada em produção nesta fase**, conforme a regra de não executar deploy automático sem
autorização específica.

A produção continua em:

- Web/Admin: release `20261119-uiux2`;
- Backend: JAR da série 3.1.

## 2. Mudanças da 3.2

### Backend

- `GET /api/usuarios/perfil` — dados públicos do próprio usuário.
- `PUT /api/usuarios/perfil` — edição do nome, com validação e auditoria.
- `PUT /api/usuarios/senha` — endurecido para recusar senha igual à atual; mantém a revogação de
  todas as sessões.
- Nenhuma migration; schema atual é compatível.
- `ContaApiTests` adicionado.

### Web

- Nova rota `/perfil` com dados da conta, edição de nome, segurança e aparência.
- Formulário de senha com senha atual, nova senha, confirmação, mostrar/ocultar, loading e erros.
- Link de perfil na topbar da Home e do formulário.
- Select de status na edição de tarefa (`PENDENTE`, `EM_ANDAMENTO`, `CONCLUIDA`).
- Botão **Reabrir** em tarefas concluídas.
- Aviso de senha temporária na Home quando `deveTrocarSenha`.
- Shell autenticado compartilhado em CSS global.
- Orçamentos de build mantidos em `angular.json`.

### Mobile

- Nova rota `/seguranca` e tela **Perfil → Segurança**.
- `AuthProvider`: `clear`, `changePassword`, `updateProfile`.
- Perfil com edição de nome.
- Aviso de sucesso no login após a troca.
- Versão 3.2.0 em `package.json`/`app.json` e fallback corrigido.

## 3. Artefatos

| Artefato | Caminho local | Tamanho |
|---|---|---|
| Backend JAR | `backend/target/backend-0.0.1-SNAPSHOT.jar` | 53 MB |
| Web Angular | `frontend/dist/frontend/browser` | 544 KB |
| Mobile Web | `dist/_expo/static/js/web` | 1,3 MB |
| Mobile Android | `dist/_expo/static/js/android` | 3,1 MB |
| Mobile iOS | `dist/_expo/static/js/ios` | 3,1 MB |

## 4. Pré-publicação

1. Autorização explícita para o deploy da 3.2.
2. `git status` limpo nos dois repositórios.
3. Backup do PostgreSQL (`systemctl start organiza-ai-backup.service`).
4. Backup da release atual:
   ```bash
   readlink -f /opt/organiza-ai/current
   cp -a /opt/organiza-ai/current /opt/organiza-ai/releases/rollback-20261119-pre32
   ```
5. Confirmar serviços:
   ```bash
   systemctl is-active organiza-ai-backend nginx postgresql
   certbot certificates
   ```

## 5. Publicação sugerida (após autorização)

```bash
REL=/opt/organiza-ai/releases/20261119-func32
install -d -m 755 -o organiza -g organiza "$REL/public"

# Frontend
rsync -a --delete frontend/dist/frontend/browser/ "$REL/public/"
chown -R organiza:organiza "$REL/public"

# Backend
cp backend/target/backend-0.0.1-SNAPSHOT.jar "$REL/backend.jar"
chown organiza:organiza "$REL/backend.jar"

# Ativação
ln -sfn "$REL" /opt/organiza-ai/current
nginx -t && systemctl reload nginx
systemctl restart organiza-ai-backend
```

> A 3.2 altera o backend (novos endpoints de perfil). O restart do serviço é necessário; a 3.1
> trocava apenas o frontend.

## 6. Smoke tests pós-publicação

```bash
curl -s -o /dev/null -w '%{http_code}\n' https://organizaai.evox.net.br/           # 200
curl -s -o /dev/null -w '%{http_code}\n' https://organizaai.evox.net.br/admin      # 200
curl -s -o /dev/null -w '%{http_code}\n' https://organizaai.evox.net.br/api/usuarios # 401
curl -s -o /dev/null -w '%{http_code}\n' https://organizaai.evox.net.br/api/usuarios/perfil # 401
```

Validar no navegador:

- login USER → `/home`;
- login ADMIN → `/admin/dashboard`;
- `/perfil` carrega nome/e-mail;
- editar nome e recarregar;
- alterar senha, confirmar revogação e entrar com a nova;
- criar/concluir/reabrir tarefa;
- definir status Em andamento;
- console sem erros;
- mobile nativo com login, perfil e segurança.

## 7. Rollback

```bash
ln -sfn /opt/organiza-ai/releases/20261119-uiux2 /opt/organiza-ai/current
systemctl restart organiza-ai-backend
systemctl reload nginx
```

- O schema não foi alterado; não há migration a reverter.
- O JAR anterior deve ser restaurado junto com a release.
- Mobile: manter o build anterior; a API 3.1 continua compatível.

## 8. Riscos

- A troca de senha revoga todas as sessões; suportar o aviso na interface é obrigatório.
- Usuários com sessão antiga continuam válidos até expirar, exceto após troca de senha.
- Sem recuperação de senha autoatendida; suporte continua via Admin.
- Sem teste em aparelho físico nesta fase.
- A release não foi publicada; a validação em produção ainda é necessária.

## 9. Critério de aceite do deploy

- Smoke tests 200/200/401/401.
- Login e perfil validados.
- Troca de senha validada com uma conta de homologação.
- Nenhum erro no console e nos logs do backend.
- Rollback ensaiado.

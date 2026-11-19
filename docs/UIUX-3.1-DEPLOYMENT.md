# UI/UX 3.1 — Preparação de release

## 1. Estado

A release **3.1 foi construída e testada, mas não publicada**. A produção continua com o build
anterior (3.0 + Admin 1.0). Nenhuma alteração de banco, firewall ou systemd foi feita nesta fase.

Commits locais (não enviados ao GitHub):

- Web/Admin: `645d309` — refinamento UI/UX 3.1.
- Mobile: `102dd8e` — login/API 3.1.
- Este documento e demais docs entram em commit local posterior.

## 2. Artefatos preparados

| Artefato | Caminho | Tamanho |
|---|---|---|
| Angular Web | `frontend/dist/frontend/browser` | ~500 KB |
| Mobile Web | `dist/_expo/static/js/web` | 1,3 MB |
| Android Hermes | `dist/_expo/static/js/android` | 3,1 MB |
| iOS Hermes | `dist/_expo/static/js/ios` | 3,1 MB |
| Backend | sem alteração de código desde `c2b99b0`/Admin | 54 MB |

Nenhuma migration nova. Backend permanece compatível com Mobile 3.1.

## 3. Pré-publicação

1. Autorização explícita para publicar.
2. `git status` limpo nos dois repositórios.
3. Backup do PostgreSQL (`systemctl start organiza-ai-backup.service`).
4. Backup do release atual:
   ```bash
   readlink -f /opt/organiza-ai/current
   cp -a /opt/organiza-ai/current /opt/organiza-ai/releases/rollback-20261119
   ```
5. Confirmar certificado e serviços:
   ```bash
   systemctl is-active organiza-ai-backend nginx postgresql
   certbot certificates
   ```

## 4. Publicação Web/Admin (quando autorizado)

```bash
REL=/opt/organiza-ai/releases/20261119-uiux
mkdir -p "$REL/public"
rsync -a --delete /caminho/local/frontend/dist/frontend/browser/ "$REL/public/"
chown -R organiza:organiza "$REL"
ln -sfn "$REL" /opt/organiza-ai/current
systemctl reload nginx
```

Observação: o `current` também contém `backend.jar`; a troca de symlink deve preservar/copiar o JAR
atual para a nova release antes do reload:

```bash
cp -a /opt/organiza-ai/releases/20261118/backend.jar "$REL/backend.jar"
```

Alternativa mais segura: manter `current` apontando para a release completa e copiar o novo `public`
para dentro dela, sem trocar o JAR.

## 5. Smoke pós-publicação

```bash
curl -s -o /dev/null -w '%{http_code}\n' https://organizaai.evox.net.br/          # 200
curl -s -o /dev/null -w '%{http_code}\n' https://organizaai.evox.net.br/admin     # 200
curl -s -o /dev/null -w '%{http_code}\n' https://organizaai.evox.net.br/api/usuarios # 401
```

Validar no navegador:

- login/cadastro com tema claro e escuro;
- login USER → `/home`;
- login ADMIN → `/admin/dashboard`;
- sidebar recolhida persiste após reload;
- tabelas e modais em 360 px, 768 px e 1440 px;
- console sem erros.

## 6. Mobile

- Builds Expo exportados, **não publicados** em lojas.
- Para distribuição, gerar build EAS ou APK/AAB/IPA assinado em etapa autorizada.
- A API oficial já é o padrão; não é necessário configurar IP.
- Em dispositivo físico, validar HTTPS, SecureStore, teclado, Safe Area e sessão.

## 7. CORS

Produção permite apenas `https://organizaai.evox.net.br`. A prévia Expo Web em `localhost:8081`
não autentica contra produção; Android/iOS nativos não usam CORS. Não ampliar CORS de produção sem
necessidade.

## 8. Rollback

```bash
ln -sfn /opt/organiza-ai/releases/20261118 /opt/organiza-ai/current
systemctl restart organiza-ai-backend
systemctl reload nginx
```

- Backend inalterado; rollback de frontend é imediato.
- Banco não foi alterado; não há migração para reverter.
- Mobile: manter build anterior; a API é compatível.

## 9. Riscos

- Aviso de orçamento CSS da Home (6,45 kB).
- Validação visual do Admin 3.1 só será possível após publicar ou montar staging.
- Sem teste em aparelho físico.
- Publicação e push continuam pendentes de autorização.

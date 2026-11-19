# UI/UX 3.1 — Preparação de release

## 1. Estado

A release **3.1 foi publicada em 19/11/2026** em `/opt/organiza-ai/releases/20261119-uiux` e, na
sequência, a release corretiva `20261119-uiux2` substituiu o frontend da área logada (Home e
formulário de tarefas). O `current` aponta para `20261119-uiux2`. Web/Admin foram validados em
HTTPS nos temas claro e escuro, em 514 px e 1440 px, sem erros de console e sem overflow.
Nenhuma alteração de banco, firewall ou systemd foi feita nesta fase. Mobile teve código e bundles
preparados, mas não foi publicado em lojas.

Commits publicados no GitHub:

- Web/Admin: `cad066a` (release 3.1, docs e refinamento).
- Mobile: `102dd8e` (login/API 3.1).

O commit `645d309` contém a implementação Web/Admin e `cad066a` a documentação de release.

## 2. Artefatos preparados

| Artefato | Caminho | Tamanho |
|---|---|---|
| Angular Web | `frontend/dist/frontend/browser` | ~524 KB |
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
REL=/opt/organiza-ai/releases/20261119-uiux2
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

- Sem teste em aparelho físico Mobile; simulador iOS validado.
- A prévia Expo Web em `localhost:8081` continua sujeita a CORS de produção.
- O CSS da Home cresceu para ~12,5 kB; o orçamento de `anyComponentStyle` foi ajustado para
  14 kB (aviso) e 20 kB (erro) em `angular.json`.
- A senha inicial do SUPER_ADMIN deve ser trocada e o arquivo temporário removido.

## 10. Histórico de releases

| Release | Conteúdo | Estado |
|---|---|---|
| `20261118` | Admin 1.0 + base 3.0 | rollback disponível |
| `20261119-uiux` | Login/cadastro 3.1 + Admin | substituída |
| `20261119-uiux2` | Home/Tarefas e formulário redesenhados | **atual** |

Rollback imediato:

```bash
ln -sfn /opt/organiza-ai/releases/20261119-uiux /opt/organiza-ai/current
systemctl reload nginx
```

O JAR do backend é o mesmo em todas as releases desta série; não há migração a reverter.

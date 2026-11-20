# Release Handoff — Organiza Aí 3.3 RC

> Documento de passagem para o Prompt 2 (publicação). Nenhum deploy foi executado aqui.

## 1. Identificação

| Item | Valor |
|---|---|
| Versão | `3.3.0-rc.1` |
| Backend | `3.3.0-RC1` |
| Branch | `release/3.3-rc` |
| Repositório | `rossiolourenco/organiza-ai-web` |
| PR | https://github.com/rossiolourenco/organiza-ai-web/pull/2 |
| Base | `main` `b1f00ee` |
| **Commit final da branch** | **`b0f89b5ddf0de8041f2b04296ea498e3ab79dc48`** |
| Commits do RC | `dbfd88f`, `03bb55b`, `a36e89d`, `933e9c1`, `b0f89b5` |
| Mobile | `rossiolourenco/organiza-ai` · https://github.com/rossiolourenco/organiza-ai/pull/2 · `8fd49e4` |

> O commit `b0f89b5` contém o código do RC. Um commit posterior apenas de documentação
> (checksums/handoff) pode existir na branch; nesse caso, o deploy deve usar o **HEAD da branch
> `release/3.3-rc` após o merge** e o HEAD registrado no relatório final desta rodada.

## 2. Checks obrigatórios

Web: `web-angular`, `web-backend`, `web-postgres`, `web-admin`.
Mobile: `mobile-quality`, `mobile-bundle`.

A `main` exige PR, checks verdes (`strict`), e bloqueia force push/exclusão. O merge do PR do RC
**não** está autorizado nesta rodada.

## 3. Artefatos e checksums

Gerados por `scripts/build-release.sh` (fora do Git, `dist-release/3.3.0-rc.1/`):

```text
59456a53bca7abd1fd2cacf8f0a0400e41c171631f1f024f41d03b2636ee1f95  organiza-ai-web-3.3.0-rc.1.tar.gz
17c13123c556cbf2818b50a3ec9121b3dfc51f17be7b4ac8598ec122016e25f6  organiza-ai-backend-3.3.0-rc.1.jar
29291d179f8b3831b14042ba570a65911b048581c3e6ee6dd35e67ba73051bd4  organiza-ai-database-3.3.0-rc.1.tar.gz
```

| Artefato | Tamanho |
|---|---|
| Web (build Angular de produção) | 148 KB |
| Backend (`backend-3.3.0-RC1.jar`) | 53 MB |
| Banco (`schema.sql` + migrations) | 6 KB |

Mobile (`dist-release/3.3.0-rc.1/` em `organiza-ai`):

```text
ce8f4696b8dfce672a2781bdbad6fc62a31f4acf3762759116a1e1bb0a0b8c41  organiza-ai-mobile-3.3.0-rc.1.tar.gz
07f38ddf475a8403326db7f634e99a5bac957c7804d6bcb69899fe20d44375aa  package-metadata.json
```

Os checksums devem ser reconferidos com `sha256sum -c SHA256SUMS` antes do uso.

## 4. Variáveis de ambiente

Nenhuma variável nova. O `/etc/organiza-ai/backend.env` permanece igual ao da 3.2. Lista completa
e verificação em `PRODUCTION-DEPLOYMENT.md` §2.

## 5. Instruções exatas para o Prompt 2

1. **Pré-condições**
   - Confirmar que os checks do PR #2 estão verdes.
   - Autorizar e executar o **squash merge** do PR #2 (o merge não foi feito nesta rodada).
   - Registrar o SHA do merge em `main`.
2. **Backup**
   - Executar o backup de `DATABASE-MIGRATION-PLAN.md` §3 e confirmar o dump íntegro.
3. **Release**
   - Criar `/opt/organiza-ai/releases/<timestamp>-3.3-rc1` a partir do commit de `main`.
   - Build backend (`./mvnw -DskipTests package`) e frontend (`npm ci && npm run build`).
4. **Sem migration**
   - O 3.3 não tem migration nova. Não aplicar DDL.
5. **Ativação**
   - `ln -sfn <release> /opt/organiza-ai/current`, `systemctl restart organiza-ai-backend`.
6. **Validação**
   - Seguir `POST-DEPLOY-VALIDATION.md` (HTTP, headers, login, CRUD, Admin, banco, logs).
7. **Em caso de falha**
   - `ROLLBACK-PLAN.md`: trocar `current` para a release da 3.2 e reiniciar.
8. **Mobile**
   - **Não** publicar em loja nesta etapa. O handoff Mobile está em
     `../organiza-ai/docs/RELEASE-HANDOFF.md` e exige build assinado.

## 6. Limites do Prompt 2

- Publicar somente os commits homologados; sem novas funcionalidades.
- Não alterar schema sem novo plano e backup.
- Não usar force push, não desativar checks, não commitar segredos.
- Não publicar app em loja sem build assinado.

## 7. Pendências externas

| Pendência | Depende de |
|---|---|
| Checks remotos verdes | CI do PR #2 (executado após o push) |
| Merge do PR | Autorização no Prompt 2 |
| Backup pré-deploy | Acesso SSH à VM (Prompt 2) |
| Deploy | Autorização no Prompt 2 |
| Build assinado Mobile | Credenciais Apple/Google/EAS |
| Teste em aparelho físico | Hardware |
| Checks obrigatórios `web-postgres` na `main` | Adicionar o contexto à proteção após o primeiro run verde do PR |

> **Ação recomendada antes do merge:** incluir `web-postgres` na lista de checks obrigatórios da
> `main` (hoje exige `web-angular`, `web-backend`, `web-admin`). Enquanto o job não estiver na
> proteção, ele roda no PR mas não bloqueia o merge.

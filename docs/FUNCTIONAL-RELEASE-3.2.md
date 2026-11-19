# Organiza Aí 3.2 — Release e publicação

## 1. Estado

A versão **3.2.0** foi publicada em produção em **19/11/2026** e está ativa na release:

```text
/opt/organiza-ai/releases/20261119-func32c
```

O symlink `/opt/organiza-ai/current` aponta para essa release. Web/Admin, backend e banco foram
validados em HTTPS. O Mobile 3.2 foi gerado/exportado, mas **não foi publicado em App Store ou
Google Play**.

## 2. Mudanças da 3.2

### Backend

- `GET /api/usuarios/perfil` — dados públicos do próprio usuário, sem hash.
- `PUT /api/usuarios/perfil` — edição do nome, com validação e auditoria.
- `PUT /api/usuarios/senha` — recusa nova senha igual à atual; mantém BCrypt, política e revogação
  de todas as sessões.
- Correção de consultas administrativas no PostgreSQL:
  - busca de usuários (`q`);
  - auditoria (`ator`, `desde`, `ate`);
  - tentativas de login (`email`, `desde`).
- Nenhuma migration; schema compatível.
- `ContaApiTests` (6) e `AdminApiTests` (+1) adicionados.

### Web

- `/perfil` com dados da conta, edição de nome, segurança e aparência.
- Formulário de senha com senha atual, nova, confirmação, mostrar/ocultar, loading e erros.
- Link de perfil na topbar.
- Select de status na edição de tarefa.
- Botão **Reabrir** em tarefas concluídas.
- Aviso de senha temporária.

### Mobile

- Tela **Perfil → Segurança → Alterar senha** (`/seguranca`).
- `AuthProvider`: `clear`, `changePassword`, `updateProfile`.
- Edição de nome no perfil.
- Confirmação de sucesso e aviso no login.
- Versão 3.2.0.

## 3. Histórico de releases

| Release | Conteúdo | Estado |
|---|---|---|
| `20261118` | Admin 1.0 + base 3.0 | rollback histórico |
| `20261119-uiux` | Login/cadastro 3.1 + Admin | histórico |
| `20261119-uiux2` | Home/Tarefas 3.1 | rollback pré-3.2 |
| `rollback-20261119-pre32` | Cópia da `uiux2` antes do deploy 3.2 | rollback |
| `20261119-func32` | Primeira publicação 3.2 | substituída |
| `20261119-func32b` | Correção do `LIKE` administrativo | substituída |
| `20261119-func32c` | Correção de parâmetros de data + **atual** | **produção** |

## 4. Artefatos publicados

| Artefato | Caminho | Tamanho |
|---|---|---|
| Backend JAR | `/opt/organiza-ai/releases/20261119-func32c/backend.jar` | 53 MB |
| Web Angular | `/opt/organiza-ai/releases/20261119-func32c/public` | 548 KB |
| Mobile Web | `dist/_expo/static/js/web` | 1,3 MB |
| Android Hermes | `dist/_expo/static/js/android` | 3,1 MB |
| iOS Hermes | `dist/_expo/static/js/ios` | 3,1 MB |

Checksum do JAR publicado:

```text
341e383f5cc5ce9aab1f4371ed25e2f20c5c3b337e61110abbc2448a2878e798
```

Bundle Web: `main-NXMTECUN.js`.

## 5. Correção pós-deploy

O primeiro smoke test da 3.2 revelou que os endpoints abaixo retornavam 500 no PostgreSQL quando
filtros de data eram nulos:

- `GET /api/admin/auditoria`
- `GET /api/admin/seguranca/tentativas`

Causa: o Hibernate 7 enviava parâmetros nulos sem tipo inferível (`lower(bytea)` e
“não foi possível determinar o tipo de dados do parâmetro”). Correção:

- padrões de `LIKE` calculados na aplicação;
- datas nulas substituídas por `Instant.EPOCH` e um limite superior (`9999-12-31`);
- consultas sem `concat` de parâmetro nulo;
- teste `AdminApiTests.adminListQueriesWorkWithAndWithoutFilters`.

Resultado em produção: usuários, auditoria e tentativas retornam 200 com dados reais.

## 6. Testes

| Verificação | Resultado |
|---|---|
| Backend | **35/35** em 7 suítes |
| Angular | **15/15** |
| Mobile | **50/50** |
| Mobile typecheck/lint | Sem erros |
| Expo Doctor | 18/18 |
| Deploy | Concluído em `func32c` |

## 7. Smoke test de produção

```text
/                         200
/admin                    200
/api/usuarios             401
/api/usuarios/perfil      401 sem token
login SUPER_ADMIN         200
perfil autenticado        200, sem hash
senha atual incorreta     400
atualizar nome            200 + auditoria
/admin/usuarios           200 (4 contas)
/admin/auditoria          200 (57 registros)
/admin/seguranca          200 (15 tentativas)
console                   sem erros
perfil em 514 px          sem overflow
```

## 8. Rollback

```bash
ln -sfn /opt/organiza-ai/releases/rollback-20261119-pre32 /opt/organiza-ai/current
systemctl restart organiza-ai-backend
systemctl reload nginx
```

- O JAR anterior está dentro de `rollback-20261119-pre32`.
- Não há migration a reverter.
- Mobile antigo continua compatível com a API.

## 9. Mobile

- Código e bundles prontos.
- Para loja: gerar build assinado EAS/APK/AAB/IPA.
- A API oficial HTTPS já é o padrão.
- Teste em aparelho físico continua pendente.

## 10. Pendências e riscos

- Recuperação de senha autoatendida (SMTP) não implementada.
- Paridade Web de hábitos/calendário/Kanban/foco/revisão.
- Teste em aparelhos físicos.
- A troca de senha revoga todas as sessões; comportamento intencional.
- Monitorar logs e auditoria nos próximos dias.

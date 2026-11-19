# Organiza Aí 3.2 — Relatório de testes funcionais

Data: 19/11/2026. Ambiente de homologação: backend local com H2 em memória, Web Angular em
`organiza.localhost:9090` (proxy local) e Mobile em Expo Web `localhost:8083`.

## 1. Resumo

| Verificação | Resultado |
|---|---|
| Backend — suítes | **7** |
| Backend — testes | **35 / 35** |
| Angular — testes | **15 / 15** |
| Mobile — testes | **50 / 50** |
| Mobile — typecheck | Sem erros |
| Mobile — lint | Sem erros |
| Expo Doctor | **18/18** |
| Expo install check | Dependências compatíveis |
| Expo export | Web, Android/Hermes e iOS/Hermes |
| Backend package | `backend-0.0.1-SNAPSHOT.jar` (53 MB) |
| Web build | 544 KB (`main-NXMTECUN.js`) |
| Deploy em produção | **Não executado nesta fase** |

## 2. Testes automatizados adicionados

### Backend — `ContaApiTests` (6)

- perfil sem hash, com dados do próprio usuário;
- atualização de nome com persistência e limites;
- bloqueio de edição de outro usuário via `usuarioId`;
- troca de senha com senha atual, política, senha igual e revogação;
- senha antiga rejeitada e nova aceita;
- sessão de outro usuário preservada;
- autenticação obrigatória.

### Angular — `PerfilComponent` (3)

- carrega o perfil autenticado;
- valida confirmação divergente antes da API;
- altera a senha, limpa a sessão e navega para o login.

### Angular — `HomeComponent` (+1)

- reabre tarefa concluída via `PUT`, preservando título e categoria.

### Backend — AdminApiTests (+1)

- lista usuários, auditoria e tentativas com e sem filtros, reproduzindo o caminho que falhava
  no PostgreSQL.

### Mobile — navegação (+1)

- a tela de segurança envia `PUT /api/usuarios/senha` com o corpo correto e apresenta a confirmação.

## 3. Cenários ponta a ponta

### 3.1 Senha na Web

1. Login com a conta de homologação.
2. Perfil → Segurança.
3. Senha atual + nova senha + confirmação.
4. `204` do backend.
5. Sessão local limpa; redirecionamento para `/login?senha=alterada`.
6. Aviso “Senha alterada. Entre novamente com a nova credencial.”
7. Login com a nova senha: sucesso.
8. Login com a senha antiga: `401`.

### 3.2 Senha no Mobile

1. Login no Mobile Web com a conta de homologação.
2. Perfil → Segurança.
3. Campos preenchidos; botão mostrar/ocultar presente.
4. `204` do backend; tela “Senha alterada.”
5. Botão “Entrar novamente” → login com aviso.
6. Login com a nova senha: sucesso.
7. Senha antiga: `401`.

### 3.3 Perfil

- nome alterado na Web persistiu após logout/login;
- `sessionStorage`/`localStorage` atualizados;
- Mobile exibiu o novo nome na Home e no Perfil.

### 3.4 Tarefas e sincronização

1. Tarefa criada na Web.
2. Visível no Mobile.
3. Concluída no Mobile.
4. Web recarregada exibiu `CONCLUÍDA`, com indicadores 1/0/0/1.
5. Reaberta na Web via novo botão; status voltou a `PENDENTE`.
6. Status alterado para `EM_ANDAMENTO` pelo select do formulário; badge e indicadores atualizados.

### 3.5 Categorias

1. Categoria “Sincronismo 3.2” criada no Mobile.
2. Formulário de tarefa da Web exibiu a categoria.
3. Exclusão no Mobile preserva tarefas (teste automatizado).

### 3.6 Isolamento e sessões

- Token de A não acessa dados de B (`403`/`404`).
- Troca de senha de A revoga apenas as sessões de A.
- Sessão de B permanece válida.
- Login sem token em `/usuarios/perfil` e `/usuarios/senha` retorna `401`.

## 4. Validação visual

- Web desktop 1440 px, temas claro e escuro: sem overflow (`scrollWidth = 1440`).
- Mobile Web 514 px: layout de segurança e confirmação sem overflow.
- Login Web e Mobile sem campo de IP/servidor.
- Capturas em `docs/assets/`:

| Arquivo | Conteúdo |
|---|---|
| `func-3.2-web-perfil-light.jpg` | Perfil/segurança na Web, tema claro |
| `func-3.2-web-perfil-dark.jpg` | Perfil/segurança na Web, tema escuro |
| `func-3.2-web-perfil-mobile.jpg` | Perfil/segurança na Web em 514 px |
| `func-3.2-mobile-perfil.jpg` | Perfil Mobile com edição de nome |
| `func-3.2-mobile-seguranca.jpg` | Tela de segurança Mobile |
| `func-3.2-mobile-senha-alterada.jpg` | Confirmação de sucesso |
| `func-3.2-mobile-login-aviso.jpg` | Login com aviso de senha alterada |

## 4.1 Produção pós-deploy (19/11/2026)

Deploy executado na release `20261119-func32c`:

| Verificação | Resultado |
|---|---|
| `/` | 200 |
| `/admin` | 200 |
| `/api/usuarios` sem token | 401 |
| `/api/usuarios/perfil` sem token | 401 |
| Login SUPER_ADMIN | 200 |
| `/api/usuarios/perfil` autenticado | 200, sem campo `senha` |
| Senha atual incorreta | 400, sem alteração |
| Atualização de nome (mesmo valor) | 200 + auditoria |
| `/api/admin/usuarios` | 200 (4 contas) |
| `/api/admin/auditoria` | 200 (57 registros) |
| `/api/admin/seguranca/tentativas` | 200 (15 tentativas) |
| Console do Admin | Sem erros |
| Perfil Web em 514 px | Sem overflow |

O primeiro deploy (`func32`) expôs um bug pré-existente de PostgreSQL em consultas administrativas
com filtros nulos. As releases `func32b` e `func32c` corrigiram, respectivamente, a construção do
`LIKE` e a tipagem dos parâmetros de data. A versão ativa é `func32c`.

## 5. Não executado / limitações

- Teste em aparelho físico Android/iOS.
- Build EAS/APK/AAB/IPA assinado.
- Deploy da 3.2 em produção.
- Recuperação de senha (funcionalidade ausente).
- Teste de carga concorrente.
- Teste de acessibilidade com leitor de tela real.

## 6. Conclusão

As lacunas P1 de conta/segurança foram corrigidas e validadas nas duas plataformas. Os fluxos de
tarefas e categorias permanecem funcionais e sincronizados. As pendências restantes são de
paridade de produto e de recuperação de senha, documentadas sem simulação.

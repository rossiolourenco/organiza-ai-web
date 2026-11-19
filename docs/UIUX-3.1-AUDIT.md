# UI/UX 3.1 — Auditoria

Data: 19/11/2026. Auditoria dos repositórios Web/Admin e Mobile antes das alterações.

## 1. Estado inicial

### Web/backend
- Branch `main`, árvore limpa, commit `c290cbb` (4 commits locais à frente de `origin/main`).
- Angular 19, login/cadastro/Home/formulário de tarefa.
- Admin com dashboard, usuários, segurança, firewall, monitoramento, auditoria e configurações.
- Produção em `https://organizaai.evox.net.br`, HTTPS e PostgreSQL ativos.
- Sidebar admin não persistia o estado recolhido.
- Textos de ambiente no admin ainda diziam “DEMO” mesmo em produção (banco PostgreSQL).

### Mobile
- Branch `main`, árvore limpa, commit `6a378cc`.
- Expo SDK 54, React Native 0.81.5, 5 abas e tema claro/escuro.
- Login exigia campo **Servidor**/IP, inclusive em produção.
- URL padrão vinha de `EXPO_PUBLIC_API_URL` ou vazio; sem fallback HTTPS oficial.
- Cadastro repetia o campo de servidor e validava senha mínima de 4 caracteres.
- Sem botão mostrar/ocultar senha.
- Tela de login com hierarquia de “tela interna”, não de produto final.

## 2. Problemas visuais e de experiência

| Área | Problema | Impacto |
|---|---|---|
| Mobile login | Campo de IP/host | Usuário final não deveria configurar infraestrutura |
| Mobile login | Sem HTTPS oficial por padrão | Risco de configuração manual e HTTP |
| Mobile login | Sem mostrar senha | Erros de digitação em mobile |
| Web login/cadastro | Cores fixas `#fff`, `#344054`, `#d0d5dd` | Tema escuro quebrado |
| Web login/cadastro | Card sem largura/estado visual consistente | Aparência de protótipo |
| Web cadastro | `alert()` para sucesso/erro | Feedback pobre e bloqueante |
| Web Home | Cores fixas em labels, bordas e ações | Contraste ruim no escuro |
| Web Home | Cards com `min-height: 235px` | Alturas inconsistentes |
| Web formulário | Cores fixas e cabeçalho sem quebra | Desktop ok, mobile apertado |
| Admin sidebar | Sem persistência do estado recolhido | Preferência perdida a cada reload |
| Admin sidebar | Sem tooltip/aria-label ao recolher | Descoberta ruim |
| Admin topo | “Ambiente DEMO” em produção | Informação incorreta |
| Global | Sem regra de redução de movimento | Acessibilidade |

## 3. O que já estava correto

- Backend, RBAC, auditoria e HTTPS.
- Admin com layout responsivo, tabelas com scroll e cards consistentes.
- Mobile com Safe Area, temas, componentes reutilizáveis e testes.
- API oficial respondendo; DNS e certificado válidos.

## 4. Decisões

1. Centralizar a API mobile em `src/services/config.ts`, com produção como padrão.
2. Remover completamente o campo de IP da UI e dos fluxos de login/cadastro.
3. Manter `EXPO_PUBLIC_API_URL` apenas como override de desenvolvimento.
4. Substituir cores fixas Web por tokens semânticos.
5. Persistir a sidebar e adicionar tooltips nativos.
6. Não alterar contratos de backend nem executar migrações.
7. Não publicar em produção nesta fase; apenas preparar release.

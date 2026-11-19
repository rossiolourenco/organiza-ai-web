# UI/UX 3.1 — Admin

## 1. Layout

- Sidebar com largura 252 px e modo recolhido de 78 px.
- Preferência de expansão persistida em `localStorage` (`organiza-admin-sidebar`).
- Ao recolher, ícones ficam centralizados e cada link expõe `aria-label` e `title` (tooltip nativo).
- Topbar fixa com ambiente, alertas, tema, usuário e logout.
- Conteúdo centralizado com largura máxima 1420 px.
- Em telas menores que 980 px, a sidebar vira off-canvas com overlay; o título da topbar é ocultado
  para preservar ações.

## 2. Ambiente

- Badge “PROD · PG”.
- Rodapé “Ambiente PROD · PostgreSQL · firewall DEMO”.
- A UI não sugere que o firewall real seja gerenciado.

## 3. Dashboard

- Quatro metric cards responsivos (4 → 2 → 1).
- Cards de alertas, política, operação, eventos e ações rápidas.
- Gráficos/indicadores com valores reais do backend.
- Skeletons e erro com retry.

## 4. Tabelas

- Container com `overflow-x` e `min-width`.
- Cabeçalhos em maiúsculas, ações por linha com `flex-wrap`.
- Paginação e filtros consistentes.
- Estados vazios, loading e erro.

## 5. Segurança e Firewall

- Central de segurança com tentativas reais, alertas e política.
- Firewall DEMO claramente marcado; o card “Firewall real” informa que o agente não está ativo.
- Nenhum controle aparenta executar nftables/UFW.

## 6. Temas e acessibilidade

- Toggle claro/escuro no topbar.
- Contraste em labels, tabelas e badges.
- Foco visível, áreas de toque ≥ 38–44 px e `prefers-reduced-motion`.
- Modais com `role="dialog"`/`alertdialog` e botão de fechar nomeado.

## 7. Responsividade

| Largura | Comportamento |
|---|---|
| > 1180 | 4 colunas de métricas |
| 980–1180 | 2 colunas |
| < 980 | sidebar off-canvas, conteúdo 18 px |
| < 720 | 1 coluna, topbar compacta |

## 8. Testes

- Build Angular e 11 testes Karma.
- Navegação e páginas do Admin validadas em produção antes desta revisão.
- Alterações 3.1 são de layout/preferência; nenhuma regra de autorização foi alterada.

# UI/UX 3.1 — Web

## 1. Login

- Card centralizado, largura máxima 440 px, padding responsivo.
- Brand com logo, nome e “Premium Experience”.
- Título “Bem-vindo de volta” e subtítulo curto.
- E-mail e senha com labels consistentes.
- Botão mostrar/ocultar senha acessível.
- Alternância de tema claro/escuro no topo.
- Erros e avisos inline (`role="alert"`/`status`).
- Botão com estado de carregamento.
- Rodapé de conexão segura; sem termos técnicos.
- Fundo com gradientes suaves usando `color-mix` sobre tokens.

Arquivos: `login.component.ts/html/css`.

## 2. Cadastro

- Mesmo layout e identidade do login.
- Feedback inline em vez de `alert`.
- Validação mínima de 8 caracteres, alinhada à política do backend.
- Mostrar/ocultar senha.
- Sucesso redireciona ao login com mensagem “Conta criada com sucesso”.

Arquivos: `cadastro.component.ts/html/css`.

## 3. Home / Tarefas (redesenho 3.1)

A área logada deixou de usar o layout legado e passou a seguir o design system:

- Topbar sticky com marca, alternância de tema e sair; sem duplicar navegação.
- Saudação com primeiro nome, data atual e resumo do dia.
- Quatro indicadores reais derivados das tarefas: total, pendentes, em andamento e concluídas.
- Filtros em card com grid de 4 colunas no desktop e 1 coluna no mobile, labels alinhados
  à esquerda, botão “Limpar filtros” com estado desabilitado.
- Cards de tarefa com status (ponto + badge), prioridade, categoria, prazo e ações.
- Cards com altura equalizada no grid; ações fixadas no rodapé do card.
- Estado vazio distingue “nenhuma tarefa” de “nenhum resultado com filtros”.
- Loading com spinner, erro com retry e feedback de sucesso dispensável.
- Exclusão por modal acessível (`role="alertdialog"`, Escape e backdrop), sem `confirm()`.
- Nenhum `alert()` no fluxo de tarefas.
- Grid `auto-fill` de 3 colunas no desktop e 1 coluna no mobile; sem scroll horizontal.

Evidências: `docs/assets/uiux-3.1-web-home-light.jpg`,
`uiux-3.1-web-home-dark.jpg`, `uiux-3.1-web-home-mobile.jpg` e
`uiux-3.1-web-delete-modal.jpg`.

## 4. Formulário de tarefa (redesenho 3.1)

- Layout em duas colunas: formulário (1,55fr) e gerenciamento de categorias (1fr).
- Card “Detalhes” com título, descrição, prioridade, prazo e categoria.
- Card lateral com criação, listagem e exclusão de categorias, com erro inline.
- Cabeçalho com link de retorno, título e subtítulo.
- Ações com estado de salvamento e validação de título.
- Erros inline com retry; sucesso via flash ao voltar para a Home.
- Empilha em uma coluna abaixo de 900 px; ações em coluna abaixo de 640 px.
- Sem `alert()`; a exclusão de categoria mantém a confirmação nativa nesta versão.

Evidências: `docs/assets/uiux-3.1-web-form.jpg` e `uiux-3.1-web-form-mobile.jpg`.

## 5. Tema escuro

O tema é aplicado por `data-theme="dark"` no `<html>` e variáveis CSS. Login, cadastro e Home agora
herdam superfícies, bordas e textos corretos. O toggle de autenticação usa `ThemeService`, que
persiste a preferência em `localStorage`.

## 6. Responsividade

| Largura | Comportamento |
|---|---|
| > 1180 | grid completo, filtros em linha |
| 800–1180 | header pode quebrar; filtros flexíveis |
| < 900 | formulário e categorias em coluna |
| < 800 | header/filtros em coluna, cards 1 coluna |
| < 640 | formulário e ações empilhados; filtros em 1 coluna |
| < 480 | autenticação com padding reduzido |

## 7. Orçamento de build

Com o novo CSS, os limites de `anyComponentStyle` foram ajustados para 14 kB (aviso) e 20 kB
(erro), e o bundle inicial para 600 kB (aviso). Isso evita falsos negativos sem remover o
controle de tamanho.

## 8. Acessibilidade

- Labels associados aos inputs.
- Botões com `aria-label` para ações de ícone.
- Mensagens com `role`.
- Foco visível global.
- Contraste conferido nos dois temas.
- `prefers-reduced-motion` respeitado.

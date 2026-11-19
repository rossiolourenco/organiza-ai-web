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

## 3. Home / Tarefas

- Cores fixas substituídas por tokens (`text-muted`, `surface-2`, `border`, `success`, `danger`).
- Cards sem altura mínima fixa; grid equaliza linhas naturalmente.
- Filtros com `flex-wrap` e inputs de data usando `surface-2`.
- Ações de cartão com quebra em telas pequenas.
- Cabeçalho e ações empilhados abaixo de 800 px.
- Grid 2 → 1 coluna em 800 px.
- `page` centralizada com largura máxima 1180 px.

## 4. Formulário de tarefa

- Cores fixas substituídas por tokens.
- Container 820 px centralizado.
- Cabeçalho empilhado em mobile.
- Ações em coluna abaixo de 640 px.
- Campos com foco visível e contraste no escuro.

## 5. Tema escuro

O tema é aplicado por `data-theme="dark"` no `<html>` e variáveis CSS. Login, cadastro e Home agora
herdam superfícies, bordas e textos corretos. O toggle de autenticação usa `ThemeService`, que
persiste a preferência em `localStorage`.

## 6. Responsividade

| Largura | Comportamento |
|---|---|
| > 1180 | grid completo, filtros em linha |
| 800–1180 | header pode quebrar; filtros flexíveis |
| < 800 | header/filtros em coluna, cards 1 coluna |
| < 640 | formulário e ações empilhados |
| < 480 | autenticação com padding reduzido |

## 7. Acessibilidade

- Labels associados aos inputs.
- Botões com `aria-label` para ações de ícone.
- Mensagens com `role`.
- Foco visível global.
- Contraste conferido nos dois temas.
- `prefers-reduced-motion` respeitado.

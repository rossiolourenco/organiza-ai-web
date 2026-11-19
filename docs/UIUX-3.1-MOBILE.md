# UI/UX 3.1 — Mobile

## 1. Login redesenhado

- Layout centralizado com Safe Area e KeyboardAvoidingView.
- Logo, nome “Organiza Aí”, mensagem curta e card.
- E-mail, senha com mostrar/ocultar, erro inline e botão com loading.
- Link para cadastro.
- Rodapé “Conexão segura HTTPS · sessão protegida por token”.
- **Campo de servidor/IP removido.**
- Cadastro também sem IP, com confirmação de senha e mínimo de 8 caracteres.

Arquivo: `src/features/auth/AuthScreen.tsx`.

## 2. Navegação

- Cinco abas: Início, Tarefas, Planejar, Foco, Perfil.
- Stack com cabeçalho próprio e botão de retorno.
- Modais em sheets com fechamento por botão e overlay.
- Áreas de toque preservadas.

## 3. Dashboard e telas

- Cards compactos, progress ring, métricas, agenda, hábitos e metas.
- Tarefas com busca, filtros, agrupamento e checklist.
- Planejamento com calendário, Kanban, plano e matriz.
- Foco com Pomodoro e modo concentração.
- Perfil com tema, hábitos, revisão, lixeira e exportação.
- Não houve remoção de funcionalidades.

## 4. Componentes

- `Input` agora suporta acessório à direita (usado no olho da senha) sem duplicar estilos.
- `PasswordInput` reutilizável em login/cadastro.
- Tokens de tema, espaçamento e tipografia mantidos.
- Textos técnicos de servidor removidos do perfil.

## 5. Safe Area e teclado

- Login usa `SafeAreaView` com top/bottom/left/right.
- ScrollView com `keyboardShouldPersistTaps`.
- KeyboardAvoidingView no iOS.
- `Screen` mantém SafeArea, largura máxima e padding inferior para FAB/tab bar.

## 6. Responsividade

- Login com largura máxima 460 px e padding adaptativo.
- Cards/screens com `maxWidth` e `flexWrap`.
- Tab bar e cabeçalhos preservam ícones e labels.
- Testado em viewport Web mobile; aparelhos físicos continuam pendentes.

## 7. Temas

- Claro/escuro/sistema via `ThemeProvider`.
- Login, cadastro, dashboard e telas internas usam tokens.
- Preferência por aparelho.

## 8. Acessibilidade

- Labels em todos os campos e botões de ícone.
- `accessibilityRole` em checkbox, switch, tab e progress.
- Áreas de toque e contraste preservados.
- Mensagens de erro com texto além de cor.

# UI/UX 3.1 — Design System

## 1. Direção

**Modern Productivity Premium + Enterprise Clean**: superfícies limpas, tipografia firme, roxo como
cor de ação, estados discretos e densidade equilibrada entre Web, Admin e Mobile.

## 2. Paleta

### Claro

| Token | Valor |
|---|---|
| background | `#F7F8FC` |
| surface | `#FFFFFF` |
| surfaceSecondary | `#F0F2F8` |
| primary | `#635BFF` |
| primaryHover | `#5148E5` |
| secondary | `#8B83FF` |
| text | `#172033` |
| muted | `#667085` |
| border | `#E5E7EF` |
| success | `#15803D` / soft `#E9F8EE` |
| warning | `#B45309` / soft `#FEF4E6` |
| danger | `#C02626` / soft `#FDECEC` |

### Escuro

| Token | Valor |
|---|---|
| background | `#0B1020` |
| surface | `#151C2E` |
| surfaceSecondary | `#1C2538` |
| primary | `#8B83FF` |
| text | `#F8FAFC` |
| muted | `#94A3B8` |
| border | `#2A3448` |
| onPrimary | `#0B1020` |

Cores de estado usam variantes `soft` para fundos de badges e alertas.

## 3. Tipografia

- Mobile: micro 11, caption 12, body 15, subtitle 17, title 21, heading 28, display 40.
- Web/Admin: títulos 24–31 px, corpo 13–15 px, metadados 11–12 px.
- Peso: 400 leitura, 600 labels, 700/800 títulos e botões.
- Fonte do sistema para não bloquear renderização.

## 4. Espaçamento, raio e elevação

- Escala base 4: 4, 8, 12, 16, 20, 24, 28, 36, 48.
- Raios: 8, 10, 12, 14, 16, 20, 22, 28 e pill.
- Sombras discretas (`0 10px 30px rgba(23,32,51,.07)`), reduzidas no escuro.
- Área de toque mínima 44–48 px.

## 5. Breakpoints

| Contexto | Largura |
|---|---|
| Web container | até 1180 px |
| Admin conteúdo | até 1420 px |
| Grid 4 → 2 | 1180 px |
| Sidebar off-canvas | 980 px |
| Web 2 → 1 coluna | 800 px |
| Formulários/autenticação | 640 px |
| Mobile | 320–430 px + tablet |

## 6. Componentes

- **Mobile:** `Screen`, `AppHeader`, `PremiumCard`, `Button`, `Input`, `PasswordInput`, `Chip`,
  `Segmented`, `Sheet`, `Confirm`, `Toast`, `ProgressRing`, badges, `FAB`, estados.
- **Web:** cards, filtros, grids, formulários, botões, badges, empty state.
- **Admin:** sidebar, topbar, `MetricCard`, tabelas, `ConfirmDialog`, modais, timeline, skeletons,
  paginação, alertas.
- **Auth Web:** card centrado, brand, password toggle, tema, mensagens inline.

## 7. Regras de consistência

1. Nenhuma tela Web deve declarar cor literal; usar `var(--token)`.
2. Mobile deve usar `useTheme()` e `tokens`.
3. Não centralizar listas/tabelas; centralizar apenas autenticação e estados vazios.
4. Formulários Web têm largura máxima de 820 px; autenticação 440 px.
5. Tabelas sempre dentro de container com overflow horizontal.
6. Feedback de erro junto ao formulário, não em `alert`.
7. Áreas de toque mobile nunca abaixo de 44 px.
8. Respeitar `prefers-reduced-motion` na Web.

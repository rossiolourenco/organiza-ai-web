# UI/UX 3.1 — Relatório de testes

Data: 19/11/2026.

## 1. Resumo

| Verificação | Resultado |
|---|---|
| Mobile testes | **49 testes / 11 suítes** |
| Mobile typecheck | Sem erros |
| Mobile lint | Sem erros/avisos |
| Expo Doctor | **18/18** |
| Expo install check | Dependências compatíveis |
| Expo export | Web, Android/Hermes e iOS/Hermes |
| Angular testes | **11 testes** |
| Angular build | Sucesso; aviso de orçamento CSS Home (6,45 kB) |
| Backend testes | **27 testes** |
| Backend build | Inalterado e funcional |

## 2. Mobile

```text
Test Suites: 11 passed
Tests:       49 passed
```

Cobertura adicional:

- configuração central da API;
- produção HTTPS por padrão;
- recusa de downgrade HTTP remoto;
- login sem campo de servidor;
- navegação protegida;
- tema;
- foco e armazenamento;
- tarefas, filtros, hábitos, planejamento, Eisenhower e revisão.

Export:

- Web: **1,3 MB**
- Android/Hermes: **3,1 MB**
- iOS/Hermes: **3,1 MB**

## 3. Angular

- Build de produção: 500 KB em `dist/frontend/browser`.
- 11 testes Karma aprovados.
- Login/cadastro responsivos.
- Tema escuro corrigido nas telas de autenticação e Home.
- Admin com sidebar persistente e tooltips.

Aviso conhecido (preexistente e agravado por pequenos ajustes): CSS da Home 6,45 kB contra orçamento
de 4 kB. Não é erro de build; pode ser dividido em arquivos/tokens em iteração futura.

## 4. Backend

- 27 testes aprovados, incluindo RBAC, rate limit do último SUPER_ADMIN, firewall, migração e
  compatibilidade Mobile/Web.
- Nenhuma migration nova na 3.1.
- Nenhuma alteração de contrato.

## 5. Validação visual

Capturas disponíveis em `docs/assets/`:

- `uiux-3.1-web-login.jpg`
- `uiux-3.1-web-cadastro.jpg`
- `uiux-3.1-mobile-login.jpg`
- `uiux-3.1-admin-dashboard.jpg`
- `uiux-3.1-ios-simulator.png`

Revisado:

- centralização e largura máxima;
- alinhamento de inputs e botões;
- espaçamento e hierarquia;
- tema claro;
- ausência do campo de IP no Mobile;
- botão de mostrar senha;
- mensagens inline.

O novo Admin foi validado em produção após a publicação em `/opt/organiza-ai/releases/20261119-uiux`:
login SUPER_ADMIN, dashboard, layout responsivo, ambiente PROD e ausência de erros de console.

O Mobile 3.1 foi aberto no simulador iOS 18.1 (iPhone 16 Pro, Xcode 16.1 em HD externo) via Expo
Go 54.0.7. A tela de login carregou com Safe Area correta, sem campo de servidor/IP, com mostrar
senha e rodapé de conexão segura. Evidência em `docs/assets/uiux-3.1-ios-simulator.png`. Simulador
não substitui teste em aparelho físico.

## 5.1 Revisão da área logada (corretiva)

Após a publicação inicial, foi identificado que a Home/Tarefas e o formulário ainda usavam o
layout legado. A publicação em si estava correta: o hash `main-VWSF7KOT.js` servido em produção
era idêntico ao build local. Não houve rollback nem perda de funcionalidades.

Correções aplicadas:

- Home redesenhada com topbar, saudação, indicadores reais, filtros em card, cards de tarefa,
  loading, erro com retry, feedback e modal de exclusão.
- Formulário redesenhado em duas colunas, com card de categorias e ações com estado de salvamento.
- Remoção de `alert()` dos fluxos de tarefas.
- Marca corrigida no tema claro (conflito com o token global da sidebar).
- Contraste do botão destrutivo corrigido no tema escuro via `:host-context`.
- Orçamentos de CSS/bundle ajustados em `angular.json`.

Validação visual:

- Desktop 1440 px, temas claro e escuro: sem overflow (`scrollWidth = 1440`).
- Mobile 514 px: sem overflow (`scrollWidth` menor que o viewport), cards em 1 coluna.
- Criar, concluir e excluir tarefas validados em produção; dados de teste removidos ao final.
- Nenhum erro de console.
- 11 testes Angular e build aprovados.

Capturas: `uiux-3.1-web-home-light.jpg`, `uiux-3.1-web-home-dark.jpg`,
`uiux-3.1-web-form.jpg`, `uiux-3.1-web-home-mobile.jpg`, `uiux-3.1-web-form-mobile.jpg` e
`uiux-3.1-web-delete-modal.jpg`.

## 6. Integração

- Mobile usa `https://organizaai.evox.net.br/api` por padrão.
- A prévia Expo Web em `localhost:8081` não autentica contra produção porque o CORS de produção
  permite apenas o domínio oficial. Isso é esperado e não afeta Android/iOS nativos.
- A conta administrativa foi usada apenas em testes; nenhum dado de usuário real foi alterado.
- Teste em aparelho físico Android/iOS não realizado.

## 7. Pendências

- Validação em aparelhos físicos.
- Captura visual do Admin 3.1 após publicação.
- Publicação concluída em 19/11/2026; builds Mobile assinados continuam pendentes.
- Redução do CSS da Home para eliminar o aviso de orçamento.

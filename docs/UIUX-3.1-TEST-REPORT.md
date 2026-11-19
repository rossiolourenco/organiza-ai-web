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

Revisado:

- centralização e largura máxima;
- alinhamento de inputs e botões;
- espaçamento e hierarquia;
- tema claro;
- ausência do campo de IP no Mobile;
- botão de mostrar senha;
- mensagens inline.

Não foi possível validar visualmente o novo Admin em produção porque a 3.1 **não foi publicada**;
o build e os testes passaram. A versão atual em produção continua sendo a 3.0/Admin 1.0.

## 6. Integração

- Mobile usa `https://organizaai.evox.net.br/api` por padrão.
- A prévia Expo Web em `localhost:8081` não autentica contra produção porque o CORS de produção
  permite apenas o domínio oficial. Isso é esperado e não afeta Android/iOS nativos.
- A conta administrativa foi usada apenas em testes; nenhum dado de usuário real foi alterado.
- Teste em aparelho físico Android/iOS não realizado.

## 7. Pendências

- Validação em aparelhos físicos.
- Captura visual do Admin 3.1 após publicação.
- Publicação da release 3.1 mediante autorização.
- Redução do CSS da Home para eliminar o aviso de orçamento.

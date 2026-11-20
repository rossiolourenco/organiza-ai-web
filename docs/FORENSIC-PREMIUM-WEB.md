# Investigação forense — interface Premium Web (3.3.1)

- **Data:** 2026-11-20
- **Escopo:** localizar a interface Premium Web original, identificar eventual regressão e propor recuperação
- **Produção observada:** `https://organizaai.evox.net.br` — 3.3.0-rc.1, `main` `48154ee`, bundle `main-NXMTECUN.js`
- **Conclusão antecipada:** a interface Premium Web **não foi perdida**. Ela é a que está publicada. Não há
  commit, branch, artefato ou backup que contenha uma versão Web diferente com sidebar/planejamento/foco.

## 1. Método

Executado em modo somente leitura sobre o repositório, o histórico, a máquina local, a cópia acadêmica e a VM:

```bash
git status / branch -avv / remote -v / ls-remote --heads
git log --all --oneline --decorate
git log --all --diff-filter=D --name-only -- frontend/src
git log --all -S"sidebar" -S"Planejar" -S"Foco" -S"PAINEL PESSOAL" -- frontend/src
git reflog --all ; git stash list ; git fsck --unreachable --lost-found
git log --all --pretty=format: --name-only | sort -u | grep -iE "sidebar|layout|dashboard|plano|foco"
git show 645d309^:frontend/src/...   (versão anterior ao redesign 3.1)
```

Também: inspeção de todas as releases em `/opt/organiza-ai/releases/*/public`, análise dos bundles
(`grep` de marcadores), configuração completa do Nginx, logs de acesso, `mdfind`/busca local por outros
projetos e leitura da cópia acadêmica.

## 2. Onde a interface Premium está

| Evidência | Valor |
|---|---|
| Commit do redesign Premium | `694e20f` — “feat: redesenha home e formulario de tarefas no padrao 3.1” (2026-11-19 17:00) |
| Commit anterior de UI/UX | `645d309` — “feat: refinamento UI/UX 3.1 em Web e Admin” |
| Evolução 3.2 | `c0e3685` — perfil e segurança da conta |
| Base 3.3 publicada | `48154ee` |
| Marcadores no bundle de produção | `PAINEL PESSOAL`, `Nova tarefa`, `indicadores` |
| Arquivo em produção | `/opt/organiza-ai/current/public/main-NXMTECUN.js` (477.997 bytes) |
| Documentação de referência | `docs/UIUX-3.1-WEB.md` e `docs/assets/uiux-3.1-web-home-dark.jpg` |

### Componentes Premium existentes (e ativos)

- `header.topbar` sticky com marca, “Premium Experience”, perfil, tema e sair.
- `section.welcome` com eyebrow **PAINEL PESSOAL**, saudação, data e ação “Nova tarefa”.
- `section.stats` com quatro indicadores: total, pendentes, em andamento, concluídas.
- `section.filters-card` com status, prioridade, prazo de/até e “Limpar filtros”.
- `tarefas-grid` com cards (status, prioridade, categoria, prazo, ações) e estado vazio Premium.
- Modal de exclusão acessível (`role="alertdialog"`, `aria-modal`, Escape/backdrop).
- Tema claro/escuro por design tokens.

## 3. Último ponto em que funcionava / primeiro em que “desapareceu”

- **Funcionava:** de `694e20f` (2026-11-19) até hoje. Verificado ao vivo em 2026-11-20 no `/home` de
  produção (captura no PR), com o bundle acima.
- **Desapareceu:** **não há ponto de desaparecimento no código.** `git log --diff-filter=D -- frontend/src`
  não retorna nenhum arquivo removido; não há stash, commit dangling ou branch remota alternativa
  (apenas `main`, `release/3.3-rc`, `docs/3.3-producao`, `chore/dev-standards-3.2`).

## 4. Evidência por release na VM

| Release | Bundle | Tamanho | Layout |
|---|---|---|---|
| `20261118` | `main-OL5Z6E7W.js` | 429.848 | **antigo** (sem `PAINEL PESSOAL`) |
| `20261119-uiux` | `main-VWSF7KOT.js` | 435.567 | intermediário (sem `PAINEL PESSOAL`) |
| `20261119-uiux2` | `main-ATVY5M6V.js` | 460.209 | Premium (`PAINEL PESSOAL`) |
| `20261119-func32*` | `main-NXMTECUN.js` | 477.997 | Premium |
| `20261120-3.3-rc1` (atual) | `main-NXMTECUN.js` | 477.997 | Premium |

O layout antigo existe apenas como **artefato histórico** da release `20261118`, que não é servida.
Não há `server_name`, alias ou diretório alternativo apontando para ela: o Nginx tem um único site,
um único `root` (`/opt/organiza-ai/current/public`) e `index.html` com `expires -1` (sem cache).

## 5. Causa raiz

**Classificação: regressão de código não comprovada.** Nenhuma evidência de commit que removeu,
substituiu ou reverteu componentes. As hipóteses compatíveis com as evidências são:

1. **Estado local do navegador** — aba antiga aberta, cache de `index.html` anterior à 3.1, extensão ou
   service worker de outra origem. Não há service worker neste projeto (`ngsw`/`workbox` ausentes).
2. **Confusão com a cópia acadêmica** (`/Users/.../organiza-ai - cópia`, commit `5bb727f`), que tem o
   layout antigo por definição e não é publicada.
3. **Expectativa de paridade com o Mobile** — ver §7. O Web nunca teve sidebar do usuário, planejamento
   diário nem foco; esses recursos existem apenas no Mobile.

### Como o proprietário pode confirmar em 30 segundos

1. Abrir `https://organizaai.evox.net.br` em **janela anônima** ou com `Cmd+Shift+R`.
2. DevTools → Network → recarregar: o script deve ser **`main-NXMTECUN.js`**.
   Se aparecer `main-OL5Z6E7W.js` (antigo), a origem é cache local.
3. A tela deve exibir **“PAINEL PESSOAL”** e os quatro cartões de indicadores.

## 6. Divergência de dados (1 tarefa na Web × 0 no Mobile)

Verificado no banco de produção (sem expor dados pessoais):

| Métrica | Valor |
|---|---|
| Tarefas ativas | 1 (`usuario_id=4`, `PENDENTE`, prazo **hoje**) |
| Usuários | 1 SUPER_ADMIN, 1 USER ativo, 2 ANONIMIZADOS |

No simulador, o Mobile exibia **0 em aberto** antes do reload e **1 em aberto** depois de reabrir o app.
Conclusão: **não é divergência de backend/regra/ambiente** — é **snapshot desatualizado** no Mobile, que
não atualiza sozinho (sem push/realtime). Web e Mobile usam a mesma conta, a mesma API e as mesmas regras
(`PENDENTE` = em aberto). Não há correção de dados necessária; a melhoria de refresh automático é P3.

## 7. Lacuna real: paridade visual com o Mobile (nunca implementada no Web)

O Mobile possui dashboard com hero (“Seu dia, com mais intenção”), “Planejar meu dia”, “Focar agora”,
anel de progresso, “Próxima melhor ação”, 5 abas e telas de planejamento/foco/revisão. Nada disso existe
no Web em nenhum commit (`-S"Planejar"`/`-S"Foco"` em `frontend/src` não retorna resultados; “sidebar”
aparece apenas no Admin).

Isso é **evolução de produto**, não recuperação. Proposta para aprovação (não implementada):

| Item | Proposta |
|---|---|
| Escopo | Hero “Seu dia”, anel de progresso, cards de foco, “Próxima melhor ação” e navegação lateral/abas no Web |
| Fonte de design | Componentes e tokens do Mobile (`src/theme`, `DashboardScreen`) traduzidos para Angular |
| Dados | Reutilizar `GET /api/tarefas` e cálculos equivalentes a `analytics()` (sem novo endpoint) |
| Fora de escopo | Foco/Pomodoro persistido, notificações Web, offline |
| Testes | Estruturais (como nesta entrega), responsividade e screenshot de referência |
| Risco | Médio (novo layout amplo); exige aprovação e não deve ser chamado de “restauração” |

## 8. Guarda de regressão adicionada

- `frontend/src/app/pages/home/home.premium.spec.ts` — **8 testes estruturais** que falham se a Home
  perder topbar/marca, eyebrow “PAINEL PESSOAL”, os quatro indicadores, o card de filtros, os cards de
  tarefa, o estado vazio Premium ou o modal acessível.
- `scripts/verify-premium.sh` passa a exigir o arquivo do teste, impedindo remoção silenciosa da guarda.
- Os testes rodam no `web-angular` (CI) e no gate local.

## 9. Limitações

- Não foi possível inspecionar o cache do navegador do proprietário; a confirmação depende do passo §5.
- Não existe captura da tela “antiga” citada; a descrição foi comparada com o layout acadêmico e com o
  artefato `20261118`.
- A proposta de paridade com o Mobile não foi implementada por exigir aprovação de escopo/design.

## 10. Conclusão

- **GO** para a interface Premium atual: ela está localizada, publicada, funcional e agora protegida por
  testes de regressão.
- **NO-GO** para “restauração”: não há interface Premium perdida para restaurar. Se o objetivo for o
  dashboard no padrão Mobile, isso é uma nova entrega (3.3.1), a ser aprovada separadamente.

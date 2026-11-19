# Organiza Aí 3.2 — Auditoria funcional

Data: 19/11/2026. Escopo: Web, Mobile, Admin, API e banco, com foco em lacunas funcionais e na
alteração de senha pelo próprio usuário.

## 1. Método

1. Inventário de rotas Web, telas Mobile e endpoints dos controllers.
2. Leitura do modelo de dados, autenticação, sessões, RBAC e políticas.
3. Execução dos testes existentes e criação de testes para as lacunas.
4. Validação ponta a ponta em backend local (H2) com contas de homologação.
5. Validação visual em desktop (1440 px) e mobile (514 px), temas claro e escuro.
6. Registro de evidências, pendências e riscos.

Não foram usados dados de usuários reais em testes destrutivos. A produção não foi alterada nesta
fase.

## 2. Inventário

| Item | Quantidade |
|---|---|
| Endpoints mapeados | 49 |
| Rotas Angular | 14 |
| Telas/rotas Mobile | 14 |
| Entidades JPA | 13 |
| Perfis | USER, ADMIN, SUPER_ADMIN |
| Suítes backend | 8 |

### Endpoints por área

- **Usuários:** cadastro público, login, logout, senha, perfil (GET/PUT), consulta do próprio usuário.
- **Tarefas:** CRUD, conclusão, lixeira, restauração, exclusão definitiva, filtros por status e prioridade.
- **Categorias:** CRUD com isolamento por dono.
- **Hábitos:** CRUD e registros por data.
- **Admin:** usuários, papéis, bloqueio, revogação de sessões, redefinição de senha, auditoria,
  segurança, política, firewall DEMO, monitoramento e configurações.

## 3. Resumo executivo

O núcleo do sistema está funcional: autenticação, sessões, tarefas, categorias, hábitos, Admin e
isolamento entre usuários são cobertos por testes e responderam corretamente. **Não foi encontrada
falha P0** (vazamento, autenticação comprometida ou perda de dados).

A lacuna P1 confirmada era de **interface**: o endpoint `PUT /api/usuarios/senha` existia, era
seguro e revogava sessões, mas o usuário comum não tinha como acessá-lo na Web nem no Mobile.
Isso foi corrigido com uma área de perfil/segurança nas duas plataformas.

Também foram corrigidas lacunas funcionais da Web:

- não era possível definir **Em andamento**;
- não era possível **reabrir** uma tarefa concluída;
- não havia edição do nome do próprio usuário;
- não havia uma área de perfil/segurança.

## 4. Problemas e situação

| ID | Prioridade | Problema | Situação |
|---|---|---|---|
| F-01 | P1 | Usuário comum sem alteração de senha na Web | **Corrigido** |
| F-02 | P1 | Usuário comum sem alteração de senha no Mobile | **Corrigido** |
| F-03 | P1 | Perfil do usuário sem edição permitida | **Corrigido** (nome) |
| F-04 | P2 | Web não permitia definir Em andamento | **Corrigido** |
| F-05 | P2 | Web não permitia reabrir tarefa | **Corrigido** |
| F-06 | P2 | Recuperação de senha autoatendimento ausente | **Lacuna documentada** |
| F-07 | P2 | Web sem páginas de hábitos, calendário, Kanban, foco, revisão e lixeira | **Lacuna de paridade documentada** |
| F-08 | P2 | Web sem recorrência, subtarefas, notas e etiquetas no formulário | **Lacuna de paridade documentada** |
| F-09 | P3 | Versão Mobile exibida como 1.0.0 na prévia Web | **Corrigido** |
| F-10 | P3 | Texto “Prioridade media” sem acentuação no Mobile | **Pendente** |

## 5. Decisões

1. Reutilizar o endpoint existente `PUT /api/usuarios/senha`, sem criar contrato paralelo.
2. Manter a revogação de todas as sessões após a troca; o cliente limpa a sessão local.
3. Expor no perfil apenas o que o backend suporta: nome editável; e-mail somente leitura.
4. Não criar botão de recuperação de senha sem SMTP e fluxo reais.
5. Não simular módulos Web inexistentes; registrar a paridade como pendência de produto.
6. Preservar compatibilidade: nenhuma migration, nenhuma mudança de contrato de tarefas/categorias.

## 6. Evidências

- `docs/FUNCTIONAL-MATRIX-3.2.md` — matriz funcional.
- `docs/FUNCTIONAL-GAPS-3.2.md` — lacunas e prioridades.
- `docs/USER-ACCOUNT-SECURITY.md` — contrato de conta e segurança.
- `docs/WEB-MOBILE-PARITY.md` — paridade entre plataformas.
- `docs/FUNCTIONAL-TEST-REPORT-3.2.md` — testes e resultados.
- `docs/assets/func-3.2-*` — capturas da Web e do Mobile.

## 7. Limitações

- Sem teste em aparelho físico Android/iOS nesta fase; a prévia Web do Mobile foi validada.
- Sem build assinado para lojas.
- A produção permanece na release 3.1 até autorização específica de deploy.
- A recuperação de senha não foi implementada porque depende de decisão de produto e SMTP.

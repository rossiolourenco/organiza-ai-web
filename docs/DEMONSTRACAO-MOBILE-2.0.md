# Roteiro de exposição — Organiza Aí

> Documento histórico da 2.0. Use [DEMONSTRACAO-MOBILE-3.0.md](DEMONSTRACAO-MOBILE-3.0.md) na versão atual.

## Preparação

1. Executar backend com perfil `local` (H2 em memória, isolado). Ao reiniciar, esse ambiente perde seus dados de demonstração.
2. Executar Mobile com `npm start`; para a prévia de navegador, `npm run web`.
3. Criar dados com o script a partir de `organiza-ai-web`:

```bash
export DEMO_PASSWORD='defina-uma-senha-para-esta-demonstracao'
node scripts/seed-demo.mjs
```

O script informa o e-mail único criado. Usar a senha definida na variável, sem a adicionar a arquivos ou commits. Cria uma nova conta, duas categorias e cinco tarefas DEMO. Só aceita localhost/127.0.0.1. Nenhum dado existente é sobrescrito ou excluído.

Para demonstrar estados vazios, cadastrar outra conta nova pela interface, sem executar seed para ela. Não atribuir exemplos a contas reais.

## Apresentação (8–12 minutos)

1. **Login:** informar servidor e conta DEMO. Explicar que o backend valida a sessão e a propriedade dos registros.
2. **Início:** mostrar data, pendências, atrasos, gráfico e próximos prazos. Os números vêm das tarefas dessa conta.
3. **Nova tarefa:** criar “DEMO · Apresentar o projeto”, prazo hoje, prioridade alta.
4. **Categoria:** cadastrar “DEMO Exposição”, escolher cor, associar à tarefa e editar o nome.
5. **Prioridade:** ordenar por prioridade, favoritar/fixar tarefa.
6. **Datas:** ativar limites iguais ao dia de hoje; combinar prioridade/categoria. Mostrar validação de intervalo invertido e limpar.
7. **Calendário:** selecionar dia, abrir agenda diária, alternar mês/semana e criar tarefa na data.
8. **Kanban:** mover tarefa entre Pendente, Em andamento e Concluída pelo menu de ações; atualizar e conferir persistência.
9. **Subtarefas:** abrir edição, adicionar etapas, marcar uma e salvar; reabrir para confirmar.
10. **Foco:** configurar um minuto para demonstração, iniciar sessão, pausar/retomar. Enquanto o tempo passa, continuar os itens seguintes. Mostrar depois a sessão registrada; não antecipar artificialmente o relógio.
11. **Produtividade:** conferir evento de conclusão, percentual, distribuição e metas. Explicar histórico legado sem data conhecida e foco local.
12. **Tema:** alternar claro/escuro e sistema. Voltar ao app para mostrar a preferência preservada.
13. **Categoria sem perda:** em conta DEMO, excluir categoria com confirmação; verificar que as tarefas continuam sem associação.

## Dependências da demonstração

- Tarefas/categorias/login precisam do backend acessível. Em aparelho físico, computador e telefone precisam conseguir se comunicar pela rede.
- Pomodoro e metas são locais; não dependem de serviços externos após restauração da sessão.
- Não apresentar a prévia Web como validação física Android/iOS.
- Sem notificações ou execução garantida em segundo plano. Retorno recupera o relógio por timestamps.
- Não há modo offline completo.

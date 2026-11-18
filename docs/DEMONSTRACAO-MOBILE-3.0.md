# Roteiro de exposição — Organiza Aí 3.0

Duração sugerida: 8 a 12 minutos. Tudo funciona no backend local isolado (H2). Não usar Supabase
durante a apresentação.

## Preparação (antes do público)

```bash
cd organiza-ai-web/backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=local

# em outro terminal
cd organiza-ai-web
export DEMO_PASSWORD='defina-uma-senha-forte'
export DEMO_EMAIL='demo@exposicao.invalid'
node scripts/seed-demo.mjs --reset

cd ../organiza-ai
npm run web
```

Abrir `http://localhost:8081`, informar servidor `localhost:8080` e a conta DEMO (e-mail e senha
definidos acima). O seed só aceita `localhost`/`127.0.0.1`, não sobrescreve contas reais e é idempotente.

## 1. Login e proteção de rota

Mostrar a tela de entrada e explicar a sessão Bearer com validade de sete dias. Entrar. O app abre
em “Início”.

## 2. Dashboard

- Saudação, data e anel de progresso do dia.
- Métricas: em aberto, concluídas, atrasadas e foco.
- “Próxima melhor ação”: mostrar os motivos (“está atrasada”, “prioridade alta”).
- Agenda do dia, hábitos de hoje, ritmo semanal, metas e horizonte.
- Destacar que nada é alterado sem aceite.

## 3. Criar tarefa completa

“Nova tarefa” → título e descrição → categoria, prioridade, prazo e horário → abrir “Planejar melhor”
→ duração estimada 20 min → abrir “Checklist e notas” → adicionar duas etapas → abrir “Matriz de
Eisenhower” → “Usar sugestão” → salvar.

Resultado esperado: toast “Tarefa criada.” e retorno à lista.

## 4. Tarefas: busca, filtros e agrupamento

- Buscar “DEMO”.
- Alternar “Por data / Por área / Lista”.
- Abrir “Filtros”: prioridade alta + somente atrasadas.
- Mostrar o progresso de checklist nos cards.

## 5. Calendário

Abrir “Planejar” → “Calendário”. Alternar mês/semana/dia. Destacar pontos de atraso. Usar
“Criar nesta data”.

## 6. Kanban

“Planejar” → “Kanban”. Abrir o menu de uma tarefa pendente → “Mover para Em andamento”. Fechar e
reabrir a tela: o status permanece. Explicar que o drag-and-drop foi evitado por instabilidade
entre plataformas; a ação é acessível por menu.

## 7. Planejamento inteligente

“Planejar” → “Plano”. Mostrar disponibilidade e pesos. “Aceitar” a primeira sugestão. A timeline
aparece com horário e duração. “Ignorar” remove a sugestão do dia. “Limpar plano” reinicia.

## 8. Matriz de Eisenhower

“Planejar” → “Matriz”. Mostrar os quatro quadrantes e as contagens. Abrir o menu de uma tarefa sem
classificação (se houver) → “Classificar na matriz” → “Usar sugestão”. Voltar: a tarefa mudou de
quadrante.

## 9. Hábitos

Perfil → “Hábitos”. Mostrar sequência, meta semanal e consistência de 28 dias. “Marcar” um hábito
(deve mudar para “Feito”). Criar “Ler 10 páginas” (diário, meta 7) e mostrar o card. Excluir depois.

## 10. Foco e concentração

Aba “Foco”. “Associar tarefa” → escolher uma tarefa. “Iniciar sessão”. Mostrar anel e contagem.
“Pausar” e “Retomar”. “Modo concentração”: tarefa, objetivo, checklist e cronômetro. Marcar uma
etapa. Sair, encerrar e registrar o tempo decorrido.

## 11. Revisão semanal

Perfil → “Revisão semanal”. Mostrar conclusões, taxa de prazos, foco, categorias, hábitos, metas e
recomendações. Navegar para a semana anterior para mostrar comparação.

## 12. Lixeira e desfazer

Na lista, menu de uma tarefa → “Excluir tarefa” → confirmar. O toast oferece “Desfazer”: clicar e
mostrar a tarefa de volta. Excluir outra, abrir Perfil → “Lixeira”, restaurar. Por fim, excluir
definitivamente um item e explicar que só é permitido a partir da lixeira.

## 13. Tema e acessibilidade

Perfil → “Aparência” → “Escuro”. Mostrar contraste e hierarquia. Testar fonte ampliada do sistema,
se possível. Mostrar “Exportar meus dados” (JSON sem token) e explicar os limites de acessibilidade.

## 14. Modo exposição

Perfil → “Modo exposição” (visível apenas para `@exposicao.invalid`). “Restaurar cenário local” limpa
foco e plano deste aparelho, sem tocar nas tarefas do banco. Repetir para demonstrar idempotência.

## 15. Encerramento técnico

- 45 testes Mobile, 19 backend, 11 Angular.
- Typecheck/lint limpos, Expo Doctor 18/18.
- Migração V003 aditiva, ainda não aplicada em produção.
- Sem commit/push; alterações locais nos dois repositórios.

## Se algo falhar

- **Sessão expirada:** entrar novamente; a senha está em `DEMO_PASSWORD`.
- **Sem conexão:** confirmar backend em `localhost:8080`; o app mostra faixa de aviso e não finge sucesso.
- **Dados alterados durante os ensaios:** rodar o seed com `--reset` e usar “Restaurar cenário local”.
- **Tema inesperado:** Perfil → Aparência → Sistema.
- **Plano estranho:** “Limpar plano” no Planejar; a disponibilidade fica em “Ajustes”.

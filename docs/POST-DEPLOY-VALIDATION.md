# Post-Deploy Validation — Organiza Aí 3.3 RC

Executar imediatamente após a ativação e novamente 24 h depois (checklist reduzido).

## 1. Infraestrutura

```bash
systemctl is-active organiza-ai-backend nginx postgresql
systemctl --failed
ss -lntp | grep -E ':8080|:5432'          # ambos em 127.0.0.1
curl -sI https://organizaai.evox.net.br/ | grep -i strict-transport-security
```

| Item | Esperado |
|---|---|
| `organiza-ai-backend` | `active` |
| `nginx` / `postgresql` | `active` |
| Portas 8080/5432 | somente loopback |
| HSTS | presente |
| Certificado | válido, sem expiração próxima |

## 2. Aplicação — HTTP

```bash
BASE=https://organizaai.evox.net.br
for rota in / /admin /login /cadastro; do
  printf '%s -> ' "$rota"; curl -s -o /dev/null -w '%{http_code}\n' "$BASE$rota"
done

curl -s -o /dev/null -w 'api_sem_token=%{http_code}\n' "$BASE/api/usuarios"      # 401
curl -s -o /dev/null -w 'admin_sem_token=%{http_code}\n' "$BASE/api/admin/usuarios"  # 401
```

Headers da API:

```bash
curl -sI "$BASE/api/usuarios" | grep -iE 'content-security-policy|x-content-type-options|x-frame-options|referrer-policy|permissions-policy|cache-control'
```

Esperado: `default-src 'none'`, `nosniff`, `DENY`, `no-referrer`, `camera=()`, `no-store`.

## 3. Build correto no ar

```bash
grep -o 'main-[A-Z0-9]*\.js' /opt/organiza-ai/current/public/index.html
ls -l /opt/organiza-ai/current/public/main-*.js
readlink -f /opt/organiza-ai/current
```

O hash do bundle deve ser **igual ao do artefato homologado**
(`dist-release/3.3.0-rc.1/organiza-ai-web-3.3.0-rc.1.tar.gz` → `index.html`).

> O 3.3 RC não alterou o código do frontend, portanto o hash esperado é o mesmo da 3.2
> (`main-NXMTECUN.js`). Isso **não** é sinal de deploy antigo: confira o `readlink` da release, a
> versão do `backend.jar` e o `VERSION` da release. O backend é o artefato que muda de fato.

## 4. Funcional — navegador

| Passo | Resultado esperado |
|---|---|
| Abrir `/` | Login Premium renderiza |
| Login com conta de teste | Redireciona para `/home` |
| Criar tarefa | Aparece na lista com status e prioridade |
| Concluir tarefa | Status “Concluída” e contador atualizado |
| Excluir tarefa | Some da lista (mantida na lixeira do banco) |
| Abrir `/perfil` | Nome, e-mail, papel, status e data de criação |
| Alterar senha | Redireciona ao login; senha antiga falha; nova funciona |
| Sair | Sessão encerrada e `localStorage` limpo |
| `/admin` com `USER` | Redireciona para login administrativo |
| `/admin` com `SUPER_ADMIN` | Dashboard + Security Console |

> Usar apenas contas de teste criadas para a validação. Não usar dados de clientes.

## 5. Funcional — Admin

| Página | Verificar |
|---|---|
| Dashboard | Indicadores carregam sem erro |
| Usuários | Lista, filtros, detalhes e sessões |
| Segurança | Política em vigor e eventos recentes |
| Firewall | Modo DEMO, regras e validação de conflitos |
| Monitoramento | CPU, memória, disco e sessões |
| Auditoria | Eventos do próprio deploy (login administrativo) |
| Configurações | Política e limites do ambiente |

## 6. Banco

```sql
SELECT count(*) FROM information_schema.tables
WHERE table_schema='public' AND table_name IN
 ('usuarios','categorias','tarefas','sessoes','tarefa_conclusoes','habitos',
  'habito_registros','tentativas_login','auditoria_admin','firewall_regras',
  'firewall_historico','firewall_snapshots','firewall_estado','politicas_seguranca'); -- 14

SELECT count(*) FROM politicas_seguranca WHERE id=1;  -- 1
SELECT count(*) FROM firewall_estado WHERE id=1;      -- 1
SELECT count(*) FROM usuarios;                        -- > 0
```

Checar logs:

```bash
grep -iE "error|exception|validate" /var/log/organiza-ai/backend.log | tail -30
journalctl -u organiza-ai-backend --since "30 min ago" --no-pager | grep -iE "error|exception"
```

## 7. Mobile (quando publicado)

| Item | Verificação |
|---|---|
| Abertura | App inicia sem crash |
| Login | Autentica contra `https://organizaai.evox.net.br` |
| Sessão | Fecha e reabre mantendo login |
| Tarefas | Listar, criar, concluir |
| Perfil/Segurança | Dados corretos e troca de senha |
| HTTPS | Sem erro de certificado ou domínio |

## 8. Monitoramento nas primeiras 24 h

- Taxa de 5xx em `/api/**` (esperada: 0 fora de manutenção).
- Falhas de login (401/429) sem pico anormal.
- Uso de CPU/memória do backend estável.
- `organiza-ai-backup.timer` executou e o dump é íntegro.
- Nenhum alerta de disco.

## 9. Gatilhos de rollback

Aplicar `ROLLBACK-PLAN.md` se ocorrer qualquer item:

- Backend não sobe ou falha `validate` do schema.
- `/` ou `/admin` retornando 5xx por mais de 2 minutos.
- Login quebrado para todas as contas.
- Erro 500 sistemático em tarefas/perfil.
- Perda ou corrupção de dados.

## 10. Registro

Preencher e anexar ao relatório do Prompt 2:

| Verificação | Resultado | Horário | Responsável |
|---|---|---|---|
| Backup | | | |
| Deploy backend | | | |
| Deploy frontend | | | |
| Smoke HTTP | | | |
| Login/CRUD | | | |
| Admin | | | |
| Banco | | | |
| Rollback necessário? | | | |

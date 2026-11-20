# Rollback Plan — Organiza Aí 3.3 RC

> Rollback preserva dados. Não existe rollback destrutivo de schema.

## 1. Estratégia

| Camada | Mecanismo | Tempo alvo |
|---|---|---|
| Frontend + backend | Troca do symlink `/opt/organiza-ai/current` para a release anterior + restart | < 2 min |
| Configuração Nginx | Backup do arquivo + `nginx -t` + reload | < 5 min |
| Banco | Dump pré-deploy (`pg_dump -Fc`) restaurado apenas se houver corrupção | 10–30 min |
| Firewall do host | Regras UFW versionadas; firewall da aplicação é DEMO | < 5 min |

## 2. Identificar a release atual e a anterior

```bash
readlink -f /opt/organiza-ai/current
ls -1t /opt/organiza-ai/releases | head -5
```

Guardar o caminho da release atual **antes** de qualquer mudança:

```bash
ATUAL="$(readlink -f /opt/organiza-ai/current)"
echo "$ATUAL" > /root/organiza-ai-release-atual.txt
```

## 3. Rollback de aplicação

1. Confirmar qual release anterior será ativada (a última que respondia corretamente):

```bash
ANTERIOR=/opt/organiza-ai/releases/<release-anterior>
test -f "$ANTERIOR/backend.jar" && test -d "$ANTERIOR/public" && echo "release íntegra"
```

2. Ativar e reiniciar:

```bash
ln -sfn "$ANTERIOR" /opt/organiza-ai/current
systemctl restart organiza-ai-backend
systemctl is-active organiza-ai-backend
systemctl reload nginx
```

3. Validar:

```bash
curl -s -o /dev/null -w 'root=%{http_code} admin=%{http_code}\n' https://organizaai.evox.net.br/
curl -s -o /dev/null -w 'api=%{http_code}\n' https://organizaai.evox.net.br/api/usuarios
journalctl -u organiza-ai-backend -n 50 --no-pager
```

## 4. Compatibilidade de schema

- O 3.3 RC **não altera o schema**. Portanto voltar para a release 3.2 é seguro do ponto de
  vista do banco.
- Se uma migration aditiva futura for aplicada, o backend anterior pode ignorar colunas novas,
  mas **não** remova colunas para “voltar” — isso destruiria dados.
- Nunca execute `DROP TABLE`, `DROP COLUMN`, `TRUNCATE` ou `DELETE` em produção durante rollback.

## 5. Rollback de banco (somente com corrupção comprovada)

```bash
systemctl stop organiza-ai-backend
sudo -u postgres pg_restore -l /var/backups/organiza-ai/<dump>.dump >/dev/null

# Guardar o estado atual antes de restaurar
sudo -u postgres pg_dump -Fc organiza_ai \
  -f /var/backups/organiza-ai/organiza_ai-antes-restore-$(date +%Y%m%d-%H%M).dump

sudo -u postgres pg_restore -d organiza_ai --clean --if-exists <dump>
sudo -u postgres psql -d organiza_ai -c "SELECT count(*) FROM usuarios;"
systemctl start organiza-ai-backend
```

Requer autorização explícita e backup do estado atual. `--clean` remove objetos existentes.

## 6. Rollback de configuração

- **Nginx:** `/etc/nginx/sites-available/organiza-ai` — manter cópia antes de editar;
  validar com `nginx -t` e aplicar com `systemctl reload nginx`.
- **Env:** `/etc/organiza-ai/backend.env` — manter cópia 0600 antes de editar; nenhuma variável
  nova é necessária no 3.3.
- **Systemd:** `/etc/systemd/system/organiza-ai-backend.service` — `daemon-reload` após alterar.

## 7. Rollback de firewall

O firewall da aplicação é **DEMO** e não altera nftables/UFW. Para o UFW do host:

```bash
ufw status numbered
ufw delete <número>
```

Não remova a regra 22 (SSH) antes de validar outro acesso.

## 8. Comunicação e critérios

1. Registrar horário, release ativada e motivo no `docs/RELEASE-HANDOFF.md` da próxima rodada.
2. Confirmar `/`, `/admin`, `/api/usuarios` e login antes de encerrar.
3. Manter o backup pré-deploy até a próxima release ser validada.
4. Não reaplicar a release com falha sem corrigir a causa e gerar novo RC.

## 9. O que **não** é afetado

- Dados de usuários, tarefas, categorias, hábitos, lixeira, auditoria e sessões.
- Certificados TLS (`certbot.timer`).
- Backups agendados (`organiza-ai-backup.timer`).
- Cópia acadêmica local (`organiza-ai - cópia`), que não participa do deploy.

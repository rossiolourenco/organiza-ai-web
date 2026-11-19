# Deploy VM — Backup

## 1. Estratégia

- `pg_dump` custom format (`-Fc`), diário às 02:30 (America/Sao_Paulo).
- Retenção de 14 dias.
- Diretório `/var/backups/organiza-ai` com permissão `0700`, arquivos `0600` root.
- Log em `/var/log/organiza-ai/backup.log`.
- Verificação de integridade com `pg_restore -l` após cada dump.
- Timer systemd persistente (executa após boot se perder a janela).
- Sem cópia externa: **não há redundância offsite** nesta entrega.

## 2. Script

`/usr/local/bin/organiza-backup.sh`:

```bash
#!/usr/bin/env bash
set -euo pipefail
umask 077
ENV_FILE=/etc/organiza-ai/backend.env
DIR=/var/backups/organiza-ai
LOG=/var/log/organiza-ai/backup.log
STAMP=$(date +%Y%m%d-%H%M%S)
FILE="$DIR/organiza_ai-$STAMP.dump"
DB_USER=$(grep '^DB_USERNAME=' "$ENV_FILE" | cut -d= -f2-)
DB_PASS=$(grep '^DB_PASSWORD=' "$ENV_FILE" | cut -d= -f2-)
export PGPASSWORD="$DB_PASS"
mkdir -p "$DIR" "$(dirname "$LOG")"
exec 9>/run/organiza-ai-backup.lock
flock -n 9 || exit 0
pg_dump -h 127.0.0.1 -U "$DB_USER" -d organiza_ai -Fc -f "$FILE"
pg_restore -l "$FILE" >/dev/null
chmod 600 "$FILE"
find "$DIR" -type f -name 'organiza_ai-*.dump' -mtime +14 -delete
echo "$(date -Is) OK $FILE" >>"$LOG"
```

## 3. Unidades systemd

- `organiza-ai-backup.service` (`Type=oneshot`, root, `Nice=10`)
- `organiza-ai-backup.timer` (`OnCalendar=*-*-* 02:30:00`, `Persistent=true`, atraso aleatório 300s)

```bash
systemctl enable --now organiza-ai-backup.timer
systemctl start organiza-ai-backup.service
systemctl list-timers organiza-ai-backup.timer
```

## 4. Evidência de execução

```text
2026-11-18T21:11:49-03:00 OK /var/backups/organiza-ai/organiza_ai-20261118-211149.dump 41497 bytes
```

Dois dumps foram gerados durante a homologação.

## 5. Teste de restauração

Executado em banco isolado:

```bash
runuser -u postgres -- createdb -O organiza_ai organiza_ai_restore_test
PGPASSWORD=<segredo> pg_restore -h 127.0.0.1 -U organiza_ai \
  -d organiza_ai_restore_test --no-owner <dump>
```

Resultado:

- 14 tabelas restauradas;
- 1 SUPER_ADMIN;
- política de segurança presente;
- banco de teste removido em seguida.

## 6. Restauração real (procedimento)

1. Colocar o backend em manutenção (`systemctl stop organiza-ai-backend`).
2. Fazer backup do estado atual antes de sobrescrever.
3. Criar banco novo ou limpar o existente com autorização.
4. `pg_restore` do dump escolhido.
5. Ajustar ownership para `organiza_ai`.
6. Subir backend e validar login/admin.
7. Se falhar, restaurar o backup anterior.

## 7. Limitações

- Backup e banco na mesma VM; perda do servidor perde ambos.
- Recomenda-se replicar os dumps para armazenamento externo criptografado.
- O teste de restauração não substitui um ensaio de disaster recovery completo.

# Production Deployment — Organiza Aí 3.3 RC

> Executar **somente** após autorização e após o merge do PR do RC. Nenhum comando deste
> documento foi executado nesta rodada.

## 1. Pré-requisitos

| Item | Valor / verificação |
|---|---|
| Host | `organiza-ai` — `organizaai.evox.net.br` |
| SO | Debian 13.7 |
| Java | OpenJDK 21 (`/usr/lib/jvm/java-21-openjdk-amd64`) |
| Maven | 3.9.9 |
| Node | 20.19.2 · npm 9.2.0 |
| PostgreSQL | 17.12, `organiza_ai`, loopback |
| Nginx | 1.26.3 |
| Serviço | `organiza-ai-backend` |
| Releases | `/opt/organiza-ai/releases/<timestamp>` e symlink `/opt/organiza-ai/current` |
| Env | `/etc/organiza-ai/backend.env` (0600) |
| Logs | `/var/log/organiza-ai/` |
| Backups | `/var/backups/organiza-ai/` |

Verificações antes de começar:

```bash
systemctl is-active organiza-ai-backend nginx postgresql
systemctl --failed
df -h /opt /var
curl -s -o /dev/null -w '%{http_code}\n' https://organizaai.evox.net.br/      # 200
curl -s -o /dev/null -w '%{http_code}\n' https://organizaai.evox.net.br/api/usuarios  # 401
```

## 2. Variáveis de ambiente (sem mudanças no 3.3)

O RC **não adiciona variável obrigatória**. O arquivo `/etc/organiza-ai/backend.env` deve
conter, no mínimo:

```text
SUPABASE_DB_URL=jdbc:postgresql://127.0.0.1:5432/organiza_ai
DB_USERNAME=organiza_ai
DB_PASSWORD=<segredo>
PORT=8080
SERVER_ADDRESS=127.0.0.1
APP_CORS_ORIGINS=https://organizaai.evox.net.br
FIREWALL_MODE=demo
SPRING_JPA_SHOW_SQL=false
LOGGING_FILE_NAME=/var/log/organiza-ai/backend.log
JAVA_TOOL_OPTIONS=-Xms256m -Xmx1024m -XX:MaxMetaspaceSize=256m -Dfile.encoding=UTF-8
```

`ADMIN_BOOTSTRAP_*` **não** deve existir (o SUPER_ADMIN já foi provisionado na 3.2).

## 3. Backup obrigatório

Seguir `DATABASE-MIGRATION-PLAN.md` §3. Sem dump íntegro confirmado, não prosseguir.

## 4. Criar a release a partir do commit homologado

Substituir `<COMMIT>` pelo SHA registrado em `RELEASE-HANDOFF.md`.

```bash
set -euo pipefail
REL=/opt/organiza-ai/releases/$(date +%Y%m%d-%H%M)-3.3-rc1
install -d -m 755 -o organiza -g organiza "$REL"
runuser -u organiza -- git clone --branch main \
  https://github.com/rossiolourenco/organiza-ai-web.git "$REL"
runuser -u organiza -- git -C "$REL" reset --hard <COMMIT>
runuser -u organiza -- git -C "$REL" rev-parse HEAD    # deve imprimir <COMMIT>
```

## 5. Backend

```bash
cd "$REL/backend"
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
runuser -u organiza -- env JAVA_HOME="$JAVA_HOME" ./mvnw -B -q -DskipTests package
install -m 640 -o organiza -g organiza target/backend-3.3.0-RC1.jar "$REL/backend.jar"
```

O `application.properties` do RC mantém `ddl-auto=validate`. Se o schema de produção divergir,
o serviço falhará ao subir — nesse caso, **não** troque `ddl-auto`; volte ao §4 de
`DATABASE-MIGRATION-PLAN.md`.

## 6. Frontend

```bash
cd "$REL/frontend"
runuser -u organiza -- npm ci --no-audit --no-fund
runuser -u organiza -- env NODE_OPTIONS=--max-old-space-size=2048 npm run build
rsync -a --delete dist/frontend/browser/ "$REL/public/"
chown -R organiza:organiza "$REL/public"
```

Conferir o hash do bundle no `index.html` gerado (será comparado no smoke test).

## 7. Ativação

```bash
ln -sfn "$REL" /opt/organiza-ai/current
systemctl restart organiza-ai-backend
systemctl is-active organiza-ai-backend
journalctl -u organiza-ai-backend -n 50 --no-pager
```

Nginx **não** muda: continua servindo `/opt/organiza-ai/current/public` e proxying `/api` para
`127.0.0.1:8080`. `systemctl reload nginx` apenas se o `nginx -t` for executado sem alteração.

## 8. Smoke tests (obrigatórios)

```bash
BASE=https://organizaai.evox.net.br
curl -s -o /dev/null -w 'root=%{http_code}\n'   "$BASE/"          # 200
curl -s -o /dev/null -w 'admin=%{http_code}\n'  "$BASE/admin"     # 200
curl -s -o /dev/null -w 'api=%{http_code}\n'    "$BASE/api/usuarios"  # 401
curl -s -o /dev/null -w 'login=%{http_code}\n'  -X POST "$BASE/api/usuarios/login" \
  -H 'Content-Type: application/json' -d '{"email":"inexistente@invalid","senha":"x"}'  # 401
curl -sI "$BASE/api/usuarios" | grep -iE 'content-security-policy|x-content-type-options|x-frame-options|cache-control'
```

Depois, no navegador: login, criar tarefa, abrir perfil, entrar no console administrativo.
Checklist completo em `POST-DEPLOY-VALIDATION.md`.

## 9. Critério de sucesso

- Serviço `active (running)` e sem erro de validação no log.
- `index.html` servido é o da nova release (hash do bundle confere).
- `/` e `/admin` em 200; `/api/usuarios` em 401.
- Login e CRUD funcionando com dados reais de teste controlado (sem dados de clientes).
- Nenhum 5xx no log durante os smoke tests.
- Backup pré-deploy preservado.

## 10. Se falhar

Aplicar `ROLLBACK-PLAN.md` imediatamente (troca de symlink e reinício). Não editar código na VM.

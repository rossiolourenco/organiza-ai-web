# Deploy VM — Instalação

## 1. Auditoria inicial

```bash
hostnamectl
nproc; free -h; swapon --show; df -hT; lsblk
ip -brief addr; ip route
ss -tulpn
systemctl --failed
sshd -T | grep -E '^(port|permitrootlogin|passwordauthentication|pubkeyauthentication)'
```

A VM estava limpa, com apenas SSH em 22, sem UFW e sem serviços de aplicação.

## 2. Correção do APT

A imagem usava repositório `cdrom`, que falhava. As linhas `deb cdrom:` foram comentadas em
`/etc/apt/sources.list` e nos arquivos de `/etc/apt/sources.list.d/`.

## 3. Pacotes

```bash
export DEBIAN_FRONTEND=noninteractive
apt-get update -y
apt-get -y upgrade
apt-get install -y openjdk-21-jdk-headless postgresql postgresql-contrib nginx git curl \
  ca-certificates unzip jq nodejs npm maven ufw certbot python3-certbot-nginx rsync \
  build-essential logrotate unattended-upgrades
```

## 4. Usuário e diretórios

```bash
adduser --disabled-password --gecos "" organiza
usermod -aG sudo organiza
install -d -m 700 -o organiza -g organiza /home/organiza/.ssh
install -m 600 -o organiza -g organiza /root/.ssh/authorized_keys /home/organiza/.ssh/authorized_keys

install -d -m 755 /opt/organiza-ai
install -d -m 755 -o organiza -g organiza /opt/organiza-ai/releases
install -d -m 750 /etc/organiza-ai
install -d -m 750 -o organiza -g organiza /var/log/organiza-ai
install -d -m 700 /var/backups/organiza-ai
```

## 5. Código

```bash
REL=/opt/organiza-ai/releases/20261118
runuser -u organiza -- git clone --branch main \
  https://github.com/rossiolourenco/organiza-ai-web.git "$REL"
runuser -u organiza -- git -C "$REL" reset --hard c2b99b0e1b37451daa27be368917bac83b60bcaa
```

Commit implantado: `c2b99b0e1b37451daa27be368917bac83b60bcaa`.

## 6. Variáveis de ambiente

Arquivo `/etc/organiza-ai/backend.env`, permissão `0600`, contendo:

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
ADMIN_BOOTSTRAP_EMAIL=admin@organizaai.evox.net.br
ADMIN_BOOTSTRAP_PASSWORD=<temporário>
ADMIN_BOOTSTRAP_NOME=Administrador Organiza Ai
```

Após o primeiro login, as três linhas `ADMIN_BOOTSTRAP_*` foram removidas e o serviço reiniciado.

## 7. Build do backend

```bash
cd "$REL/backend"
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
runuser -u organiza -- env JAVA_HOME="$JAVA_HOME" mvn -B test -Dspring.profiles.active=local
runuser -u organiza -- env JAVA_HOME="$JAVA_HOME" mvn -B package -DskipTests
install -m 640 -o organiza -g organiza target/backend-0.0.1-SNAPSHOT.jar "$REL/backend.jar"
```

## 8. Build do frontend

```bash
cd "$REL/frontend"
runuser -u organiza -- npm ci --no-audit --no-fund
runuser -u organiza -- env NODE_OPTIONS=--max-old-space-size=2048 npm run build
rsync -a --delete dist/frontend/browser/ "$REL/public/"
chown -R organiza:organiza "$REL/public"
ln -sfn "$REL" /opt/organiza-ai/current
```

Correções de produção aplicadas ao frontend:

- `API_BASE` relativo (`/api`) fora de localhost.
- Interceptor reconhece `/api` relativo.
- `inlineCritical: false` para compatibilidade com CSP sem `unsafe-inline` em scripts.
- Textos de ambiente ajustados para PostgreSQL.

## 9. Serviço

Arquivo `/etc/systemd/system/organiza-ai-backend.service`: usuário `organiza`, `EnvironmentFile`,
`Restart=on-failure`, `NoNewPrivileges`, `ProtectSystem=strict`, `ProtectHome`, log em
`/var/log/organiza-ai`.

```bash
systemctl daemon-reload
systemctl enable --now organiza-ai-backend
```

## 10. Cronologia de falha corrigida

1. Migrações aplicadas como `postgres` deixaram tabelas com owner `postgres`.
2. O serviço falhou com “permissão negada para tabela usuarios”.
3. `ALTER TABLE/SEQUENCE ... OWNER TO organiza_ai` e `GRANT ALL ON SCHEMA public` foram aplicados.
4. O backend reiniciou, validou o schema e provisionou o SUPER_ADMIN.

## 11. Verificação

```bash
systemctl is-active organiza-ai-backend
ss -lntp | grep -E ':8080|:5432'
curl -s -o /dev/null -w '%{http_code}' http://127.0.0.1:8080/api/usuarios   # 401
```

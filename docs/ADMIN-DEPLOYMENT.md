# Admin 1.0 — Implantação na VM

Referência: Ubuntu 24.04, Java 21, Node 20+, PostgreSQL 16, Nginx. Ajuste versões conforme a VM.

## 1. Pré-requisitos

```bash
sudo apt update
sudo apt install -y openjdk-21-jre-headless postgresql nginx git
# Node apenas se o frontend for compilado na VM
curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash -
sudo apt install -y nodejs
```

Verifique:

```bash
java -version
psql --version
node -v
nginx -v
```

## 2. Acesso SSH

No Mac, a chave já foi gerada em `~/.ssh/organiza_ai_admin`. Instale a pública na VM:

```bash
ssh-copy-id -i ~/.ssh/organiza_ai_admin.pub organiza@IP_DA_VM
ssh -i ~/.ssh/organiza_ai_admin organiza@IP_DA_VM
```

Recomendado: manter `PermitRootLogin prohibit-password` ou `no` e usar `sudo` para tarefas
administrativas. Não coloque a chave privada na VM nem no repositório.

## 3. Banco

```bash
sudo -u postgres psql
```

```sql
CREATE USER organiza WITH PASSWORD 'troque-esta-senha';
CREATE DATABASE organiza_ai OWNER organiza;
\q
```

Aplique as migrações **nesta ordem**:

```bash
sudo -u postgres psql -d organiza_ai -f database/schema.sql
sudo -u postgres psql -d organiza_ai -f database/migrations/V002__mobile_produtividade.sql
sudo -u postgres psql -d organiza_ai -f database/migrations/V003__premium_produtividade.sql
sudo -u postgres psql -d organiza_ai -f database/migrations/V004__admin_rbac_seguranca_firewall.sql
```

Antes de qualquer migração em banco existente: backup e ensaio em cópia.

```bash
pg_dump -Fc organiza_ai > backup-organiza-$(date +%F).dump
```

## 4. Variáveis de ambiente

Crie `/etc/organiza-ai/backend.env` com permissão `600`:

```bash
sudo mkdir -p /etc/organiza-ai
sudo tee /etc/organiza-ai/backend.env >/dev/null <<'EOF'
SUPABASE_DB_URL=jdbc:postgresql://localhost:5432/organiza_ai
DB_USERNAME=organiza
DB_PASSWORD=troque-esta-senha
PORT=8080
APP_CORS_ORIGINS=https://admin.seudominio.com
FIREWALL_MODE=demo
ADMIN_BOOTSTRAP_EMAIL=admin@seudominio.com
ADMIN_BOOTSTRAP_PASSWORD=troque-por-uma-senha-forte-12+
ADMIN_BOOTSTRAP_NOME=Administrador
EOF
sudo chmod 600 /etc/organiza-ai/backend.env
```

O bootstrap cria o primeiro SUPER_ADMIN somente se não existir nenhum. Depois do primeiro acesso,
recomenda-se remover `ADMIN_BOOTSTRAP_PASSWORD` do arquivo e reiniciar o serviço.

## 5. Backend

Compile localmente ou na VM:

```bash
cd organiza-ai-web/backend
./mvnw package -DskipTests
sudo mkdir -p /opt/organiza-ai
sudo cp target/backend-0.0.1-SNAPSHOT.jar /opt/organiza-ai/backend.jar
```

Serviço systemd `/etc/systemd/system/organiza-ai.service`:

```ini
[Unit]
Description=Organiza Ai Backend
After=network.target postgresql.service

[Service]
Type=simple
User=organiza
WorkingDirectory=/opt/organiza-ai
EnvironmentFile=/etc/organiza-ai/backend.env
ExecStart=/usr/bin/java -jar /opt/organiza-ai/backend.jar
Restart=on-failure
RestartSec=5
NoNewPrivileges=true
PrivateTmp=true
ProtectSystem=strict
ProtectHome=true
ReadWritePaths=/opt/organiza-ai

[Install]
WantedBy=multi-user.target
```

```bash
sudo systemctl daemon-reload
sudo systemctl enable --now organiza-ai
sudo systemctl status organiza-ai
curl -i http://127.0.0.1:8080/api/usuarios
```

Esperado: `401` (API viva e protegida).

## 6. Frontend Angular

```bash
cd organiza-ai-web/frontend
npm ci
npm run build
sudo mkdir -p /var/www/organiza-ai
sudo rsync -a --delete dist/frontend/browser/ /var/www/organiza-ai/
```

Se o build gerar `dist/frontend` diretamente, ajuste o caminho.

Nginx `/etc/nginx/sites-available/organiza-ai`:

```nginx
server {
    listen 80;
    server_name admin.seudominio.com;

    root /var/www/organiza-ai;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://127.0.0.1:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_read_timeout 30s;
    }
}
```

```bash
sudo ln -s /etc/nginx/sites-available/organiza-ai /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx
```

## 7. HTTPS

```bash
sudo apt install -y certbot python3-certbot-nginx
sudo certbot --nginx -d admin.seudominio.com
```

Depois ajuste `APP_CORS_ORIGINS` para o domínio HTTPS e reinicie o backend.

## 8. Firewall do host

Enquanto o módulo real do Organiza Aí estiver desabilitado, use o firewall do sistema com cautela:

```bash
sudo ufw allow OpenSSH
sudo ufw allow 'Nginx Full'
sudo ufw enable
sudo ufw status verbose
```

**Não** remova a regra SSH antes de testar uma segunda sessão. O painel Admin não altera o UFW.

## 9. Validação

1. Abra `https://admin.seudominio.com`.
2. Login com o SUPER_ADMIN do bootstrap.
3. Confirme `/admin/dashboard`, `/admin/usuarios`, `/admin/seguranca`, `/admin/firewall`,
   `/admin/monitoramento`, `/admin/auditoria`, `/admin/configuracoes`.
4. Crie um usuário, bloqueie, desbloqueie e revogue sessões.
5. Verifique a auditoria.
6. Confirme que o firewall está em `DEMO` e que nada foi alterado no host.
7. Teste Mobile/Web antigos: login e tarefas devem continuar funcionando.

## 10. Operação

```bash
sudo journalctl -u organiza-ai -f
sudo systemctl restart organiza-ai
```

- Não use `ddl-auto=update` em produção.
- Não rode seeds de demonstração em produção.
- Não habilite `FIREWALL_MODE=real` sem o agente e autorização.
- Mantenha `/etc/organiza-ai/backend.env` com `600`.
- Faça backup antes de migrações e rotação de credenciais.

## 11. Rollback

- Backend: restaure o JAR anterior e reinicie.
- Frontend: restaure o diretório anterior em `/var/www/organiza-ai`.
- Banco: mantenha as tabelas aditivas; se necessário, restaure o dump.
- Nunca remova colunas/tabelas em produção sem backup e autorização.

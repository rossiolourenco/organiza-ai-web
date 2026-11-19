# Deploy VM — Nginx

## 1. Virtual host

Arquivo: `/etc/nginx/sites-available/organiza-ai`  
Link: `/etc/nginx/sites-enabled/organiza-ai`  
Default removido.

```nginx
server {
    server_name organizaai.evox.net.br;
    root /opt/organiza-ai/current/public;
    index index.html;
    client_max_body_size 2m;
    ...
}
```

## 2. SPA

```nginx
location / {
    try_files $uri $uri/ /index.html;
}
```

Assets com hash recebem cache de 7 dias. `index.html` usa `expires -1` (no-cache). O `inlineCritical`
foi desativado no build Angular para não gerar handler inline bloqueado pela CSP.

## 3. API

```nginx
location /api/ {
    proxy_pass http://127.0.0.1:8080;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
    proxy_read_timeout 30s;
}
```

Sem reescrita de caminho: `/api/...` chega igual ao Spring Boot.

## 4. Segurança

- `server_tokens off`.
- HSTS, CSP, nosniff, DENY frame, Referrer-Policy, Permissions-Policy.
- Dotfiles negados.
- ACME em `/.well-known/acme-challenge/`.
- TLS 1.2/1.3 via `options-ssl-nginx.conf`.
- HTTP redireciona para HTTPS (301).

## 5. Certificado

```bash
certbot --nginx -d organizaai.evox.net.br --redirect
```

- Emissor: Let's Encrypt
- Validade: 18/11/2026 a 16/02/2027
- Renovação: `certbot.timer` ativo
- Renovação automática configurada; `certbot renew --dry-run` concluído com sucesso.

## 6. Validação

```bash
nginx -t
systemctl reload nginx
curl -I https://organizaai.evox.net.br/
curl -s -o /dev/null -w '%{http_code}' https://organizaai.evox.net.br/api/usuarios
```

Resultados: HTML 200, API 401 sem token, HTTP 301 para HTTPS.

## 7. Operação

```bash
tail -f /var/log/nginx/access.log
tail -f /var/log/nginx/error.log
systemctl reload nginx
```

Logs padrão em `/var/log/nginx/`; não contêm corpos de requisição com senhas.

# Deploy VM — Segurança

## 1. Acesso SSH

- Autenticação: somente chave Ed25519.
- `PasswordAuthentication no`
- `KbdInteractiveAuthentication no`
- `PubkeyAuthentication yes`
- `PermitRootLogin prohibit-password`
- `MaxAuthTries 3`
- `X11Forwarding no`, `AllowAgentForwarding no`, `AllowTcpForwarding no`
- UFW: `22/tcp LIMIT`.

Chaves:

- Privada local: `~/.ssh/organiza_ai_admin` (nunca enviada à VM).
- Pública instalada em `/root/.ssh/authorized_keys` e `/home/organiza/.ssh/authorized_keys`.
- Fingerprint: `SHA256:WkRvhj87fO1rvMiKW4ZHR5ftna9NmQoHW2FaDORAvrY`.

O usuário `organiza` tem sudo com senha (conta sem senha definida); root por chave permanece como
break-glass. Acesso de recuperação deve ser feito pelo console do Proxmox.

## 2. Firewall do host (UFW)

```text
22/tcp  LIMIT
80/tcp  ALLOW
443/tcp ALLOW
default deny incoming
default allow outgoing
```

- PostgreSQL e Spring Boot não são expostos.
- 8080 e 5432 responderam “fechada” externamente.
- UFW é oneshot: `systemctl is-active ufw` pode mostrar inactive, mas `ufw status` está ativo e
  as regras persistem no boot.

## 3. TLS e cabeçalhos

Certificado Let's Encrypt válido para `organizaai.evox.net.br`, renovação por `certbot.timer`.
Nginx envia:

- `Strict-Transport-Security: max-age=31536000; includeSubDomains`
- `Content-Security-Policy` sem `unsafe-inline` para scripts
- `X-Content-Type-Options: nosniff`
- `X-Frame-Options: DENY`
- `Referrer-Policy: no-referrer`
- `Permissions-Policy` restritiva

HTTP redireciona 301 para HTTPS. A aplicação Angular foi ajustada para não depender de handler
inline no `index.html`.

## 4. Autenticação e RBAC

- Token opaco de 256 bits; hash SHA-256 no banco; validade de 7 dias.
- Status da conta verificado em cada requisição.
- Sessões revogadas deixam de autenticar imediatamente.
- ADMIN gerencia apenas USER; SUPER_ADMIN gerencia administradores e políticas.
- Último SUPER_ADMIN ativo é protegido.
- Autoelevação/auto-bloqueio são recusados.
- Rate limiting de login e bloqueio temporário.
- Política de senha configurável; contas administrativas exigem 12+ com caixa e número.

## 5. Segredos

- `/etc/organiza-ai/backend.env`: `0600`.
- Senha inicial do SUPER_ADMIN: arquivo root-only, removido após a troca.
- Senha do banco não é exibida em logs nem no Git.
- Nenhuma credencial DEMO foi criada em produção.
- Logs não incluem senhas/tokens.

## 6. Banco

- PostgreSQL escuta somente `127.0.0.1`/`::1`.
- Role exclusiva `organiza_ai`, sem superusuário.
- Objetos pertencentes à role da aplicação.
- Acesso externo bloqueado por UFW.

## 7. Auditoria

- Eventos administrativos no banco, somente leitura pela API.
- Login administrativo, falhas, bloqueios, papéis, sessões, políticas e firewall.
- IP, user agent e correlação registrados.
- Senhas temporárias não são auditadas.

## 8. Riscos residuais

- Sem MFA.
- Sem WAF, IDS/IPS ou fail2ban dedicado; UFW limita SSH.
- Rate limit por instância/banco, não distribuído.
- Backup na mesma VM (sem offsite).
- Firewall real da aplicação desabilitado.
- Certbot `renew --dry-run` foi concluído com sucesso na segunda tentativa; certificado real válido e timer ativo.

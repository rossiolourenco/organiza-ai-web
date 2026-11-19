# Deploy VM — Operações

## 1. Serviços

```bash
systemctl status organiza-ai-backend
systemctl restart organiza-ai-backend
systemctl status nginx postgresql chrony
systemctl list-timers organiza-ai-backup.timer certbot.timer
```

## 2. Logs

```bash
journalctl -u organiza-ai-backend -f
tail -f /var/log/organiza-ai/backend.log
tail -f /var/log/organiza-ai/backup.log
tail -f /var/log/nginx/access.log
tail -f /var/log/nginx/error.log
```

Logrotate diário, 14 arquivos, compressão.

## 3. Health checks

```bash
curl -s -o /dev/null -w '%{http_code}\n' http://127.0.0.1:8080/api/usuarios   # 401
curl -s -o /dev/null -w '%{http_code}\n' https://organizaai.evox.net.br/      # 200
curl -s -o /dev/null -w '%{http_code}\n' https://organizaai.evox.net.br/api/usuarios  # 401
ss -lntp | grep -E ':8080|:5432|:443|:80|:22'
ufw status verbose
```

## 4. Atualização de versão

```bash
REL=/opt/organiza-ai/releases/$(date +%Y%m%d-%H%M)
runuser -u organiza -- git clone --branch main https://github.com/rossiolourenco/organiza-ai-web.git "$REL"
runuser -u organiza -- git -C "$REL" reset --hard <commit>
# build backend/frontend conforme DEPLOY-VM-INSTALLATION
ln -sfn "$REL" /opt/organiza-ai/current
systemctl restart organiza-ai-backend
systemctl reload nginx
```

Sempre validar `/api/usuarios` (401), `/admin` e backup antes.

## 5. Credencial inicial do SUPER_ADMIN

- Arquivo root-only: `/root/.organiza-admin-password` (`0600`).
- E-mail: `admin@organizaai.evox.net.br`.
- Após o primeiro acesso, altere em **Configurações → Alterar minha senha**.
- Depois remova o arquivo:

```bash
shred -u /root/.organiza-admin-password
```

- A troca revoga todas as sessões e exige novo login.
- Não há cadastro público de administrador.

## 6. Recuperação de acesso administrativo

1. Acesse a VM pelo console do Proxmox ou por SSH root com chave.
2. Se necessário, redefina a senha via API/console com token de SUPER_ADMIN.
3. Se todos os SUPER_ADMIN estiverem bloqueados, use o console e um procedimento SQL autorizado
   para reativar a conta, seguido de auditoria.
4. Nunca remova a proteção do último SUPER_ADMIN em produção.

## 7. Backup manual

```bash
systemctl start organiza-ai-backup.service
tail -5 /var/log/organiza-ai/backup.log
ls -lh /var/backups/organiza-ai
```

## 8. Renovação TLS

```bash
certbot certificates
systemctl status certbot.timer
certbot renew --dry-run
```

Se o dry-run retornar rate limit do staging, repetir mais tarde. O certificado real e a renovação
automática permanecem válidos.

## 9. Firewall do host

```bash
ufw status numbered
ufw allow 443/tcp
ufw delete <número>
```

Não remova a regra 22 sem outra sessão testada. O firewall da aplicação é somente DEMO.

## 10. Disco/memória

```bash
df -h /
free -h
du -sh /var/backups/organiza-ai /var/log/organiza-ai /opt/organiza-ai
```

## 11. Rotina

- Diário: conferir backup e serviços.
- Semanal: revisar auditoria e tentativas de login.
- Mensal: validar restauração em banco isolado e atualizar SO.
- A cada release: backup, build, deploy, smoke test e rollback preparado.

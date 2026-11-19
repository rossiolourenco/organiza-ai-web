# Deploy VM — Rollback

## 1. Princípios

- O banco usa migrações aditivas; não há rollback destrutivo automático.
- A release fica em `/opt/organiza-ai/releases/<data>` e `current` é um symlink.
- O frontend é estático; rollback pode ser feito por troca de diretório.
- Segredos e dados não são sobrescritos por rollback de aplicação.

## 2. Rollback de aplicação (backend + frontend)

1. Identificar a release anterior:

```bash
ls -1 /opt/organiza-ai/releases
readlink -f /opt/organiza-ai/current
```

2. Criar uma release a partir do commit anterior (exemplo `c5302ee`):

```bash
REL=/opt/organiza-ai/releases/rollback-$(date +%Y%m%d-%H%M)
runuser -u organiza -- git clone --branch main https://github.com/rossiolourenco/organiza-ai-web.git "$REL"
runuser -u organiza -- git -C "$REL" reset --hard <commit-anterior>
```

3. Compilar backend/frontend na release e copiar artefatos. O banco permanece com V004; o backend
   anterior pode não entender as colunas novas, então **não** faça rollback de código sem avaliar
   compatibilidade com o schema atual. Prefira corrigir a release atual.

4. Trocar o symlink:

```bash
ln -sfn "$REL" /opt/organiza-ai/current
systemctl restart organiza-ai-backend
systemctl reload nginx
```

## 3. Rollback de configuração Nginx

```bash
cp /etc/nginx/sites-available/organiza-ai /root/organiza-ai.nginx.bak
# editar
nginx -t && systemctl reload nginx
```

Se o certificado estiver válido, não remova os blocos 443.

## 4. Rollback do banco

- Preferencial: restaurar o dump anterior em banco novo e apontar a aplicação.
- Nunca execute `DROP`/`DELETE` em produção sem backup e autorização.
- As migrações V002–V004 são aditivas; manter as colunas é mais seguro.

## 5. Rollback de firewall

O firewall real da aplicação nunca foi alterado. Para o UFW do host:

```bash
ufw status numbered
# remover regra específica:
ufw delete <número>
```

Não remova a regra 22 antes de testar outra sessão SSH.

## 6. Rollback de deploy completo

1. Parar backend.
2. Restaurar release anterior + `current`.
3. Restaurar dump compatível, se necessário.
4. Subir backend, validar `/api/usuarios` (401) e `/admin`.
5. Revalidar HTTPS e firewall.

## 7. Critério de segurança

Qualquer rollback deve preservar:

- acesso SSH;
- PostgreSQL local;
- certificado TLS;
- dados de usuários;
- auditoria.

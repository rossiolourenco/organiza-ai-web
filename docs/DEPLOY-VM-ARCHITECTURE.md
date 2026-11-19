# Deploy VM — Arquitetura

## 1. Identificação

| Item | Valor |
|---|---|
| Provedor | Proxmox (KVM) |
| IP público | `45.165.159.86` |
| Hostname | `organiza-ai` |
| Domínio | `organizaai.evox.net.br` |
| Debian | 13.7 (trixie) |
| Kernel | 6.12.117+deb13-amd64 |
| vCPU | 4 |
| RAM | 3,8 GB |
| Swap | 2,1 GB |
| Disco | 38 GB (32 GB livres após instalação) |
| Timezone | America/Sao_Paulo |
| NTP | chrony (`c.ntp.br`) |

## 2. Versões

| Componente | Versão |
|---|---|
| Java | OpenJDK 21.0.13 |
| Maven | 3.9.9 |
| Spring Boot | 4.1.1 (JAR do commit `c2b99b0`) |
| PostgreSQL | 17.12 |
| Nginx | 1.26.3 |
| Node.js | 20.19.2 |
| npm | 9.2.0 |
| Certbot | 4.0.0 |
| Angular | 19.2 |

## 3. Fluxo

```
Internet
  │ 443 (TLS Let's Encrypt)
  ▼
Nginx :443
  ├─ /            → /opt/organiza-ai/current/public (Angular SPA)
  └─ /api/...     → 127.0.0.1:8080 (Spring Boot)
                         │
                         ▼
                    PostgreSQL 17
                    127.0.0.1:5432
```

- Frontend e API usam a mesma origem; o Angular chama `/api`.
- O backend escuta somente em loopback.
- PostgreSQL escuta somente em loopback.
- Portas 22/80/443 são as únicas públicas no UFW.
- Firewall da aplicação está em modo **DEMO**; não altera nftables/UFW.

## 4. Estrutura de diretórios

```
/opt/organiza-ai/
  releases/20261118/       código e artefatos da release
  current -> releases/20261118
    backend.jar
    public/                build Angular
    frontend/              fonte Angular
    backend/               fonte Spring Boot
/etc/organiza-ai/backend.env         segredos, 0600
/var/log/organiza-ai/                backend.log, backup.log
/var/backups/organiza-ai/            dumps PostgreSQL, 0700
```

## 5. Serviços systemd

| Serviço | Função | Estado |
|---|---|---|
| `organiza-ai-backend` | Spring Boot | active/enabled |
| `nginx` | TLS + SPA + proxy | active/enabled |
| `postgresql` | banco | active/enabled |
| `chrony` | NTP | active/enabled |
| `ufw` | firewall (oneshot) | enabled; `ufw status` active |
| `organiza-ai-backup.timer` | backup diário | active/enabled |
| `certbot.timer` | renovação TLS | active/enabled |

## 6. Segurança de borda

- SSH somente por chave; senha e teclado interativo desabilitados.
- root permitido apenas por chave (`prohibit-password`).
- UFW: 22 limitado, 80/443 liberados, resto negado.
- TLS Let's Encrypt com HSTS, CSP, X-Frame-Options, nosniff, Referrer-Policy e Permissions-Policy.
- API sem cookie de sessão; token Bearer.
- Endpoints administrativos exigem papel e são auditados.

## 7. Limites

- Firewall real do Organiza Aí não implementado.
- Backup reside na própria VM; não há réplica externa.
- Sem HA, load balancer ou múltiplas instâncias.
- Mobile não foi publicado em loja; integra via HTTPS.

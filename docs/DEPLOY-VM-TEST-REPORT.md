# Deploy VM — Relatório de testes

Data: 18/11/2026. Resultados reais coletados na VM `45.165.159.86`.

## 1. Infraestrutura

| Teste | Resultado |
|---|---|
| Debian 13.7 / kernel 6.12.117 | OK |
| 4 vCPU / 3,8 GB RAM / 2,1 GB swap | OK |
| Disco 38 GB (32 GB livres) | OK |
| NTP chrony (`c.ntp.br`) | Sincronizado |
| Timezone America/Sao_Paulo | OK |
| Nenhuma unidade systemd falhada | 0 failed |

## 2. SSH

| Teste | Resultado |
|---|---|
| `root` com chave Ed25519 | OK |
| `organiza` com a mesma chave | OK |
| Login por senha | `Permission denied (publickey)` |
| `PasswordAuthentication` | `no` |
| `PermitRootLogin` | `prohibit-password` |
| Sessão nova após hardening | OK |
| Chave privada ausente na VM | OK (somente pública) |

## 3. PostgreSQL

| Teste | Resultado |
|---|---|
| Versão | 17.12 |
| Escuta | `127.0.0.1:5432` e `[::1]:5432` |
| Porta 5432 externa | fechada |
| Migrações em homologação | 14 tabelas |
| Migrações em produção | 14 tabelas |
| `ddl-auto=validate` | iniciou sem erro |
| Ownership corrigido | backend conectou |
| Dados após restart | SUPER_ADMIN persistiu |
| Backup/restauração | 14 tabelas, 1 SUPER_ADMIN, política presente |

## 4. Backend

Comando na VM:

```bash
mvn -B test -Dspring.profiles.active=local
```

Resultado: **27 testes, 0 falhas**:

- 8 `AdminApiTests`
- 10 `MobileApiTests`
- 7 `PremiumApiTests`
- 1 `MigrationTests`
- 1 contexto Spring

Serviço: `active/enabled`, porta `127.0.0.1:8080`, porta 8080 externa fechada.

## 5. Frontend

- Build de produção na VM: sucesso (Angular 19, 476 kB inicial, 117 kB transfer).
- Aviso preexistente de orçamento CSS da Home (6,25 kB).
- 11 testes Karma aprovados localmente.
- SPA servida por Nginx com fallback.
- CSP sem erros no console após desativar `inlineCritical`.
- Login USER → `/home`.
- Login ADMIN → `/admin`.
- Todas as páginas do admin carregaram sem erro de console.

## 6. HTTPS e DNS

| Teste | Resultado |
|---|---|
| A `organizaai.evox.net.br` | `45.165.159.86` |
| AAAA | ausente |
| HTTP externo | 200 e 301 para HTTPS |
| HTTPS externo | 200 |
| Certificado | Let's Encrypt, 18/11/2026–16/02/2027 |
| Cadeia | válida |
| API sem token | 401 |
| HSTS/CSP/X-Frame/nosniff | presentes |
| Renovação | `certbot.timer` ativo |
| `certbot renew --dry-run` | **sucesso** (segunda tentativa) |

## 7. RBAC

| Teste | Resultado |
|---|---|
| SUPER_ADMIN login | OK, 11 permissões |
| ADMIN login | OK, 9 permissões |
| ADMIN altera política | 403 |
| ADMIN altera papel | 403 |
| USER acessa `/admin` | 403 (testado em suíte) |
| Revogação de sessão | token invalidado |
| Autoelevação/autobloqueio | 400 (suíte) |
| Último SUPER_ADMIN | protegido, inclusive contra rate limit automático (suíte) |

## 8. Funcional Web/Mobile/API

Fluxo real executado em produção:

1. Cadastro de usuário.
2. Login.
3. Categoria criada.
4. Tarefa com checklist, prazo, etiquetas e Eisenhower.
5. Conclusão da tarefa.
6. Hábito criado e registro do dia.
7. Listagem de tarefas e hábitos.
8. Limpeza: tarefas, categorias e hábitos removidos.
9. Conta anonimizada.

Resultados: criação 200, conclusão `CONCLUIDA` com 1 conclusão, hábito com 1 registro.
Dados de teste não permaneceram ativos.

## 9. Firewall host

```text
22/tcp LIMIT
80/tcp ALLOW
443/tcp ALLOW
default deny incoming
```

- 8080 e 5432 fechadas externamente.
- SSH preservado e testado após ativação.
- Firewall da aplicação: DEMO, 5 regras simuladas, sem alteração real.

## 10. Persistência

Após `systemctl restart organiza-ai-backend`:

- login SUPER_ADMIN OK;
- banco PostgreSQL OK;
- monitoramento OK;
- firewall DEMO 5 regras;
- nenhuma unidade falhada.

## 11. Pendências de teste

- Teste físico Android/iOS não realizado.
- Firewall real (nftables/UFW pelo painel) não implementado.
- `certbot renew --dry-run` deve ser repetido quando o staging liberar.
- Teste de carga/penetração não executado.
- Backup offsite não configurado.

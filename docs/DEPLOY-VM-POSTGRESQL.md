# Deploy VM — PostgreSQL

## 1. Instalação

PostgreSQL 17.12 instalado via Debian, cluster `17/main`, escutando apenas em loopback.

```bash
pg_lsclusters
ss -lntp | grep 5432
```

## 2. Objetos

```sql
CREATE ROLE organiza_ai LOGIN PASSWORD '<segredo>';
CREATE DATABASE organiza_ai OWNER organiza_ai;
CREATE DATABASE organiza_ai_homolog OWNER organiza_ai;  -- usado no ensaio
```

- A aplicação usa `organiza_ai`, nunca `postgres`.
- `organiza_ai_homolog` e `organiza_ai_restore_test` foram removidos/existem apenas para ensaio.
- A role não é superusuária.

## 3. Migrações aplicadas

Ordem executada em homologação e depois em produção:

1. `database/schema.sql`
2. `V002__mobile_produtividade.sql`
3. `V003__premium_produtividade.sql`
4. `V004__admin_rbac_seguranca_firewall.sql`

Resultado: **14 tabelas** em `public`.

O ensaio em `organiza_ai_homolog` validou toda a sequência antes da produção. Não houve uso de H2
como banco definitivo.

## 4. Correção de ownership

As migrações foram executadas como `postgres`; as tabelas nasceram com owner `postgres` e o backend
recebeu “permissão negada”. Correção aplicada nos dois bancos:

```sql
DO $$
DECLARE r RECORD;
BEGIN
  FOR r IN SELECT tablename FROM pg_tables WHERE schemaname='public' LOOP
    EXECUTE 'ALTER TABLE public.'||quote_ident(r.tablename)||' OWNER TO organiza_ai';
  END LOOP;
  FOR r IN SELECT sequence_name FROM information_schema.sequences WHERE sequence_schema='public' LOOP
    EXECUTE 'ALTER SEQUENCE public.'||quote_ident(r.sequence_name)||' OWNER TO organiza_ai';
  END LOOP;
END $$;
GRANT ALL ON SCHEMA public TO organiza_ai;
```

## 5. Validação do schema

- Backend inicia com `ddl-auto=validate`.
- Hibernate conectou em `jdbc:postgresql://127.0.0.1:5432/organiza_ai`.
- Dialect PostgreSQL, versão 17.12.
- Nenhum erro de validação após ownership.

## 6. Dados atuais

| Item | Quantidade |
|---|---:|
| Tabelas | 14 |
| Usuários | 3 (1 SUPER_ADMIN ativo + 2 contas de teste anonimizadas) |
| Tarefas | 0 |
| Categorias | 0 |
| Hábitos | 0 |
| Política de segurança | 1 |
| Regras firewall DEMO | 5 |

As contas de teste foram anonimizadas após a homologação; nenhum dado de demonstração foi mantido.

## 7. Acesso

```bash
sudo -u postgres psql -d organiza_ai
sudo -u postgres psql -d organiza_ai -c '\dt'
```

A role da aplicação acessa via TCP local com senha (SCRAM). Porta 5432 não é exposta.

## 8. Backup e restauração

Ver `DEPLOY-VM-BACKUP.md`. O dump foi restaurado em banco isolado e apresentou 14 tabelas,
1 SUPER_ADMIN e a política de segurança.

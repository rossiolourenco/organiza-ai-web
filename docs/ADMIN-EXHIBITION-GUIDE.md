# Admin 1.0 — Roteiro de exposição

Duração sugerida: 6 a 10 minutos. O cenário é local e isolado; não usa Supabase.

## Preparação

```bash
# 1) Backend com bootstrap do primeiro SUPER_ADMIN
cd organiza-ai-web/backend
export ADMIN_BOOTSTRAP_EMAIL='admin@organizaai.invalid'
export ADMIN_BOOTSTRAP_PASSWORD='defina-uma-senha-forte-12+'
export ADMIN_BOOTSTRAP_NOME='Administrador da Exposição'
./mvnw spring-boot:run -Dspring-boot.run.profiles=local

# 2) Em outro terminal: cenário administrativo
cd organiza-ai-web
export ADMIN_EMAIL='admin@organizaai.invalid'
export ADMIN_PASSWORD='defina-uma-senha-forte-12+'
export DEMO_USER_PASSWORD='defina-uma-senha-demo-8+'
node scripts/seed-admin-demo.mjs --reset

# 3) Dados do Mobile (opcional, mesma base)
export DEMO_PASSWORD='defina-uma-senha-demo-8+'
export DEMO_EMAIL='demo@exposicao.invalid'
node scripts/seed-demo.mjs --reset

# 4) Frontend
cd frontend
npm start -- --port 4200
```

Abra `http://localhost:4200` e entre com `ADMIN_EMAIL`/`ADMIN_PASSWORD`.

## Roteiro

### 1. Login administrativo

Mostre que o login encaminha para `/admin/dashboard`, identifica o ambiente `DEMO · H2` e o papel
`SUPER_ADMIN`. Explique que `USER` iria para `/home` e receberia 403 nas APIs administrativas.

### 2. Dashboard

Destaque contas ativas/bloqueadas, sessões, falhas 24h, alertas, política, banco, CPU e eventos.
Explique que os números vêm de consultas reais.

### 3. Usuários

Busque `ana.souza@exposicao.invalid`. Abra detalhes: sessões ativas, eventos, papel e status.
Mostre `bruno.lima` bloqueado e `carla.mendes` como ADMIN.

### 4. Bloqueio e revogação

Abra a conta da Ana, clique “Revogar sessões” e confirme. Mostre o aviso de sucesso. Explique que o
token é invalidado imediatamente (401) e que a ação aparece na auditoria.

### 5. Redefinição de senha

Em uma conta de teste, clique “Redefinir senha”. Mostre a senha temporária uma única vez, copie e
feche. Destaque que ela não é registrada em log/auditoria.

### 6. Central de segurança

Abra “Segurança”. Mostre 5 falhas, sessões, alertas e tentativas reais (Ana/intruso). Explique a
política: 5 tentativas/15 min → bloqueio de 15 min → 429.

### 7. Política

Abra “Configurações” como SUPER_ADMIN. Ajuste a janela ou o bloqueio, salve e mostre a auditoria.
Se estiver como ADMIN, mostre que os campos são somente leitura.

### 8. Firewall DEMO

Abra “Firewall”. Leia o aviso de simulação. Mostre as regras padrão + “DEMO Bloquear SMTP”.
Crie uma regra conflitante (bloquear 443) e clique “Validar conflitos”: deve apontar o conflito.
Explique que “Aplicar” retorna 409 até corrigir. Remova a regra, valide e aplique a simulação.
Mostre o snapshot e faça “Rollback”. Reforce: **nada toca o firewall real**.

### 9. Monitoramento

Abra “Monitoramento”. Mostre uptime, banco, CPU, memória, disco, HTTP e firewall. Se algum valor não
puder ser coletado, aparece “Indisponível”. Nenhum comando é executado.

### 10. Auditoria

Abra “Auditoria”. Filtre por `CONTA_BLOQUEADA`, `SESSOES_REVOGADAS` ou `FIREWALL_APLICADO`.
Expanda detalhes e mostre ator, IP, correlação e resultado. Explique que é somente leitura.

### 11. Tema e responsividade

Alterne claro/escuro e reduza a janela para tablet. Mostre sidebar retrátil e tabelas com rolagem.

### 12. Encerramento

Explique:

- RBAC efetivo no backend, não só na UI;
- proteção do último SUPER_ADMIN;
- firewall DEMO persistido e real desabilitado;
- 26 testes backend + 11 Angular;
- migração V004 aditiva;
- Mobile/Web preservados.

## Restauração após os ensaios

```bash
node scripts/seed-admin-demo.mjs --reset
node scripts/seed-demo.mjs --reset
```

O reset administrativo anonimiza somente contas `@exposicao.invalid` e recria o cenário. O reset do
Mobile atua somente na conta informada. Nenhum dos dois toca dados de outras contas.

## Se algo falhar

- **Login 403:** conta bloqueada/desativada; use o SUPER_ADMIN para reativar.
- **Token expirado:** entre novamente.
- **Firewall 409:** há conflito; valide e remova a regra conflitante.
- **Banco recriado:** o H2 é em memória; reinicie backend e rode os seeds.
- **Credenciais:** nunca estão no código; use as variáveis de ambiente definidas na preparação.

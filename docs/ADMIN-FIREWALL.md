# Admin 1.0 — Firewall

## 1. Estado da entrega

| Item | Estado |
|---|---|
| Firewall DEMO persistido | **Implementado e testado** |
| Validação de IP/CIDR, porta e protocolo | **Implementado** |
| Detecção de conflitos | **Implementado** |
| Snapshot, aplicação simulada e rollback | **Implementado** |
| Firewall real (nftables/UFW) | **Não implementado** |
| Agente privilegiado | **Apenas arquitetura documentada** |
| Execução de comandos pelo painel | **Proibida por desenho** |

O backend nunca executa `nft`, `ufw`, `iptables`, `systemctl` ou shell. O frontend nunca envia
comandos. Tudo em `/api/admin/firewall/**` altera somente tabelas do banco.

## 2. Modo DEMO

- `app.firewall.mode=demo` (padrão e ambiente local). `FIREWALL_MODE=real` desabilita escritas e
  retorna conflito, pois não existe agente real.
- Regras: nome, descrição, IP/CIDR, porta (nula = todas), protocolo (TCP/UDP/ANY), direção
  (INBOUND/OUTBOUND), ação (ALLOW/DENY), prioridade (1–999) e ativa.
- Validação de IP/CIDR aceita IPv4/IPv6 literais e prefixo; nomes DNS são recusados.
- Bloqueio de segurança: no DEMO é recusado permitir SSH (porta 22) a partir de `0.0.0.0/0`.

### Conflitos

Duas regras **ativas** conflitam quando:

- mesma direção;
- protocolo igual ou um deles `ANY`;
- porta igual ou uma delas nula;
- CIDRs se sobrepõem;
- ações diferentes.

Conflito não é aplicado automaticamente; a aplicação retorna 409 até a correção.

### Aplicar e rollback

- **Aplicar** cria um snapshot JSON com todas as regras e grava `aplicado_em`.
- **Rollback** apaga as regras atuais, restaura o snapshot e limpa o estado aplicado.
- **Restaurar DEMO** recria cinco regras padrão.
- Histórico e auditoria registram cada operação com ator e resultado “SIMULADO”.

## 3. Arquitetura planejada para o firewall real

> Nada abaixo está ativo nesta entrega. É o desenho para uma fase posterior, com autorização
> explícita e ensaio em VM descartável.

```
Angular ──HTTPS/Bearer──► Spring Boot (sem root)
                              │ socket Unix local (0600, grupo firewall-agent)
                              ▼
                       firewall-agent (root mínimo, systemd)
                              │ allowlist de operações
                              ▼
                       nftables (ou UFW)
```

### Princípios

1. Spring Boot roda sem root e sem `sudo`.
2. Agente separado, com usuário dedicado e capabilities mínimas (`CAP_NET_ADMIN`).
3. Comunicação por socket Unix local, autenticado por peer credentials/grupo.
4. Nenhum comando livre: apenas operações tipadas (`validate`, `preview`, `apply`, `rollback`,
   `status`, `backup`).
5. IP/CIDR, porta, protocolo e direção validados novamente no agente.
6. Pré-visualização obrigatória antes de aplicar.
7. Backup do ruleset atual; rollback automático se a sessão SSH/administrativa cair.
8. Allowlist das portas administrativas e da rede de gestão.
9. Auditoria local e no backend, com correlação.
10. Falha fechada: se o agente não responder, nenhuma regra é alterada.

### Operações permitidas no agente

```text
status() -> resumo do ruleset
preview(ruleset) -> diff nftables
validate(ruleset) -> conflitos e riscos
apply(ruleset, timeout, correlation) -> aplica com rollback temporizado
rollback(snapshotId)
backup() -> snapshot local
```

### Proteção de SSH

- Nunca aplicar regra que bloqueie a porta SSH da sessão atual.
- Exigir allowlist da rede de administração.
- Rollback agendado (`at`/systemd timer) caso o agente perca conectividade.
- Aplicar em duas fases: regras aditivas e, depois, remoção das antigas.
- Testar em VM descartável; nunca diretamente em produção.

### nftables vs UFW

- **UFW**: interface simples, bom para hosts pequenos; operações `ufw allow/deny` mapeadas.
- **nftables**: controle fino, tabelas/chains; preferível para o agente com ruleset versionado.
- O agente deve escolher um backend por configuração e validar disponibilidade no start.

### Empacotamento futuro

- Serviço systemd `organiza-firewall-agent.service`, `User=firewall-agent`,
  `AmbientCapabilities=CAP_NET_ADMIN`, `NoNewPrivileges=yes`, `ProtectSystem=strict`.
- Socket `organiza-firewall.sock` com grupo `organiza-firewall` e modo `0660`.
- Backend com `FIREWALL_AGENT_SOCKET=/run/organiza-firewall/agent.sock` e feature flag desligada.
- Migração e auditoria já preparadas no backend.

## 4. Limitações atuais

- Nenhuma regra real é aplicada.
- Não há agente nem socket implementados.
- Não há teste em VM, nftables ou UFW real.
- O modo `real` deve permanecer desabilitado até a fase de homologação.

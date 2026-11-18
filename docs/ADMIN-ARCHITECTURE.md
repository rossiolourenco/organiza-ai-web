# Admin 1.0 — Arquitetura

Atualizado em 18/11/2026. Documento técnico do console administrativo integrado ao Organiza Aí.

## 1. Visão geral

```
Angular 19 (Enterprise Security Console)
        │ HTTP + Bearer
        ▼
Spring Boot 4 / Java 21
  ├─ AutorizacaoFilter (token, status, papel, headers)
  ├─ AdminUsuarioService / SecurityService
  ├─ FirewallService (modo DEMO)
  ├─ MonitorService + MetricsRegistry
  ├─ AuditoriaService / LoginAttemptService
  └─ JPA → PostgreSQL (H2 isolado na demonstração)
```

O console é uma área do mesmo frontend Angular (`/admin`), não um backend separado. O Mobile continua
usando `/api/tarefas`, `/api/categorias`, `/api/habitos` e `/api/usuarios`, sem alteração de contrato.

## 2. Decisões de arquitetura

- **Um único backend**: evita duplicação de regras e mantém o Mobile funcional.
- **Autorização no servidor**: a interface esconde ações, mas todo endpoint administrativo valida
  token, status da conta e papel no `AutorizacaoFilter` e nos serviços.
- **Auditoria no mesmo banco**: operações críticas e tentativas de login são persistidas. Não há
  serviço externo de logs.
- **Firewall DEMO persistido**: regras, snapshots, histórico e estado são tabelas reais; nenhuma
  chamada ao sistema operacional é feita.
- **Monitoramento sem agente**: métricas da JVM/OS via `ManagementFactory`, banco via `DataSource`,
  HTTP via filtro próprio. Nenhum comando remoto.
- **Bootstrap seguro**: primeiro `SUPER_ADMIN` só por variáveis de ambiente; não existe endpoint
  público de criação de administrador.
- **Sem CSRF por cookie**: a API usa Bearer em `Authorization`. CORS é restritivo e o token não é
  armazenado em cookie.

## 3. Fluxo de autenticação

1. `POST /api/usuarios/login` normaliza e-mail, verifica política e tentativas recentes.
2. Credenciais válidas geram token opaco de 256 bits; somente o hash SHA-256 é persistido.
3. A sessão registra criação, expiração, IP e user agent.
4. Cada requisição autenticada carrega o usuário e verifica `status == ATIVO`.
5. `/api/admin/**` exige papel `ADMIN` ou `SUPER_ADMIN`.
6. Regras adicionais são verificadas por ação (gerir conta, alterar papel, políticas).
7. Logout e revogação marcam `revogada_em`; o token deixa de autenticar imediatamente.

## 4. Fluxo de gestão de contas

- `GET /api/admin/usuarios` pagina e filtra sem expor tarefas ou notas dos usuários.
- Detalhes mostram apenas dados da conta, sessões resumidas e eventos daquele usuário.
- Bloquear/desativar revoga todas as sessões.
- ADMIN gerencia somente USER; SUPER_ADMIN gerencia USER, ADMIN e outros SUPER_ADMIN.
- O último SUPER_ADMIN ativo é protegido contra bloqueio, desativação, rebaixamento e anonimização.
- Redefinição gera senha temporária forte, exibida uma única vez e nunca registrada em auditoria.
- Anonimização remove identificação, mantém tarefas/categorias/hábitos por integridade referencial.

## 5. Auditoria

Campos: data, ator (id/e-mail/papel), ação, recurso, id do recurso, resultado, IP, user agent,
detalhes e correlação. A leitura exige ADMIN; não existe endpoint de alteração/exclusão.

Eventos registrados incluem login administrativo, falhas, bloqueios automáticos e manuais,
desbloqueios, alterações de papel, revogação de sessões, redefinição de senha, anonimização,
mudanças de política e operações de firewall.

## 6. Firewall DEMO

- `app.firewall.mode=demo` por padrão.
- Escritas exigem modo DEMO e papel administrativo.
- Validação de IP/CIDR, porta, protocolo, direção, ação e prioridade.
- Conflitos: regras ativas com mesma direção, protocolo compatível, porta compatível, CIDR
  sobreposto e ações opostas.
- Aplicar cria snapshot JSON no banco e marca estado aplicado.
- Rollback restaura o último snapshot.
- Reset recria cinco regras padrão.
- Nenhum comando é enviado ao sistema operacional.

## 7. Monitoramento

- Uptime da JVM, CPU do sistema/processo, load average, núcleos.
- Heap usado/máximo/commitado e memória do sistema.
- Disco total/livre do diretório de trabalho.
- Latência e disponibilidade do banco.
- Total de requisições, erros 4xx/5xx e tempo médio desde o início do processo.
- Sessões ativas, usuários ativos/bloqueados e estado do firewall.
- Valores indisponíveis retornam `null`/`disponivel:false`; a UI mostra “Indisponível”.

## 8. Componentes do frontend

- `AdminLayoutComponent`: sidebar, topbar, tema, alertas, identificação do ambiente e logout.
- `AdminDashboardComponent`: visão consolidada de segurança e operação.
- `AdminUsersComponent`: tabela, filtros, detalhes, ações e senha temporária.
- `AdminSecurityComponent`: tentativas de login, alertas e política.
- `AdminFirewallComponent`: regras simuladas, conflitos, apply/rollback/reset e histórico.
- `AdminMonitoringComponent`: métricas e atualização automática opcional.
- `AdminAuditComponent`: trilha read-only com filtros e detalhes.
- `AdminSettingsComponent`: política (SUPER_ADMIN) e informações do ambiente.
- Componentes compartilhados em `admin/components/ui.ts`: cards, badges, modais, vazios, erros,
  skeletons, paginação, timeline e alertas.

## 9. Limites deliberados

- Firewall real não implementado; apenas desenho documentado.
- Sem push, e-mail, SIEM ou serviço externo.
- Sem execução remota de comandos.
- Sem acesso administrativo ao conteúdo privado das tarefas.
- Sem exclusão física de contas; anonimização preserva integridade.
- Web/Mobile continuam dependendo do mesmo backend e do mesmo token.

## 10. Implantação

Ver `ADMIN-DEPLOYMENT.md`. A ordem obrigatória é: backup → V002 → V003 → V004 → backend → Web →
bootstrap do SUPER_ADMIN → validação.

# Admin 1.0 — Guia do usuário

## 1. Acesso

1. Abra `http://localhost:4200` (ou o domínio de produção).
2. Entre com e-mail e senha de uma conta `ADMIN` ou `SUPER_ADMIN`.
3. Usuários comuns são encaminhados para `/home`; administradores para `/admin/dashboard`.
4. O console mostra o ambiente no topo (`DEMO · H2`) e o papel autenticado.

Não existe cadastro público de administrador. O primeiro `SUPER_ADMIN` é provisionado pelo
bootstrap do servidor.

## 2. Navegação

- **Dashboard**: contas, sessões, falhas, alertas, política, operação e eventos recentes.
- **Usuários**: busca, filtros, paginação, detalhes e ações de conta.
- **Segurança**: tentativas de login, alertas e política vigente.
- **Firewall**: simulador de regras, conflitos, apply/rollback/reset e histórico.
- **Monitoramento**: CPU, memória, disco, banco, HTTP, sessões e firewall.
- **Auditoria**: trilha somente leitura de ações críticas.
- **Configurações**: política e dados do ambiente.
- **Tema**: alterna claro/escuro; preferência fica no aparelho.
- **Sair**: remove token e usuário do navegador.

## 3. Gerenciar uma conta

### Buscar

Use nome/e-mail, status e papel. A tabela mostra papel, status, último login e sessões ativas.

### Detalhes

Abra “Detalhes” para ver:

- dados da conta;
- bloqueio e motivo;
- troca de senha pendente;
- sessões ativas (sem token);
- últimos eventos da conta.

### Bloquear/desbloquear

- **Bloquear** revoga todas as sessões e impede login.
- **Desbloquear/Reativar** devolve status `ATIVO` no próximo login.
- ADMIN só pode agir em `USER`.
- Não é possível alterar a própria conta por essas ações.

### Alterar papel

Somente `SUPER_ADMIN`. O seletor aparece no detalhe da conta. O último `SUPER_ADMIN` ativo não pode
ser rebaixado. A ação pede confirmação e é auditada.

### Revogar sessões

Encerra imediatamente todos os tokens do alvo. O usuário precisa entrar novamente.

### Redefinir senha

Gera senha temporária forte e a mostra **uma única vez**. Copie e entregue por canal seguro.
Todas as sessões são revogadas; o usuário deve trocar a senha após entrar.

### Anonimizar

Remove nome/e-mail/senha e desativa a conta. Tarefas, categorias e hábitos permanecem no banco por
integridade, mas ficam inacessíveis. Ação irreversível e confirmada.

### Criar usuário

“Novo usuário” cria `USER`; `SUPER_ADMIN` também pode criar `ADMIN`. A senha temporária é exibida
uma vez.

## 4. Segurança

- Acompanhe falhas, sucessos, bloqueios e sessões.
- Filtre tentativas por e-mail e resultado.
- Alertas são calculados por regras reais (sem dados fictícios).
- A política em vigor aparece no card; altere em Configurações se for SUPER_ADMIN.

## 5. Firewall DEMO

1. Leia o aviso: nada é aplicado no sistema operacional.
2. Crie/edite regras com IP/CIDR, porta, protocolo, direção, ação e prioridade.
3. Use “Validar conflitos” antes de aplicar.
4. “Aplicar simulação” cria um snapshot; “Rollback” restaura o último snapshot.
5. “Restaurar cenário DEMO” recria as regras padrão.
6. O histórico mostra operações simuladas e o ator.

## 6. Monitoramento

- Métricas reais da JVM/OS/banco/API.
- “Indisponível” significa que a coleta não foi possível; não há valor inventado.
- Ative atualização a cada 15 s se desejar.
- O painel não executa comandos.

## 7. Auditoria

- Filtre por ação, ator e período.
- “Detalhes” mostra correlação, IP, user agent e descrição.
- Não existe edição ou exclusão de registros.
- A leitura é permitida a ADMIN e SUPER_ADMIN.

## 8. Boas práticas

- Use contas administrativas nominais, não compartilhadas.
- Não compartilhe senhas temporárias em chats públicos.
- Revogue sessões ao trocar de função.
- Revise a política antes de eventos com público.
- Para demonstração, reinicie backend + seed para voltar ao estado inicial.

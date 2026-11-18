# Admin 1.0 — Matriz RBAC

## Papéis

| Papel | Uso | Observação |
|---|---|---|
| `USER` | Conta comum do Mobile/Web | Acessa somente os próprios dados |
| `ADMIN` | Operação do console | Gerencia contas `USER`, firewall DEMO, auditoria e monitoramento |
| `SUPER_ADMIN` | Governança | Gerencia administradores, papéis e políticas críticas |

O papel e o status são atribuídos apenas pelo backend. O cadastro público força `USER/ATIVO`.
`SUPER_ADMIN` é criado somente pelo bootstrap por variáveis de ambiente.

## Matriz de permissões

| Capacidade | USER | ADMIN | SUPER_ADMIN |
|---|---:|---:|---:|
| Login e dados próprios | ✅ | ✅ | ✅ |
| Abrir console `/admin` | ❌ | ✅ | ✅ |
| Listar/buscar contas | ❌ | ✅ | ✅ |
| Ver detalhes de `USER` | ❌ | ✅ | ✅ |
| Ver detalhes de `ADMIN` | ❌ | ❌ | ✅ |
| Bloquear/desbloquear/desativar `USER` | ❌ | ✅ | ✅ |
| Bloquear/desativar `ADMIN` | ❌ | ❌ | ✅ |
| Bloquear/desativar `SUPER_ADMIN` | ❌ | ❌ | ✅ (exceto o último) |
| Revogar sessões de `USER` | ❌ | ✅ | ✅ |
| Revogar sessões de `ADMIN`/`SUPER_ADMIN` | ❌ | ❌ | ✅ |
| Redefinir senha de `USER` | ❌ | ✅ | ✅ |
| Redefinir senha de `ADMIN`/`SUPER_ADMIN` | ❌ | ❌ | ✅ |
| Criar conta `USER` | ❌ | ✅ | ✅ |
| Criar conta `ADMIN` | ❌ | ❌ | ✅ |
| Criar `SUPER_ADMIN` pela API | ❌ | ❌ | ❌ (somente bootstrap) |
| Alterar papéis | ❌ | ❌ | ✅ |
| Promover a `SUPER_ADMIN` | ❌ | ❌ | ✅ |
| Editar política de segurança | ❌ | ❌ | ✅ |
| Ler auditoria | ❌ | ✅ | ✅ |
| Alterar/excluir auditoria | ❌ | ❌ | ❌ |
| Ler central de segurança | ❌ | ✅ | ✅ |
| Ler monitoramento | ❌ | ✅ | ✅ |
| Gerenciar firewall DEMO | ❌ | ✅ | ✅ |
| Gerenciar firewall real | ❌ | ❌ | ❌ (desabilitado) |
| Anonimizar `USER` | ❌ | ✅ | ✅ |
| Anonimizar `ADMIN`/`SUPER_ADMIN` | ❌ | ❌ | ✅ (exceto o último) |

## Invariantes de segurança

1. Ninguém altera o próprio status, papel, sessões ou anonimização por endpoint administrativo.
2. ADMIN não gerencia ADMIN nem SUPER_ADMIN.
3. Somente SUPER_ADMIN altera papéis.
4. ADMIN não promove ninguém a SUPER_ADMIN.
5. O último SUPER_ADMIN ativo não pode ser bloqueado, desativado, rebaixado ou anonimizado.
6. Bloqueio/desativação revoga todas as sessões do alvo.
7. O backend valida status e papel em cada requisição, não apenas na interface.
8. Toda ação crítica é registrada em auditoria com ator, IP, resultado e correlação.
9. Senhas temporárias não são registradas em log ou auditoria.
10. Operações de firewall são simulações persistidas; nenhum comando chega ao host.

## Verificação

Os casos estão cobertos por `AdminApiTests`:

- USER recebe 403 em todos os endpoints `/api/admin/**`.
- ADMIN gerencia USER e recebe 403 para ADMIN/SUPER_ADMIN e para alteração de papel.
- SUPER_ADMIN altera papéis e cria ADMIN.
- Autoelevação e auto-bloqueio retornam 400.
- Bloqueio revoga sessões e invalida tokens.
- Política de tentativas bloqueia a conta após o limite.

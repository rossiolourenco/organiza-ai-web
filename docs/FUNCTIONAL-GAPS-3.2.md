# Organiza Aí 3.2 — Lacunas funcionais

## 1. Prioridade P0

Nenhuma falha crítica encontrada: não houve vazamento de hash, acesso cruzado, bypass de
autenticação, perda de dados ou elevação indevida de privilégio.

## 2. Prioridade P1 — corrigidas

| Lacuna | Impacto | Correção | Evidência |
|---|---|---|---|
| Usuário comum não alterava a própria senha na Web | Bloqueava a gestão da conta | Página **Meu perfil → Segurança** com senha atual, nova senha, confirmação, mostrar/ocultar, loading e feedback | `func-3.2-web-perfil-*.jpg`; ContaApiTests; E2E |
| Usuário comum não alterava a própria senha no Mobile | Bloqueava a gestão da conta no app | Tela **Perfil → Segurança → Alterar senha**, mesmo contrato, confirmação e encerramento de sessão | `func-3.2-mobile-seguranca.jpg`; E2E |
| Perfil sem edição de dados permitidos | Usuário não corrigia o próprio nome | `GET/PUT /api/usuarios/perfil` + edição de nome na Web e no Mobile | ContaApiTests; E2E |

## 3. Prioridade P2 — lacunas e decisões

### 3.1 Recuperação de senha autoatendimento

**Estado:** ausente.

- Não há endpoint de solicitação/consumo de token.
- Não há SMTP nem provedor de e-mail configurado.
- Um botão “Esqueci minha senha” sem envio real seria enganoso; por isso **não foi criado**.

**Implementação recomendada (fase futura):**

1. `POST /api/usuarios/recuperacao` com resposta genérica (evita enumeração).
2. Token aleatório de uso único, hash no banco, expiração de 15–30 minutos.
3. Envio por SMTP configurado por variáveis de ambiente, sem credenciais no repositório.
4. `POST /api/usuarios/recuperacao/confirmar` com token + nova senha.
5. Revogação de todas as sessões e auditoria.
6. Rate limiting por e-mail e IP.
7. Testes de expiração, uso único, token inválido e resposta uniforme.

### 3.2 Paridade Web

A Web cobre autenticação, tarefas e Admin. O Mobile tem módulos adicionais que **não existem** na
Web Angular:

- hábitos;
- calendário/planejamento;
- Kanban;
- Pomodoro/foco;
- revisão semanal;
- lixeira/restauração;
- busca e ordenação de tarefas;
- subtarefas, recorrência, notas e etiquetas.

Isso é uma diferença de escopo de produto, não uma regressão da 3.2. A API já suporta a maior parte
desses recursos; a lacuna é de interface Web.

**Sugestão:** tratar como 3.3, com priorização de:

1. Lixeira e restauração (baixo esforço, alto valor).
2. Busca e ordenação na lista.
3. Categorias com página dedicada.
4. Calendário e revisão.
5. Hábitos e foco (maior esforço).

### 3.3 Edição de e-mail e exclusão de conta

- O e-mail é a identidade de login e não é editável pelo usuário.
- Não há fluxo de exclusão/anonimização de conta autoatendida (o Admin pode desativar/anonymizar).
- Implementar somente com decisão de produto, verificação de e-mail e trilha de auditoria.

### 3.4 Recorrência e checklist na Web

O backend e o Mobile suportam `detalhes` (subtarefas, recorrência, notas, etiquetas). O formulário
Web envia apenas os campos básicos e, por compatibilidade, o backend preserva os detalhes
existentes. Para editar esses campos na Web seria necessário um formulário avançado.

## 4. Prioridade P3

| Item | Estado | Observação |
|---|---|---|
| Versão exibida na prévia Web do Mobile | Corrigido | Fallback passou a 3.2.0 |
| Texto “Prioridade media” sem acento no Mobile | Pendente | Ajuste de copy |
| Notificações locais na Web | Não aplicável | Recurso nativo |
| Exportação de dados na Web | Ausente | Existe no Mobile |

## 5. Riscos residuais

- A troca de senha revoga todas as sessões; usuários com múltiplos aparelhos precisarão entrar
  novamente. Isso é intencional e informado na interface.
- A conta de homologação local usa H2 em memória; os dados desaparecem ao reiniciar o backend.
- A release 3.2 ainda não foi publicada; o comportamento em produção é o da 3.1 até o deploy.

## 6. Critério para encerrar as lacunas

- Recuperação de senha: só encerrar com envio real de e-mail e testes de uso único.
- Paridade Web: encerrar por módulo, com contrato e testes próprios.
- Edição de e-mail/exclusão de conta: só com requisito de produto e verificação de identidade.

# Organiza Aí — Conta e segurança do usuário

Atualizado em 19/11/2026 (versão 3.2).

## 1. Contrato de alteração de senha

```http
PUT /api/usuarios/senha
Authorization: Bearer <token>
Content-Type: application/json

{ "senhaAtual": "...", "novaSenha": "..." }
```

Respostas:

- `204 No Content` — senha alterada e sessões revogadas.
- `400 Bad Request` — senha atual incorreta, política violada, nova senha igual à atual ou payload inválido.
- `401 Unauthorized` — sem sessão válida ou token expirado.
- `403 Forbidden` — tentativa de usar `usuarioId` de outra conta.

O endpoint opera sempre sobre o usuário autenticado; não existe caminho para alterar a senha de
terceiros. O `AutorizacaoFilter` compara qualquer `usuarioId` informado com o id da sessão.

### Regras aplicadas no backend

1. Confirma a senha atual com BCrypt (`PasswordEncoder.matches`).
2. Valida a nova senha pela política vigente:
   - mínimo configurável (padrão 8) e máximo de 72 bytes UTF-8;
   - número e maiúscula quando exigidos pela política.
3. Recusa nova senha idêntica à atual.
4. Gera novo hash BCrypt; nunca armazena texto puro.
5. Atualiza `senhaAlteradaEm` e zera `deveTrocarSenha`.
6. Revoga **todas** as sessões do usuário (inclusive a usada na troca).
7. Registra `SENHA_ALTERADA` na auditoria, sem incluir senhas.
8. Não devolve hash, token ou dados sensíveis.

Arquivos: `UsuarioController`, `AutenticacaoService`, `AlterarSenhaRequest`.

## 2. Contrato de perfil

```http
GET /api/usuarios/perfil
PUT /api/usuarios/perfil   { "nome": "..." }
Authorization: Bearer <token>
```

Resposta:

```json
{
  "id": 1,
  "nome": "Nome",
  "email": "usuario@exemplo.com",
  "papel": "USER",
  "status": "ATIVO",
  "deveTrocarSenha": false,
  "dataCriacao": "2026-11-19T...",
  "senhaAlteradaEm": "2026-11-19T..."
}
```

Somente o nome é editável. E-mail, papel e status não são alteráveis por esse endpoint. O hash de
senha é `WRITE_ONLY` no modelo e não aparece em nenhuma resposta.

## 3. Fluxos de interface

### Web

`/perfil` (protegida por `authGuard`):

- dados do usuário, papel, status, membro desde;
- edição do nome;
- card **Segurança** com senha atual, nova senha e confirmação;
- botão **Mostrar senhas/Ocultar senhas**;
- validação local de mínimo, máximo, confirmação e senha diferente;
- erro inline vindo do backend;
- sucesso: limpa a sessão local e navega para `/login?senha=alterada`.

Link de perfil disponível na topbar da Home e do formulário de tarefa.

### Mobile

`Perfil → Segurança` (`/seguranca`):

- orientação de que a troca revoga todas as sessões;
- três campos `PasswordInput` com mostrar/ocultar;
- validação e erro inline;
- confirmação de sucesso com botão **Entrar novamente**;
- limpeza da sessão local via `AuthProvider.clear()` e retorno ao login com aviso.

O perfil também permite editar o nome (`AuthProvider.updateProfile`).

## 4. Modelo de sessão

- Token aleatório de 32 bytes (Base64 URL, 43 caracteres).
- Banco armazena apenas SHA-256 do token.
- Expiração padrão de 7 dias (configurável de 1 a 30).
- Revogação por logout, bloqueio, redefinição administrativa ou troca de senha.
- `ultimoUsoEm` atualizado no máximo a cada 60 segundos.
- Login com rate limiting por e-mail; o último SUPER_ADMIN não é bloqueado por tentativas.

## 5. Recuperação de senha

**Não implementada.** Não há SMTP configurado. A interface não exibe link de recuperação para não
prometer um fluxo inexistente. Ver `FUNCTIONAL-GAPS-3.2.md` para o desenho recomendado.

## 6. Testes

`ContaApiTests` (6 testes):

- perfil retorna dados do próprio usuário e não expõe hash;
- atualização de nome persiste e valida limites;
- tentativa de editar outro usuário via `usuarioId` retorna 403;
- troca de senha valida senha atual, política, senha igual e revoga a sessão;
- senha antiga deixa de autenticar e a nova funciona;
- troca de senha de A não revoga a sessão de B;
- endpoint exige autenticação.

Validação ponta a ponta:

- Web: troca de senha → login com a nova → senha antiga rejeitada (401).
- Mobile: troca de senha → confirmação → login com a nova → senha antiga rejeitada.

## 7. Recomendações

1. Implementar recuperação com token de uso único e SMTP real.
2. Adicionar limite de tentativas específico para `PUT /usuarios/senha`.
3. Avaliar exigência de senha administrativa reforçada também na troca pelo próprio Admin.
4. Adicionar verificação de e-mail antes de permitir alteração de e-mail.
5. Monitorar auditoria `SENHA_ALTERADA` e `LOGIN_FALHA` no Admin.

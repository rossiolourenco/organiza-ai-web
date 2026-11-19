# Mobile — Configuração da API

## 1. Padrão de produção

O aplicativo usa automaticamente:

```text
https://organizaai.evox.net.br/api
```

Nenhum IP, host ou porta é solicitado ao usuário. A configuração fica centralizada em
`src/services/config.ts`:

```ts
const PRODUCTION_ORIGIN = 'https://organizaai.evox.net.br';
export const API_ORIGIN = resolveApiOrigin(process.env.EXPO_PUBLIC_API_URL);
export const API_BASE = `${API_ORIGIN}/api`;
```

`API_ORIGIN` é a origem; todas as chamadas acrescentam `/api` em `request()`.

## 2. Desenvolvimento

Defina a variável pública antes de iniciar o Expo:

```bash
EXPO_PUBLIC_API_URL=http://localhost:8080 npm start
```

Também é aceito `http://127.0.0.1:8080` ou uma URL terminada em `/api`:

```bash
EXPO_PUBLIC_API_URL=http://localhost:8080/api npm start
```

Sem a variável, produção é usada. Não há fallback silencioso para HTTP em host remoto.

## 3. Regras de resolução

| Entrada | Resultado |
|---|---|
| vazia | produção HTTPS |
| `https://organizaai.evox.net.br` | produção HTTPS |
| `https://organizaai.evox.net.br/api` | produção HTTPS |
| `http://localhost:8080` | mantém localhost (dev) |
| `http://api.example.org` | recusado; usa produção |
| URL com usuário/senha/query/caminho | recusada; usa produção |

O aviso é emitido no console de desenvolvimento, sem expor segredos.

## 4. Segurança

- HTTPS obrigatório fora de localhost.
- Certificados validados pelo sistema (sem bypass).
- Nenhum token é registrado em log.
- Sessão nativa em SecureStore; Web em memória.
- Logout revoga no servidor quando online.
- `EXPO_PUBLIC_API_URL` é pública: nunca coloque segredos.

## 5. Web preview

A prévia Expo Web em `http://localhost:8081` é um cliente web e está sujeita a CORS. O backend de
produção permite apenas `https://organizaai.evox.net.br` e origens locais configuradas no backend
local. Para testar a prévia Web contra produção, adicione a origem ao CORS do ambiente de
desenvolvimento; **não** amplie o CORS de produção desnecessariamente.

Android/iOS nativos não usam CORS.

## 6. Migração de sessões antigas

Sessões antigas que apontavam para `localhost:8080` são normalizadas para a API oficial ao serem
lidas pelo `AuthProvider`. O usuário não precisa reconfigurar nada; se o servidor antigo não existir,
basta entrar novamente.

## 7. Testes

`tests/config.test.ts` cobre produção padrão, `/api`, localhost e recusa de downgrade HTTP remoto.
`tests/navigation.test.tsx` garante que o campo “Servidor” não existe e que tarefas usam a URL
oficial.

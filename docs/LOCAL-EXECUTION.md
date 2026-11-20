# Execução local — Web/Admin

## 1. Caminhos oficiais

| Serviço | Caminho |
|---|---|
| Frontend | `/Users/rossiolourenco/ProjetosFaama/organiza-ai-web/frontend` |
| Backend | `/Users/rossiolourenco/ProjetosFaama/organiza-ai-web/backend` |
| Repositório | `/Users/rossiolourenco/ProjetosFaama/organiza-ai-web` |

**Não execute** a cópia acadêmica `organiza-ai - cópia` nem os ZIPs de `~/Downloads`. Eles contêm o
layout antigo.

## 2. Backend local (H2)

```bash
cd /Users/rossiolourenco/ProjetosFaama/organiza-ai-web/backend
ADMIN_BOOTSTRAP_EMAIL='admin@local.invalid' \
ADMIN_BOOTSTRAP_PASSWORD='<defina-uma-senha-local-forte>' \
ADMIN_BOOTSTRAP_NOME='Admin Local' \
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

- Porta: `8080`
- API: `http://localhost:8080/api`
- Banco: H2 em memória; reiniciar apaga os dados.
- Para uma conta comum, use `/cadastro` no frontend.

## 3. Frontend

```bash
cd /Users/rossiolourenco/ProjetosFaama/organiza-ai-web/frontend
npm start -- --port 4200
```

- Web: `http://localhost:4200`
- Admin: `http://localhost:4200/admin`
- O `API_BASE` usa `http://localhost:8080/api` quando o host é `localhost`.

## 4. Testes e gate

```bash
cd backend && ./mvnw test -Dspring.profiles.active=local
cd frontend && npm test -- --watch=false --browsers=ChromeHeadless
cd frontend && npm run build
cd /Users/rossiolourenco/ProjetosFaama/organiza-ai-web && bash scripts/verify-premium.sh
```

## 5. Mobile contra o backend local (opcional)

```bash
cd /Users/rossiolourenco/ProjetosFaama/organiza-ai
EXPO_PUBLIC_API_URL=http://localhost:8080 npm start
```

O backend local precisa permitir a origem do Expo Web (`8081` já está no profile local). Para uma
porta diferente, ajuste `APP_CORS_ORIGINS`. Em dispositivo físico, use o IP LAN do Mac; nunca
reintroduza o campo de IP na UI.

## 6. Problemas comuns

| Sintoma | Causa provável | Ação |
|---|---|---|
| Layout acadêmico | cópia `organiza-ai - cópia` ou ZIP antigo | usar os caminhos oficiais |
| Login com “IP do servidor” | app/cache antigo | limpar cache e reabrir Expo (`npx expo start -c`) |
| Web antiga | aba/cache do navegador | hard refresh/janela anônima |
| Porta 3000/5173 ocupada | outro projeto (`smart`) | não é o Organiza Aí |
| Backend não responde | processo encerrado | reiniciar; verificar `8080` |
| Sessão inválida | H2 reiniciado | entrar novamente |

## 7. Produção

Não faça deploy a partir deste documento. A release 3.2 está em produção na VM; qualquer nova
publicação exige autorização, backup, smoke tests e rollback documentado.

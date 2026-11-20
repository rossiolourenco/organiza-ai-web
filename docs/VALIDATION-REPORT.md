# Relatório de validação — integração dev-standards 3.2

Data: 2026-11-20.

## 1. Estado inicial

- Web/Admin: `main` em `99bed03`, árvore limpa, remoto sincronizado.
- Mobile: `main` em `b6f5070`, árvore limpa, remoto sincronizado.
- Cópia acadêmica `organiza-ai - cópia`: `5bb727f`, preservada e não alterada.
- Branches desta intervenção: `chore/dev-standards-3.2` nos dois repositórios.

## 2. Framework

- Fonte: `/Users/rossiolourenco/Framework-Projetos/dev-standards`.
- Fingerprint: `37a59b23899aa564e9cf67e351f73c6dda7198b6dff2ffc3bc2fc2361e7fa32f`.
- Cópias locais idênticas à fonte (`diff -rq` sem diferenças, excluindo `.DS_Store`).
- OpenCode: `AGENTS.md` + `opencode.json.instructions`.
- Evidência Web: o OpenCode carregou automaticamente 14 arquivos, incluindo `AGENTS.md`, `STACK.md`,
  `AI-GUIDELINES.md` e padrões de TS/Java/PostgreSQL/REST/segurança/arquitetura/testes/versionamento;
  uma nova sessão `opencode run` respondeu `WEB_STACK_OK`.
- Evidência Mobile: nova sessão `opencode run` no diretório Mobile respondeu `MOBILE_STACK_OK`.

## 3. Testes executados

| Verificação | Resultado |
|---|---|
| Angular build | Sucesso |
| Angular testes | **15/15** |
| Backend testes | **35/35** em 7 suítes |
| Mobile typecheck | Sucesso |
| Mobile lint | Sucesso |
| Mobile testes | **50/50** em 11 suítes |
| Expo Doctor | **18/18** |
| Expo export | Web 1,32 MB; Android/iOS 3,3 MB |
| Gate Web `verify-premium.sh` | Sucesso |
| Gate Mobile `verify-premium.sh` | Sucesso |

## 4. Evidências visuais

### Web local

- `docs/assets/dev-standards-web-login.jpg` — login premium, sem IP.
- `docs/assets/dev-standards-web-home.jpg` — Home com painel, indicadores e filtros.
- `docs/assets/dev-standards-web-profile.jpg` — perfil e segurança.
- `docs/assets/dev-standards-web-admin.jpg` — Admin 1.0 com dados reais.
- Sem erros de console; sem overflow.

### Mobile local

- `docs/assets/dev-standards-mobile-home.jpg` — dashboard Premium, cinco abas.
- `docs/assets/dev-standards-mobile-profile.jpg` — perfil, versão **v3.2.0**.
- `docs/assets/dev-standards-mobile-security.jpg` — alteração de senha.
- Sem erros de console.

### Produção

- `docs/assets/dev-standards-production-login.jpg` — login premium.
- Bundle `main-NXMTECUN.js`, SHA-256
  `2140a1774a7a9e2c08d80f0e4ecab45c3b989db8058d48c5abf658177ddd73a5`.
- O bundle contém `PAINEL PESSOAL`, `Meu perfil` e `Security Console`; não contém o marcador
  acadêmico `Organize suas tarefas de forma simples`.
- Nenhum deploy foi executado nesta intervenção.

## 5. Achado de cache

Na primeira captura Mobile, o Perfil exibiu `v1.0.0` apesar de `package.json`/`app.json` e do
manifesto estarem em 3.2.0. A reinicialização do Expo com `npx expo start -c` passou a exibir
`v3.2.0`. Isso confirma que **cache do Metro** pode reproduzir configuração antiga; o código não
estava regredido.

## 6. Causa da divergência visual

- A cópia acadêmica `organiza-ai - cópia` está em `5bb727f`, com app 1.0.0, `IP do servidor` e
  bundle iOS antigo.
- ZIPs de setembro/outubro em `~/Downloads` contêm o mesmo app/layout antigo.
- Os servidores oficiais e a produção entregam 3.2; a tela antiga vinha de cópia/cache/cliente
  antigo, não do código atual.

## 7. Limitações

- Sem teste em aparelho físico iOS/Android nesta execução.
- Sem build assinado para lojas.
- Sem teste E2E de navegador automatizado; validação visual manual.
- Sem alteração de banco/produção.
- A cópia acadêmica foi preservada, não removida/renomeada.
- Nenhum commit/push foi feito.

## 8. Conclusão

A integração do dev-standards está funcional e carregada nos dois repositórios. Web/Admin e Mobile
3.2 foram executados pelos caminhos oficiais, com testes e gates verdes. A divergência visual não é
uma regressão de código: é proveniente de cópia/cache antigos. Nenhuma publicação foi feita.

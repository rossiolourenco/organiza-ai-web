# Auditoria antes das alterações — 17/11/2026

- Web/backend: `organiza-ai-web`, main limpo, `c5302ee`.
- Mobile: `../organiza-ai`, main limpo, `bf9ba5c`. Não confundir com `organiza-ai - cópia`.
- Mobile: Expo resolvido 54.0.37, React Native 0.81.5, React 19.1.0, Router 6.0.24. Lock npm presente; packageManager declarava Yarn sem lock correspondente.
- Sete rotas JS, Stack Router, estado local React, API fetch com timeout de 5 s; URL e identidade somente em memória/parâmetros de rota. Sem TypeScript, lint ou testes.
- CRUD de tarefas, cadastro/login, categorias (criação/lista/exclusão/associação), status/prioridade e datas já existentes. Edição de categorias não exposta. Formulários duplicados; datas de formulário só verificavam formato.
- Backend Java 21 / Spring Boot 4.1.1 / JPA. PostgreSQL em produção com ddl-auto=validate; H2 em memória no perfil local. Um teste de contexto passou antes das alterações.
- Entidades: Usuario, Categoria, Tarefa; campos e endpoints documentados no README existente. PUT tarefa aceita título/descrição/prioridade/status/dataPrazo/categoria; PATCH concluir.
- Risco confirmado: login verifica BCrypt, mas não cria sessão. Endpoints confiam no usuarioId fornecido. GET usuarios lista pessoas. Exclusão de categoria depende de FK PostgreSQL SET NULL, ausente no esquema gerado em H2.
- Referência Web verificada localmente no commit solicitado. Contratos básicos serão mantidos, com autenticação obrigatória e adaptação coordenada do cliente Angular.
- AGENTS.md Mobile exige documentação SDK 54: consultada fonte `expo/expo/sdk-54/docs/pages/versions/v54.0.0/sdk/securestore.mdx` (site público redirecionava para latest).
- Nenhum framework dev-standards localizado.

## Decisões

Evolução no Mobile existente, migração das telas para TypeScript, Router preservado. Serviços e regras extraídos. Backend compartilhado recebe autorização, validação e campos aditivos via SQL versionado. Produção não será acessada/migrada automaticamente. Pomodoro e metas serão locais por servidor/usuário, com essa limitação visível. Sem serviços externos, commits ou push.

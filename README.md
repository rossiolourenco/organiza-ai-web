# Organiza Aí Web

Sistema Web Full Stack para gerenciamento e organização de tarefas pessoais.

O **Organiza Aí** permite que usuários criem uma conta, realizem login e organizem suas tarefas por prioridade, categoria, status e prazo.

O projeto foi desenvolvido como atividade acadêmica do curso de **Análise e Desenvolvimento de Sistemas**, utilizando Angular no frontend, Java com Spring Boot no backend e PostgreSQL/Supabase como banco de dados principal.

---

## Funcionalidades

### Usuários

- Cadastro de usuário
- Login
- Logout
- Persistência da sessão no frontend
- Separação das informações por usuário

### Tarefas

- Criar tarefa
- Listar tarefas
- Editar tarefa
- Excluir tarefa
- Marcar tarefa como concluída
- Definir descrição
- Definir prazo
- Definir prioridade
- Associar categoria
- Filtrar tarefas por status
- Filtrar tarefas por prioridade

### Prioridades

As tarefas podem possuir as seguintes prioridades:

- Baixa
- Média
- Alta

### Status

As tarefas podem utilizar os seguintes status:

- Pendente
- Em andamento
- Concluída

### Categorias

O sistema possui suporte a categorias vinculadas ao usuário.

Uma tarefa pode ser associada a uma categoria ou permanecer sem categoria.

---

## Tecnologias utilizadas

### Frontend

- Angular
- TypeScript
- HTML
- CSS
- Angular Router
- Angular Forms
- HttpClient

### Backend

- Java 21
- Spring Boot 4.1.1
- Spring Web
- Spring Data JPA
- Hibernate
- Maven
- Spring Security Crypto

### Banco de dados

Banco principal:

- PostgreSQL
- Supabase

Ambiente de testes locais:

- H2 Database

### Versionamento

- Git
- GitHub

---

## Arquitetura

A aplicação utiliza uma arquitetura Full Stack dividida entre frontend, API REST e banco de dados.

```text
Angular
   |
   | HTTP / REST
   v
Spring Boot
   |
   | JPA / Hibernate
   v
PostgreSQL / Supabase
```

O frontend Angular é responsável pela interface e interação com o usuário.

O backend Spring Boot disponibiliza a API REST e realiza o acesso aos dados através do Spring Data JPA e Hibernate.

O PostgreSQL hospedado no Supabase é utilizado para persistência dos dados da aplicação.

---

## Estrutura do projeto

```text
organiza-ai-web/
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   └── resources/
│   │   └── test/
│   ├── mvnw
│   └── pom.xml
│
├── frontend/
│   ├── src/
│   │   ├── app/
│   │   │   ├── pages/
│   │   │   │   ├── cadastro/
│   │   │   │   ├── home/
│   │   │   │   ├── login/
│   │   │   │   └── tarefa-form/
│   │   │   └── services/
│   │   └── styles.css
│   ├── angular.json
│   └── package.json
│
├── database/
│   ├── MODELAGEM.md
│   ├── schema.sql
│   ├── seed.sql
│   └── testes.sql
│
├── CRONOGRAMA.md
├── README.md
└── .gitignore
```

---

## Modelo de dados

O banco de dados possui três entidades principais:

- Usuários
- Categorias
- Tarefas

### Usuários

Armazena os usuários cadastrados na aplicação.

Principais informações:

- ID
- Nome
- E-mail
- Senha
- Data de criação

### Categorias

Armazena as categorias utilizadas para organizar as tarefas.

Principais informações:

- ID
- Nome
- Cor
- Usuário
- Data de criação

Cada categoria pertence a um usuário.

### Tarefas

Armazena as tarefas cadastradas.

Principais informações:

- ID
- Título
- Descrição
- Prioridade
- Status
- Prazo
- Usuário
- Categoria
- Data de criação
- Data de atualização

Cada tarefa pertence a um usuário e pode possuir uma categoria.

---

## Relacionamentos

```text
USUÁRIO
   |
   | 1:N
   v
TAREFA

USUÁRIO
   |
   | 1:N
   v
CATEGORIA

CATEGORIA
   |
   | 1:N
   v
TAREFA
```

O relacionamento com o usuário permite manter tarefas e categorias associadas ao respectivo usuário da aplicação.

---

## API REST

A API foi desenvolvida utilizando Java e Spring Boot.

Os principais recursos da aplicação estão disponíveis através das rotas:

```text
/api/usuarios
/api/tarefas
/api/categorias
```

A API permite ao frontend realizar as operações necessárias para usuários, tarefas e categorias.

As operações relacionadas às tarefas são vinculadas ao usuário informado pela aplicação.

---

## Interface

A aplicação possui uma interface responsiva composta pelas seguintes telas:

- Login
- Cadastro
- Dashboard de tarefas
- Criação de tarefa
- Edição de tarefa

O dashboard apresenta:

- Lista de tarefas
- Quantidade de tarefas
- Prioridade
- Status
- Categoria
- Prazo
- Filtros por status
- Filtros por prioridade
- Ações para concluir, editar e excluir tarefas

A interface utiliza indicadores visuais para facilitar a identificação de prioridades, status e categorias.

---

# Como executar o projeto

## Pré-requisitos

Para executar o projeto são necessários:

- Java 21
- Node.js
- npm
- Git

Versões utilizadas durante a validação final:

```text
Java 21.0.11
Node.js 22.23.2
npm 10.9.8
```

---

## Executando o backend

Acesse a pasta:

```bash
cd backend
```

Para utilizar o PostgreSQL/Supabase, configure a variável de ambiente utilizada pelo projeto:

```bash
export SUPABASE_DB_URL="SUA_STRING_DE_CONEXAO"
```

As credenciais reais do banco de dados não devem ser adicionadas ao repositório Git.

Depois execute:

```bash
./mvnw spring-boot:run
```

Por padrão, a API fica disponível em:

```text
http://localhost:8080
```

---

## Testes locais do backend

O projeto possui um perfil local utilizando banco H2.

Para executar os testes:

```bash
cd backend
./mvnw test -Dspring.profiles.active=local
```

Resultado obtido na validação final:

```text
Tests run: 1
Failures: 0
Errors: 0
Skipped: 0

BUILD SUCCESS
```

---

## Executando o frontend

Acesse a pasta:

```bash
cd frontend
```

Instale as dependências:

```bash
npm install
```

Execute o servidor de desenvolvimento:

```bash
npm start
```

Também é possível utilizar:

```bash
ng serve
```

A aplicação fica disponível em:

```text
http://localhost:4200
```

---

## Build do frontend

Para gerar o build da aplicação Angular:

```bash
cd frontend
npm run build
```

Na validação final do projeto, o build foi gerado com sucesso.

---

## Banco de dados

Os arquivos utilizados para documentação e criação da estrutura do banco estão disponíveis na pasta:

```text
database/
```

Arquivos disponíveis:

```text
MODELAGEM.md
schema.sql
seed.sql
testes.sql
```

### schema.sql

Contém a estrutura das tabelas e seus relacionamentos.

### seed.sql

Contém dados utilizados para inicialização ou demonstração do banco.

### testes.sql

Contém consultas utilizadas durante a validação da estrutura do banco.

### MODELAGEM.md

Contém a documentação da modelagem do banco de dados.

---

## Segurança das configurações

Credenciais e informações privadas de acesso ao banco de dados não devem ser armazenadas diretamente no código-fonte.

A conexão com PostgreSQL/Supabase utiliza a variável de ambiente:

```text
SUPABASE_DB_URL
```

Antes de publicar o projeto, deve-se verificar que senhas, tokens e strings privadas de conexão não estejam versionados no Git.

---

## Testes realizados

Durante o desenvolvimento foram validados os principais fluxos da aplicação:

- Cadastro de usuário
- Login
- Logout
- Criação de tarefa
- Listagem de tarefas
- Edição de tarefa
- Exclusão de tarefa
- Conclusão de tarefa
- Prioridade baixa
- Prioridade média
- Prioridade alta
- Associação de categoria
- Tarefa sem categoria
- Definição de prazo
- Filtro por status
- Filtro por prioridade
- Combinação dos filtros
- Separação das tarefas por usuário
- Persistência no PostgreSQL/Supabase
- Comunicação entre Angular e Spring Boot
- Build do frontend
- Testes automatizados do backend

O fluxo Full Stack validado é:

```text
Angular
   ↓
API REST
   ↓
Spring Boot
   ↓
PostgreSQL / Supabase
   ↓
Spring Boot
   ↓
Angular
```

---

## Validação final

### Backend

```text
Tests run: 1
Failures: 0
Errors: 0
Skipped: 0

BUILD SUCCESS
```

### Frontend

```text
Application bundle generation complete.

BUILD OK
```

O build do frontend apresenta apenas um aviso não bloqueante referente ao orçamento de tamanho do CSS do componente Home.

Esse aviso não impede a geração do bundle da aplicação.

### Segurança

Na verificação final dos arquivos versionados não foram encontrados padrões óbvios de credenciais ou segredos armazenados no repositório.

---

## Status do projeto

O **Organiza Aí Web** encontra-se funcional e integrado.

- Frontend Angular: concluído
- Backend Spring Boot: concluído
- API REST: concluída
- PostgreSQL/Supabase: integrado
- H2 para testes locais: configurado
- CRUD de tarefas: concluído
- Usuários: implementados
- Categorias: implementadas
- Filtros: implementados
- Interface responsiva: implementada
- Testes finais: executados

---

## Projeto acadêmico

Projeto desenvolvido para fins acadêmicos no curso de **Análise e Desenvolvimento de Sistemas**.

**Organiza Aí Web**
Sistema Web Full Stack para organização pessoal e gerenciamento de tarefas.
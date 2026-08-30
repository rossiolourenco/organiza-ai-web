# Modelagem do Banco de Dados — Organiza Aí Web

## Visão Geral

O banco de dados relacional do Organiza Aí Web foi desenvolvido utilizando PostgreSQL através da plataforma Supabase.

A modelagem foi criada para armazenar usuários, categorias e tarefas, permitindo que cada usuário organize suas próprias tarefas em diferentes categorias.

---

## Entidade: usuarios

Representa os usuários cadastrados no sistema.

### Campos

- id — Identificador único do usuário (chave primária)
- nome — Nome do usuário
- email — E-mail do usuário
- senha — Senha do usuário
- data_criacao — Data e hora de criação do registro

---

## Entidade: categorias

Representa as categorias utilizadas para organizar as tarefas.

### Campos

- id — Identificador único da categoria (chave primária)
- nome — Nome da categoria
- cor — Cor utilizada para identificação visual
- usuario_id — Identificador do usuário proprietário da categoria (chave estrangeira)
- data_criacao — Data e hora de criação do registro

---

## Entidade: tarefas

Representa as tarefas cadastradas pelos usuários.

### Campos

- id — Identificador único da tarefa (chave primária)
- titulo — Título da tarefa
- descricao — Descrição da tarefa
- prioridade — Prioridade da tarefa
- status — Situação atual da tarefa
- data_prazo — Prazo para conclusão
- usuario_id — Identificador do usuário proprietário da tarefa (chave estrangeira)
- categoria_id — Identificador da categoria da tarefa (chave estrangeira)
- data_criacao — Data e hora de criação
- data_atualizacao — Data e hora da última atualização

---

## Relacionamentos

### usuarios → categorias

Um usuário pode possuir várias categorias.

Relacionamento:

1:N

`usuarios.id` → `categorias.usuario_id`

---

### usuarios → tarefas

Um usuário pode possuir várias tarefas.

Relacionamento:

1:N

`usuarios.id` → `tarefas.usuario_id`

---

### categorias → tarefas

Uma categoria pode estar associada a várias tarefas.

Relacionamento:

1:N

`categorias.id` → `tarefas.categoria_id`

A categoria de uma tarefa pode ser opcional.

---

## Modelo Relacional Simplificado

USUARIOS
- PK id
- nome
- email
- senha
- data_criacao

        1
        |
        | N
        |
CATEGORIAS
- PK id
- nome
- cor
- FK usuario_id
- data_criacao

USUARIOS
        1
        |
        | N
        |
TAREFAS
- PK id
- titulo
- descricao
- prioridade
- status
- data_prazo
- FK usuario_id
- FK categoria_id
- data_criacao
- data_atualizacao

CATEGORIAS
        1
        |
        | N
        |
TAREFAS
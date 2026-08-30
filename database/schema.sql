-- =====================================================
-- ORGANIZA AÍ WEB
-- Estrutura do Banco de Dados
-- PostgreSQL / Supabase
-- =====================================================


-- -----------------------------------------------------
-- TABELA: usuarios
-- Armazena os usuários cadastrados no sistema
-- -----------------------------------------------------

CREATE TABLE usuarios (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


-- -----------------------------------------------------
-- TABELA: categorias
-- Categorias utilizadas para organizar as tarefas
-- -----------------------------------------------------

CREATE TABLE categorias (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    cor VARCHAR(20),
    usuario_id BIGINT NOT NULL,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_categoria_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id)
        ON DELETE CASCADE
);


-- -----------------------------------------------------
-- TABELA: tarefas
-- Armazena as tarefas criadas pelos usuários
-- -----------------------------------------------------

CREATE TABLE tarefas (
    id BIGSERIAL PRIMARY KEY,
    titulo VARCHAR(150) NOT NULL,
    descricao TEXT,
    prioridade VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',
    data_prazo DATE,
    usuario_id BIGINT NOT NULL,
    categoria_id BIGINT,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_tarefa_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_tarefa_categoria
        FOREIGN KEY (categoria_id)
        REFERENCES categorias(id)
        ON DELETE SET NULL
);
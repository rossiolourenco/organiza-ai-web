-- =====================================================
-- ORGANIZA AÍ WEB
-- Testes do Banco de Dados
-- PostgreSQL / Supabase
-- =====================================================


-- -----------------------------------------------------
-- TESTE 1 - CONSULTAR USUÁRIOS
-- -----------------------------------------------------

SELECT * FROM usuarios;


-- -----------------------------------------------------
-- TESTE 2 - CONSULTAR CATEGORIAS
-- -----------------------------------------------------

SELECT * FROM categorias;


-- -----------------------------------------------------
-- TESTE 3 - CONSULTAR TAREFAS
-- -----------------------------------------------------

SELECT * FROM tarefas;


-- -----------------------------------------------------
-- TESTE 4 - CONSULTAR TAREFAS COM USUÁRIO E CATEGORIA
-- Teste de relacionamento entre as tabelas
-- -----------------------------------------------------

SELECT
    t.id,
    t.titulo,
    u.nome AS usuario,
    c.nome AS categoria,
    t.prioridade,
    t.status,
    t.data_prazo
FROM tarefas t
INNER JOIN usuarios u
    ON t.usuario_id = u.id
LEFT JOIN categorias c
    ON t.categoria_id = c.id
ORDER BY t.id;


-- -----------------------------------------------------
-- TESTE 5 - ATUALIZAR UMA TAREFA
-- -----------------------------------------------------

UPDATE tarefas
SET
    status = 'CONCLUIDA',
    data_atualizacao = CURRENT_TIMESTAMP
WHERE id = 1;

SELECT
    id,
    titulo,
    prioridade,
    status,
    data_atualizacao
FROM tarefas
WHERE id = 1;


-- -----------------------------------------------------
-- TESTE 6 - INSERIR TAREFA TEMPORÁRIA
-- -----------------------------------------------------

INSERT INTO tarefas (
    titulo,
    descricao,
    prioridade,
    status,
    data_prazo,
    usuario_id,
    categoria_id
)
VALUES (
    'Tarefa temporária',
    'Registro criado apenas para testar exclusão',
    'BAIXA',
    'PENDENTE',
    '2026-09-10',
    1,
    1
);

SELECT *
FROM tarefas
WHERE titulo = 'Tarefa temporária';


-- -----------------------------------------------------
-- TESTE 7 - EXCLUIR TAREFA TEMPORÁRIA
-- -----------------------------------------------------

DELETE FROM tarefas
WHERE titulo = 'Tarefa temporária';

SELECT *
FROM tarefas
WHERE titulo = 'Tarefa temporária';
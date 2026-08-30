-- =====================================================
-- ORGANIZA AÍ WEB
-- Dados fictícios para testes
-- PostgreSQL / Supabase
-- =====================================================


-- -----------------------------------------------------
-- USUÁRIOS
-- -----------------------------------------------------

INSERT INTO usuarios (nome, email, senha)
VALUES
    ('João Silva', 'joao@email.com', 'senha123'),
    ('Maria Oliveira', 'maria@email.com', 'senha456');


-- -----------------------------------------------------
-- CATEGORIAS
-- -----------------------------------------------------

INSERT INTO categorias (nome, cor, usuario_id)
VALUES
    ('Trabalho', '#2563EB', 1),
    ('Estudos', '#7C3AED', 1),
    ('Pessoal', '#16A34A', 1),
    ('Faculdade', '#EA580C', 2);


-- -----------------------------------------------------
-- TAREFAS
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
VALUES
    (
        'Finalizar relatório',
        'Concluir relatório mensal do trabalho',
        'ALTA',
        'PENDENTE',
        '2026-09-02',
        1,
        1
    ),
    (
        'Estudar Java',
        'Revisar orientação a objetos e API REST',
        'ALTA',
        'EM_ANDAMENTO',
        '2026-09-05',
        1,
        2
    ),
    (
        'Comprar mantimentos',
        'Fazer compras para a semana',
        'MEDIA',
        'PENDENTE',
        '2026-09-01',
        1,
        3
    ),
    (
        'Entregar atividade',
        'Finalizar atividade da faculdade',
        'ALTA',
        'CONCLUIDA',
        '2026-08-30',
        2,
        4
    );
-- Organiza Aí 3.3.1 — centralização de planejamento diário e sessões de foco.
-- Aditiva. Não remove dados e não executa DDL em produção automaticamente.
BEGIN;

-- Plano diário por usuário, com itens em JSON e controle otimista por versão.
CREATE TABLE IF NOT EXISTS planejamentos (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    data DATE NOT NULL,
    itens TEXT NOT NULL,
    versao BIGINT NOT NULL DEFAULT 1,
    atualizado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_planejamento_usuario_data UNIQUE (usuario_id, data)
);
CREATE INDEX IF NOT EXISTS idx_planejamentos_usuario ON planejamentos(usuario_id, data);

-- Sessões de foco concluídas, associadas ao usuário (e opcionalmente a uma tarefa).
CREATE TABLE IF NOT EXISTS sessoes_foco (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    tarefa_id BIGINT REFERENCES tarefas(id) ON DELETE SET NULL,
    minutos INTEGER NOT NULL CHECK (minutos BETWEEN 1 AND 1440),
    data DATE NOT NULL,
    iniciada_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_sessoes_foco_usuario_data ON sessoes_foco(usuario_id, data);

COMMIT;

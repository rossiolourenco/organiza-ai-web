-- Organiza Aí 3.0 — lixeira e hábitos.
-- Aditiva e sem exclusão de dados. Não executada automaticamente.
BEGIN;

-- Lixeira: exclusão lógica reversível. Registros antigos permanecem ativos.
ALTER TABLE tarefas ADD COLUMN IF NOT EXISTS excluida_em TIMESTAMP WITH TIME ZONE;
CREATE INDEX IF NOT EXISTS idx_tarefas_usuario_lixeira ON tarefas(usuario_id, excluida_em);

-- Hábitos pessoais por usuário.
CREATE TABLE IF NOT EXISTS habitos (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    nome VARCHAR(100) NOT NULL,
    descricao VARCHAR(500),
    frequencia VARCHAR(20) NOT NULL DEFAULT 'DIARIA',
    meta_semanal INTEGER NOT NULL DEFAULT 7,
    cor VARCHAR(20),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    arquivado_em TIMESTAMP WITH TIME ZONE
);
CREATE INDEX IF NOT EXISTS idx_habitos_usuario ON habitos(usuario_id);

CREATE TABLE IF NOT EXISTS habito_registros (
    id BIGSERIAL PRIMARY KEY,
    habito_id BIGINT NOT NULL REFERENCES habitos(id) ON DELETE CASCADE,
    data DATE NOT NULL,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_habito_data UNIQUE (habito_id, data)
);
CREATE INDEX IF NOT EXISTS idx_habito_registros_habito ON habito_registros(habito_id, data);

COMMIT;

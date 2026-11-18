-- Aplicar uma vez, antes do novo backend. Não executada automaticamente.
BEGIN;
CREATE TABLE sessoes (
    hash VARCHAR(255) PRIMARY KEY,
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    expira_em TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX idx_sessoes_expira ON sessoes(expira_em);
ALTER TABLE tarefas ADD COLUMN detalhes TEXT;
ALTER TABLE tarefas ADD COLUMN proxima_gerada BOOLEAN NOT NULL DEFAULT FALSE;
CREATE TABLE tarefa_conclusoes (
    tarefa_id BIGINT NOT NULL REFERENCES tarefas(id) ON DELETE CASCADE,
    concluida_em TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX idx_conclusoes_tarefa ON tarefa_conclusoes(tarefa_id);
CREATE INDEX idx_tarefas_usuario_prazo ON tarefas(usuario_id, data_prazo);
COMMIT;

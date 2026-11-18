-- Organiza Aí Admin 1.0 — RBAC, auditoria, segurança, firewall DEMO e monitoramento.
-- Migração aditiva. Não executa firewall real e não remove dados existentes.
BEGIN;

-- ---------------------------------------------------------------- usuários
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS papel VARCHAR(20) NOT NULL DEFAULT 'USER';
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'ATIVO';
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS bloqueado_ate TIMESTAMP WITH TIME ZONE;
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS motivo_bloqueio VARCHAR(255);
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS ultimo_login_em TIMESTAMP WITH TIME ZONE;
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS senha_alterada_em TIMESTAMP WITH TIME ZONE;
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS deve_trocar_senha BOOLEAN NOT NULL DEFAULT FALSE;
CREATE INDEX IF NOT EXISTS idx_usuarios_papel_status ON usuarios(papel, status);

-- ---------------------------------------------------------------- sessões
ALTER TABLE sessoes ADD COLUMN IF NOT EXISTS criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW();
ALTER TABLE sessoes ADD COLUMN IF NOT EXISTS ultimo_uso_em TIMESTAMP WITH TIME ZONE;
ALTER TABLE sessoes ADD COLUMN IF NOT EXISTS revogada_em TIMESTAMP WITH TIME ZONE;
ALTER TABLE sessoes ADD COLUMN IF NOT EXISTS ip VARCHAR(64);
ALTER TABLE sessoes ADD COLUMN IF NOT EXISTS user_agent VARCHAR(255);
CREATE INDEX IF NOT EXISTS idx_sessoes_usuario ON sessoes(usuario_id, revogada_em);

-- ---------------------------------------------------------------- tentativas de login
CREATE TABLE IF NOT EXISTS tentativas_login (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(150) NOT NULL,
    sucesso BOOLEAN NOT NULL,
    ip VARCHAR(64),
    user_agent VARCHAR(255),
    motivo VARCHAR(120),
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_tentativas_email_data ON tentativas_login(email, criado_em);
CREATE INDEX IF NOT EXISTS idx_tentativas_sucesso_data ON tentativas_login(sucesso, criado_em);

-- ---------------------------------------------------------------- auditoria administrativa
CREATE TABLE IF NOT EXISTS auditoria_admin (
    id BIGSERIAL PRIMARY KEY,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    ator_id BIGINT,
    ator_email VARCHAR(150),
    ator_papel VARCHAR(20),
    acao VARCHAR(60) NOT NULL,
    recurso VARCHAR(80),
    recurso_id VARCHAR(64),
    resultado VARCHAR(20) NOT NULL DEFAULT 'SUCESSO',
    ip VARCHAR(64),
    user_agent VARCHAR(255),
    detalhes VARCHAR(1000),
    correlacao VARCHAR(64)
);
CREATE INDEX IF NOT EXISTS idx_auditoria_data ON auditoria_admin(criado_em DESC);
CREATE INDEX IF NOT EXISTS idx_auditoria_acao ON auditoria_admin(acao, criado_em DESC);
CREATE INDEX IF NOT EXISTS idx_auditoria_ator ON auditoria_admin(ator_email, criado_em DESC);
CREATE INDEX IF NOT EXISTS idx_auditoria_recurso ON auditoria_admin(recurso, recurso_id);

-- ---------------------------------------------------------------- políticas de segurança
CREATE TABLE IF NOT EXISTS politicas_seguranca (
    id BIGINT PRIMARY KEY,
    senha_minima INTEGER NOT NULL DEFAULT 8,
    exigir_numero BOOLEAN NOT NULL DEFAULT FALSE,
    exigir_maiuscula BOOLEAN NOT NULL DEFAULT FALSE,
    max_tentativas INTEGER NOT NULL DEFAULT 5,
    janela_minutos INTEGER NOT NULL DEFAULT 15,
    bloqueio_minutos INTEGER NOT NULL DEFAULT 15,
    sessao_dias INTEGER NOT NULL DEFAULT 7,
    atualizado_em TIMESTAMP WITH TIME ZONE,
    atualizado_por VARCHAR(150)
);
INSERT INTO politicas_seguranca (id)
SELECT 1 WHERE NOT EXISTS (SELECT 1 FROM politicas_seguranca WHERE id = 1);

-- ---------------------------------------------------------------- firewall DEMO
CREATE TABLE IF NOT EXISTS firewall_regras (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    descricao VARCHAR(300),
    ip_cidr VARCHAR(64) NOT NULL,
    porta INTEGER,
    protocolo VARCHAR(10) NOT NULL DEFAULT 'TCP',
    direcao VARCHAR(12) NOT NULL DEFAULT 'INBOUND',
    acao VARCHAR(10) NOT NULL DEFAULT 'ALLOW',
    prioridade INTEGER NOT NULL DEFAULT 100,
    ativa BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    atualizado_em TIMESTAMP WITH TIME ZONE,
    criado_por VARCHAR(150),
    atualizado_por VARCHAR(150)
);
CREATE INDEX IF NOT EXISTS idx_firewall_ordem ON firewall_regras(prioridade, id);

CREATE TABLE IF NOT EXISTS firewall_historico (
    id BIGSERIAL PRIMARY KEY,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    ator_email VARCHAR(150),
    operacao VARCHAR(40) NOT NULL,
    regra_id BIGINT,
    detalhes VARCHAR(600),
    resultado VARCHAR(20) NOT NULL DEFAULT 'SIMULADO'
);
CREATE INDEX IF NOT EXISTS idx_firewall_hist_data ON firewall_historico(criado_em DESC);

CREATE TABLE IF NOT EXISTS firewall_snapshots (
    id BIGSERIAL PRIMARY KEY,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    ator_email VARCHAR(150),
    motivo VARCHAR(300) NOT NULL,
    regras_json TEXT NOT NULL,
    restaurado_em TIMESTAMP WITH TIME ZONE
);

CREATE TABLE IF NOT EXISTS firewall_estado (
    id BIGINT PRIMARY KEY,
    aplicado_em TIMESTAMP WITH TIME ZONE,
    snapshot_atual_id BIGINT,
    atualizado_em TIMESTAMP WITH TIME ZONE,
    atualizado_por VARCHAR(150)
);
INSERT INTO firewall_estado (id)
SELECT 1 WHERE NOT EXISTS (SELECT 1 FROM firewall_estado WHERE id = 1);

COMMIT;

-- ============================================================
-- SISTEMA DE BILHETAGEM - VALE TRANSPORTE
-- Script de criação do banco de dados SQLite
-- Versão: 1.0.0
-- Data: 2026-01-08
-- ============================================================

-- ------------------------------------------------------------
-- Tabela: solicitacoes
-- Armazena todas as solicitações de vale transporte
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS solicitacoes (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    data_efetivacao DATE NOT NULL,
    mes_referencia TEXT NOT NULL,
    matricula TEXT NOT NULL,
    cpf TEXT NOT NULL,
    nome TEXT NOT NULL,
    numero_cartao TEXT,
    quantidade_vale_tipo_a INTEGER DEFAULT 0,
    tipo_solicitacao TEXT CHECK(tipo_solicitacao IN ('adesao', 'renuncia', 'alteracao')),
    observacao TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ------------------------------------------------------------
-- Índices para otimização de consultas
-- ------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_matricula ON solicitacoes(matricula);
CREATE INDEX IF NOT EXISTS idx_cpf ON solicitacoes(cpf);
CREATE INDEX IF NOT EXISTS idx_mes_referencia ON solicitacoes(mes_referencia);
CREATE INDEX IF NOT EXISTS idx_tipo_solicitacao ON solicitacoes(tipo_solicitacao);
CREATE INDEX IF NOT EXISTS idx_data_efetivacao ON solicitacoes(data_efetivacao);

-- ------------------------------------------------------------
-- Tabela: logs_operacao
-- Registra todas as operações realizadas no sistema
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS logs_operacao (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    operacao TEXT NOT NULL,
    usuario TEXT DEFAULT 'sistema',
    detalhes TEXT,
    data_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ------------------------------------------------------------
-- View: vw_relatorio_mensal
-- Relatório consolidado por mês e tipo de solicitação
-- ------------------------------------------------------------
CREATE VIEW IF NOT EXISTS vw_relatorio_mensal AS
SELECT 
    mes_referencia,
    tipo_solicitacao,
    COUNT(*) as quantidade_total,
    SUM(quantidade_vale_tipo_a) as total_vales,
    COUNT(DISTINCT matricula) as funcionarios_unicos
FROM solicitacoes
GROUP BY mes_referencia, tipo_solicitacao
ORDER BY mes_referencia DESC, tipo_solicitacao;

-- ------------------------------------------------------------
-- View: vw_resumo_geral
-- Resumo geral do sistema
-- ------------------------------------------------------------
CREATE VIEW IF NOT EXISTS vw_resumo_geral AS
SELECT 
    COUNT(*) as total_solicitacoes,
    COUNT(DISTINCT matricula) as total_funcionarios,
    SUM(CASE WHEN tipo_solicitacao = 'adesao' THEN 1 ELSE 0 END) as total_adesoes,
    SUM(CASE WHEN tipo_solicitacao = 'renuncia' THEN 1 ELSE 0 END) as total_renuncias,
    SUM(CASE WHEN tipo_solicitacao = 'alteracao' THEN 1 ELSE 0 END) as total_alteracoes
FROM solicitacoes;

-- ------------------------------------------------------------
-- Tabela: usuarios
-- Gerencia os usuários do sistema
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS usuarios (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    login TEXT UNIQUE NOT NULL,
    senha TEXT NOT NULL,
    nome TEXT NOT NULL,
    email TEXT,
    perfil TEXT CHECK(perfil IN ('ADMIN', 'GERENTE', 'OPERADOR', 'CONSULTOR')),
    ativo INTEGER DEFAULT 1,
    ultimo_acesso TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ------------------------------------------------------------
-- Tabela: logs_auditoria
-- Registra todas as ações dos usuários
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS logs_auditoria (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    usuario_id INTEGER,
    acao TEXT NOT NULL,
    entidade TEXT,
    entidade_id INTEGER,
    detalhes TEXT,
    ip TEXT,
    data_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

-- ------------------------------------------------------------
-- Inserir usuário padrão (admin)
-- ------------------------------------------------------------
INSERT OR IGNORE INTO usuarios (login, senha, nome, email, perfil)
VALUES ('admin', 'admin123', 'Administrador', 'admin@bilhetagem.com', 'ADMIN');
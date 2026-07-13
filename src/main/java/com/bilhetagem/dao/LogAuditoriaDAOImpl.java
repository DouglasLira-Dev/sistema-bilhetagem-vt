package com.bilhetagem.dao;

import com.bilhetagem.model.LogAuditoria;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementação do DAO de logs de auditoria.
 * 
 * <p>Responsável por todas as operações de persistência
 * relacionadas aos logs de auditoria do sistema.</p>
 * 
 * @author [Seu Nome]
 * @version 1.0.0
 * @since 2026-01-08
 */
public class LogAuditoriaDAOImpl implements LogAuditoriaDAO {
    
    private static final Logger LOGGER = LogManager.getLogger(LogAuditoriaDAOImpl.class);
    
    // ===== CONSTANTES SQL =====
    private static final String SQL_INSERT = """
        INSERT INTO logs_auditoria (usuario_id, acao, entidade, entidade_id, detalhes, ip)
        VALUES (?, ?, ?, ?, ?, ?)
    """;
    
    private static final String SQL_SELECT_BY_USUARIO = 
        "SELECT * FROM logs_auditoria WHERE usuario_id = ? ORDER BY data_hora DESC";
    
    private static final String SQL_SELECT_BY_ENTIDADE = 
        "SELECT * FROM logs_auditoria WHERE entidade = ? AND entidade_id = ? ORDER BY data_hora DESC";
    
    private static final String SQL_SELECT_ULTIMOS = 
        "SELECT * FROM logs_auditoria ORDER BY data_hora DESC LIMIT ?";
    
    private static final String SQL_SELECT_BY_PERIODO = 
        "SELECT * FROM logs_auditoria WHERE data_hora BETWEEN ? AND ? ORDER BY data_hora DESC";
    
    private static final String SQL_SELECT_ALL = 
        "SELECT * FROM logs_auditoria ORDER BY data_hora DESC";
    
    private static final String SQL_SELECT_BY_ACAO = 
        "SELECT * FROM logs_auditoria WHERE acao = ? ORDER BY data_hora DESC";
    
    private static final String SQL_COUNT_BY_PERIODO = 
        "SELECT COUNT(*) FROM logs_auditoria WHERE data_hora BETWEEN ? AND ?";
    
    // ===== MÉTODOS PRINCIPAIS =====
    
    @Override
    public void registrar(LogAuditoria log) throws SQLException {
        LOGGER.debug("📝 Registrando log de auditoria: {}", log.getAcao());
        
        try (Connection conn = ConexaoBD.getInstance();
             PreparedStatement stmt = conn.prepareStatement(SQL_INSERT)) {
            
            stmt.setLong(1, log.getUsuarioId());
            stmt.setString(2, log.getAcao());
            stmt.setString(3, log.getEntidade());
            stmt.setObject(4, log.getEntidadeId());
            stmt.setString(5, log.getDetalhes());
            stmt.setString(6, log.getIp());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                // Recuperar ID usando SQLite específico (mesmo padrão de
                // UsuarioDAOImpl e SolicitacaoDAOImpl).
                try (Statement idStmt = conn.createStatement();
                     ResultSet rs = idStmt.executeQuery("SELECT last_insert_rowid()")) {
                    if (rs.next()) {
                        log.setId(rs.getLong(1));
                    }
                }
                LOGGER.info("✅ Log registrado com ID: {}", log.getId());
            } else {
                LOGGER.warn("⚠️ Nenhum log foi registrado");
            }
        }
    }
    
    @Override
    public List<LogAuditoria> listarPorUsuario(Long usuarioId) throws SQLException {
        LOGGER.debug("🔍 Buscando logs do usuário ID: {}", usuarioId);
        
        try (Connection conn = ConexaoBD.getInstance();
             PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_BY_USUARIO)) {
            
            stmt.setLong(1, usuarioId);
            return executarConsulta(stmt);
        }
    }
    
    @Override
    public List<LogAuditoria> listarPorEntidade(String entidade, Long entidadeId) throws SQLException {
        LOGGER.debug("🔍 Buscando logs da entidade: {} ID: {}", entidade, entidadeId);
        
        try (Connection conn = ConexaoBD.getInstance();
             PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_BY_ENTIDADE)) {
            
            stmt.setString(1, entidade);
            stmt.setLong(2, entidadeId);
            
            return executarConsulta(stmt);
        }
    }
    
    @Override
    public List<LogAuditoria> listarUltimos(int limite) throws SQLException {
        LOGGER.debug("🔍 Buscando últimos {} logs", limite);
        
        try (Connection conn = ConexaoBD.getInstance();
             PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_ULTIMOS)) {
            
            stmt.setInt(1, limite);
            return executarConsulta(stmt);
        }
    }
    
    @Override
    public List<LogAuditoria> listarPorPeriodo(String dataInicio, String dataFim) throws SQLException {
        LOGGER.debug("🔍 Buscando logs no período: {} a {}", dataInicio, dataFim);
        
        try (Connection conn = ConexaoBD.getInstance();
             PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_BY_PERIODO)) {
            
            stmt.setString(1, dataInicio);
            stmt.setString(2, dataFim);
            return executarConsulta(stmt);
        }
    }
    
    /**
     * Lista todos os logs (método adicional para facilitar consultas).
     */
    public List<LogAuditoria> listarTodos() throws SQLException {
        LOGGER.debug("🔍 Listando todos os logs");
        
        try (Connection conn = ConexaoBD.getInstance();
             PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_ALL);
             ResultSet rs = stmt.executeQuery()) {
            
            List<LogAuditoria> logs = new ArrayList<>();
            while (rs.next()) {
                logs.add(mapearResultSet(rs));
            }
            return logs;
        }
    }
    
    /**
     * Lista logs por ação específica.
     */
    public List<LogAuditoria> listarPorAcao(String acao) throws SQLException {
        LOGGER.debug("🔍 Buscando logs por ação: {}", acao);
        
        try (Connection conn = ConexaoBD.getInstance();
             PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_BY_ACAO)) {
            
            stmt.setString(1, acao);
            return executarConsulta(stmt);
        }
    }
    
    /**
     * Conta logs em um período.
     */
    public long contarPorPeriodo(String dataInicio, String dataFim) throws SQLException {
        LOGGER.debug("📊 Contando logs no período: {} a {}", dataInicio, dataFim);
        
        try (Connection conn = ConexaoBD.getInstance();
             PreparedStatement stmt = conn.prepareStatement(SQL_COUNT_BY_PERIODO)) {
            
            stmt.setString(1, dataInicio);
            stmt.setString(2, dataFim);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
                return 0;
            }
        }
    }
    
    // ===== MÉTODOS AUXILIARES =====
    
    /**
     * Executa uma consulta preparada e retorna a lista de logs.
     */
    private List<LogAuditoria> executarConsulta(PreparedStatement stmt) throws SQLException {
        List<LogAuditoria> logs = new ArrayList<>();
        
        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                logs.add(mapearResultSet(rs));
            }
        }
        
        LOGGER.debug("✅ Encontrados {} logs", logs.size());
        return logs;
    }
    
    /**
     * Mapeia um ResultSet para um objeto LogAuditoria.
     */
    private LogAuditoria mapearResultSet(ResultSet rs) throws SQLException {
        LogAuditoria log = new LogAuditoria();
        
        log.setId(rs.getLong("id"));
        log.setUsuarioId(rs.getLong("usuario_id"));
        log.setAcao(rs.getString("acao"));
        log.setEntidade(rs.getString("entidade"));
        
        // Entidade ID pode ser null
        Object entidadeIdObj = rs.getObject("entidade_id");
        if (entidadeIdObj != null) {
            log.setEntidadeId(rs.getLong("entidade_id"));
        }
        
        log.setDetalhes(rs.getString("detalhes"));
        log.setIp(rs.getString("ip"));
        
        // Data/Hora
        String dataHoraStr = rs.getString("data_hora");
        if (dataHoraStr != null) {
            // Converter formato SQLite para LocalDateTime
            dataHoraStr = dataHoraStr.replace(' ', 'T');
            log.setDataHora(LocalDateTime.parse(dataHoraStr));
        }
        
        return log;
    }
}
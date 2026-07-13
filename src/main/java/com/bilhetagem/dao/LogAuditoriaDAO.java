package com.bilhetagem.dao;

import com.bilhetagem.model.LogAuditoria;
import java.sql.SQLException;
import java.util.List;

/**
 * Interface para operações de acesso a dados de logs de auditoria.
 * 
 * @author [Seu Nome]
 * @version 1.0.0
 * @since 2026-01-09
 */
public interface LogAuditoriaDAO {
    
    /**
     * Registra um log de auditoria.
     */
    void registrar(LogAuditoria log) throws SQLException;
    
    /**
     * Lista logs por usuário.
     */
    List<LogAuditoria> listarPorUsuario(Long usuarioId) throws SQLException;
    
    /**
     * Lista logs por entidade.
     */
    List<LogAuditoria> listarPorEntidade(String entidade, Long entidadeId) throws SQLException;
    
    /**
     * Lista os últimos logs.
     */
    List<LogAuditoria> listarUltimos(int limite) throws SQLException;
    
    /**
     * Lista logs por período.
     */
    List<LogAuditoria> listarPorPeriodo(String dataInicio, String dataFim) throws SQLException;
    
    /**
     * Lista logs por ação específica.
     */
    List<LogAuditoria> listarPorAcao(String acao) throws SQLException;
}
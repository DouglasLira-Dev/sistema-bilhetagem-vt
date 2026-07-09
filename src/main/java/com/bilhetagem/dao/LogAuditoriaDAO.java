package com.bilhetagem.dao;

import com.bilhetagem.model.LogAuditoria;
import java.sql.SQLException;
import java.util.List;

public interface LogAuditoriaDAO {
    void registrar(LogAuditoria log) throws SQLException;
    List<LogAuditoria> listarPorUsuario(Long usuarioId) throws SQLException;
    List<LogAuditoria> listarPorEntidade(String entidade, Long entidadeId) throws SQLException;
    List<LogAuditoria> listarUltimos(int limite) throws SQLException;
    List<LogAuditoria> listarPorPeriodo(String dataInicio, String dataFim) throws SQLException;
}
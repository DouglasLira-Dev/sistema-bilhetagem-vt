package com.bilhetagem.dao;

import com.bilhetagem.model.Solicitacao;
import com.bilhetagem.model.Solicitacao.TipoSolicitacao;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementação concreta do DAO para solicitações usando SQLite com Soft Delete.
 * 
 * @author [Seu Nome]
 * @version 1.0.0
 * @since 2026-01-08
 */
public class SolicitacaoDAOImpl implements SolicitacaoDAO {
    
    private static final Logger LOGGER = LogManager.getLogger(SolicitacaoDAOImpl.class);
    
    // ===== CONSTANTES SQL =====
    private static final String SQL_INSERT = """
        INSERT INTO solicitacoes (
            data_efetivacao, mes_referencia, matricula, cpf, nome,
            numero_cartao, quantidade_vale_tipo_a, tipo_solicitacao, observacao
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
    """;
    
    private static final String SQL_SELECT_BY_ID = 
        "SELECT * FROM solicitacoes WHERE id = ? AND deleted_at IS NULL";
    
    private static final String SQL_SELECT_BY_ID_INCLUINDO_DELETADO = 
        "SELECT * FROM solicitacoes WHERE id = ?";
    
    private static final String SQL_SELECT_ALL = 
        "SELECT * FROM solicitacoes WHERE deleted_at IS NULL ORDER BY data_efetivacao DESC, id DESC";
    
    private static final String SQL_SELECT_ALL_INCLUINDO_DELETADOS = 
        "SELECT * FROM solicitacoes ORDER BY data_efetivacao DESC, id DESC";
    
    private static final String SQL_SELECT_DELETADOS = 
        "SELECT * FROM solicitacoes WHERE deleted_at IS NOT NULL ORDER BY deleted_at DESC";
    
    private static final String SQL_SELECT_BY_MATRICULA = 
        "SELECT * FROM solicitacoes WHERE matricula = ? AND deleted_at IS NULL ORDER BY data_efetivacao DESC";
    
    private static final String SQL_SELECT_BY_CPF = 
        "SELECT * FROM solicitacoes WHERE cpf = ? AND deleted_at IS NULL ORDER BY data_efetivacao DESC";
    
    private static final String SQL_SELECT_BY_NOME = 
        "SELECT * FROM solicitacoes WHERE nome LIKE ? AND deleted_at IS NULL ORDER BY data_efetivacao DESC";
    
    private static final String SQL_SELECT_BY_MES = 
        "SELECT * FROM solicitacoes WHERE mes_referencia = ? AND deleted_at IS NULL ORDER BY data_efetivacao DESC";
    
    private static final String SQL_SELECT_BY_MES_TIPO = 
        "SELECT * FROM solicitacoes WHERE mes_referencia = ? AND tipo_solicitacao = ? AND deleted_at IS NULL " +
        "ORDER BY data_efetivacao DESC";
    
    private static final String SQL_SELECT_BY_PERIODO = 
        "SELECT * FROM solicitacoes WHERE data_efetivacao BETWEEN ? AND ? AND deleted_at IS NULL " +
        "ORDER BY data_efetivacao DESC";
    
    private static final String SQL_UPDATE = """
        UPDATE solicitacoes SET
            data_efetivacao = ?,
            mes_referencia = ?,
            matricula = ?,
            cpf = ?,
            nome = ?,
            numero_cartao = ?,
            quantidade_vale_tipo_a = ?,
            tipo_solicitacao = ?,
            observacao = ?,
            updated_at = CURRENT_TIMESTAMP
        WHERE id = ? AND deleted_at IS NULL
    """;
    
    private static final String SQL_DELETE = 
        "UPDATE solicitacoes SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?";
    
    private static final String SQL_DELETE_PERMANENTE = 
        "DELETE FROM solicitacoes WHERE id = ?";
    
    private static final String SQL_RESTAURAR = 
        "UPDATE solicitacoes SET deleted_at = NULL, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
    
    private static final String SQL_EXISTS_BY_ID = 
        "SELECT 1 FROM solicitacoes WHERE id = ? AND deleted_at IS NULL";
    
    private static final String SQL_COUNT = 
        "SELECT COUNT(*) FROM solicitacoes WHERE deleted_at IS NULL";
    
    private static final String SQL_COUNT_DELETADOS = 
        "SELECT COUNT(*) FROM solicitacoes WHERE deleted_at IS NOT NULL";
    
    private static final String SQL_SELECT_FILTRADO = """
        SELECT * FROM solicitacoes 
        WHERE deleted_at IS NULL
          AND (matricula = ? OR ? IS NULL OR ? = '')
          AND (cpf = ? OR ? IS NULL OR ? = '')
          AND (nome LIKE ? OR ? IS NULL OR ? = '')
          AND (mes_referencia = ? OR ? IS NULL OR ? = '')
          AND (tipo_solicitacao = ? OR ? IS NULL OR ? = '')
        ORDER BY data_efetivacao DESC
    """;
    
    // ===== MÉTODOS PRIVADOS =====
    
    private Solicitacao mapearResultSet(ResultSet rs) throws SQLException {
        Solicitacao solicitacao = new Solicitacao();
        
        solicitacao.setId(rs.getLong("id"));
        
        String dataStr = rs.getString("data_efetivacao");
        if (dataStr != null) {
            solicitacao.setDataEfetivacao(LocalDate.parse(dataStr));
        }
        
        solicitacao.setMesReferencia(rs.getString("mes_referencia"));
        solicitacao.setMatricula(rs.getString("matricula"));
        solicitacao.setCpf(rs.getString("cpf"));
        solicitacao.setNome(rs.getString("nome"));
        solicitacao.setNumeroCartao(rs.getString("numero_cartao"));
        solicitacao.setQuantidadeValeTipoA(rs.getInt("quantidade_vale_tipo_a"));
        
        String tipoStr = rs.getString("tipo_solicitacao");
        if (tipoStr != null) {
            solicitacao.setTipoSolicitacao(TipoSolicitacao.fromValor(tipoStr));
        }
        
        solicitacao.setObservacao(rs.getString("observacao"));
        
        String createdAtStr = rs.getString("created_at");
        if (createdAtStr != null) {
            solicitacao.setCreatedAt(LocalDateTime.parse(createdAtStr.replace(' ', 'T')));
        }
        
        String updatedAtStr = rs.getString("updated_at");
        if (updatedAtStr != null) {
            solicitacao.setUpdatedAt(LocalDateTime.parse(updatedAtStr.replace(' ', 'T')));
        }
        
        String deletedAtStr = rs.getString("deleted_at");
        if (deletedAtStr != null) {
            solicitacao.setDeletedAt(LocalDateTime.parse(deletedAtStr.replace(' ', 'T')));
        }
        
        return solicitacao;
    }
    
    private void preencherStatement(PreparedStatement stmt, Solicitacao s) throws SQLException {
        stmt.setString(1, s.getDataEfetivacao().toString());
        stmt.setString(2, s.getMesReferencia());
        stmt.setString(3, s.getMatricula());
        stmt.setString(4, s.getCpf());
        stmt.setString(5, s.getNome());
        stmt.setString(6, s.getNumeroCartao());
        stmt.setInt(7, s.getQuantidadeValeTipoA() != null ? s.getQuantidadeValeTipoA() : 0);
        stmt.setString(8, s.getTipoSolicitacao().getValor());
        stmt.setString(9, s.getObservacao());
    }
    
    private List<Solicitacao> executarConsulta(PreparedStatement stmt) throws SQLException {
        List<Solicitacao> lista = new ArrayList<>();
        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearResultSet(rs));
            }
        }
        return lista;
    }
    
    // ===== IMPLEMENTAÇÃO DOS MÉTODOS =====
    
    @Override
    public Solicitacao salvar(Solicitacao solicitacao) throws SQLException {
        LOGGER.debug("Salvando solicitação: {}", solicitacao);
        
        if (!solicitacao.isValid()) {
            throw new IllegalArgumentException("Solicitação inválida para persistência");
        }
        
        try (Connection conn = ConexaoBD.getInstance();
             PreparedStatement stmt = conn.prepareStatement(SQL_INSERT)) {
            
            preencherStatement(stmt, solicitacao);
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Falha ao salvar solicitação");
            }
            
            // Recuperar ID usando SQLite específico
            try (Statement idStmt = conn.createStatement();
                 ResultSet rs = idStmt.executeQuery("SELECT last_insert_rowid()")) {
                if (rs.next()) {
                    solicitacao.setId(rs.getLong(1));
                }
            }
            
            LOGGER.info("✅ Solicitação salva com ID: {}", solicitacao.getId());
            return solicitacao;
        }
    }
    
    @Override
    public Optional<Solicitacao> buscarPorId(Long id) throws SQLException {
        LOGGER.debug("Buscando solicitação por ID: {}", id);
        
        try (Connection conn = ConexaoBD.getInstance();
             PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_BY_ID)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearResultSet(rs));
                }
            }
        }
        return Optional.empty();
    }
    
    @Override
    public List<Solicitacao> listarTodos() throws SQLException {
        LOGGER.debug("Listando todas as solicitações (não deletadas)");
        try (Connection conn = ConexaoBD.getInstance();
             PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_ALL)) {
            return executarConsulta(stmt);
        }
    }
    
    @Override
    public List<Solicitacao> listarTodosIncluindoDeletados() throws SQLException {
        LOGGER.debug("Listando todas as solicitações (incluindo deletadas)");
        try (Connection conn = ConexaoBD.getInstance();
             PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_ALL_INCLUINDO_DELETADOS)) {
            return executarConsulta(stmt);
        }
    }
    
    @Override
    public List<Solicitacao> listarDeletados() throws SQLException {
        LOGGER.debug("Listando solicitações deletadas");
        try (Connection conn = ConexaoBD.getInstance();
             PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_DELETADOS)) {
            return executarConsulta(stmt);
        }
    }
    
    @Override
    public List<Solicitacao> buscarPorMatricula(String matricula) throws SQLException {
        LOGGER.debug("Buscando solicitações por matrícula: {}", matricula);
        try (Connection conn = ConexaoBD.getInstance();
             PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_BY_MATRICULA)) {
            stmt.setString(1, matricula);
            return executarConsulta(stmt);
        }
    }
    
    @Override
    public List<Solicitacao> buscarPorCpf(String cpf) throws SQLException {
        LOGGER.debug("Buscando solicitações por CPF: {}", cpf);
        try (Connection conn = ConexaoBD.getInstance();
             PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_BY_CPF)) {
            stmt.setString(1, cpf);
            return executarConsulta(stmt);
        }
    }
    
    @Override
    public List<Solicitacao> buscarPorNome(String nome) throws SQLException {
        LOGGER.debug("Buscando solicitações por nome: {}", nome);
        try (Connection conn = ConexaoBD.getInstance();
             PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_BY_NOME)) {
            stmt.setString(1, "%" + nome + "%");
            return executarConsulta(stmt);
        }
    }
    
    @Override
    public List<Solicitacao> buscarPorMesReferencia(String mesReferencia) throws SQLException {
        LOGGER.debug("Buscando solicitações por mês: {}", mesReferencia);
        try (Connection conn = ConexaoBD.getInstance();
             PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_BY_MES)) {
            stmt.setString(1, mesReferencia);
            return executarConsulta(stmt);
        }
    }
    
    @Override
    public List<Solicitacao> buscarPorMesETipo(String mesReferencia, TipoSolicitacao tipo) 
            throws SQLException {
        LOGGER.debug("Buscando solicitações por mês {} e tipo {}", mesReferencia, tipo);
        try (Connection conn = ConexaoBD.getInstance();
             PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_BY_MES_TIPO)) {
            stmt.setString(1, mesReferencia);
            stmt.setString(2, tipo.getValor());
            return executarConsulta(stmt);
        }
    }
    
    @Override
    public List<Solicitacao> buscarPorPeriodo(String dataInicial, String dataFinal) 
            throws SQLException {
        LOGGER.debug("Buscando solicitações no período: {} a {}", dataInicial, dataFinal);
        try (Connection conn = ConexaoBD.getInstance();
             PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_BY_PERIODO)) {
            stmt.setString(1, dataInicial);
            stmt.setString(2, dataFinal);
            return executarConsulta(stmt);
        }
    }
    
    @Override
    public boolean atualizar(Solicitacao solicitacao) throws SQLException {
        LOGGER.debug("Atualizando solicitação: {}", solicitacao);
        
        if (solicitacao.getId() == null) {
            throw new IllegalArgumentException("ID da solicitação não pode ser null");
        }
        
        try (Connection conn = ConexaoBD.getInstance();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE)) {
            
            preencherStatement(stmt, solicitacao);
            stmt.setLong(10, solicitacao.getId());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                LOGGER.info("✅ Solicitação {} atualizada com sucesso", solicitacao.getId());
                return true;
            }
            return false;
        }
    }
    
    @Override
    public boolean excluir(Long id) throws SQLException {
        LOGGER.debug("Soft delete da solicitação ID: {}", id);
        try (Connection conn = ConexaoBD.getInstance();
             PreparedStatement stmt = conn.prepareStatement(SQL_DELETE)) {
            stmt.setLong(1, id);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                LOGGER.info("🗑️ Solicitação {} movida para lixeira", id);
                return true;
            }
            return false;
        }
    }
    
    @Override
    public boolean excluirPermanente(Long id) throws SQLException {
        LOGGER.debug("Exclusão permanente da solicitação ID: {}", id);
        try (Connection conn = ConexaoBD.getInstance();
             PreparedStatement stmt = conn.prepareStatement(SQL_DELETE_PERMANENTE)) {
            stmt.setLong(1, id);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                LOGGER.info("🗑️ Solicitação {} excluída permanentemente", id);
                return true;
            }
            return false;
        }
    }
    
    @Override
    public boolean restaurar(Long id) throws SQLException {
        LOGGER.debug("Restaurando solicitação ID: {}", id);
        try (Connection conn = ConexaoBD.getInstance();
             PreparedStatement stmt = conn.prepareStatement(SQL_RESTAURAR)) {
            stmt.setLong(1, id);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                LOGGER.info("♻️ Solicitação {} restaurada com sucesso", id);
                return true;
            }
            return false;
        }
    }
    
    @Override
    public boolean existePorId(Long id) throws SQLException {
        LOGGER.debug("Verificando existência da solicitação ID: {}", id);
        try (Connection conn = ConexaoBD.getInstance();
             PreparedStatement stmt = conn.prepareStatement(SQL_EXISTS_BY_ID)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }
    
    @Override
    public long contarTotal() throws SQLException {
        LOGGER.debug("Contando total de solicitações não deletadas");
        try (Connection conn = ConexaoBD.getInstance();
             PreparedStatement stmt = conn.prepareStatement(SQL_COUNT);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0;
        }
    }
    
    @Override
    public long contarDeletados() throws SQLException {
        LOGGER.debug("Contando solicitações deletadas");
        try (Connection conn = ConexaoBD.getInstance();
             PreparedStatement stmt = conn.prepareStatement(SQL_COUNT_DELETADOS);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0;
        }
    }
    
    @Override
    public List<Solicitacao> buscarComFiltros(String matricula, String cpf, String nome,
                                             String mesReferencia, TipoSolicitacao tipo) 
            throws SQLException {
        LOGGER.debug("Buscando com filtros");
        try (Connection conn = ConexaoBD.getInstance();
             PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_FILTRADO)) {
            
            stmt.setString(1, matricula);
            stmt.setString(2, matricula);
            stmt.setString(3, matricula);
            
            stmt.setString(4, cpf);
            stmt.setString(5, cpf);
            stmt.setString(6, cpf);
            
            String nomeLike = (nome != null && !nome.isEmpty()) ? "%" + nome + "%" : null;
            stmt.setString(7, nomeLike);
            stmt.setString(8, nomeLike);
            stmt.setString(9, nomeLike);
            
            stmt.setString(10, mesReferencia);
            stmt.setString(11, mesReferencia);
            stmt.setString(12, mesReferencia);
            
            String tipoValor = tipo != null ? tipo.getValor() : null;
            stmt.setString(13, tipoValor);
            stmt.setString(14, tipoValor);
            stmt.setString(15, tipoValor);
            
            return executarConsulta(stmt);
        }
    }
}
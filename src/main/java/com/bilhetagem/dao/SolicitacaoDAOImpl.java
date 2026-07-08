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
 * Implementação concreta do DAO para solicitações usando SQLite.
 * 
 * <p>Esta classe implementa todas as operações definidas na interface
 * {@link SolicitacaoDAO} utilizando JDBC para comunicação com o SQLite.</p>
 * 
 * <p>Utiliza PreparedStatement para prevenir SQL Injection e garantir
 * segurança nas operações com o banco de dados.</p>
 * 
 * @author [Seu Nome]
 * @version 1.0.0
 * @since 2026-01-08
 */
public class SolicitacaoDAOImpl implements SolicitacaoDAO {
    
    /** Logger para registro de operações */
    private static final Logger LOGGER = LogManager.getLogger(SolicitacaoDAOImpl.class);
    
    // ===== CONSTANTES SQL =====
    private static final String SQL_INSERT = """
        INSERT INTO solicitacoes (
            data_efetivacao, mes_referencia, matricula, cpf, nome,
            numero_cartao, quantidade_vale_tipo_a, tipo_solicitacao, observacao
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
    """;
    
    private static final String SQL_SELECT_BY_ID = "SELECT * FROM solicitacoes WHERE id = ?";
    
    private static final String SQL_SELECT_BY_MATRICULA = 
        "SELECT * FROM solicitacoes WHERE matricula = ? ORDER BY data_efetivacao DESC";
    
    private static final String SQL_SELECT_BY_CPF = 
        "SELECT * FROM solicitacoes WHERE cpf = ? ORDER BY data_efetivacao DESC";
    
    private static final String SQL_SELECT_BY_NOME = 
        "SELECT * FROM solicitacoes WHERE nome LIKE ? ORDER BY data_efetivacao DESC";
    
    private static final String SQL_SELECT_BY_MES = 
        "SELECT * FROM solicitacoes WHERE mes_referencia = ? ORDER BY data_efetivacao DESC";
    
    private static final String SQL_SELECT_BY_MES_TIPO = 
        "SELECT * FROM solicitacoes WHERE mes_referencia = ? AND tipo_solicitacao = ? " +
        "ORDER BY data_efetivacao DESC";
    
    private static final String SQL_SELECT_BY_PERIODO = 
        "SELECT * FROM solicitacoes WHERE data_efetivacao BETWEEN ? AND ? " +
        "ORDER BY data_efetivacao DESC";
    
    private static final String SQL_SELECT_ALL = 
        "SELECT * FROM solicitacoes ORDER BY data_efetivacao DESC, id DESC";
    
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
        WHERE id = ?
    """;
    
    private static final String SQL_DELETE = "DELETE FROM solicitacoes WHERE id = ?";
    
    private static final String SQL_EXISTS_BY_ID = "SELECT 1 FROM solicitacoes WHERE id = ?";
    
    private static final String SQL_COUNT = "SELECT COUNT(*) FROM solicitacoes";
    
    private static final String SQL_SELECT_FILTRADO = """
        SELECT * FROM solicitacoes 
        WHERE (matricula = ? OR ? IS NULL OR ? = '')
          AND (cpf = ? OR ? IS NULL OR ? = '')
          AND (nome LIKE ? OR ? IS NULL OR ? = '')
          AND (mes_referencia = ? OR ? IS NULL OR ? = '')
          AND (tipo_solicitacao = ? OR ? IS NULL OR ? = '')
        ORDER BY data_efetivacao DESC
    """;
    
    // ===== MÉTODOS PRIVADOS =====
    
    /**
     * Mapeia um ResultSet para um objeto Solicitacao.
     * 
     * @param rs ResultSet com os dados
     * @return Objeto Solicitacao populado
     * @throws SQLException Se houver erro na leitura
     */
    private Solicitacao mapearResultSet(ResultSet rs) throws SQLException {
        Solicitacao solicitacao = new Solicitacao();
        
        solicitacao.setId(rs.getLong("id"));
        
        // Data de efetivação
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
        
        // Tipo de solicitação
        String tipoStr = rs.getString("tipo_solicitacao");
        if (tipoStr != null) {
            solicitacao.setTipoSolicitacao(TipoSolicitacao.fromValor(tipoStr));
        }
        
        solicitacao.setObservacao(rs.getString("observacao"));
        
        // Datas de auditoria
        String createdAtStr = rs.getString("created_at");
        if (createdAtStr != null) {
            solicitacao.setCreatedAt(LocalDateTime.parse(createdAtStr.replace(' ', 'T')));
        }
        
        String updatedAtStr = rs.getString("updated_at");
        if (updatedAtStr != null) {
            solicitacao.setUpdatedAt(LocalDateTime.parse(updatedAtStr.replace(' ', 'T')));
        }
        
        return solicitacao;
    }
    
    /**
     * Prepara um PreparedStatement para INSERT ou UPDATE.
     * 
     * @param stmt PreparedStatement a ser preparado
     * @param s Solicitacao com os dados
     * @throws SQLException Se houver erro na preparação
     */
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
    
    // ===== IMPLEMENTAÇÃO DOS MÉTODOS DA INTERFACE =====
    
    @Override
    public Solicitacao salvar(Solicitacao solicitacao) throws SQLException {
        LOGGER.debug("Salvando solicitação: {}", solicitacao);
        
        if (!solicitacao.isValid()) {
            throw new IllegalArgumentException("Solicitação inválida para persistência");
        }
        
        String sql = SQL_INSERT;
        
        try (Connection conn = ConexaoBD.getInstance();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            preencherStatement(stmt, solicitacao);
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Falha ao salvar solicitação, nenhuma linha afetada.");
            }
            
            // Recuperar o ID gerado
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    solicitacao.setId(generatedKeys.getLong(1));
                    LOGGER.info("✅ Solicitação salva com ID: {}", solicitacao.getId());
                } else {
                    throw new SQLException("Falha ao recuperar ID gerado.");
                }
            }
            
            // Registrar operação no log
            registrarLog("INSERT", solicitacao.getId(), "Solicitação criada");
            
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
                    Solicitacao solicitacao = mapearResultSet(rs);
                    LOGGER.debug("✅ Solicitação encontrada: {}", solicitacao);
                    return Optional.of(solicitacao);
                }
            }
        }
        
        LOGGER.debug("❌ Solicitação não encontrada para ID: {}", id);
        return Optional.empty();
    }
    
    @Override
    public List<Solicitacao> buscarPorMatricula(String matricula) throws SQLException {
        LOGGER.debug("Buscando solicitações por matrícula: {}", matricula);
        return buscarPorString(SQL_SELECT_BY_MATRICULA, matricula);
    }
    
    @Override
    public List<Solicitacao> buscarPorCpf(String cpf) throws SQLException {
        LOGGER.debug("Buscando solicitações por CPF: {}", cpf);
        return buscarPorString(SQL_SELECT_BY_CPF, cpf);
    }
    
    @Override
    public List<Solicitacao> buscarPorNome(String nome) throws SQLException {
        LOGGER.debug("Buscando solicitações por nome: {}", nome);
        return buscarPorString(SQL_SELECT_BY_NOME, "%" + nome + "%");
    }
    
    @Override
    public List<Solicitacao> buscarPorMesReferencia(String mesReferencia) throws SQLException {
        LOGGER.debug("Buscando solicitações por mês: {}", mesReferencia);
        return buscarPorString(SQL_SELECT_BY_MES, mesReferencia);
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
    public List<Solicitacao> listarTodos() throws SQLException {
        LOGGER.debug("Listando todas as solicitações");
        
        try (Connection conn = ConexaoBD.getInstance();
             PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_ALL)) {
            
            return executarConsulta(stmt);
        }
    }
    
    @Override
    public boolean atualizar(Solicitacao solicitacao) throws SQLException {
        LOGGER.debug("Atualizando solicitação: {}", solicitacao);
        
        if (solicitacao.getId() == null) {
            throw new IllegalArgumentException("ID da solicitação não pode ser null");
        }
        
        if (!solicitacao.isValid()) {
            throw new IllegalArgumentException("Solicitação inválida para atualização");
        }
        
        try (Connection conn = ConexaoBD.getInstance();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE)) {
            
            preencherStatement(stmt, solicitacao);
            stmt.setLong(10, solicitacao.getId());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                LOGGER.info("✅ Solicitação {} atualizada com sucesso", solicitacao.getId());
                registrarLog("UPDATE", solicitacao.getId(), "Solicitação atualizada");
                return true;
            }
            
            LOGGER.warn("⚠️ Nenhuma solicitação atualizada para ID: {}", solicitacao.getId());
            return false;
        }
    }
    
    @Override
    public boolean excluir(Long id) throws SQLException {
        LOGGER.debug("Excluindo solicitação ID: {}", id);
        
        try (Connection conn = ConexaoBD.getInstance();
             PreparedStatement stmt = conn.prepareStatement(SQL_DELETE)) {
            
            stmt.setLong(1, id);
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                LOGGER.info("🗑️ Solicitação {} excluída com sucesso", id);
                registrarLog("DELETE", id, "Solicitação excluída");
                return true;
            }
            
            LOGGER.warn("⚠️ Nenhuma solicitação excluída para ID: {}", id);
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
        LOGGER.debug("Contando total de solicitações");
        
        try (Connection conn = ConexaoBD.getInstance();
             PreparedStatement stmt = conn.prepareStatement(SQL_COUNT);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getLong(1);
            }
            
            return 0;
        }
    }
    
    @Override
    public List<Solicitacao> buscarComFiltros(String matricula, String cpf, String nome,
                                             String mesReferencia, TipoSolicitacao tipo) 
            throws SQLException {
        LOGGER.debug("Buscando com filtros - Mat: {}, CPF: {}, Nome: {}, Mês: {}, Tipo: {}",
                    matricula, cpf, nome, mesReferencia, tipo);
        
        try (Connection conn = ConexaoBD.getInstance();
             PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_FILTRADO)) {
            
            // Parâmetros para matrícula
            stmt.setString(1, matricula);
            stmt.setString(2, matricula);
            stmt.setString(3, matricula);
            
            // Parâmetros para CPF
            stmt.setString(4, cpf);
            stmt.setString(5, cpf);
            stmt.setString(6, cpf);
            
            // Parâmetros para nome
            String nomeLike = (nome != null && !nome.isEmpty()) ? "%" + nome + "%" : null;
            stmt.setString(7, nomeLike);
            stmt.setString(8, nomeLike);
            stmt.setString(9, nomeLike);
            
            // Parâmetros para mês
            stmt.setString(10, mesReferencia);
            stmt.setString(11, mesReferencia);
            stmt.setString(12, mesReferencia);
            
            // Parâmetros para tipo
            String tipoValor = tipo != null ? tipo.getValor() : null;
            stmt.setString(13, tipoValor);
            stmt.setString(14, tipoValor);
            stmt.setString(15, tipoValor);
            
            return executarConsulta(stmt);
        }
    }
    
    // ===== MÉTODOS AUXILIARES =====
    
    /**
     * Método auxiliar para consultas com um único parâmetro string.
     * 
     * @param sql SQL da consulta
     * @param valor Valor do parâmetro
     * @return Lista de solicitações
     * @throws SQLException Se houver erro na operação
     */
    private List<Solicitacao> buscarPorString(String sql, String valor) throws SQLException {
        try (Connection conn = ConexaoBD.getInstance();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, valor);
            return executarConsulta(stmt);
        }
    }
    
    /**
     * Executa uma consulta e retorna a lista de solicitações.
     * 
     * @param stmt PreparedStatement já configurado
     * @return Lista de solicitações
     * @throws SQLException Se houver erro na operação
     */
    private List<Solicitacao> executarConsulta(PreparedStatement stmt) throws SQLException {
        List<Solicitacao> lista = new ArrayList<>();
        
        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearResultSet(rs));
            }
        }
        
        LOGGER.debug("✅ Encontradas {} solicitações", lista.size());
        return lista;
    }
    
    /**
     * Registra uma operação no log de auditoria.
     * 
     * @param operacao Tipo de operação (INSERT, UPDATE, DELETE)
     * @param idSolicitacao ID da solicitação afetada
     * @param detalhes Detalhes adicionais
     */
    private void registrarLog(String operacao, Long idSolicitacao, String detalhes) {
        // TODO: Implementar registro em tabela de logs
        LOGGER.info("📝 LOG: {} - Solicitação ID: {} - {}", operacao, idSolicitacao, detalhes);
    }
}
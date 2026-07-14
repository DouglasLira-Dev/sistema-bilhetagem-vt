package com.bilhetagem.util;

import com.bilhetagem.dao.ConexaoBD;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

/**
 * Classe utilitária para gerenciamento do banco de dados.
 * 
 * <p>Responsável por inicializar a estrutura do banco de dados,
 * executar scripts SQL e verificar integridade.</p>
 * 
 * @author Equipe de desenvolvimento
 * @version 1.0.0
 * @since 2026-01-08
 */
public class BancoUtil {
    
    private static final Logger LOGGER = LogManager.getLogger(BancoUtil.class);
    
    /**
     * Inicializa o banco de dados com a estrutura necessária.
     * Executa o script schema.sql se o banco estiver vazio.
     */
    public static void inicializarBanco() {
        LOGGER.info("🔄 Verificando estrutura do banco de dados...");
        
        try (Connection conn = ConexaoBD.getInstance()) {
            // Verificar se a tabela principal existe
            boolean tabelaExiste = verificarTabelaExiste(conn, "solicitacoes");
            
            if (!tabelaExiste) {
                LOGGER.info("📦 Criando estrutura do banco de dados...");
                executarScriptSQL(conn, "/database/schema.sql");
                LOGGER.info("✅ Estrutura criada com sucesso!");
            } else {
                LOGGER.info("✅ Estrutura já existe. Banco de dados pronto.");
            }
            
        } catch (SQLException e) {
            LOGGER.error("❌ Erro ao inicializar banco de dados!", e);
            throw new RuntimeException("Falha ao inicializar banco de dados", e);
        }
    }
    
    /**
     * Verifica se uma tabela existe no banco de dados.
     * 
     * @param conn Conexão ativa
     * @param nomeTabela Nome da tabela a verificar
     * @return true se a tabela existe
     * @throws SQLException se houver erro na consulta
     */
    private static boolean verificarTabelaExiste(Connection conn, String nomeTabela) throws SQLException {
        String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name=?";
        try (var stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nomeTabela);
            var rs = stmt.executeQuery();
            return rs.next();
        }
    }
    
    /**
     * Executa um script SQL contido em um recurso do classpath.
     * 
     * @param conn Conexão ativa
     * @param resourcePath Caminho do recurso (ex: "/database/schema.sql")
     * @throws SQLException se houver erro na execução
     */
    private static void executarScriptSQL(Connection conn, String resourcePath) throws SQLException {
        try (InputStream input = BancoUtil.class.getResourceAsStream(resourcePath)) {
            if (input == null) {
                throw new RuntimeException("Recurso não encontrado: " + resourcePath);
            }
            
            String script = new BufferedReader(new InputStreamReader(input))
                    .lines()
                    .filter(line -> !line.trim().startsWith("--") && !line.trim().isEmpty())
                    .collect(Collectors.joining("\n"));
            
            // Dividir o script em comandos separados por ";"
            String[] comandos = script.split(";");
            
            try (Statement stmt = conn.createStatement()) {
                for (String comando : comandos) {
                    String cmd = comando.trim();
                    if (!cmd.isEmpty()) {
                        LOGGER.debug("Executando: {}", cmd);
                        stmt.execute(cmd);
                    }
                }
            }
            LOGGER.info("✅ Script executado com sucesso!");
            
        } catch (Exception e) {
            LOGGER.error("❌ Erro ao executar script SQL!", e);
            throw new SQLException("Erro ao executar script", e);
        }
    }
}
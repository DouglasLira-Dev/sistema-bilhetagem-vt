package com.bilhetagem.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.File;
import java.nio.file.Paths;

/**
 * Classe responsável pela gestão da conexão com o banco de dados SQLite.
 * 
 * <p>Esta classe implementa o padrão Singleton para garantir que apenas
 * uma instância de conexão seja criada durante a execução da aplicação.</p>
 * 
 * <p>O banco de dados é criado automaticamente no diretório 'data/' 
 * caso não exista, com o nome 'bilhetagem.db'.</p>
 * 
 * @author [Seu Nome]
 * @version 1.0.0
 * @since 2026-01-08
 */
public class ConexaoBD {
    
    /** Logger para registro de eventos e erros */
    private static final Logger LOGGER = LogManager.getLogger(ConexaoBD.class);
    
    /** Nome do arquivo do banco de dados */
    private static final String DB_NAME = "bilhetagem.db";
    
    /** Diretório onde o banco será armazenado */
    private static final String DB_DIRECTORY = "data";
    
    /** URL de conexão com o SQLite */
    private static final String DB_URL = "jdbc:sqlite:" + DB_DIRECTORY + File.separator + DB_NAME;
    
    /** Instância única da conexão (Singleton) */
    private static Connection connection = null;
    
    /**
     * Construtor privado para evitar instanciação externa.
     * Configura o diretório do banco e estabelece a conexão.
     */
    private ConexaoBD() {
        criarDiretorioBanco();
        conectar();
    }
    
    /**
     * Obtém a instância única da conexão com o banco de dados.
     * 
     * @return Connection conexão ativa com o SQLite
     * @throws SQLException se houver erro ao conectar
     */
    public static Connection getInstance() throws SQLException {
        if (connection == null || connection.isClosed()) {
            synchronized (ConexaoBD.class) {
                if (connection == null || connection.isClosed()) {
                    new ConexaoBD();
                }
            }
        }
        return connection;
    }
    
    /**
     * Estabelece a conexão com o banco de dados SQLite.
     * Registra o driver JDBC e configura a conexão.
     */
    private void conectar() {
        try {
            LOGGER.info("🔄 Tentando conectar ao banco de dados: {}", DB_URL);
            
            // Registrar driver SQLite
            Class.forName("org.sqlite.JDBC");
            
            // Estabelecer conexão
            connection = DriverManager.getConnection(DB_URL);
            
            // Configurar comportamentos do SQLite
            try (Statement stmt = connection.createStatement()) {
                // Habilitar chaves estrangeiras
                stmt.execute("PRAGMA foreign_keys = ON");
                
                // Usar WAL (Write-Ahead Logging) para melhor performance
                stmt.execute("PRAGMA journal_mode = WAL");
                
                // Sincronizar em modo NORMAL para performance
                stmt.execute("PRAGMA synchronous = NORMAL");
                
                // Cache de páginas para performance
                stmt.execute("PRAGMA cache_size = 10000");
            }
            
            LOGGER.info("✅ Conexão estabelecida com sucesso!");
            LOGGER.info("📁 Banco de dados: {}", Paths.get(DB_DIRECTORY, DB_NAME).toAbsolutePath());
            
        } catch (ClassNotFoundException e) {
            LOGGER.error("❌ Driver JDBC do SQLite não encontrado!", e);
            throw new RuntimeException("Driver JDBC do SQLite não encontrado", e);
        } catch (SQLException e) {
            LOGGER.error("❌ Erro ao conectar ao banco de dados!", e);
            throw new RuntimeException("Erro ao conectar ao banco de dados", e);
        }
    }
    
    /**
     * Cria o diretório 'data/' se ele não existir.
     * Garante que o banco de dados tenha um local para ser criado.
     */
    private void criarDiretorioBanco() {
        try {
            File diretorio = new File(DB_DIRECTORY);
            if (!diretorio.exists()) {
                boolean criado = diretorio.mkdirs();
                if (criado) {
                    LOGGER.info("📂 Diretório '{}' criado com sucesso!", DB_DIRECTORY);
                } else {
                    LOGGER.warn("⚠️ Não foi possível criar o diretório '{}'", DB_DIRECTORY);
                }
            }
        } catch (SecurityException e) {
            LOGGER.error("❌ Erro de permissão ao criar diretório!", e);
        }
    }
    
    /**
     * Fecha a conexão com o banco de dados.
     * Método deve ser chamado ao finalizar a aplicação.
     */
    public static void fecharConexao() {
        if (connection != null) {
            try {
                connection.close();
                LOGGER.info("🔒 Conexão com o banco de dados fechada.");
            } catch (SQLException e) {
                LOGGER.error("❌ Erro ao fechar conexão!", e);
            }
        }
    }
    
    /**
     * Testa a conexão com o banco de dados.
     * 
     * @return true se a conexão está ativa, false caso contrário
     */
    public static boolean testarConexao() {
        try {
            Connection conn = getInstance();
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            LOGGER.error("❌ Teste de conexão falhou!", e);
            return false;
        }
    }
    
    /**
     * Obtém o caminho absoluto do arquivo do banco de dados.
     * 
     * @return String caminho completo do arquivo .db
     */
    public static String getCaminhoBanco() {
        return Paths.get(DB_DIRECTORY, DB_NAME).toAbsolutePath().toString();
    }
}
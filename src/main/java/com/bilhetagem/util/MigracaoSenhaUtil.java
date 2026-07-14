package com.bilhetagem.util;

import com.bilhetagem.dao.ConexaoBD;
import com.bilhetagem.dao.UsuarioDAO;
import com.bilhetagem.dao.UsuarioDAOImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Utilitário para migrar senhas existentes para hash BCrypt.
 * 
 * @author Equipe de desenvolvimento
 * @version 1.0.0
 * @since 2026-01-09
 */
public class MigracaoSenhaUtil {
    
    private static final Logger LOGGER = LogManager.getLogger(MigracaoSenhaUtil.class);
    private static final UsuarioDAO usuarioDAO = new UsuarioDAOImpl();
    
    /**
     * Migra todas as senhas em texto puro para hash BCrypt.
     */
    public static void migrarTodasSenhas() {
        LOGGER.info("🔄 Iniciando migração de senhas...");
        
        String sql = "SELECT id, login, senha FROM usuarios WHERE senha NOT LIKE '$2a$%' AND senha NOT LIKE '$2b$%'";
        
        try (Connection conn = ConexaoBD.getInstance();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            int migrados = 0;
            int erros = 0;
            
            while (rs.next()) {
                Long id = rs.getLong("id");
                String login = rs.getString("login");
                String senha = rs.getString("senha");
                
                try {
                    LOGGER.info("🔄 Migrando senha do usuário: {}", login);
                    
                    String novoHash = CriptografiaUtil.hashSenha(senha);
                    atualizarSenha(id, novoHash);
                    
                    migrados++;
                    LOGGER.info("✅ Senha migrada para: {}", login);
                    
                } catch (Exception e) {
                    LOGGER.error("❌ Erro ao migrar senha do usuário: {}", login, e);
                    erros++;
                }
            }
            
            LOGGER.info("✅ Migração concluída: {} migrados, {} erros", migrados, erros);
            
        } catch (SQLException e) {
            LOGGER.error("❌ Erro na migração de senhas", e);
        }
    }
    
    /**
     * Atualiza a senha de um usuário.
     */
    private static void atualizarSenha(Long id, String senhaHash) throws SQLException {
        String sql = "UPDATE usuarios SET senha = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        
        try (Connection conn = ConexaoBD.getInstance();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, senhaHash);
            stmt.setLong(2, id);
            stmt.executeUpdate();
        }
    }
    
    /**
     * Verifica se há senhas em texto puro no banco.
     */
    public static boolean temSenhasEmTextoPuro() {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE senha NOT LIKE '$2a$%' AND senha NOT LIKE '$2b$%'";
        
        try (Connection conn = ConexaoBD.getInstance();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            LOGGER.error("Erro ao verificar senhas em texto puro", e);
        }
        
        return false;
    }
}
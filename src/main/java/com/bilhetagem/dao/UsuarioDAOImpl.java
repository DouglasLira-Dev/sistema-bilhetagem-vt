package com.bilhetagem.dao;

import com.bilhetagem.model.Usuario;
import com.bilhetagem.model.Usuario.Perfil;
import com.bilhetagem.util.CriptografiaUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementação do DAO de usuários com SQLite e segurança.
 * 
 * @author [Seu Nome]
 * @version 1.0.0
 * @since 2026-01-09
 */
public class UsuarioDAOImpl implements UsuarioDAO {
    
    private static final Logger LOGGER = LogManager.getLogger(UsuarioDAOImpl.class);
    
    private static final String SQL_INSERT = """
        INSERT INTO usuarios (login, senha, nome, email, perfil, ativo)
        VALUES (?, ?, ?, ?, ?, ?)
    """;
    
    private static final String SQL_SELECT_BY_ID = "SELECT * FROM usuarios WHERE id = ?";
    private static final String SQL_SELECT_BY_LOGIN = "SELECT * FROM usuarios WHERE login = ?";
    private static final String SQL_SELECT_ALL = "SELECT * FROM usuarios WHERE ativo = 1 ORDER BY nome";
    private static final String SQL_UPDATE = """
        UPDATE usuarios SET
            login = ?,
            nome = ?,
            email = ?,
            perfil = ?,
            ativo = ?,
            updated_at = CURRENT_TIMESTAMP
        WHERE id = ?
    """;
    private static final String SQL_UPDATE_SENHA = """
        UPDATE usuarios SET senha = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?
    """;
    private static final String SQL_DELETE = "UPDATE usuarios SET ativo = 0 WHERE id = ?";
    private static final String SQL_UPDATE_ULTIMO_ACESSO = 
        "UPDATE usuarios SET ultimo_acesso = CURRENT_TIMESTAMP WHERE login = ?";
    private static final String SQL_AUTENTICAR = 
        "SELECT * FROM usuarios WHERE login = ? AND ativo = 1";
    private static final String SQL_MIGRAR_SENHA = 
        "UPDATE usuarios SET senha = ? WHERE id = ?";
    
    @Override
    public Usuario salvar(Usuario usuario) throws SQLException {
        LOGGER.debug("Salvando usuário: {}", usuario.getLogin());
        
        String senhaHash = CriptografiaUtil.hashSenha(usuario.getSenha());
        usuario.setSenha(senhaHash);
        
        try (Connection conn = ConexaoBD.getInstance();
             PreparedStatement stmt = conn.prepareStatement(SQL_INSERT)) {
            
            stmt.setString(1, usuario.getLogin());
            stmt.setString(2, usuario.getSenha());
            stmt.setString(3, usuario.getNome());
            stmt.setString(4, usuario.getEmail());
            stmt.setString(5, usuario.getPerfil().name());
            stmt.setInt(6, usuario.isAtivo() ? 1 : 0);
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Falha ao salvar usuário");
            }
            
            // Recuperar ID usando SQLite específico
            try (Statement idStmt = conn.createStatement();
                 ResultSet rs = idStmt.executeQuery("SELECT last_insert_rowid()")) {
                if (rs.next()) {
                    usuario.setId(rs.getLong(1));
                }
            }
            
            LOGGER.info("✅ Usuário salvo com ID: {}", usuario.getId());
            return usuario;
        }
    }
    
    @Override
    public Optional<Usuario> buscarPorId(Long id) throws SQLException {
        LOGGER.debug("Buscando usuário por ID: {}", id);
        
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
    public Optional<Usuario> buscarPorLogin(String login) throws SQLException {
        LOGGER.debug("Buscando usuário por login: {}", login);
        
        try (Connection conn = ConexaoBD.getInstance();
             PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_BY_LOGIN)) {
            
            stmt.setString(1, login);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearResultSet(rs));
                }
            }
        }
        
        return Optional.empty();
    }
    
    @Override
    public List<Usuario> listarTodos() throws SQLException {
        LOGGER.debug("Listando todos os usuários ativos");
        
        List<Usuario> usuarios = new ArrayList<>();
        
        try (Connection conn = ConexaoBD.getInstance();
             PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_ALL);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                usuarios.add(mapearResultSet(rs));
            }
        }
        
        LOGGER.debug("Encontrados {} usuários", usuarios.size());
        return usuarios;
    }
    
    @Override
    public boolean atualizar(Usuario usuario) throws SQLException {
        LOGGER.debug("Atualizando usuário: {}", usuario.getId());
        
        try (Connection conn = ConexaoBD.getInstance();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE)) {
            
            stmt.setString(1, usuario.getLogin());
            stmt.setString(2, usuario.getNome());
            stmt.setString(3, usuario.getEmail());
            stmt.setString(4, usuario.getPerfil().name());
            stmt.setInt(5, usuario.isAtivo() ? 1 : 0);
            stmt.setLong(6, usuario.getId());
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    @Override
    public boolean atualizarSenha(Long id, String novaSenha) throws SQLException {
        LOGGER.debug("Atualizando senha do usuário ID: {}", id);
        
        String senhaHash = CriptografiaUtil.hashSenha(novaSenha);
        
        try (Connection conn = ConexaoBD.getInstance();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE_SENHA)) {
            
            stmt.setString(1, senhaHash);
            stmt.setLong(2, id);
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    @Override
    public boolean excluir(Long id) throws SQLException {
        LOGGER.debug("Desativando usuário ID: {}", id);
        
        try (Connection conn = ConexaoBD.getInstance();
             PreparedStatement stmt = conn.prepareStatement(SQL_DELETE)) {
            
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        }
    }
    
    @Override
    public boolean autenticar(String login, String senha) throws SQLException {
        LOGGER.debug("Autenticando usuário: {}", login);
        
        try (Connection conn = ConexaoBD.getInstance();
             PreparedStatement stmt = conn.prepareStatement(SQL_AUTENTICAR)) {
            
            stmt.setString(1, login);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String senhaHash = rs.getString("senha");
                    
                    // Verificar se a senha está em texto puro (migração)
                    if (!CriptografiaUtil.isHashValido(senhaHash)) {
                        // Migrar para hash
                        String novoHash = CriptografiaUtil.hashSenha(senha);
                        if (senha.equals(senhaHash)) {
                            migrarSenha(rs.getLong("id"), senha);
                            return true;
                        }
                        return false;
                    }
                    
                    // Verificar hash BCrypt
                    return CriptografiaUtil.verificarSenha(senha, senhaHash);
                }
            }
        }
        
        return false;
    }
    
    @Override
    public void atualizarUltimoAcesso(String login) throws SQLException {
        LOGGER.debug("Atualizando último acesso: {}", login);
        
        try (Connection conn = ConexaoBD.getInstance();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE_ULTIMO_ACESSO)) {
            
            stmt.setString(1, login);
            stmt.executeUpdate();
        }
    }
    
    /**
     * Migra uma senha em texto puro para hash BCrypt.
     */
    private void migrarSenha(Long id, String senha) throws SQLException {
        LOGGER.info("🔄 Migrando senha do usuário ID: {}", id);
        
        String senhaHash = CriptografiaUtil.hashSenha(senha);
        
        try (Connection conn = ConexaoBD.getInstance();
             PreparedStatement stmt = conn.prepareStatement(SQL_MIGRAR_SENHA)) {
            
            stmt.setString(1, senhaHash);
            stmt.setLong(2, id);
            stmt.executeUpdate();
            
            LOGGER.info("✅ Senha migrada com sucesso para ID: {}", id);
        }
    }
    
    /**
     * Mapeia um ResultSet para um objeto Usuario.
     */
    private Usuario mapearResultSet(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setId(rs.getLong("id"));
        usuario.setLogin(rs.getString("login"));
        usuario.setSenha(rs.getString("senha")); // Hash armazenado
        usuario.setNome(rs.getString("nome"));
        usuario.setEmail(rs.getString("email"));
        usuario.setPerfil(Perfil.valueOf(rs.getString("perfil")));
        usuario.setAtivo(rs.getInt("ativo") == 1);
        
        String ultimoAcesso = rs.getString("ultimo_acesso");
        if (ultimoAcesso != null) {
            usuario.setUltimoAcesso(LocalDateTime.parse(ultimoAcesso.replace(' ', 'T')));
        }
        
        String criadoEm = rs.getString("created_at");
        if (criadoEm != null) {
            usuario.setCriadoEm(LocalDateTime.parse(criadoEm.replace(' ', 'T')));
        }
        
        String atualizadoEm = rs.getString("updated_at");
        if (atualizadoEm != null) {
            usuario.setAtualizadoEm(LocalDateTime.parse(atualizadoEm.replace(' ', 'T')));
        }
        
        return usuario;
    }
}
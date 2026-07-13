package com.bilhetagem.dao;

import com.bilhetagem.model.Usuario;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Interface para operações de acesso a dados de usuários.
 */
public interface UsuarioDAO {
    
    Usuario salvar(Usuario usuario) throws SQLException;
    Optional<Usuario> buscarPorId(Long id) throws SQLException;
    Optional<Usuario> buscarPorLogin(String login) throws SQLException;
    List<Usuario> listarTodos() throws SQLException;
    boolean atualizar(Usuario usuario) throws SQLException;
    boolean atualizarSenha(Long id, String novaSenha) throws SQLException;
    boolean excluir(Long id) throws SQLException;
    boolean autenticar(String login, String senha) throws SQLException;
    void atualizarUltimoAcesso(String login) throws SQLException;
}
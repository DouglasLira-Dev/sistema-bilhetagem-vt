package com.bilhetagem.dao;

import com.bilhetagem.model.Usuario;
import com.bilhetagem.model.Usuario.Perfil;
import com.bilhetagem.util.CriptografiaUtil;
import com.bilhetagem.util.BancoUtil;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para UsuarioDAO.
 * 
 * @author Equipe de Desenvolvimento
 * @version 1.0.0
 * @since 2026-01-13
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UsuarioDAOTest {
    
    private static UsuarioDAO dao;
    private static Usuario usuarioTeste;
    
    @BeforeAll
    static void setUpAll() throws SQLException {
        // Inicializar banco para testes
        BancoUtil.inicializarBanco();
        dao = new UsuarioDAOImpl();
        
        // Criar usuário de teste
        usuarioTeste = new Usuario();
        usuarioTeste.setLogin("testuser");
        usuarioTeste.setSenha("test123");
        usuarioTeste.setNome("Usuário Teste");
        usuarioTeste.setEmail("teste@email.com");
        usuarioTeste.setPerfil(Perfil.OPERADOR);
        usuarioTeste.setAtivo(true);
    }
    
    @Test
    @Order(1)
    void testSalvar() throws SQLException {
        Usuario salvo = dao.salvar(usuarioTeste);
        
        assertNotNull(salvo);
        assertNotNull(salvo.getId());
        assertEquals(usuarioTeste.getLogin(), salvo.getLogin());
        
        // Verificar se a senha foi hasheada
        assertNotEquals("test123", salvo.getSenha());
        assertTrue(CriptografiaUtil.isHashValido(salvo.getSenha()));
        
        // Armazenar ID para testes subsequentes
        usuarioTeste.setId(salvo.getId());
    }
    
    @Test
    @Order(2)
    void testBuscarPorId() throws SQLException {
        Optional<Usuario> encontrado = dao.buscarPorId(usuarioTeste.getId());
        
        assertTrue(encontrado.isPresent());
        assertEquals(usuarioTeste.getLogin(), encontrado.get().getLogin());
        assertEquals(usuarioTeste.getNome(), encontrado.get().getNome());
    }
    
    @Test
    @Order(3)
    void testBuscarPorLogin() throws SQLException {
        Optional<Usuario> encontrado = dao.buscarPorLogin(usuarioTeste.getLogin());
        
        assertTrue(encontrado.isPresent());
        assertEquals(usuarioTeste.getId(), encontrado.get().getId());
        assertEquals(usuarioTeste.getPerfil(), encontrado.get().getPerfil());
    }
    
    @Test
    @Order(4)
    void testAutenticar() throws SQLException {
        // Testar autenticação com senha correta
        boolean autenticado = dao.autenticar(usuarioTeste.getLogin(), "test123");
        assertTrue(autenticado);
        
        // Testar autenticação com senha incorreta
        boolean naoAutenticado = dao.autenticar(usuarioTeste.getLogin(), "senhaerrada");
        assertFalse(naoAutenticado);
    }
    
    @Test
    @Order(5)
    void testAtualizarSenha() throws SQLException {
        String novaSenha = "novaSenha123";
        boolean atualizado = dao.atualizarSenha(usuarioTeste.getId(), novaSenha);
        assertTrue(atualizado);
        
        // Verificar se a nova senha funciona
        boolean autenticado = dao.autenticar(usuarioTeste.getLogin(), novaSenha);
        assertTrue(autenticado);
        
        // Verificar se a senha antiga não funciona mais
        boolean naoAutenticado = dao.autenticar(usuarioTeste.getLogin(), "test123");
        assertFalse(naoAutenticado);
    }
    
    @Test
    @Order(6)
    void testListarTodos() throws SQLException {
        List<Usuario> usuarios = dao.listarTodos();
        assertNotNull(usuarios);
        assertTrue(usuarios.size() > 0);
        
        // Verificar se o usuário de teste está na lista
        boolean encontrado = usuarios.stream()
            .anyMatch(u -> u.getId().equals(usuarioTeste.getId()));
        assertTrue(encontrado);
    }
    
    @Test
    @Order(7)
    void testAtualizar() throws SQLException {
        String novoNome = "Usuário Teste Atualizado";
        usuarioTeste.setNome(novoNome);
        usuarioTeste.setEmail("novo@email.com");
        usuarioTeste.setPerfil(Perfil.GERENTE);
        
        boolean atualizado = dao.atualizar(usuarioTeste);
        assertTrue(atualizado);
        
        // Verificar atualização
        Optional<Usuario> encontrado = dao.buscarPorId(usuarioTeste.getId());
        assertTrue(encontrado.isPresent());
        assertEquals(novoNome, encontrado.get().getNome());
        assertEquals("novo@email.com", encontrado.get().getEmail());
        assertEquals(Perfil.GERENTE, encontrado.get().getPerfil());
    }
    
    @Test
    @Order(8)
    void testExcluir() throws SQLException {
        boolean excluido = dao.excluir(usuarioTeste.getId());
        assertTrue(excluido);
        
        // Verificar se foi desativado (soft delete)
        Optional<Usuario> encontrado = dao.buscarPorId(usuarioTeste.getId());
        assertTrue(encontrado.isPresent());
        assertFalse(encontrado.get().isAtivo());
    }
}
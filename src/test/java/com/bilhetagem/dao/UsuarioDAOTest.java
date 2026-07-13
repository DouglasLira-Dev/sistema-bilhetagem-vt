package com.bilhetagem.dao;

import com.bilhetagem.model.Usuario;
import com.bilhetagem.model.Usuario.Perfil;
import com.bilhetagem.util.CriptografiaUtil;
import com.bilhetagem.util.BancoUtil;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UsuarioDAOTest {
    
    private static UsuarioDAO dao;
    private static Usuario usuarioTeste;
    private static Long idSalvo;
    
    @BeforeAll
    static void setUpAll() throws SQLException {
        BancoUtil.inicializarBanco();
        dao = new UsuarioDAOImpl();
        
        // Limpar tabela de usuários para teste (exceto admin)
        try (Connection conn = ConexaoBD.getInstance();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM usuarios WHERE login != 'admin'");
        }
        
        usuarioTeste = new Usuario();
        usuarioTeste.setLogin("testuser_" + System.currentTimeMillis());
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
        assertNotEquals("test123", salvo.getSenha());
        assertTrue(CriptografiaUtil.isHashValido(salvo.getSenha()));
        
        idSalvo = salvo.getId();
        usuarioTeste.setId(idSalvo);
    }
    
    @Test
    @Order(2)
    void testBuscarPorId() throws SQLException {
        assertNotNull(idSalvo, "ID não foi salvo no teste anterior");
        Optional<Usuario> encontrado = dao.buscarPorId(idSalvo);
        assertTrue(encontrado.isPresent());
        assertEquals(usuarioTeste.getLogin(), encontrado.get().getLogin());
    }
    
    @Test
    @Order(3)
    void testBuscarPorLogin() throws SQLException {
        Optional<Usuario> encontrado = dao.buscarPorLogin(usuarioTeste.getLogin());
        assertTrue(encontrado.isPresent());
        assertEquals(idSalvo, encontrado.get().getId());
    }
    
    @Test
    @Order(4)
    void testAutenticar() throws SQLException {
        boolean autenticado = dao.autenticar(usuarioTeste.getLogin(), "test123");
        assertTrue(autenticado);
        
        boolean naoAutenticado = dao.autenticar(usuarioTeste.getLogin(), "senhaerrada");
        assertFalse(naoAutenticado);
    }
    
    @Test
    @Order(5)
    void testAtualizarSenha() throws SQLException {
        assertNotNull(idSalvo, "ID não foi salvo no teste anterior");
        
        String novaSenha = "novaSenha123";
        boolean atualizado = dao.atualizarSenha(idSalvo, novaSenha);
        assertTrue(atualizado);
        
        boolean autenticado = dao.autenticar(usuarioTeste.getLogin(), novaSenha);
        assertTrue(autenticado);
        
        boolean naoAutenticado = dao.autenticar(usuarioTeste.getLogin(), "test123");
        assertFalse(naoAutenticado);
    }
    
    @Test
    @Order(6)
    void testListarTodos() throws SQLException {
        List<Usuario> usuarios = dao.listarTodos();
        assertNotNull(usuarios);
        assertTrue(usuarios.size() > 0);
        
        boolean encontrado = usuarios.stream()
            .anyMatch(u -> u.getId().equals(idSalvo));
        assertTrue(encontrado);
    }
    
    @Test
    @Order(7)
    void testAtualizar() throws SQLException {
        assertNotNull(idSalvo, "ID não foi salvo no teste anterior");
        
        String novoNome = "Usuário Teste Atualizado";
        usuarioTeste.setNome(novoNome);
        usuarioTeste.setEmail("novo@email.com");
        usuarioTeste.setPerfil(Perfil.GERENTE);
        
        boolean atualizado = dao.atualizar(usuarioTeste);
        assertTrue(atualizado);
        
        Optional<Usuario> encontrado = dao.buscarPorId(idSalvo);
        assertTrue(encontrado.isPresent());
        assertEquals(novoNome, encontrado.get().getNome());
        assertEquals("novo@email.com", encontrado.get().getEmail());
        assertEquals(Perfil.GERENTE, encontrado.get().getPerfil());
    }
    
    @Test
    @Order(8)
    void testExcluir() throws SQLException {
        assertNotNull(idSalvo, "ID não foi salvo no teste anterior");
        
        boolean excluido = dao.excluir(idSalvo);
        assertTrue(excluido);
        
        Optional<Usuario> encontrado = dao.buscarPorId(idSalvo);
        assertTrue(encontrado.isPresent());
        assertFalse(encontrado.get().isAtivo());
    }
}
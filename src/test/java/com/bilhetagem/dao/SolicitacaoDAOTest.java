package com.bilhetagem.dao;

import com.bilhetagem.model.Solicitacao;
import com.bilhetagem.model.Solicitacao.TipoSolicitacao;
import com.bilhetagem.util.BancoUtil;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para o SolicitacaoDAO.
 * 
 * @author [Seu Nome]
 * @version 1.0.0
 * @since 2026-01-08
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SolicitacaoDAOTest {
    
    private static SolicitacaoDAO dao;
    private static Solicitacao solicitacaoTeste;
    
    @BeforeAll
    static void setUpAll() throws SQLException {
        // Inicializar banco para testes
        BancoUtil.inicializarBanco();
        dao = new SolicitacaoDAOImpl();
        
        // Criar solicitação de teste
        solicitacaoTeste = new Solicitacao();
        solicitacaoTeste.setDataEfetivacao(LocalDate.now());
        solicitacaoTeste.setMesReferencia("01/2026");
        solicitacaoTeste.setMatricula("12345");
        solicitacaoTeste.setCpf("12345678900");
        solicitacaoTeste.setNome("João da Silva Teste");
        solicitacaoTeste.setNumeroCartao("VT123456");
        solicitacaoTeste.setQuantidadeValeTipoA(2);
        solicitacaoTeste.setTipoSolicitacao(TipoSolicitacao.ADESAO);
        solicitacaoTeste.setObservacao("Solicitação de teste unitário");
    }
    
    @Test
    @Order(1)
    void testSalvar() throws SQLException {
        // Testar inserção
        Solicitacao salva = dao.salvar(solicitacaoTeste);
        
        assertNotNull(salva);
        assertNotNull(salva.getId());
        assertEquals(solicitacaoTeste.getNome(), salva.getNome());
        
        // Armazenar ID para testes subsequentes
        solicitacaoTeste.setId(salva.getId());
        
        // Verificar se foi salvo corretamente
        Optional<Solicitacao> encontrada = dao.buscarPorId(salva.getId());
        assertTrue(encontrada.isPresent());
        assertEquals(salva.getNome(), encontrada.get().getNome());
    }
    
    @Test
    @Order(2)
    void testBuscarPorId() throws SQLException {
        Optional<Solicitacao> encontrada = dao.buscarPorId(solicitacaoTeste.getId());
        
        assertTrue(encontrada.isPresent());
        assertEquals(solicitacaoTeste.getMatricula(), encontrada.get().getMatricula());
        assertEquals(solicitacaoTeste.getCpf(), encontrada.get().getCpf());
    }
    
    @Test
    @Order(3)
    void testBuscarPorMatricula() throws SQLException {
        List<Solicitacao> lista = dao.buscarPorMatricula(solicitacaoTeste.getMatricula());
        
        assertFalse(lista.isEmpty());
        assertTrue(lista.stream().anyMatch(s -> s.getId().equals(solicitacaoTeste.getId())));
    }
    
    @Test
    @Order(4)
    void testBuscarPorCpf() throws SQLException {
        List<Solicitacao> lista = dao.buscarPorCpf(solicitacaoTeste.getCpf());
        
        assertFalse(lista.isEmpty());
        assertTrue(lista.stream().anyMatch(s -> s.getId().equals(solicitacaoTeste.getId())));
    }
    
    @Test
    @Order(5)
    void testBuscarPorNome() throws SQLException {
        List<Solicitacao> lista = dao.buscarPorNome("Silva");
        
        assertFalse(lista.isEmpty());
        assertTrue(lista.stream().anyMatch(s -> s.getNome().contains("Silva")));
    }
    
    @Test
    @Order(6)
    void testBuscarPorMesReferencia() throws SQLException {
        List<Solicitacao> lista = dao.buscarPorMesReferencia("01/2026");
        
        assertFalse(lista.isEmpty());
        assertTrue(lista.stream().anyMatch(s -> s.getMesReferencia().equals("01/2026")));
    }
    
    @Test
    @Order(7)
    void testAtualizar() throws SQLException {
        // Modificar alguns dados
        solicitacaoTeste.setNome("João da Silva Teste Atualizado");
        solicitacaoTeste.setQuantidadeValeTipoA(3);
        solicitacaoTeste.setTipoSolicitacao(TipoSolicitacao.ALTERACAO);
        
        boolean atualizado = dao.atualizar(solicitacaoTeste);
        assertTrue(atualizado);
        
        // Verificar atualização
        Optional<Solicitacao> encontrada = dao.buscarPorId(solicitacaoTeste.getId());
        assertTrue(encontrada.isPresent());
        assertEquals("João da Silva Teste Atualizado", encontrada.get().getNome());
        assertEquals(3, encontrada.get().getQuantidadeValeTipoA());
        assertEquals(TipoSolicitacao.ALTERACAO, encontrada.get().getTipoSolicitacao());
    }
    
    @Test
    @Order(8)
    void testBuscarComFiltros() throws SQLException {
        // Testar filtro apenas por nome
        List<Solicitacao> lista = dao.buscarComFiltros(null, null, "Teste", null, null);
        assertFalse(lista.isEmpty());
        
        // Testar filtro por matrícula e tipo
        lista = dao.buscarComFiltros(
            solicitacaoTeste.getMatricula(), 
            null, 
            null, 
            null, 
            TipoSolicitacao.ALTERACAO
        );
        assertFalse(lista.isEmpty());
        assertTrue(lista.stream().allMatch(s -> 
            s.getMatricula().equals(solicitacaoTeste.getMatricula()) &&
            s.getTipoSolicitacao() == TipoSolicitacao.ALTERACAO
        ));
    }
    
    @Test
    @Order(9)
    void testListarTodos() throws SQLException {
        List<Solicitacao> lista = dao.listarTodos();
        assertNotNull(lista);
        assertTrue(lista.size() > 0);
    }
    
    @Test
    @Order(10)
    void testContarTotal() throws SQLException {
        long total = dao.contarTotal();
        assertTrue(total > 0);
    }
    
    @Test
    @Order(11)
    void testExistePorId() throws SQLException {
        boolean existe = dao.existePorId(solicitacaoTeste.getId());
        assertTrue(existe);
        
        boolean naoExiste = dao.existePorId(999999L);
        assertFalse(naoExiste);
    }
    
    @Test
    @Order(12)
    void testExcluir() throws SQLException {
        boolean excluido = dao.excluir(solicitacaoTeste.getId());
        assertTrue(excluido);
        
        // Verificar se realmente foi excluído
        Optional<Solicitacao> encontrada = dao.buscarPorId(solicitacaoTeste.getId());
        assertFalse(encontrada.isPresent());
    }
}
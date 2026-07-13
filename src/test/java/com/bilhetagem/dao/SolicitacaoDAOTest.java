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

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SolicitacaoDAOTest {
    
    private static SolicitacaoDAO dao;
    private static Solicitacao solicitacaoTeste;
    private static Long idSalvo;
    
    @BeforeAll
    static void setUpAll() throws SQLException {
        BancoUtil.inicializarBanco();
        dao = new SolicitacaoDAOImpl();
        
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
        Solicitacao salva = dao.salvar(solicitacaoTeste);
        
        assertNotNull(salva);
        assertNotNull(salva.getId());
        assertEquals(solicitacaoTeste.getNome(), salva.getNome());
        
        idSalvo = salva.getId();
        solicitacaoTeste.setId(idSalvo);
    }
    
    @Test
    @Order(2)
    void testBuscarPorId() throws SQLException {
        assertNotNull(idSalvo, "ID não foi salvo no teste anterior");
        
        Optional<Solicitacao> encontrada = dao.buscarPorId(idSalvo);
        assertTrue(encontrada.isPresent());
        assertEquals(solicitacaoTeste.getMatricula(), encontrada.get().getMatricula());
        assertEquals(solicitacaoTeste.getCpf(), encontrada.get().getCpf());
    }
    
    @Test
    @Order(3)
    void testBuscarPorMatricula() throws SQLException {
        List<Solicitacao> lista = dao.buscarPorMatricula(solicitacaoTeste.getMatricula());
        assertFalse(lista.isEmpty());
        assertTrue(lista.stream().anyMatch(s -> s.getId().equals(idSalvo)));
    }
    
    @Test
    @Order(4)
    void testBuscarPorCpf() throws SQLException {
        List<Solicitacao> lista = dao.buscarPorCpf(solicitacaoTeste.getCpf());
        assertFalse(lista.isEmpty());
        assertTrue(lista.stream().anyMatch(s -> s.getId().equals(idSalvo)));
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
        assertNotNull(idSalvo, "ID não foi salvo no teste anterior");
        
        solicitacaoTeste.setNome("João da Silva Teste Atualizado");
        solicitacaoTeste.setQuantidadeValeTipoA(3);
        solicitacaoTeste.setTipoSolicitacao(TipoSolicitacao.ALTERACAO);
        
        boolean atualizado = dao.atualizar(solicitacaoTeste);
        assertTrue(atualizado);
        
        Optional<Solicitacao> encontrada = dao.buscarPorId(idSalvo);
        assertTrue(encontrada.isPresent());
        assertEquals("João da Silva Teste Atualizado", encontrada.get().getNome());
        assertEquals(3, encontrada.get().getQuantidadeValeTipoA());
        assertEquals(TipoSolicitacao.ALTERACAO, encontrada.get().getTipoSolicitacao());
    }
    
    @Test
    @Order(8)
    void testBuscarComFiltros() throws SQLException {
        List<Solicitacao> lista = dao.buscarComFiltros(null, null, "Teste", null, null);
        assertFalse(lista.isEmpty());
        
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
        assertNotNull(idSalvo, "ID não foi salvo no teste anterior");
        
        boolean existe = dao.existePorId(idSalvo);
        assertTrue(existe);
        
        boolean naoExiste = dao.existePorId(999999L);
        assertFalse(naoExiste);
    }
    
    @Test
    @Order(12)
    void testExcluir() throws SQLException {
        assertNotNull(idSalvo, "ID não foi salvo no teste anterior");
        
        boolean excluido = dao.excluir(idSalvo);
        assertTrue(excluido);
        
        Optional<Solicitacao> encontrada = dao.buscarPorId(idSalvo);
        assertFalse(encontrada.isPresent());
    }
}
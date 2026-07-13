package com.bilhetagem.util;

import com.bilhetagem.model.Usuario;
import com.bilhetagem.model.Usuario.Perfil;
import com.bilhetagem.model.Usuario.Permissao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para SessaoUtil e RBAC.
 * 
 * @author Equipe de Desenvolvimento
 * @version 1.0.0
 * @since 2026-01-13
 */
class SessaoUtilTest {
    
    private Usuario admin;
    private Usuario gerente;
    private Usuario operador;
    private Usuario consultor;
    
    @BeforeEach
    void setUp() {
        // Criar usuários com diferentes perfis
        admin = new Usuario();
        admin.setLogin("admin");
        admin.setPerfil(Perfil.ADMIN);
        
        gerente = new Usuario();
        gerente.setLogin("gerente");
        gerente.setPerfil(Perfil.GERENTE);
        
        operador = new Usuario();
        operador.setLogin("operador");
        operador.setPerfil(Perfil.OPERADOR);
        
        consultor = new Usuario();
        consultor.setLogin("consultor");
        consultor.setPerfil(Perfil.CONSULTOR);
        
        // Encerrar sessão antes de cada teste
        SessaoUtil.encerrarSessao();
    }
    
    @Test
    void testIniciarSessao() {
        SessaoUtil.iniciarSessao(admin);
        assertTrue(SessaoUtil.isLogado());
        assertEquals(admin, SessaoUtil.getUsuarioLogado());
    }
    
    @Test
    void testEncerrarSessao() {
        SessaoUtil.iniciarSessao(admin);
        assertTrue(SessaoUtil.isLogado());
        
        SessaoUtil.encerrarSessao();
        assertFalse(SessaoUtil.isLogado());
        assertNull(SessaoUtil.getUsuarioLogado());
    }
    
    @Test
    void testTemPerfil() {
        SessaoUtil.iniciarSessao(admin);
        assertTrue(SessaoUtil.temPerfil(Perfil.ADMIN));
        assertFalse(SessaoUtil.temPerfil(Perfil.OPERADOR));
    }
    
    @Test
    void testTemPermissaoAdmin() {
        SessaoUtil.iniciarSessao(admin);
        
        // ADMIN deve ter todas as permissões
        assertTrue(SessaoUtil.temPermissao(Permissao.CADASTRAR_SOLICITACAO));
        assertTrue(SessaoUtil.temPermissao(Permissao.EDITAR_SOLICITACAO));
        assertTrue(SessaoUtil.temPermissao(Permissao.EXCLUIR_SOLICITACAO));
        assertTrue(SessaoUtil.temPermissao(Permissao.CONSULTAR_SOLICITACAO));
        assertTrue(SessaoUtil.temPermissao(Permissao.IMPORTAR_DADOS));
        assertTrue(SessaoUtil.temPermissao(Permissao.EXPORTAR_DADOS));
        assertTrue(SessaoUtil.temPermissao(Permissao.GERAR_RELATORIOS));
        assertTrue(SessaoUtil.temPermissao(Permissao.GERENCIAR_USUARIOS));
        assertTrue(SessaoUtil.temPermissao(Permissao.VER_AUDITORIA));
    }
    
    @Test
    void testTemPermissaoGerente() {
        SessaoUtil.iniciarSessao(gerente);
        
        // GERENTE deve ter permissões de gestão, mas não de admin
        assertTrue(SessaoUtil.temPermissao(Permissao.CADASTRAR_SOLICITACAO));
        assertTrue(SessaoUtil.temPermissao(Permissao.EDITAR_SOLICITACAO));
        assertTrue(SessaoUtil.temPermissao(Permissao.CONSULTAR_SOLICITACAO));
        assertTrue(SessaoUtil.temPermissao(Permissao.IMPORTAR_DADOS));
        assertTrue(SessaoUtil.temPermissao(Permissao.EXPORTAR_DADOS));
        assertTrue(SessaoUtil.temPermissao(Permissao.GERAR_RELATORIOS));
        assertTrue(SessaoUtil.temPermissao(Permissao.VER_AUDITORIA));
        
        // GERENTE NÃO deve ter permissões de admin
        assertFalse(SessaoUtil.temPermissao(Permissao.EXCLUIR_SOLICITACAO));
        assertFalse(SessaoUtil.temPermissao(Permissao.GERENCIAR_USUARIOS));
    }
    
    @Test
    void testTemPermissaoOperador() {
        SessaoUtil.iniciarSessao(operador);
        
        // OPERADOR deve ter permissões básicas
        assertTrue(SessaoUtil.temPermissao(Permissao.CADASTRAR_SOLICITACAO));
        assertTrue(SessaoUtil.temPermissao(Permissao.EDITAR_SOLICITACAO));
        assertTrue(SessaoUtil.temPermissao(Permissao.CONSULTAR_SOLICITACAO));
        
        // OPERADOR NÃO deve ter permissões avançadas
        assertFalse(SessaoUtil.temPermissao(Permissao.EXCLUIR_SOLICITACAO));
        assertFalse(SessaoUtil.temPermissao(Permissao.IMPORTAR_DADOS));
        assertFalse(SessaoUtil.temPermissao(Permissao.EXPORTAR_DADOS));
        assertFalse(SessaoUtil.temPermissao(Permissao.GERAR_RELATORIOS));
        assertFalse(SessaoUtil.temPermissao(Permissao.GERENCIAR_USUARIOS));
        assertFalse(SessaoUtil.temPermissao(Permissao.VER_AUDITORIA));
    }
    
    @Test
    void testTemPermissaoConsultor() {
        SessaoUtil.iniciarSessao(consultor);
        
        // CONSULTOR deve ter apenas permissão de consulta
        assertTrue(SessaoUtil.temPermissao(Permissao.CONSULTAR_SOLICITACAO));
        
        // CONSULTOR NÃO deve ter nenhuma outra permissão
        assertFalse(SessaoUtil.temPermissao(Permissao.CADASTRAR_SOLICITACAO));
        assertFalse(SessaoUtil.temPermissao(Permissao.EDITAR_SOLICITACAO));
        assertFalse(SessaoUtil.temPermissao(Permissao.EXCLUIR_SOLICITACAO));
        assertFalse(SessaoUtil.temPermissao(Permissao.IMPORTAR_DADOS));
        assertFalse(SessaoUtil.temPermissao(Permissao.EXPORTAR_DADOS));
        assertFalse(SessaoUtil.temPermissao(Permissao.GERAR_RELATORIOS));
        assertFalse(SessaoUtil.temPermissao(Permissao.GERENCIAR_USUARIOS));
        assertFalse(SessaoUtil.temPermissao(Permissao.VER_AUDITORIA));
    }
    
    @Test
    void testVerificarPermissao() {
        SessaoUtil.iniciarSessao(consultor);
        
        // Deve lançar exceção para permissão não autorizada
        assertThrows(SecurityException.class, () -> {
            SessaoUtil.verificarPermissao(Permissao.EDITAR_SOLICITACAO);
        });
        
        // Não deve lançar exceção para permissão autorizada
        assertDoesNotThrow(() -> {
            SessaoUtil.verificarPermissao(Permissao.CONSULTAR_SOLICITACAO);
        });
    }
    
    @Test
    void testTemTodasPermissoes() {
        SessaoUtil.iniciarSessao(admin);
        
        assertTrue(SessaoUtil.temTodasPermissoes(
            Permissao.CADASTRAR_SOLICITACAO,
            Permissao.EDITAR_SOLICITACAO,
            Permissao.CONSULTAR_SOLICITACAO
        ));
        
        SessaoUtil.iniciarSessao(consultor);
        
        assertFalse(SessaoUtil.temTodasPermissoes(
            Permissao.CADASTRAR_SOLICITACAO,
            Permissao.CONSULTAR_SOLICITACAO
        ));
    }
    
    @Test
    void testTemAlgumaPermissao() {
        SessaoUtil.iniciarSessao(operador);
        
        assertTrue(SessaoUtil.temAlgumaPermissao(
            Permissao.CADASTRAR_SOLICITACAO,
            Permissao.EXPORTAR_DADOS,
            Permissao.GERAR_RELATORIOS
        ));
        
        assertFalse(SessaoUtil.temAlgumaPermissao(
            Permissao.EXPORTAR_DADOS,
            Permissao.GERAR_RELATORIOS,
            Permissao.VER_AUDITORIA
        ));
    }
    
    @Test
    void testSessaoExpirada() {
        // Sessão não deve expirar imediatamente
        SessaoUtil.iniciarSessao(admin);
        assertFalse(SessaoUtil.sessaoExpirada());
        
        // Sem sessão, deve considerar expirada
        SessaoUtil.encerrarSessao();
        assertTrue(SessaoUtil.sessaoExpirada());
    }
}
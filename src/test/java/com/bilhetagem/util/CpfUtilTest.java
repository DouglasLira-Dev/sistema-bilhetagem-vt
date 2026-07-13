package com.bilhetagem.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para CpfUtil.
 * 
 * @author Equipe de Desenvolvimento
 * @version 1.0.0
 * @since 2026-01-13
 */
class CpfUtilTest {
    
    @Test
    void testIsValidComCpfValido() {
        // CPFs válidos conhecidos
        assertTrue(CpfUtil.isValid("529.982.247-25"));
        assertTrue(CpfUtil.isValid("52998224725"));
        assertTrue(CpfUtil.isValid("111.444.777-35"));
        assertTrue(CpfUtil.isValid("11144477735"));
    }
    
    @Test
    void testIsValidComCpfInvalido() {
        // CPFs inválidos
        assertFalse(CpfUtil.isValid("000.000.000-00"));
        assertFalse(CpfUtil.isValid("111.111.111-11"));
        assertFalse(CpfUtil.isValid("123.456.789-00"));
        assertFalse(CpfUtil.isValid("529.982.247-24")); // Dígito errado
        assertFalse(CpfUtil.isValid("123456789")); // Menos de 11 dígitos
        assertFalse(CpfUtil.isValid("abcdefghijk"));
        assertFalse(CpfUtil.isValid(null));
        assertFalse(CpfUtil.isValid(""));
    }
    
    @Test
    void testFormatar() {
        assertEquals("529.982.247-25", CpfUtil.formatar("52998224725"));
        assertEquals("111.444.777-35", CpfUtil.formatar("11144477735"));
        assertEquals("", CpfUtil.formatar(null));
        assertEquals("123", CpfUtil.formatar("123")); // CPF inválido retorna o original
    }
    
    @Test
    void testMascarar() {
        assertEquals("529.***.***-25", CpfUtil.mascarar("52998224725"));
        assertEquals("111.***.***-35", CpfUtil.mascarar("11144477735"));
        assertEquals("", CpfUtil.mascarar(null));
        assertEquals("123", CpfUtil.mascarar("123")); // CPF inválido retorna o original
    }
    
    @Test
    void testLimpar() {
        assertEquals("52998224725", CpfUtil.limpar("529.982.247-25"));
        assertEquals("11144477735", CpfUtil.limpar("111.444.777-35"));
        assertEquals("", CpfUtil.limpar(null));
        assertEquals("", CpfUtil.limpar(""));
        assertEquals("12345678901", CpfUtil.limpar("123.456.789-01"));
    }
}
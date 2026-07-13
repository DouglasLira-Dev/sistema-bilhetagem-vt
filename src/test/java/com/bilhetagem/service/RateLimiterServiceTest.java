package com.bilhetagem.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.TimeUnit;

/**
 * Testes unitários para RateLimiterService.
 * 
 * @author Equipe de Desenvolvimento
 * @version 1.0.0
 * @since 2026-01-13
 */
class RateLimiterServiceTest {
    
    private RateLimiterService rateLimiter;
    private static final String TEST_LOGIN = "testuser";
    
    @BeforeEach
    void setUp() {
        rateLimiter = new RateLimiterService();
    }
    
    @Test
    void testRegistrarTentativasAteBloqueio() {
        // Registrar 5 tentativas (não deve bloquear)
        for (int i = 0; i < 5; i++) {
            boolean blocked = rateLimiter.registerAttempt(TEST_LOGIN);
            assertFalse(blocked, "Não deveria bloquear antes de 5 tentativas");
        }
        
        // 6ª tentativa deve bloquear
        boolean blocked = rateLimiter.registerAttempt(TEST_LOGIN);
        assertTrue(blocked, "Deveria bloquear após 5 tentativas");
        
        // Verificar que está bloqueado
        assertTrue(rateLimiter.isBlocked(TEST_LOGIN));
        
        // Verificar que o tempo restante é maior que 0
        assertTrue(rateLimiter.getRemainingBlockMinutes(TEST_LOGIN) > 0);
    }
    
    @Test
    void testResetAttempts() {
        // Registrar algumas tentativas
        for (int i = 0; i < 3; i++) {
            rateLimiter.registerAttempt(TEST_LOGIN);
        }
        
        // Resetar
        rateLimiter.resetAttempts(TEST_LOGIN);
        
        // Verificar que foi resetado
        assertEquals(0, rateLimiter.getAttemptCount(TEST_LOGIN));
        assertFalse(rateLimiter.isBlocked(TEST_LOGIN));
    }
    
    @Test
    void testIsBlocked() {
        // Inicialmente não deve estar bloqueado
        assertFalse(rateLimiter.isBlocked(TEST_LOGIN));
        
        // Bloquear
        for (int i = 0; i < 6; i++) {
            rateLimiter.registerAttempt(TEST_LOGIN);
        }
        
        // Deve estar bloqueado
        assertTrue(rateLimiter.isBlocked(TEST_LOGIN));
    }
    
    @Test
    void testGetAttemptCount() {
        // Inicialmente deve ser 0
        assertEquals(0, rateLimiter.getAttemptCount(TEST_LOGIN));
        
        // Registrar 3 tentativas
        for (int i = 0; i < 3; i++) {
            rateLimiter.registerAttempt(TEST_LOGIN);
        }
        
        // Deve ser 3
        assertEquals(3, rateLimiter.getAttemptCount(TEST_LOGIN));
    }
    
    @Test
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    void testBloqueioTemporario() throws InterruptedException {
        // Bloquear o usuário
        for (int i = 0; i < 6; i++) {
            rateLimiter.registerAttempt(TEST_LOGIN);
        }
        
        assertTrue(rateLimiter.isBlocked(TEST_LOGIN));
        
        // Aguardar um pouco (não esperamos 15 minutos, apenas verificamos a lógica)
        // O tempo de bloqueio é 15 minutos, então não podemos testar o desbloqueio completo
        // em um teste unitário rápido. Apenas verificamos que o método existe e funciona.
        
        // Verificar que o tempo restante é positivo
        long remaining = rateLimiter.getRemainingBlockMinutes(TEST_LOGIN);
        assertTrue(remaining > 0);
    }
}
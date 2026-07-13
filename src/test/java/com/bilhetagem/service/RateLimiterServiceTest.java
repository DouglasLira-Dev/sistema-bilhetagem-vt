package com.bilhetagem.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RateLimiterServiceTest {
    
    private RateLimiterService rateLimiter;
    private static final String TEST_LOGIN = "testuser";
    
    @BeforeEach
    void setUp() {
        rateLimiter = new RateLimiterService();
    }
    
    @Test
    void testRegistrarTentativasAteBloqueio() {
        // Registrar 5 tentativas (não deve bloquear - limite é > 5)
        for (int i = 0; i < 5; i++) {
            boolean blocked = rateLimiter.registerAttempt(TEST_LOGIN);
            assertFalse(blocked, "Não deveria bloquear antes de 5 tentativas");
        }
        
        // 6ª tentativa deve bloquear (agora > 5)
        boolean blocked = rateLimiter.registerAttempt(TEST_LOGIN);
        assertTrue(blocked, "Deveria bloquear após 5 tentativas");
        
        assertTrue(rateLimiter.isBlocked(TEST_LOGIN));
        assertTrue(rateLimiter.getRemainingBlockMinutes(TEST_LOGIN) > 0);
    }
    
    @Test
    void testResetAttempts() {
        for (int i = 0; i < 3; i++) {
            rateLimiter.registerAttempt(TEST_LOGIN);
        }
        
        rateLimiter.resetAttempts(TEST_LOGIN);
        
        assertEquals(0, rateLimiter.getAttemptCount(TEST_LOGIN));
        assertFalse(rateLimiter.isBlocked(TEST_LOGIN));
    }
    
    @Test
    void testIsBlocked() {
        assertFalse(rateLimiter.isBlocked(TEST_LOGIN));
        
        for (int i = 0; i < 6; i++) {
            rateLimiter.registerAttempt(TEST_LOGIN);
        }
        
        assertTrue(rateLimiter.isBlocked(TEST_LOGIN));
    }
    
    @Test
    void testGetAttemptCount() {
        assertEquals(0, rateLimiter.getAttemptCount(TEST_LOGIN));
        
        for (int i = 0; i < 3; i++) {
            rateLimiter.registerAttempt(TEST_LOGIN);
        }
        
        assertEquals(3, rateLimiter.getAttemptCount(TEST_LOGIN));
    }
    
    @Test
    void testBloqueioTemporario() {
        for (int i = 0; i < 6; i++) {
            rateLimiter.registerAttempt(TEST_LOGIN);
        }
        
        assertTrue(rateLimiter.isBlocked(TEST_LOGIN));
        assertTrue(rateLimiter.getRemainingBlockMinutes(TEST_LOGIN) > 0);
    }
}
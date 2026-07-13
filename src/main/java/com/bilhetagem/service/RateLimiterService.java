package com.bilhetagem.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Serviço para controle de tentativas de login.
 * 
 * <p>Bloqueia temporariamente após múltiplas tentativas falhas.</p>
 * 
 * @author Equipe de Desenvolvimento
 * @version 1.0.0
 * @since 2026-01-09
 */
public class RateLimiterService {
    
    private static final int MAX_ATTEMPTS = 5;
    private static final long BLOCK_TIME_MS = 15 * 60 * 1000; // 15 minutos
    
    private Map<String, Integer> attempts = new ConcurrentHashMap<>();
    private Map<String, Long> blockUntil = new ConcurrentHashMap<>();
    
    /**
     * Registra uma tentativa de login.
     * 
     * @param login Identificador do usuário
     * @return true se o login está bloqueado
     */
    public synchronized boolean registerAttempt(String login) {
        // Verificar se está bloqueado
        if (isBlocked(login)) {
            return true;
        }
        
        // Incrementar tentativas
        int current = attempts.getOrDefault(login, 0);
        current++;
        attempts.put(login, current);
        
        // Verificar se excedeu o limite (após 5 tentativas, bloqueia na 6ª)
        if (current > MAX_ATTEMPTS) {
            blockUntil.put(login, System.currentTimeMillis() + BLOCK_TIME_MS);
            return true;
        }
        
        return false;
    }
    
    /**
     * Verifica se um login está bloqueado.
     */
    public boolean isBlocked(String login) {
        Long blockEnd = blockUntil.get(login);
        if (blockEnd == null) {
            return false;
        }
        
        if (System.currentTimeMillis() > blockEnd) {
            // Desbloquear
            blockUntil.remove(login);
            attempts.remove(login);
            return false;
        }
        
        return true;
    }
    
    /**
     * Obtém o tempo restante de bloqueio em minutos.
     */
    public long getRemainingBlockMinutes(String login) {
        Long blockEnd = blockUntil.get(login);
        if (blockEnd == null) {
            return 0;
        }
        
        long remaining = (blockEnd - System.currentTimeMillis()) / 1000 / 60;
        return Math.max(0, remaining);
    }
    
    /**
     * Reseta as tentativas de um login (após login bem-sucedido).
     */
    public void resetAttempts(String login) {
        attempts.remove(login);
        blockUntil.remove(login);
    }
    
    /**
     * Obtém o número de tentativas para um login.
     */
    public int getAttemptCount(String login) {
        return attempts.getOrDefault(login, 0);
    }
}
package com.bilhetagem.util;

import org.mindrot.jbcrypt.BCrypt;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utilitário para criptografia de dados sensíveis.
 * 
 * <p>Utiliza BCrypt para hash de senhas com salt automático.</p>
 * 
 * @author [Seu Nome]
 * @version 1.0.0
 * @since 2026-01-09
 */
public class CriptografiaUtil {
    
    private static final Logger LOGGER = LogManager.getLogger(CriptografiaUtil.class);
    
    // Fator de custo (work factor) - recomendado: 10-12
    private static final int WORK_FACTOR = 10;
    
    /**
     * Gera um hash BCrypt para uma senha.
     * 
     * @param senha Senha em texto puro
     * @return Hash da senha
     */
    public static String hashSenha(String senha) {
        if (senha == null || senha.isEmpty()) {
            throw new IllegalArgumentException("Senha não pode ser vazia");
        }
        
        String hash = BCrypt.hashpw(senha, BCrypt.gensalt(WORK_FACTOR));
        LOGGER.debug("Hash gerado com sucesso");
        return hash;
    }
    
    /**
     * Verifica se uma senha corresponde ao hash.
     * 
     * @param senha Senha em texto puro
     * @param hash Hash armazenado
     * @return true se a senha corresponde ao hash
     */
    public static boolean verificarSenha(String senha, String hash) {
        if (senha == null || hash == null) {
            return false;
        }
        
        try {
            return BCrypt.checkpw(senha, hash);
        } catch (Exception e) {
            LOGGER.error("Erro ao verificar senha", e);
            return false;
        }
    }
    
    /**
     * Verifica se um hash é válido (formato BCrypt).
     */
    public static boolean isHashValido(String hash) {
        return hash != null && hash.startsWith("$2a$") || hash.startsWith("$2b$");
    }
}
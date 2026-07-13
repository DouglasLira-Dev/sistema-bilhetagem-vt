package com.bilhetagem.util;

/**
 * Utilitário para validação e formatação de CPF.
 * 
 * @author Equipe de Desenvolvimento
 * @version 1.0.0
 * @since 2026-01-13
 */
public class CpfUtil {
    
    /**
     * Valida se um CPF é válido.
     * 
     * @param cpf CPF a ser validado (pode conter formatação)
     * @return true se o CPF é válido
     */
    public static boolean isValid(String cpf) {
        if (cpf == null) return false;
        
        // Remover formatação
        String cpfLimpo = cpf.replaceAll("[^0-9]", "");
        
        // Verificar se tem 11 dígitos
        if (cpfLimpo.length() != 11) return false;
        
        // Verificar se todos os dígitos são iguais (caso inválido)
        if (cpfLimpo.matches("(\\d)\\1{10}")) return false;
        
        // Calcular primeiro dígito verificador
        int soma = 0;
        for (int i = 0; i < 9; i++) {
            soma += (cpfLimpo.charAt(i) - '0') * (10 - i);
        }
        int primeiroDigito = 11 - (soma % 11);
        if (primeiroDigito >= 10) primeiroDigito = 0;
        
        // Calcular segundo dígito verificador
        soma = 0;
        for (int i = 0; i < 10; i++) {
            soma += (cpfLimpo.charAt(i) - '0') * (11 - i);
        }
        int segundoDigito = 11 - (soma % 11);
        if (segundoDigito >= 10) segundoDigito = 0;
        
        // Verificar dígitos
        return cpfLimpo.charAt(9) - '0' == primeiroDigito &&
               cpfLimpo.charAt(10) - '0' == segundoDigito;
    }
    
    /**
     * Formata um CPF para exibição.
     * 
     * @param cpf CPF sem formatação
     * @return CPF formatado (XXX.XXX.XXX-XX)
     */
    public static String formatar(String cpf) {
        if (cpf == null) return "";
        String cpfLimpo = cpf.replaceAll("[^0-9]", "");
        if (cpfLimpo.length() != 11) return cpf;
        
        return cpfLimpo.substring(0, 3) + "." +
               cpfLimpo.substring(3, 6) + "." +
               cpfLimpo.substring(6, 9) + "-" +
               cpfLimpo.substring(9, 11);
    }
    
    /**
     * Mascara um CPF para exibição (mostra apenas os 3 primeiros e 2 últimos).
     * 
     * @param cpf CPF completo
     * @return CPF mascarado (XXX.***.***-XX)
     */
    public static String mascarar(String cpf) {
        if (cpf == null) return "";
        String cpfLimpo = cpf.replaceAll("[^0-9]", "");
        if (cpfLimpo.length() != 11) return cpf;
        
        return cpfLimpo.substring(0, 3) + ".***.***-" + cpfLimpo.substring(9, 11);
    }
    
    /**
     * Remove formatação do CPF.
     * 
     * @param cpf CPF com formatação
     * @return CPF apenas com números
     */
    public static String limpar(String cpf) {
        if (cpf == null) return "";
        return cpf.replaceAll("[^0-9]", "");
    }
}
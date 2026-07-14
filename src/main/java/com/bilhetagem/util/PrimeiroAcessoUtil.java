package com.bilhetagem.util;

import com.bilhetagem.dao.UsuarioDAO;
import com.bilhetagem.dao.UsuarioDAOImpl;
import com.bilhetagem.model.Usuario;
import com.bilhetagem.model.Usuario.Perfil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Utilitário para gerenciar o primeiro acesso ao sistema.
 * 
 * @author Equipe de desenvolvimento
 * @version 1.0.0
 * @since 2026-01-13
 */
public class PrimeiroAcessoUtil {
    
    private static final Logger LOGGER = LogManager.getLogger(PrimeiroAcessoUtil.class);
    
    /**
     * Verifica se o sistema está sendo executado pela primeira vez.
     * Se não houver usuários cadastrados, cria um admin com senha aleatória.
     */
    public static void verificarPrimeiroAcesso() {
        try {
            UsuarioDAO dao = new UsuarioDAOImpl();
            long total = dao.listarTodos().size();
            
            if (total == 0) {
                LOGGER.info("🔐 Primeiro acesso detectado - Criando usuário ADMIN");
                
                // Gerar senha aleatória
                String senhaTemp = gerarSenhaTemporaria();
                String nome = "Administrador";
                String login = "admin";
                String email = "admin@bilhetagem.com";
                
                Usuario admin = new Usuario(login, senhaTemp, nome, email, Perfil.ADMIN);
                dao.salvar(admin);
                
                LOGGER.info("✅ Usuário ADMIN criado com senha temporária");
                
                // Exibir senha para o usuário
                JOptionPane.showMessageDialog(null,
                    "🔐 PRIMEIRO ACESSO - SISTEMA DE BILHETAGEM\n\n" +
                    "Usuário criado automaticamente:\n" +
                    "Login: " + login + "\n" +
                    "Senha: " + senhaTemp + "\n\n" +
                    "⚠️ IMPORTANTE: Altere esta senha no primeiro login!\n" +
                    "Acesse o menu 'Usuário > Informações' para alterar.",
                    "Senha Temporária",
                    JOptionPane.INFORMATION_MESSAGE);
                
            } else {
                LOGGER.info("✅ Sistema já possui {} usuários cadastrados", total);
            }
            
        } catch (SQLException e) {
            LOGGER.error("❌ Erro ao verificar primeiro acesso", e);
        }
    }
    
    /**
     * Gera uma senha temporária aleatória.
     */
    private static String gerarSenhaTemporaria() {
        // Gerar UUID e pegar os primeiros 8 caracteres + números
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        return uuid.substring(0, 8) + gerarNumerosAleatorios(4);
    }
    
    /**
     * Gera uma string com números aleatórios.
     */
    private static String gerarNumerosAleatorios(int quantidade) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < quantidade; i++) {
            sb.append((int) (Math.random() * 10));
        }
        return sb.toString();
    }
}
package com.bilhetagem.util;

import com.bilhetagem.model.Usuario;
import com.bilhetagem.model.Usuario.Perfil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utilitário para gerenciamento de sessão do usuário.
 * 
 * @author [Seu Nome]
 * @version 1.0.0
 * @since 2026-01-08
 */
public class SessaoUtil {
    
    private static final Logger LOGGER = LogManager.getLogger(SessaoUtil.class);
    
    private static Usuario usuarioLogado;
    private static long tempoInicioSessao;
    
    /**
     * Inicia uma nova sessão para o usuário.
     */
    public static void iniciarSessao(Usuario usuario) {
        usuarioLogado = usuario;
        tempoInicioSessao = System.currentTimeMillis();
        LOGGER.info("🟢 Sessão iniciada para: {}", usuario.getLogin());
    }
    
    /**
     * Encerra a sessão atual.
     */
    public static void encerrarSessao() {
        if (usuarioLogado != null) {
            LOGGER.info("🔴 Sessão encerrada para: {}", usuarioLogado.getLogin());
        }
        usuarioLogado = null;
        tempoInicioSessao = 0;
    }
    
    /**
     * Verifica se há uma sessão ativa.
     */
    public static boolean isLogado() {
        return usuarioLogado != null;
    }
    
    /**
     * Obtém o usuário logado.
     */
    public static Usuario getUsuarioLogado() {
        return usuarioLogado;
    }
    
    /**
     * Verifica se o usuário tem um determinado perfil.
     */
    public static boolean temPerfil(Perfil perfil) {
        return usuarioLogado != null && usuarioLogado.getPerfil() == perfil;
    }
    
    /**
     * Verifica se o usuário tem permissão para uma ação.
     */
    public static boolean temPermissao(Usuario.Permissao permissao) {
        if (usuarioLogado == null) {
            return false;
        }
        return usuarioLogado.temPermissao(permissao);
    }
    
    /**
     * Verifica se a sessão expirou (8 horas).
     */
    public static boolean sessaoExpirada() {
        if (usuarioLogado == null) {
            return true;
        }
        long tempoSessao = System.currentTimeMillis() - tempoInicioSessao;
        long tempoMaximo = 8 * 60 * 60 * 1000; // 8 horas
        return tempoSessao > tempoMaximo;
    }
}
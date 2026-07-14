package com.bilhetagem.util;

import com.bilhetagem.model.Usuario;
import com.bilhetagem.model.Usuario.Perfil;
import com.bilhetagem.model.Usuario.Permissao;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utilitário para gerenciamento de sessão do usuário.
 * 
 * @author Equipe de desenvolvimento
 * @version 1.0.0
 * @since 2026-01-09
 */
public class SessaoUtil {
    
    private static final Logger LOGGER = LogManager.getLogger(SessaoUtil.class);
    
    private static Usuario usuarioLogado;
    private static long tempoInicioSessao;
    private static final long TEMPO_MAXIMO_SESSAO = 8 * 60 * 60 * 1000; // 8 horas
    
    /**
     * Inicia uma nova sessão para o usuário.
     */
    public static void iniciarSessao(Usuario usuario) {
        usuarioLogado = usuario;
        tempoInicioSessao = System.currentTimeMillis();
        LOGGER.info("🟢 Sessão iniciada para: {} (Perfil: {})", 
                   usuario.getLogin(), usuario.getPerfil());
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
     * Verifica se o usuário tem uma permissão específica.
     * Lança exceção se não tiver permissão.
     */
    public static void verificarPermissao(Permissao permissao) {
        if (!temPermissao(permissao)) {
            throw new SecurityException(
                "Usuário não tem permissão para: " + permissao.getDescricao()
            );
        }
    }
    
    /**
     * Verifica se o usuário tem permissão para uma ação.
     * Retorna true/false (sem lançar exceção).
     */
    public static boolean temPermissao(Permissao permissao) {
        if (usuarioLogado == null) {
            LOGGER.warn("⚠️ Tentativa de verificar permissão sem usuário logado");
            return false;
        }
        return usuarioLogado.temPermissao(permissao);
    }
    
    /**
     * Verifica se o usuário tem todas as permissões.
     */
    public static boolean temTodasPermissoes(Permissao... permissoes) {
        if (usuarioLogado == null) return false;
        return usuarioLogado.temTodasPermissoes(permissoes);
    }
    
    /**
     * Verifica se o usuário tem pelo menos uma permissão.
     */
    public static boolean temAlgumaPermissao(Permissao... permissoes) {
        if (usuarioLogado == null) return false;
        return usuarioLogado.temAlgumaPermissao(permissoes);
    }
    
    /**
     * Verifica se a sessão expirou.
     */
    public static boolean sessaoExpirada() {
        if (usuarioLogado == null) {
            return true;
        }
        long tempoSessao = System.currentTimeMillis() - tempoInicioSessao;
        return tempoSessao > TEMPO_MAXIMO_SESSAO;
    }
    
    /**
     * Obtém o tempo restante de sessão em minutos.
     */
    public static long getTempoRestanteSessaoMinutos() {
        if (usuarioLogado == null) return 0;
        long tempoSessao = System.currentTimeMillis() - tempoInicioSessao;
        long restante = TEMPO_MAXIMO_SESSAO - tempoSessao;
        return Math.max(0, restante / 1000 / 60);
    }
}
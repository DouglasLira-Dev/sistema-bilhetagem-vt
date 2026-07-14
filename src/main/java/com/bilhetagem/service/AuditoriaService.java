package com.bilhetagem.service;

import com.bilhetagem.dao.LogAuditoriaDAO;
import com.bilhetagem.dao.LogAuditoriaDAOImpl;
import com.bilhetagem.model.LogAuditoria;
import com.bilhetagem.model.Usuario;
import com.bilhetagem.util.SessaoUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;

/**
 * Serviço para gerenciamento de auditoria.
 * 
 * <p>Responsável por registrar todas as ações dos usuários
 * no sistema para fins de auditoria e compliance.</p>
 * 
 * @author Equipe de desenvolvimento
 * @version 1.0.0
 * @since 2026-01-08
 */
public class AuditoriaService {
    
    private static final Logger LOGGER = LogManager.getLogger(AuditoriaService.class);
    
    /** Identificador da máquina local (host + IP), resolvido uma única vez. */
    private static final String HOST_LOCAL = resolverHostLocal();
    
    private LogAuditoriaDAO logDAO;
    
    public AuditoriaService() {
        this.logDAO = new LogAuditoriaDAOImpl();
    }
    
    /**
     * Resolve o nome do host e o IP da máquina local.
     * 
     * <p>Como esta é uma aplicação desktop (sem requisições HTTP), não existe
     * um "IP do cliente" no sentido de servidor web. O melhor identificador
     * disponível é a máquina onde a aplicação está rodando.</p>
     */
    private static String resolverHostLocal() {
        try {
            InetAddress local = InetAddress.getLocalHost();
            return local.getHostName() + " (" + local.getHostAddress() + ")";
        } catch (UnknownHostException e) {
            LOGGER.warn("⚠️ Não foi possível resolver o host local para auditoria", e);
            return "desconhecido";
        }
    }
    
    /**
     * Registra uma ação no log de auditoria.
     */
    public void registrarAcao(String acao, String entidade, Long entidadeId, String detalhes) {
        try {
            Usuario usuario = SessaoUtil.getUsuarioLogado();
            
            if (usuario == null) {
                LOGGER.warn("⚠️ Tentativa de registrar log sem usuário logado");
                return;
            }
            
            LogAuditoria log = new LogAuditoria();
            log.setUsuarioId(usuario.getId());
            log.setAcao(acao);
            log.setEntidade(entidade);
            log.setEntidadeId(entidadeId);
            log.setDetalhes(detalhes);
            log.setIp(HOST_LOCAL);
            
            logDAO.registrar(log);
            LOGGER.info("📝 Log registrado: {}", acao);
            
        } catch (SQLException e) {
            LOGGER.error("❌ Erro ao registrar log de auditoria", e);
        }
    }
    
    /**
     * Registra uma ação de criação.
     */
    public void registrarCriacao(String entidade, Long entidadeId, String detalhes) {
        registrarAcao("CRIACAO", entidade, entidadeId, detalhes);
    }
    
    /**
     * Registra uma ação de atualização.
     */
    public void registrarAtualizacao(String entidade, Long entidadeId, String detalhes) {
        registrarAcao("ATUALIZACAO", entidade, entidadeId, detalhes);
    }
    
    /**
     * Registra uma ação de exclusão.
     */
    public void registrarExclusao(String entidade, Long entidadeId, String detalhes) {
        registrarAcao("EXCLUSAO", entidade, entidadeId, detalhes);
    }
    
    /**
     * Registra uma ação de consulta.
     */
    public void registrarConsulta(String entidade, Long entidadeId, String detalhes) {
        registrarAcao("CONSULTA", entidade, entidadeId, detalhes);
    }
    
    /**
     * Registra uma ação de exportação.
     */
    public void registrarExportacao(String entidade, String detalhes) {
        registrarAcao("EXPORTACAO", entidade, null, detalhes);
    }
    
    /**
     * Registra uma ação de importação.
     */
    public void registrarImportacao(String entidade, String detalhes) {
        registrarAcao("IMPORTACAO", entidade, null, detalhes);
    }
    
    /**
     * Registra uma ação de login.
     */
    public void registrarLogin(Usuario usuario) {
        registrarAcao("LOGIN", "USUARIO", usuario.getId(), "Login realizado: " + usuario.getLogin());
    }
    
    /**
     * Registra uma ação de logout.
     */
    public void registrarLogout(Usuario usuario) {
        registrarAcao("LOGOUT", "USUARIO", usuario.getId(), "Logout realizado: " + usuario.getLogin());
    }
}
package com.bilhetagem.model;

import java.time.LocalDateTime;

/**
 * Classe que representa um log de auditoria.
 * 
 * @author Equipe de desenvolvimento
 * @version 1.0.0
 * @since 2026-01-08
 */
public class LogAuditoria {
    
    private Long id;
    private Long usuarioId;
    private String usuarioLogin;
    private String acao;
    private String entidade;
    private Long entidadeId;
    private String detalhes;
    private String ip;
    private LocalDateTime dataHora;
    
    // Construtores, getters e setters
    public LogAuditoria() {}
    
    public LogAuditoria(Long usuarioId, String acao, String entidade, Long entidadeId, String detalhes) {
        this.usuarioId = usuarioId;
        this.acao = acao;
        this.entidade = entidade;
        this.entidadeId = entidadeId;
        this.detalhes = detalhes;
        this.dataHora = LocalDateTime.now();
    }
    
    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    
    public String getUsuarioLogin() { return usuarioLogin; }
    public void setUsuarioLogin(String usuarioLogin) { this.usuarioLogin = usuarioLogin; }
    
    public String getAcao() { return acao; }
    public void setAcao(String acao) { this.acao = acao; }
    
    public String getEntidade() { return entidade; }
    public void setEntidade(String entidade) { this.entidade = entidade; }
    
    public Long getEntidadeId() { return entidadeId; }
    public void setEntidadeId(Long entidadeId) { this.entidadeId = entidadeId; }
    
    public String getDetalhes() { return detalhes; }
    public void setDetalhes(String detalhes) { this.detalhes = detalhes; }
    
    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }
    
    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }
}
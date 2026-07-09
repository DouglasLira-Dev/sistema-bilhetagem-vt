package com.bilhetagem.model;

import java.time.LocalDateTime;

/**
 * Classe que representa um usuário do sistema.
 * 
 * <p>Contém informações de autenticação e permissões
 * para controle de acesso ao sistema.</p>
 * 
 * @author [Seu Nome]
 * @version 1.0.0
 * @since 2026-01-08
 */
public class Usuario {
    
    private Long id;
    private String login;
    private String senha;
    private String nome;
    private String email;
    private Perfil perfil;
    private boolean ativo;
    private LocalDateTime ultimoAcesso;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;
    
    /**
     * Enumeração de perfis de usuário.
     */
    public enum Perfil {
        ADMIN("Administrador", "Acesso total ao sistema"),
        GERENTE("Gerente", "Gerenciar solicitações e relatórios"),
        OPERADOR("Operador", "Cadastrar e consultar solicitações"),
        CONSULTOR("Consultor", "Apenas consultar dados");
        
        private final String descricao;
        private final String permissao;
        
        Perfil(String descricao, String permissao) {
            this.descricao = descricao;
            this.permissao = permissao;
        }
        
        public String getDescricao() {
            return descricao;
        }
        
        public String getPermissao() {
            return permissao;
        }
    }
    
    // Construtores
    public Usuario() {}
    
    public Usuario(String login, String senha, String nome, String email, Perfil perfil) {
        this.login = login;
        this.senha = senha;
        this.nome = nome;
        this.email = email;
        this.perfil = perfil;
        this.ativo = true;
    }
    
    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
    
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public Perfil getPerfil() { return perfil; }
    public void setPerfil(Perfil perfil) { this.perfil = perfil; }
    
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
    
    public LocalDateTime getUltimoAcesso() { return ultimoAcesso; }
    public void setUltimoAcesso(LocalDateTime ultimoAcesso) { this.ultimoAcesso = ultimoAcesso; }
    
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
    
    public LocalDateTime getAtualizadoEm() { return atualizadoEm; }
    public void setAtualizadoEm(LocalDateTime atualizadoEm) { this.atualizadoEm = atualizadoEm; }
    
    /**
     * Verifica se o usuário tem permissão para uma determinada ação.
     */
    public boolean temPermissao(Permissao permissao) {
        if (this.perfil == Perfil.ADMIN) {
            return true;
        }
        // Implementar regras específicas por perfil
        return false;
    }
    
    /**
     * Enumeração de permissões do sistema.
     */
    public enum Permissao {
        CADASTRAR_SOLICITACAO,
        EDITAR_SOLICITACAO,
        EXCLUIR_SOLICITACAO,
        CONSULTAR_SOLICITACAO,
        EXPORTAR_DADOS,
        IMPORTAR_DADOS,
        GERAR_RELATORIOS,
        GERENCIAR_USUARIOS,
        CONFIGURAR_SISTEMA
    }
}
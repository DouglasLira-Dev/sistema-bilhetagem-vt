package com.bilhetagem.model;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Classe que representa um usuário do sistema.
 * 
 * <p>Contém informações de autenticação, perfil e permissões
 * para controle de acesso ao sistema.</p>
 * 
 * @author [Seu Nome]
 * @version 1.0.0
 * @since 2026-01-09
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
        OPERADOR("Operador", "Cadastrar e editar solicitações"),
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
    
    /**
     * Enumeração de permissões do sistema.
     */
    public enum Permissao {
        // Solicitações
        CADASTRAR_SOLICITACAO("Cadastrar solicitação"),
        EDITAR_SOLICITACAO("Editar solicitação"),
        EXCLUIR_SOLICITACAO("Excluir solicitação"),
        CONSULTAR_SOLICITACAO("Consultar solicitação"),
        
        // Dados
        EXPORTAR_DADOS("Exportar dados"),
        IMPORTAR_DADOS("Importar dados"),
        
        // Relatórios
        GERAR_RELATORIOS("Gerar relatórios"),
        
        // Administração
        GERENCIAR_USUARIOS("Gerenciar usuários"),
        CONFIGURAR_SISTEMA("Configurar sistema"),
        VER_AUDITORIA("Ver auditoria");
        
        private final String descricao;
        
        Permissao(String descricao) {
            this.descricao = descricao;
        }
        
        public String getDescricao() {
            return descricao;
        }
    }
    
    /**
     * Matriz de permissões por perfil.
     */
    private static final Map<Perfil, Set<Permissao>> PERMISSOES = Map.of(
        Perfil.ADMIN, EnumSet.allOf(Permissao.class),
        
        Perfil.GERENTE, EnumSet.of(
            Permissao.CADASTRAR_SOLICITACAO,
            Permissao.EDITAR_SOLICITACAO,
            Permissao.CONSULTAR_SOLICITACAO,
            Permissao.EXPORTAR_DADOS,
            Permissao.IMPORTAR_DADOS,
            Permissao.GERAR_RELATORIOS,
            Permissao.VER_AUDITORIA
        ),
        
        Perfil.OPERADOR, EnumSet.of(
            Permissao.CADASTRAR_SOLICITACAO,
            Permissao.EDITAR_SOLICITACAO,
            Permissao.CONSULTAR_SOLICITACAO
        ),
        
        Perfil.CONSULTOR, EnumSet.of(
            Permissao.CONSULTAR_SOLICITACAO
        )
    );
    
    // ===== CONSTRUTORES =====
    
    public Usuario() {}
    
    public Usuario(String login, String senha, String nome, String email, Perfil perfil) {
        this.login = login;
        this.senha = senha;
        this.nome = nome;
        this.email = email;
        this.perfil = perfil;
        this.ativo = true;
    }
    
    // ===== GETTERS E SETTERS =====
    
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
    
    // ===== MÉTODOS DE PERMISSÃO =====
    
    /**
     * Verifica se o usuário tem uma permissão específica.
     */
    public boolean temPermissao(Permissao permissao) {
        if (perfil == null) return false;
        return PERMISSOES.getOrDefault(perfil, EnumSet.noneOf(Permissao.class))
                         .contains(permissao);
    }
    
    /**
     * Verifica se o usuário tem todas as permissões especificadas.
     */
    public boolean temTodasPermissoes(Permissao... permissoes) {
        for (Permissao p : permissoes) {
            if (!temPermissao(p)) return false;
        }
        return true;
    }
    
    /**
     * Verifica se o usuário tem pelo menos uma das permissões especificadas.
     */
    public boolean temAlgumaPermissao(Permissao... permissoes) {
        for (Permissao p : permissoes) {
            if (temPermissao(p)) return true;
        }
        return false;
    }
    
    /**
     * Obtém todas as permissões do usuário.
     */
    public Set<Permissao> getPermissoes() {
        return PERMISSOES.getOrDefault(perfil, EnumSet.noneOf(Permissao.class));
    }
    
    @Override
    public String toString() {
        return String.format("Usuario{id=%d, login='%s', nome='%s', perfil=%s}", 
                           id, login, nome, perfil);
    }
}
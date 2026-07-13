package com.bilhetagem.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Classe que representa uma solicitação de vale transporte.
 * 
 * <p>Esta classe mapeia os dados da tabela 'solicitacoes' do banco de dados,
 * encapsulando os atributos e fornecendo métodos para manipulação.</p>
 * 
 * <p>Os tipos de solicitação possíveis são:
 * <ul>
 *   <li><b>adesao</b> - Primeira solicitação ou reativação</li>
 *   <li><b>renuncia</b> - Cancelamento do benefício</li>
 *   <li><b>alteracao</b> - Alteração de dados do benefício</li>
 * </ul>
 * </p>
 * 
 * @author [Seu Nome]
 * @version 1.0.0
 * @since 2026-01-08
 */
public class Solicitacao {
    
    // ===== ATRIBUTOS =====
    private Long id;
    private LocalDate dataEfetivacao;
    private String mesReferencia;
    private String matricula;
    private String cpf;
    private String nome;
    private String numeroCartao;
    private Integer quantidadeValeTipoA;
    private TipoSolicitacao tipoSolicitacao;
    private String observacao;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;  // Soft delete
    
    // ===== CONSTRUTORES =====
    
    /**
     * Construtor padrão para criação de uma nova solicitação.
     * Inicializa os campos com valores padrão.
     */
    public Solicitacao() {
        this.quantidadeValeTipoA = 0;
        this.tipoSolicitacao = TipoSolicitacao.ADESAO;
    }
    
    /**
     * Construtor com todos os campos obrigatórios.
     * 
     * @param dataEfetivacao Data da efetivação da solicitação
     * @param mesReferencia Mês de referência (MM/yyyy)
     * @param matricula Matrícula do funcionário
     * @param cpf CPF do funcionário
     * @param nome Nome completo do funcionário
     * @param tipoSolicitacao Tipo da solicitação
     */
    public Solicitacao(LocalDate dataEfetivacao, String mesReferencia, 
                       String matricula, String cpf, String nome,
                       TipoSolicitacao tipoSolicitacao) {
        this();
        this.dataEfetivacao = dataEfetivacao;
        this.mesReferencia = mesReferencia;
        this.matricula = matricula;
        this.cpf = cpf;
        this.nome = nome;
        this.tipoSolicitacao = tipoSolicitacao;
    }
    
    // ===== ENUMERAÇÃO =====
    
    /**
     * Enumeração que define os tipos de solicitação possíveis.
     */
    public enum TipoSolicitacao {
        ADESAO("adesao", "Adesão"),
        RENUNCIA("renuncia", "Renúncia"),
        ALTERACAO("alteracao", "Alteração");
        
        private final String valor;
        private final String descricao;
        
        TipoSolicitacao(String valor, String descricao) {
            this.valor = valor;
            this.descricao = descricao;
        }
        
        public String getValor() {
            return valor;
        }
        
        public String getDescricao() {
            return descricao;
        }
        
        /**
         * Converte uma string para o enum correspondente.
         * 
         * @param valor String com o tipo (ex: "adesao")
         * @return TipoSolicitacao correspondente
         * @throws IllegalArgumentException se o valor for inválido
         */
        public static TipoSolicitacao fromValor(String valor) {
            for (TipoSolicitacao tipo : TipoSolicitacao.values()) {
                if (tipo.valor.equalsIgnoreCase(valor)) {
                    return tipo;
                }
            }
            throw new IllegalArgumentException("Tipo de solicitação inválido: " + valor);
        }
        
        /**
         * Converte uma string para o enum ignorando acentos e case.
         * 
         * @param descricao Descrição do tipo (ex: "adesão", "renúncia")
         * @return TipoSolicitacao correspondente
         */
        public static TipoSolicitacao fromDescricao(String descricao) {
            String normalized = descricao.toLowerCase().trim();
            if (normalized.contains("adesao") || normalized.contains("adesão")) {
                return ADESAO;
            } else if (normalized.contains("renuncia") || normalized.contains("renúncia")) {
                return RENUNCIA;
            } else if (normalized.contains("alteracao") || normalized.contains("alteração")) {
                return ALTERACAO;
            }
            throw new IllegalArgumentException("Descrição de tipo inválida: " + descricao);
        }
    }
    
    // ===== GETTERS E SETTERS =====
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public LocalDate getDataEfetivacao() {
        return dataEfetivacao;
    }
    
    public void setDataEfetivacao(LocalDate dataEfetivacao) {
        this.dataEfetivacao = dataEfetivacao;
    }
    
    public String getMesReferencia() {
        return mesReferencia;
    }
    
    public void setMesReferencia(String mesReferencia) {
        this.mesReferencia = mesReferencia;
    }
    
    public String getMatricula() {
        return matricula;
    }
    
    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }
    
    public String getCpf() {
        return cpf;
    }
    
    public void setCpf(String cpf) {
        this.cpf = cpf;
    }
    
    public String getNome() {
        return nome;
    }
    
    public void setNome(String nome) {
        this.nome = nome;
    }
    
    public String getNumeroCartao() {
        return numeroCartao;
    }
    
    public void setNumeroCartao(String numeroCartao) {
        this.numeroCartao = numeroCartao;
    }
    
    public Integer getQuantidadeValeTipoA() {
        return quantidadeValeTipoA;
    }
    
    public void setQuantidadeValeTipoA(Integer quantidadeValeTipoA) {
        this.quantidadeValeTipoA = quantidadeValeTipoA != null ? quantidadeValeTipoA : 0;
    }
    
    public TipoSolicitacao getTipoSolicitacao() {
        return tipoSolicitacao;
    }
    
    public void setTipoSolicitacao(TipoSolicitacao tipoSolicitacao) {
        this.tipoSolicitacao = tipoSolicitacao;
    }
    
    public String getObservacao() {
        return observacao;
    }
    
    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }
    
    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }
    
    /**
     * Verifica se a solicitação foi deletada (soft delete).
     * 
     * @return true se foi deletada
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }
    
    // ===== MÉTODOS UTILITÁRIOS =====
    
    /**
     * Verifica se a solicitação é válida para persistência.
     * 
     * @return true se todos os campos obrigatórios estão preenchidos
     */
    public boolean isValid() {
        return dataEfetivacao != null 
            && mesReferencia != null && !mesReferencia.trim().isEmpty()
            && matricula != null && !matricula.trim().isEmpty()
            && cpf != null && !cpf.trim().isEmpty()
            && nome != null && !nome.trim().isEmpty()
            && tipoSolicitacao != null;
    }
    
    /**
     * Formata a data de efetivação para exibição (dd/MM/yyyy).
     * 
     * @return String data formatada
     */
    public String getDataEfetivacaoFormatada() {
        if (dataEfetivacao == null) {
            return "";
        }
        return dataEfetivacao.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
    
    /**
     * Obtém a descrição do tipo de solicitação.
     * 
     * @return String descrição em português
     */
    public String getTipoDescricao() {
        return tipoSolicitacao != null ? tipoSolicitacao.getDescricao() : "";
    }
    
    @Override
    public String toString() {
        return String.format("Solicitação [ID=%d, Matrícula=%s, Nome=%s, Tipo=%s, Deletada=%s]",
                            id, matricula, nome, tipoSolicitacao, isDeleted());
    }
}
package com.bilhetagem.dao;

import com.bilhetagem.model.Solicitacao;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Interface que define as operações de acesso a dados para solicitações.
 * 
 * <p>Esta interface estabelece o contrato para todas as operações
 * CRUD (Create, Read, Update, Delete) e consultas específicas
 * relacionadas à tabela de solicitações.</p>
 * 
 * @author [Seu Nome]
 * @version 1.0.0
 * @since 2026-01-08
 */
public interface SolicitacaoDAO {
    
    /**
     * Insere uma nova solicitação no banco de dados.
     * 
     * @param solicitacao Objeto contendo os dados da solicitação
     * @return A solicitação salva com o ID gerado
     * @throws SQLException Se houver erro na operação
     */
    Solicitacao salvar(Solicitacao solicitacao) throws SQLException;
    
    /**
     * Busca uma solicitação pelo ID.
     * 
     * @param id ID da solicitação
     * @return Optional contendo a solicitação se encontrada
     * @throws SQLException Se houver erro na operação
     */
    Optional<Solicitacao> buscarPorId(Long id) throws SQLException;
    
    /**
     * Busca solicitações pela matrícula do funcionário.
     * 
     * @param matricula Matrícula do funcionário
     * @return Lista de solicitações encontradas
     * @throws SQLException Se houver erro na operação
     */
    List<Solicitacao> buscarPorMatricula(String matricula) throws SQLException;
    
    /**
     * Busca solicitações pelo CPF do funcionário.
     * 
     * @param cpf CPF do funcionário
     * @return Lista de solicitações encontradas
     * @throws SQLException Se houver erro na operação
     */
    List<Solicitacao> buscarPorCpf(String cpf) throws SQLException;
    
    /**
     * Busca solicitações pelo nome do funcionário (busca parcial).
     * 
     * @param nome Nome ou parte do nome
     * @return Lista de solicitações encontradas
     * @throws SQLException Se houver erro na operação
     */
    List<Solicitacao> buscarPorNome(String nome) throws SQLException;
    
    /**
     * Busca solicitações por mês de referência.
     * 
     * @param mesReferencia Mês de referência (MM/yyyy)
     * @return Lista de solicitações do mês
     * @throws SQLException Se houver erro na operação
     */
    List<Solicitacao> buscarPorMesReferencia(String mesReferencia) throws SQLException;
    
    /**
     * Busca solicitações por mês de referência e tipo.
     * 
     * @param mesReferencia Mês de referência (MM/yyyy)
     * @param tipo Tipo de solicitação
     * @return Lista de solicitações que atendem aos critérios
     * @throws SQLException Se houver erro na operação
     */
    List<Solicitacao> buscarPorMesETipo(String mesReferencia, Solicitacao.TipoSolicitacao tipo) 
            throws SQLException;
    
    /**
     * Busca solicitações por período de datas.
     * 
     * @param dataInicial Data inicial (inclusive)
     * @param dataFinal Data final (inclusive)
     * @return Lista de solicitações no período
     * @throws SQLException Se houver erro na operação
     */
    List<Solicitacao> buscarPorPeriodo(String dataInicial, String dataFinal) throws SQLException;
    
    /**
     * Busca todas as solicitações ordenadas por data decrescente.
     * 
     * @return Lista com todas as solicitações
     * @throws SQLException Se houver erro na operação
     */
    List<Solicitacao> listarTodos() throws SQLException;
    
    /**
     * Atualiza os dados de uma solicitação existente.
     * 
     * @param solicitacao Objeto com dados atualizados
     * @return true se atualizou com sucesso
     * @throws SQLException Se houver erro na operação
     */
    boolean atualizar(Solicitacao solicitacao) throws SQLException;
    
    /**
     * Exclui uma solicitação pelo ID.
     * 
     * @param id ID da solicitação a ser excluída
     * @return true se excluiu com sucesso
     * @throws SQLException Se houver erro na operação
     */
    boolean excluir(Long id) throws SQLException;
    
    /**
     * Verifica se uma solicitação existe pelo ID.
     * 
     * @param id ID da solicitação
     * @return true se a solicitação existe
     * @throws SQLException Se houver erro na operação
     */
    boolean existePorId(Long id) throws SQLException;
    
    /**
     * Obtém o total de solicitações cadastradas.
     * 
     * @return Número total de solicitações
     * @throws SQLException Se houver erro na operação
     */
    long contarTotal() throws SQLException;
    
    /**
     * Busca solicitações com filtros avançados.
     * 
     * @param matricula Matrícula (pode ser null)
     * @param cpf CPF (pode ser null)
     * @param nome Nome (pode ser null)
     * @param mesReferencia Mês de referência (pode ser null)
     * @param tipo Tipo de solicitação (pode ser null)
     * @return Lista de solicitações que atendem aos filtros
     * @throws SQLException Se houver erro na operação
     */
    List<Solicitacao> buscarComFiltros(String matricula, String cpf, String nome, 
                                      String mesReferencia, Solicitacao.TipoSolicitacao tipo) 
            throws SQLException;
}
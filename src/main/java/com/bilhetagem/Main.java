package com.bilhetagem;

import com.bilhetagem.dao.ConexaoBD;
import com.bilhetagem.util.BancoUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Classe principal do Sistema de Bilhetagem de Vale Transporte.
 * 
 * <p>Esta classe é responsável por inicializar a aplicação,
 * configurar o ambiente e exibir a interface gráfica principal.</p>
 * 
 * <p>O sistema gerencia solicitações de vale transporte nos tipos:
 * <ul>
 *   <li>Adesão - Primeira solicitação ou reativação</li>
 *   <li>Renúncia - Cancelamento do benefício</li>
 *   <li>Alteração - Alteração de dados do benefício</li>
 * </ul>
 * </p>
 * 
 * @author [Seu Nome]
 * @version 1.0.0
 * @since 2026-01-08
 */
public class Main {
    
    /** Logger para registro de eventos da aplicação */
    private static final Logger LOGGER = LogManager.getLogger(Main.class);
    
    /**
     * Método principal que inicia a aplicação.
     * 
     * <p>Realiza as seguintes etapas:
     * <ol>
     *   <li>Inicializa o logger</li>
     *   <li>Testa a conexão com o banco de dados</li>
     *   <li>Inicializa a estrutura do banco de dados</li>
     *   <li>Exibe informações do sistema</li>
     *   <li>Prepara para iniciar a interface gráfica</li>
     * </ol>
     * </p>
     * 
     * @param args Argumentos da linha de comando (não utilizados)
     */
    public static void main(String[] args) {
        try {
            LOGGER.info("=".repeat(60));
            LOGGER.info("🚀 INICIANDO SISTEMA DE BILHETAGEM - VALE TRANSPORTE");
            LOGGER.info("=".repeat(60));
            
            // Informações da aplicação
            LOGGER.info("📅 Versão: 1.0.0-SNAPSHOT");
            LOGGER.info("☕ Java: {}", System.getProperty("java.version"));
            LOGGER.info("💻 SO: {}", System.getProperty("os.name"));
            LOGGER.info("📁 Diretório: {}", System.getProperty("user.dir"));
            LOGGER.info("=".repeat(60));
            
            // Testar conexão com banco de dados
            LOGGER.info("🔌 Testando conexão com banco de dados...");
            boolean conexaoOk = ConexaoBD.testarConexao();
            
            if (conexaoOk) {
                LOGGER.info("✅ Conexão com banco de dados estabelecida com sucesso!");
                LOGGER.info("📊 Banco: {}", ConexaoBD.getCaminhoBanco());
                
                // Inicializar estrutura do banco
                BancoUtil.inicializarBanco();
            } else {
                LOGGER.warn("⚠️ Não foi possível estabelecer conexão com o banco de dados.");
                LOGGER.warn("   Verifique se o diretório 'data/' tem permissões de escrita.");
            }
            
            LOGGER.info("=".repeat(60));
            LOGGER.info("📋 Sistema pronto para uso!");
            LOGGER.info("=".repeat(60));
            
            // TODO: Próximo passo - Inicializar interface gráfica
            // SwingUtilities.invokeLater(() -> new TelaPrincipal());
            
        } catch (Exception e) {
            LOGGER.error("❌ Erro fatal ao iniciar a aplicação!", e);
            System.err.println("Erro ao iniciar o sistema. Verifique o log para mais detalhes.");
            System.exit(1);
        } finally {
            // Garantir que a conexão seja fechada ao finalizar
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                LOGGER.info("🔄 Finalizando aplicação...");
                ConexaoBD.fecharConexao();
                LOGGER.info("👋 Sistema encerrado.");
            }));
        }
    }
}
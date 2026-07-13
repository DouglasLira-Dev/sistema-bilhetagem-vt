package com.bilhetagem.view;

import com.bilhetagem.dao.SolicitacaoDAO;
import com.bilhetagem.dao.SolicitacaoDAOImpl;
import com.bilhetagem.model.Solicitacao;
import com.bilhetagem.model.Usuario.Permissao;
import com.bilhetagem.service.AuditoriaService;
import com.bilhetagem.util.ExcelUtil;
import com.bilhetagem.util.SessaoUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.sql.SQLException;
import java.util.List;

/**
 * Tela de importação de dados do Excel.
 * 
 * <p>Permite selecionar um arquivo Excel, visualizar os dados
 * e importá-los para o banco de dados.</p>
 * 
 * @author [Seu Nome]
 * @version 1.0.0
 * @since 2026-01-08
 */
public class TelaImportacao extends JDialog {
    
    private static final Logger LOGGER = LogManager.getLogger(TelaImportacao.class);
    
    // ===== COMPONENTES =====
    private JTextField txtArquivo;
    private JButton btnSelecionar;
    private JButton btnImportar;
    private JButton btnCancelar;
    private JTable tabelaPreview;
    private DefaultTableModel modeloTabela;
    private JLabel lblStatus;
    private JProgressBar progressBar;
    
    // ===== DADOS =====
    private SolicitacaoDAO dao;
    private AuditoriaService auditoriaService;
    private List<Solicitacao> dadosImportados;
    private JFrame parent;
    
    // ===== CORES =====
    private static final Color COR_PRIMARIA = new Color(52, 152, 219);
    private static final Color COR_SUCESSO = new Color(46, 204, 113);
    
    /**
     * Construtor da tela de importação.
     */
    public TelaImportacao(JFrame parent) {
        super(parent, "Importar Dados do Excel", true);
        this.parent = parent;
        
        // Auto-checagem de permissão (defesa em profundidade), mesmo padrão
        // já usado em TelaUsuarios e TelaAuditoria.
        if (!SessaoUtil.temPermissao(Permissao.IMPORTAR_DADOS)) {
            JOptionPane.showMessageDialog(this,
                "Você não tem permissão para importar dados.",
                "Acesso Negado", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }
        
        this.dao = new SolicitacaoDAOImpl();
        this.auditoriaService = new AuditoriaService();
        
        configurarJanela();
        criarComponentes();
        
        LOGGER.info("📥 Tela de importação inicializada");
    }
    
    /**
     * Configura as propriedades da janela.
     */
    private void configurarJanela() {
        setSize(800, 600);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        setResizable(true);
    }
    
    /**
     * Cria os componentes da tela.
     */
    private void criarComponentes() {
        // Painel superior - Seleção de arquivo
        JPanel panelSuperior = criarPainelArquivo();
        add(panelSuperior, BorderLayout.NORTH);
        
        // Painel central - Preview dos dados
        JPanel panelCentral = criarPainelPreview();
        add(panelCentral, BorderLayout.CENTER);
        
        // Painel inferior - Status e botões
        JPanel panelInferior = criarPainelRodape();
        add(panelInferior, BorderLayout.SOUTH);
    }
    
    /**
     * Cria o painel de seleção de arquivo.
     */
    private JPanel criarPainelArquivo() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));
        panel.setBackground(new Color(240, 244, 248));
        
        JPanel panelSelecao = new JPanel(new BorderLayout(10, 5));
        panelSelecao.setOpaque(false);
        
        JLabel lblTitulo = new JLabel("📂 Selecione o arquivo Excel");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 14));
        panelSelecao.add(lblTitulo, BorderLayout.NORTH);
        
        JPanel panelArquivo = new JPanel(new BorderLayout(10, 5));
        panelArquivo.setOpaque(false);
        
        txtArquivo = new JTextField();
        txtArquivo.setEditable(false);
        txtArquivo.setFont(new Font("Arial", Font.PLAIN, 12));
        
        btnSelecionar = new JButton("📁 Selecionar");
        btnSelecionar.setBackground(COR_PRIMARIA);
        btnSelecionar.setForeground(Color.WHITE);
        btnSelecionar.setFont(new Font("Arial", Font.BOLD, 12));
        btnSelecionar.addActionListener(e -> selecionarArquivo());
        
        panelArquivo.add(txtArquivo, BorderLayout.CENTER);
        panelArquivo.add(btnSelecionar, BorderLayout.EAST);
        
        panelSelecao.add(panelArquivo, BorderLayout.SOUTH);
        panel.add(panelSelecao, BorderLayout.CENTER);
        
        // Informações de formato
        JLabel lblInfo = new JLabel("💡 Formatos suportados: .xlsx e .xls");
        lblInfo.setFont(new Font("Arial", Font.ITALIC, 11));
        lblInfo.setForeground(Color.GRAY);
        panel.add(lblInfo, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Cria o painel de preview dos dados.
     */
    private JPanel criarPainelPreview() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(0, 15, 10, 15));
        
        // Título do preview
        JLabel lblPreview = new JLabel("📋 Preview dos Dados");
        lblPreview.setFont(new Font("Arial", Font.BOLD, 13));
        panel.add(lblPreview, BorderLayout.NORTH);
        
        // Tabela de preview
        String[] colunas = {
            "Data", "Mês", "Matrícula", "CPF", "Nome",
            "Cartão", "Qtd", "Tipo", "Observação"
        };
        
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tabelaPreview = new JTable(modeloTabela);
        tabelaPreview.setRowHeight(25);
        tabelaPreview.setFont(new Font("Arial", Font.PLAIN, 12));
        tabelaPreview.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        tabelaPreview.getTableHeader().setBackground(new Color(52, 73, 94));
        tabelaPreview.getTableHeader().setForeground(Color.WHITE);
        
        // Configurar largura das colunas
        tabelaPreview.getColumnModel().getColumn(0).setPreferredWidth(80);
        tabelaPreview.getColumnModel().getColumn(1).setPreferredWidth(70);
        tabelaPreview.getColumnModel().getColumn(2).setPreferredWidth(80);
        tabelaPreview.getColumnModel().getColumn(3).setPreferredWidth(100);
        tabelaPreview.getColumnModel().getColumn(4).setPreferredWidth(200);
        tabelaPreview.getColumnModel().getColumn(5).setPreferredWidth(90);
        tabelaPreview.getColumnModel().getColumn(6).setPreferredWidth(50);
        tabelaPreview.getColumnModel().getColumn(7).setPreferredWidth(80);
        tabelaPreview.getColumnModel().getColumn(8).setPreferredWidth(150);
        
        JScrollPane scrollPane = new JScrollPane(tabelaPreview);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        scrollPane.setPreferredSize(new Dimension(0, 350));
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Cria o painel inferior com status e botões.
     */
    private JPanel criarPainelRodape() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));
        panel.setBackground(new Color(240, 244, 248));
        
        // Status
        JPanel panelStatus = new JPanel(new BorderLayout(10, 5));
        panelStatus.setOpaque(false);
        
        lblStatus = new JLabel("ℹ️ Aguardando seleção do arquivo...");
        lblStatus.setFont(new Font("Arial", Font.PLAIN, 12));
        
        progressBar = new JProgressBar();
        progressBar.setVisible(false);
        progressBar.setPreferredSize(new Dimension(150, 20));
        
        panelStatus.add(lblStatus, BorderLayout.WEST);
        panelStatus.add(progressBar, BorderLayout.EAST);
        
        // Botões
        JPanel panelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        panelBotoes.setOpaque(false);
        
        btnImportar = new JButton("📥 Importar");
        btnImportar.setBackground(COR_SUCESSO);
        btnImportar.setForeground(Color.WHITE);
        btnImportar.setFont(new Font("Arial", Font.BOLD, 12));
        btnImportar.setEnabled(false);
        btnImportar.addActionListener(e -> importarDados());
        
        btnCancelar = new JButton("❌ Cancelar");
        btnCancelar.setBackground(new Color(149, 165, 166));
        btnCancelar.setForeground(Color.WHITE);
        btnCancelar.setFont(new Font("Arial", Font.BOLD, 12));
        btnCancelar.addActionListener(e -> dispose());
        
        panelBotoes.add(btnImportar);
        panelBotoes.add(btnCancelar);
        
        panel.add(panelStatus, BorderLayout.NORTH);
        panel.add(panelBotoes, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Abre o seletor de arquivos.
     */
    private void selecionarArquivo() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Selecionar arquivo Excel");
        fileChooser.setFileFilter(new FileNameExtensionFilter(
            "Arquivos Excel (*.xlsx, *.xls)", "xlsx", "xls"
        ));
        
        int resultado = fileChooser.showOpenDialog(this);
        if (resultado == JFileChooser.APPROVE_OPTION) {
            File arquivo = fileChooser.getSelectedFile();
            txtArquivo.setText(arquivo.getAbsolutePath());
            carregarPreview(arquivo);
        }
    }
    
    /**
     * Carrega o preview dos dados do Excel.
     */
    private void carregarPreview(File arquivo) {
        try {
            lblStatus.setText("🔄 Lendo arquivo...");
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            
            // Validar arquivo
            if (!ExcelUtil.validarArquivoExcel(arquivo)) {
                JOptionPane.showMessageDialog(this,
                    "Arquivo inválido ou corrompido!\n" +
                    "Certifique-se de que é um arquivo Excel válido.",
                    "Erro", JOptionPane.ERROR_MESSAGE);
                lblStatus.setText("❌ Arquivo inválido");
                setCursor(Cursor.getDefaultCursor());
                return;
            }
            
            // Importar dados
            dadosImportados = ExcelUtil.importarExcel(arquivo);
            
            // Limpar tabela
            modeloTabela.setRowCount(0);
            
            if (dadosImportados.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Nenhum dado válido encontrado no arquivo.\n" +
                    "Verifique se as colunas estão no formato correto.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
                lblStatus.setText("⚠️ Nenhum dado encontrado");
                btnImportar.setEnabled(false);
                setCursor(Cursor.getDefaultCursor());
                return;
            }
            
            // Preencher tabela
            for (Solicitacao s : dadosImportados) {
                Object[] row = {
                    s.getDataEfetivacaoFormatada(),
                    s.getMesReferencia(),
                    s.getMatricula(),
                    s.getCpf(),
                    s.getNome(),
                    s.getNumeroCartao(),
                    s.getQuantidadeValeTipoA(),
                    s.getTipoDescricao(),
                    s.getObservacao()
                };
                modeloTabela.addRow(row);
            }
            
            lblStatus.setText(String.format("✅ %d registros encontrados", dadosImportados.size()));
            btnImportar.setEnabled(true);
            
            LOGGER.info("✅ Preview carregado: {} registros", dadosImportados.size());
            
        } catch (Exception e) {
            LOGGER.error("❌ Erro ao carregar preview", e);
            JOptionPane.showMessageDialog(this,
                "Erro ao ler o arquivo:\n" + e.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
            lblStatus.setText("❌ Erro ao carregar arquivo");
            btnImportar.setEnabled(false);
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }
    
    /**
     * Importa os dados para o banco de dados.
     */
    private void importarDados() {
        if (dadosImportados == null || dadosImportados.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Não há dados para importar.",
                "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Confirmar importação
        int confirm = JOptionPane.showConfirmDialog(this,
            String.format("Deseja importar %d registros?\n\n" +
                         "Adesões: %d\n" +
                         "Renúncias: %d\n" +
                         "Alterações: %d",
                         dadosImportados.size(),
                         dadosImportados.stream()
                             .filter(s -> s.getTipoSolicitacao() == 
                                 Solicitacao.TipoSolicitacao.ADESAO)
                             .count(),
                         dadosImportados.stream()
                             .filter(s -> s.getTipoSolicitacao() == 
                                 Solicitacao.TipoSolicitacao.RENUNCIA)
                             .count(),
                         dadosImportados.stream()
                             .filter(s -> s.getTipoSolicitacao() == 
                                 Solicitacao.TipoSolicitacao.ALTERACAO)
                             .count()),
            "Confirmar Importação",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        // Importar
        try {
            btnImportar.setEnabled(false);
            btnSelecionar.setEnabled(false);
            progressBar.setVisible(true);
            progressBar.setMaximum(dadosImportados.size());
            progressBar.setValue(0);
            
            int sucessos = 0;
            int erros = 0;
            
            for (int i = 0; i < dadosImportados.size(); i++) {
                Solicitacao s = dadosImportados.get(i);
                try {
                    dao.salvar(s);
                    sucessos++;
                    lblStatus.setText(String.format("🔄 Importando... %d/%d", i + 1, dadosImportados.size()));
                } catch (SQLException e) {
                    LOGGER.error("Erro ao importar registro {}: {}", i, e.getMessage());
                    erros++;
                }
                progressBar.setValue(i + 1);
            }
            
            // Mensagem final
            String mensagem = String.format(
                "✅ Importação concluída!\n\n" +
                "📊 Total de registros: %d\n" +
                "✅ Importados com sucesso: %d\n" +
                "❌ Falhas: %d",
                dadosImportados.size(), sucessos, erros
            );
            
            JOptionPane.showMessageDialog(this, mensagem, "Resultado da Importação",
                erros > 0 ? JOptionPane.WARNING_MESSAGE : JOptionPane.INFORMATION_MESSAGE);
            
            lblStatus.setText(String.format("✅ Importação finalizada: %d sucessos, %d erros", sucessos, erros));
            
            // Só registra auditoria se algo foi realmente persistido no banco.
            if (sucessos > 0) {
                auditoriaService.registrarImportacao("SOLICITACAO",
                    String.format("Importação de dados do Excel: %d registros importados, %d falhas",
                        sucessos, erros));
            }
            
            // Fechar tela se tudo deu certo
            if (erros == 0) {
                dispose();
            }
            
        } catch (Exception e) {
            LOGGER.error("❌ Erro durante importação", e);
            JOptionPane.showMessageDialog(this,
                "Erro durante a importação:\n" + e.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
        } finally {
            btnImportar.setEnabled(true);
            btnSelecionar.setEnabled(true);
            progressBar.setVisible(false);
            progressBar.setValue(0);
        }
    }
}
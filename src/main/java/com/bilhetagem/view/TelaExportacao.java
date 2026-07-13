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
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Tela de exportação de dados para Excel.
 * 
 * <p>Permite exportar dados completos ou relatórios resumidos
 * para arquivos Excel (.xlsx).</p>
 * 
 * @author [Seu Nome]
 * @version 1.0.0
 * @since 2026-01-08
 */
public class TelaExportacao extends JDialog {
    
    private static final Logger LOGGER = LogManager.getLogger(TelaExportacao.class);
    
    // ===== COMPONENTES =====
    private JRadioButton rbDadosCompletos;
    private JRadioButton rbRelatorioResumido;
    private JCheckBox chkApenasMesSelecionado;
    private JComboBox<String> cbMesReferencia;
    private JTextField txtArquivo;
    private JButton btnSelecionar;
    private JButton btnExportar;
    private JButton btnCancelar;
    private JLabel lblStatus;
    private JProgressBar progressBar;
    
    // ===== DADOS =====
    private SolicitacaoDAO dao;
    private AuditoriaService auditoriaService;
    private List<Solicitacao> todasSolicitacoes;
    private JFrame parent;
    private Map<String, Map<String, Long>> dadosConsolidados;
    
    // ===== CORES =====
    private static final Color COR_PRIMARIA = new Color(52, 152, 219);
    private static final Color COR_SUCESSO = new Color(46, 204, 113);
    
    /**
     * Construtor da tela de exportação.
     */
    public TelaExportacao(JFrame parent) {
        super(parent, "Exportar Dados para Excel", true);
        this.parent = parent;
        
        // Auto-checagem de permissão (defesa em profundidade), mesmo padrão
        // já usado em TelaUsuarios e TelaAuditoria.
        if (!SessaoUtil.temPermissao(Permissao.EXPORTAR_DADOS)) {
            JOptionPane.showMessageDialog(this,
                "Você não tem permissão para exportar dados.",
                "Acesso Negado", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }
        
        this.dao = new SolicitacaoDAOImpl();
        this.auditoriaService = new AuditoriaService();
        
        configurarJanela();
        criarComponentes();
        carregarDados();
        
        LOGGER.info("📤 Tela de exportação inicializada");
    }
    
    /**
     * Configura as propriedades da janela.
     */
    private void configurarJanela() {
        setSize(600, 450);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        setResizable(false);
    }
    
    /**
     * Cria os componentes da tela.
     */
    private void criarComponentes() {
        // Painel central - Opções
        JPanel panelCentral = criarPainelOpcoes();
        add(panelCentral, BorderLayout.CENTER);
        
        // Painel inferior - Status e botões
        JPanel panelInferior = criarPainelRodape();
        add(panelInferior, BorderLayout.SOUTH);
    }
    
    /**
     * Cria o painel de opções de exportação.
     */
    private JPanel criarPainelOpcoes() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Título
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel lblTitulo = new JLabel("📤 Opções de Exportação");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(lblTitulo, gbc);
        gbc.gridwidth = 1;
        
        // Separador
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        JSeparator separator = new JSeparator();
        panel.add(separator, gbc);
        gbc.gridwidth = 1;
        
        // Tipo de exportação
        gbc.gridy = 2;
        JLabel lblTipo = new JLabel("📋 Tipo de Exportação:");
        lblTipo.setFont(new Font("Arial", Font.BOLD, 13));
        panel.add(lblTipo, gbc);
        
        gbc.gridy = 3;
        gbc.gridx = 0;
        rbDadosCompletos = new JRadioButton("Dados Completos", true);
        rbDadosCompletos.setFont(new Font("Arial", Font.PLAIN, 12));
        rbDadosCompletos.addActionListener(e -> atualizarOpcoes());
        panel.add(rbDadosCompletos, gbc);
        
        gbc.gridy = 4;
        rbRelatorioResumido = new JRadioButton("Relatório Resumido por Mês", false);
        rbRelatorioResumido.setFont(new Font("Arial", Font.PLAIN, 12));
        rbRelatorioResumido.addActionListener(e -> atualizarOpcoes());
        panel.add(rbRelatorioResumido, gbc);
        
        ButtonGroup groupTipo = new ButtonGroup();
        groupTipo.add(rbDadosCompletos);
        groupTipo.add(rbRelatorioResumido);
        
        // Opções adicionais
        gbc.gridy = 5;
        gbc.gridx = 0;
        chkApenasMesSelecionado = new JCheckBox("Apenas mês selecionado");
        chkApenasMesSelecionado.setFont(new Font("Arial", Font.PLAIN, 12));
        chkApenasMesSelecionado.setEnabled(true);
        chkApenasMesSelecionado.addActionListener(e -> atualizarOpcoes());
        panel.add(chkApenasMesSelecionado, gbc);
        
        gbc.gridy = 6;
        gbc.gridx = 0;
        JLabel lblMes = new JLabel("Mês Referência:");
        lblMes.setFont(new Font("Arial", Font.PLAIN, 12));
        panel.add(lblMes, gbc);
        
        gbc.gridx = 1;
        cbMesReferencia = new JComboBox<>();
        cbMesReferencia.setPreferredSize(new Dimension(150, 25));
        cbMesReferencia.setEnabled(true);
        panel.add(cbMesReferencia, gbc);
        
        // Separador
        gbc.gridy = 7;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JSeparator separator2 = new JSeparator();
        panel.add(separator2, gbc);
        gbc.gridwidth = 1;
        
        // Local do arquivo
        gbc.gridy = 8;
        gbc.gridx = 0;
        JLabel lblArquivo = new JLabel("📁 Local do Arquivo:");
        lblArquivo.setFont(new Font("Arial", Font.BOLD, 13));
        panel.add(lblArquivo, gbc);
        
        gbc.gridy = 9;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JPanel panelArquivo = new JPanel(new BorderLayout(10, 5));
        panelArquivo.setOpaque(false);
        
        txtArquivo = new JTextField();
        txtArquivo.setEditable(false);
        txtArquivo.setFont(new Font("Arial", Font.PLAIN, 12));
        
        btnSelecionar = new JButton("📂 Selecionar");
        btnSelecionar.setBackground(COR_PRIMARIA);
        btnSelecionar.setForeground(Color.WHITE);
        btnSelecionar.setFont(new Font("Arial", Font.BOLD, 12));
        btnSelecionar.addActionListener(e -> selecionarLocalArquivo());
        
        panelArquivo.add(txtArquivo, BorderLayout.CENTER);
        panelArquivo.add(btnSelecionar, BorderLayout.EAST);
        
        panel.add(panelArquivo, gbc);
        gbc.gridwidth = 1;
        
        // Informações
        gbc.gridy = 10;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JLabel lblInfo = new JLabel("💡 O arquivo será salvo no formato .xlsx");
        lblInfo.setFont(new Font("Arial", Font.ITALIC, 11));
        lblInfo.setForeground(Color.GRAY);
        panel.add(lblInfo, gbc);
        gbc.gridwidth = 1;
        
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
        
        lblStatus = new JLabel("ℹ️ Configure as opções e selecione o local do arquivo");
        lblStatus.setFont(new Font("Arial", Font.PLAIN, 12));
        
        progressBar = new JProgressBar();
        progressBar.setVisible(false);
        progressBar.setPreferredSize(new Dimension(150, 20));
        
        panelStatus.add(lblStatus, BorderLayout.WEST);
        panelStatus.add(progressBar, BorderLayout.EAST);
        
        // Botões
        JPanel panelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        panelBotoes.setOpaque(false);
        
        btnExportar = new JButton("📤 Exportar");
        btnExportar.setBackground(COR_SUCESSO);
        btnExportar.setForeground(Color.WHITE);
        btnExportar.setFont(new Font("Arial", Font.BOLD, 12));
        btnExportar.setEnabled(false);
        btnExportar.addActionListener(e -> exportar());
        
        btnCancelar = new JButton("❌ Cancelar");
        btnCancelar.setBackground(new Color(149, 165, 166));
        btnCancelar.setForeground(Color.WHITE);
        btnCancelar.setFont(new Font("Arial", Font.BOLD, 12));
        btnCancelar.addActionListener(e -> dispose());
        
        panelBotoes.add(btnExportar);
        panelBotoes.add(btnCancelar);
        
        panel.add(panelStatus, BorderLayout.NORTH);
        panel.add(panelBotoes, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Carrega os dados do banco.
     */
    private void carregarDados() {
        try {
            LOGGER.info("🔄 Carregando dados para exportação...");
            todasSolicitacoes = dao.listarTodos();
            
            // Carregar meses no combo
            carregarMeses();
            
            // Consolidar dados para relatório
            consolidarDados();
            
            LOGGER.info("✅ Dados carregados: {} registros", todasSolicitacoes.size());
        } catch (SQLException e) {
            LOGGER.error("❌ Erro ao carregar dados", e);
            JOptionPane.showMessageDialog(this,
                "Erro ao carregar dados: " + e.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Carrega os meses no combo box.
     */
    private void carregarMeses() {
        java.util.Set<String> meses = todasSolicitacoes.stream()
            .map(Solicitacao::getMesReferencia)
            .filter(mes -> mes != null && !mes.isEmpty())
            .sorted(java.util.Collections.reverseOrder())
            .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));
        
        cbMesReferencia.removeAllItems();
        cbMesReferencia.addItem("Todos os Meses");
        for (String mes : meses) {
            cbMesReferencia.addItem(mes);
        }
    }
    
    /**
     * Consolida os dados para relatório resumido.
     */
    private void consolidarDados() {
        dadosConsolidados = new java.util.LinkedHashMap<>();
        
        for (Solicitacao s : todasSolicitacoes) {
            String mes = s.getMesReferencia();
            if (mes == null || mes.isEmpty()) continue;
            
            String tipo = s.getTipoSolicitacao() != null ? 
                s.getTipoSolicitacao().getDescricao() : "Desconhecido";
            
            dadosConsolidados.putIfAbsent(mes, new java.util.HashMap<>());
            Map<String, Long> tipos = dadosConsolidados.get(mes);
            tipos.put(tipo, tipos.getOrDefault(tipo, 0L) + 1);
        }
    }
    
    /**
     * Atualiza as opções baseado nas seleções.
     */
    private void atualizarOpcoes() {
        boolean isResumido = rbRelatorioResumido.isSelected();
        boolean isMesSelecionado = chkApenasMesSelecionado.isSelected();
        
        cbMesReferencia.setEnabled(isMesSelecionado || isResumido);
        chkApenasMesSelecionado.setEnabled(!isResumido);
        
        // Atualizar status
        String status = "ℹ️ ";
        if (isResumido) {
            status += "Exportando relatório resumido por mês";
        } else if (isMesSelecionado) {
            status += "Exportando dados do mês selecionado";
        } else {
            status += "Exportando todos os dados completos";
        }
        lblStatus.setText(status);
    }
    
    /**
     * Abre o seletor de local do arquivo.
     */
    private void selecionarLocalArquivo() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Salvar arquivo Excel");
        fileChooser.setFileFilter(new FileNameExtensionFilter(
            "Arquivo Excel (*.xlsx)", "xlsx"
        ));
        
        // Nome padrão
        String nomePadrao = "bilhetagem_exportacao_" + 
            java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        fileChooser.setSelectedFile(new File(nomePadrao + ".xlsx"));
        
        int resultado = fileChooser.showSaveDialog(this);
        if (resultado == JFileChooser.APPROVE_OPTION) {
            File arquivo = fileChooser.getSelectedFile();
            String caminho = arquivo.getAbsolutePath();
            
            // Adicionar extensão se não tiver
            if (!caminho.toLowerCase().endsWith(".xlsx")) {
                caminho += ".xlsx";
                arquivo = new File(caminho);
            }
            
            txtArquivo.setText(caminho);
            btnExportar.setEnabled(true);
            lblStatus.setText("✅ Arquivo selecionado: " + arquivo.getName());
        }
    }
    
    /**
     * Realiza a exportação dos dados.
     */
    private void exportar() {
        // Validar arquivo
        String caminho = txtArquivo.getText().trim();
        if (caminho.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Selecione um local para salvar o arquivo!",
                "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        File arquivo = new File(caminho);
        
        // Confirmar sobrescrita
        if (arquivo.exists()) {
            int confirm = JOptionPane.showConfirmDialog(this,
                "O arquivo já existe. Deseja sobrescrever?",
                "Confirmar Sobrescrita",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
        }
        
        try {
            btnExportar.setEnabled(false);
            btnSelecionar.setEnabled(false);
            progressBar.setVisible(true);
            progressBar.setValue(0);
            progressBar.setIndeterminate(true);
            
            lblStatus.setText("🔄 Exportando dados...");
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            
            if (rbRelatorioResumido.isSelected()) {
                // Exportar relatório resumido
                exportarRelatorioResumido(arquivo);
            } else if (chkApenasMesSelecionado.isSelected()) {
                // Exportar dados de um mês específico
                exportarDadosPorMes(arquivo);
            } else {
                // Exportar dados completos
                exportarDadosCompletos(arquivo);
            }
            
            progressBar.setIndeterminate(false);
            progressBar.setValue(100);
            
            // Só registra auditoria após o arquivo ter sido efetivamente gravado com sucesso.
            String tipoExportacao = rbRelatorioResumido.isSelected() ? "Relatório resumido" : "Dados completos";
            auditoriaService.registrarExportacao("SOLICITACAO",
                "Exportação de dados para Excel (" + tipoExportacao + "): " + arquivo.getName());
            
            JOptionPane.showMessageDialog(this,
                "✅ Exportação concluída com sucesso!\n\n" +
                "📁 Arquivo salvo em:\n" + arquivo.getAbsolutePath(),
                "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            
            lblStatus.setText("✅ Exportação concluída!");
            dispose();
            
        } catch (Exception e) {
            LOGGER.error("❌ Erro durante exportação", e);
            JOptionPane.showMessageDialog(this,
                "Erro durante a exportação:\n" + e.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
            lblStatus.setText("❌ Erro durante exportação");
        } finally {
            btnExportar.setEnabled(true);
            btnSelecionar.setEnabled(true);
            progressBar.setVisible(false);
            progressBar.setIndeterminate(false);
            progressBar.setValue(0);
            setCursor(Cursor.getDefaultCursor());
        }
    }
    
    /**
     * Exporta os dados completos.
     */
    private void exportarDadosCompletos(File arquivo) throws Exception {
        LOGGER.info("📤 Exportando dados completos: {} registros", todasSolicitacoes.size());
        ExcelUtil.exportarExcel(todasSolicitacoes, arquivo);
    }
    
    /**
     * Exporta dados de um mês específico.
     */
    private void exportarDadosPorMes(File arquivo) throws Exception {
        String mesSelecionado = (String) cbMesReferencia.getSelectedItem();
        
        if (mesSelecionado == null || mesSelecionado.equals("Todos os Meses")) {
            exportarDadosCompletos(arquivo);
            return;
        }
        
        List<Solicitacao> filtradas = todasSolicitacoes.stream()
            .filter(s -> mesSelecionado.equals(s.getMesReferencia()))
            .collect(Collectors.toList());
        
        LOGGER.info("📤 Exportando dados do mês {}: {} registros", mesSelecionado, filtradas.size());
        
        if (filtradas.isEmpty()) {
            throw new Exception("Nenhum dado encontrado para o mês selecionado: " + mesSelecionado);
        }
        
        ExcelUtil.exportarExcel(filtradas, arquivo);
    }
    
    /**
     * Exporta o relatório resumido.
     */
    private void exportarRelatorioResumido(File arquivo) throws Exception {
        LOGGER.info("📤 Exportando relatório resumido");
        
        // Filtrar por mês se necessário
        String mesSelecionado = (String) cbMesReferencia.getSelectedItem();
        Map<String, Map<String, Long>> dadosParaExportar = dadosConsolidados;
        
        if (mesSelecionado != null && !mesSelecionado.equals("Todos os Meses")) {
            dadosParaExportar = new java.util.LinkedHashMap<>();
            if (dadosConsolidados.containsKey(mesSelecionado)) {
                dadosParaExportar.put(mesSelecionado, dadosConsolidados.get(mesSelecionado));
            }
        }
        
        if (dadosParaExportar.isEmpty()) {
            throw new Exception("Nenhum dado encontrado para exportar.");
        }
        
        ExcelUtil.exportarRelatorioExcel(dadosParaExportar, arquivo);
    }
}
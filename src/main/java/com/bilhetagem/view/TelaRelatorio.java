package com.bilhetagem.view;

import com.bilhetagem.dao.SolicitacaoDAO;
import com.bilhetagem.dao.SolicitacaoDAOImpl;
import com.bilhetagem.model.Solicitacao;
import com.bilhetagem.model.Solicitacao.TipoSolicitacao;
import com.bilhetagem.model.Usuario.Permissao;
import com.bilhetagem.service.AuditoriaService;
import com.bilhetagem.util.ExcelUtil;
import com.bilhetagem.util.SessaoUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Tela de relatórios e gráficos do sistema.
 * 
 * <p>Esta classe implementa a visualização de dados com gráficos
 * de barras e pizza, além de tabelas consolidadas por mês.</p>
 * 
 * @author Equipe de desenvolvimento
 * @version 1.0.0
 * @since 2026-01-08
 */
public class TelaRelatorio extends JFrame {
    
    private static final Logger LOGGER = LogManager.getLogger(TelaRelatorio.class);
    
    // ===== COMPONENTES =====
    private JComboBox<String> cbMesInicio;
    private JComboBox<String> cbAnoInicio;
    private JComboBox<String> cbMesFim;
    private JComboBox<String> cbAnoFim;

    private static final String[] NOMES_MESES = {
        "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
        "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
    };

    private JTable tabelaResumo;

    private DefaultTableModel modeloTabela;

    private JPanel panelGraficoBarras;
    private JPanel panelGraficoPizza;
    private JLabel lblTotalPeriodo;
    private JLabel lblAdesoesPeriodo;
    private JLabel lblRenunciasPeriodo;
    private JLabel lblAlteracoesPeriodo;
    
    // ===== DADOS =====
    private SolicitacaoDAO dao;
    private AuditoriaService auditoriaService;
    private List<Solicitacao> todasSolicitacoes;
    private Map<String, Map<String, Long>> dadosConsolidados;
    
    // ===== CORES =====
    private static final Color COR_SUCESSO = new Color(46, 204, 113);
    private static final Color COR_PERIGO = new Color(231, 76, 60);
    private static final Color COR_AVISO = new Color(241, 196, 15);
    private static final Color COR_PRIMARIA = new Color(52, 152, 219);
    
    /**
     * Construtor da tela de relatórios.
     */
    public TelaRelatorio() {
        // Auto-checagem de permissão (defesa em profundidade), mesmo padrão
        // já usado em TelaUsuarios e TelaAuditoria.
        if (!SessaoUtil.temPermissao(Permissao.GERAR_RELATORIOS)) {
            JOptionPane.showMessageDialog(this,
                "Você não tem permissão para acessar relatórios.",
                "Acesso Negado", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }
        
        dao = new SolicitacaoDAOImpl();
        auditoriaService = new AuditoriaService();
        dadosConsolidados = new LinkedHashMap<>();
        
        configurarJanela();
        criarPainelPrincipal();
        carregarDados();
        
        LOGGER.info("📊 Tela de relatórios inicializada");
    }
    
    /**
     * Configura as propriedades da janela.
     */
    private void configurarJanela() {
        setTitle("📊 Relatórios e Gráficos - Vale Transporte");
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
    }
    
    /**
     * Cria o painel principal da tela.
     */
    private void criarPainelPrincipal() {
        // Painel superior - Filtros
        JPanel panelSuperior = criarPainelFiltros();
        add(panelSuperior, BorderLayout.NORTH);
        
        // Painel central - Gráficos
        JPanel panelCentral = criarPainelGraficos();
        add(panelCentral, BorderLayout.CENTER);
        
        // Painel inferior - Tabela e totalizadores
        JPanel panelInferior = criarPainelInferior();
        add(panelInferior, BorderLayout.SOUTH);
    }
    
    /**
     * Cria o painel de filtros.
     */
    private JPanel criarPainelFiltros() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.setBackground(new Color(240, 244, 248));
        
        // Filtro de mês
        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        panelFiltros.setOpaque(false);
        
        JLabel lblPeriodo = new JLabel("📅 Período:");
        JLabel lblDe = new JLabel("De:");

        cbMesInicio = new JComboBox<>(criarItensMeses());
        cbAnoInicio = new JComboBox<>();
        cbMesInicio.addActionListener(e -> atualizarRelatorio());
        cbAnoInicio.addActionListener(e -> atualizarRelatorio());

        JLabel lblAte = new JLabel("Até (opcional):");
        cbMesFim = new JComboBox<>(criarItensMeses());
        cbAnoFim = new JComboBox<>();
        cbMesFim.addActionListener(e -> atualizarRelatorio());
        cbAnoFim.addActionListener(e -> atualizarRelatorio());
        
        JButton btnAtualizar = new JButton("🔄 Atualizar");
        btnAtualizar.setBackground(COR_PRIMARIA);
        btnAtualizar.setForeground(Color.WHITE);
        btnAtualizar.setFont(new Font("Arial", Font.BOLD, 12));
        btnAtualizar.addActionListener(e -> atualizarRelatorio());
        
        JButton btnExportar = new JButton("📤 Exportar Excel");
        btnExportar.setBackground(new Color(46, 204, 113));
        btnExportar.setForeground(Color.WHITE);
        btnExportar.setFont(new Font("Arial", Font.BOLD, 12));
        btnExportar.addActionListener(e -> exportarRelatorio());
        
        panelFiltros.add(lblPeriodo);
        panelFiltros.add(lblDe);
        panelFiltros.add(cbMesInicio);
        panelFiltros.add(cbAnoInicio);
        panelFiltros.add(lblAte);
        panelFiltros.add(cbMesFim);
        panelFiltros.add(cbAnoFim);
        panelFiltros.add(btnAtualizar);
        panelFiltros.add(btnExportar);
        
        // Totalizadores rápidos
        JPanel panelTotalizadores = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 5));
        panelTotalizadores.setOpaque(false);
        
        lblTotalPeriodo = criarLabelTotalizador("📊 Total:", "0", Color.BLACK);
        lblAdesoesPeriodo = criarLabelTotalizador("✅ Adesões:", "0", COR_SUCESSO);
        lblRenunciasPeriodo = criarLabelTotalizador("❌ Renúncias:", "0", COR_PERIGO);
        lblAlteracoesPeriodo = criarLabelTotalizador("🔄 Alterações:", "0", COR_AVISO);
        
        panelTotalizadores.add(lblTotalPeriodo);
        panelTotalizadores.add(lblAdesoesPeriodo);
        panelTotalizadores.add(lblRenunciasPeriodo);
        panelTotalizadores.add(lblAlteracoesPeriodo);
        
        panel.add(panelFiltros, BorderLayout.WEST);
        panel.add(panelTotalizadores, BorderLayout.EAST);
        
        return panel;
    }
    
    /**
     * Cria o painel com os gráficos.
     */
    private JPanel criarPainelGraficos() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 15, 10, 15));
        
        // Painel do gráfico de barras
        panelGraficoBarras = new JPanel(new BorderLayout());
        panelGraficoBarras.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            "📊 Quantitativo por Tipo",
            TitledBorder.CENTER,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14)
        ));
        panelGraficoBarras.setBackground(Color.WHITE);
        
        // Painel do gráfico de pizza
        panelGraficoPizza = new JPanel(new BorderLayout());
        panelGraficoPizza.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            "📈 Porcentagem por Tipo",
            TitledBorder.CENTER,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14)
        ));
        panelGraficoPizza.setBackground(Color.WHITE);
        
        // Adicionar gráficos placeholder
        JLabel lblPlaceholder = new JLabel("Carregando gráficos...", SwingConstants.CENTER);
        lblPlaceholder.setFont(new Font("Arial", Font.ITALIC, 16));
        lblPlaceholder.setForeground(Color.GRAY);
        panelGraficoBarras.add(lblPlaceholder, BorderLayout.CENTER);
        panelGraficoPizza.add(lblPlaceholder, BorderLayout.CENTER);
        
        panel.add(panelGraficoBarras);
        panel.add(panelGraficoPizza);
        
        return panel;
    }
    
    /**
     * Cria o painel inferior com tabela e totalizadores.
     */
    private JPanel criarPainelInferior() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 15, 15, 15));
        
        // Tabela de resumo
        String[] colunas = {"Mês Referência", "Adesões", "Renúncias", "Alterações", "Total"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tabelaResumo = new JTable(modeloTabela);
        tabelaResumo.setRowHeight(25);
        tabelaResumo.setFont(new Font("Arial", Font.PLAIN, 12));
        tabelaResumo.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        tabelaResumo.getTableHeader().setBackground(new Color(52, 73, 94));
        tabelaResumo.getTableHeader().setForeground(Color.WHITE);
        
        // Configurar largura das colunas
        tabelaResumo.getColumnModel().getColumn(0).setPreferredWidth(100);
        tabelaResumo.getColumnModel().getColumn(1).setPreferredWidth(80);
        tabelaResumo.getColumnModel().getColumn(2).setPreferredWidth(80);
        tabelaResumo.getColumnModel().getColumn(3).setPreferredWidth(80);
        tabelaResumo.getColumnModel().getColumn(4).setPreferredWidth(80);
        
        JScrollPane scrollPane = new JScrollPane(tabelaResumo);
        scrollPane.setPreferredSize(new Dimension(0, 200));
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            "📋 Resumo Mensal"
        ));
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Cria um label estilizado para totalizadores.
     */
    private JLabel criarLabelTotalizador(String label, String valor, Color cor) {
        JLabel lbl = new JLabel(label + " " + valor);
        lbl.setFont(new Font("Arial", Font.BOLD, 13));
        lbl.setForeground(cor);
        return lbl;
    }
    
    /**
     * Carrega os dados do banco.
     */
    private void carregarDados() {
        try {
            LOGGER.info("🔄 Carregando dados para relatórios...");
            todasSolicitacoes = dao.listarTodos();
            
            // Carregar anos no combo
            carregarAnos();
            
            // Consolidar dados
            consolidarDados();
            
            // Atualizar relatório
            atualizarRelatorio();
            
            LOGGER.info("✅ Dados carregados: {} registros", todasSolicitacoes.size());
        } catch (SQLException e) {
            LOGGER.error("❌ Erro ao carregar dados", e);
            JOptionPane.showMessageDialog(this,
                "Erro ao carregar dados: " + e.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private String[] criarItensMeses() {
        String[] itens = new String[NOMES_MESES.length + 1];
        itens[0] = "";
        for (int i = 0; i < NOMES_MESES.length; i++) {
            itens[i + 1] = String.format("%02d - %s", i + 1, NOMES_MESES[i]);
        }
        return itens;
    }

    private void carregarAnos() {
        java.util.Set<String> anos = new java.util.TreeSet<>();
        try {
            List<Solicitacao> lista = dao.listarTodos();
            for (Solicitacao s : lista) {
                String mes = s.getMesReferencia();
                if (mes != null && mes.contains("/")) {
                    String[] partes = mes.split("/");
                    if (partes.length == 2) anos.add(partes[1]);
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Erro ao carregar anos", e);
        }

        Object anoInicioSel = cbAnoInicio != null ? cbAnoInicio.getSelectedItem() : null;
        Object anoFimSel = cbAnoFim != null ? cbAnoFim.getSelectedItem() : null;

        cbAnoInicio.removeAllItems();
        cbAnoFim.removeAllItems();
        cbAnoInicio.addItem("");
        cbAnoFim.addItem("");

        for (String ano : anos) {
            cbAnoInicio.addItem(ano);
            cbAnoFim.addItem(ano);
        }
        if (anoInicioSel != null) cbAnoInicio.setSelectedItem(anoInicioSel);
        if (anoFimSel != null) cbAnoFim.setSelectedItem(anoFimSel);
    }

    private Integer extrairNumeroMes(String itemCombo) {
        if (itemCombo == null || itemCombo.isEmpty()) return null;
        return Integer.parseInt(itemCombo.substring(0, 2));
    }

    private Integer calcularChavePeriodo(String itemMes, String itemAno, boolean limiteInicial) {
        if (itemAno == null || itemAno.isEmpty()) return null;
        int ano = Integer.parseInt(itemAno);
        Integer mes = extrairNumeroMes(itemMes);
        if (mes == null) mes = limiteInicial ? 1 : 12;
        return ano * 100 + mes;
    }

    private Integer chaveDoMesReferencia(String mesReferencia) {
        if (mesReferencia == null || !mesReferencia.contains("/")) return null;
        try {
            String[] partes = mesReferencia.split("/");
            int mes = Integer.parseInt(partes[0]);
            int ano = Integer.parseInt(partes[1]);
            return ano * 100 + mes;
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * Consolida os dados por mês e tipo.
     */
    private void consolidarDados() {
        dadosConsolidados.clear();
        
        for (Solicitacao s : todasSolicitacoes) {
            String mes = s.getMesReferencia();
            if (mes == null || mes.isEmpty()) continue;
            
            String tipo = s.getTipoSolicitacao() != null ? 
                s.getTipoSolicitacao().getDescricao() : "Desconhecido";
            
            dadosConsolidados.putIfAbsent(mes, new HashMap<>());
            Map<String, Long> tipos = dadosConsolidados.get(mes);
            tipos.put(tipo, tipos.getOrDefault(tipo, 0L) + 1);
        }
    }
    
    /**
     * Atualiza todos os componentes do relatório.
     */
    private void atualizarRelatorio() {
        if (panelGraficoBarras == null || panelGraficoPizza == null || modeloTabela == null) { return; }
        
        Integer chaveInicio = calcularChavePeriodo(
            (String) cbMesInicio.getSelectedItem(), (String) cbAnoInicio.getSelectedItem(), true);
        Integer chaveFim = calcularChavePeriodo(
            (String) cbMesFim.getSelectedItem(), (String) cbAnoFim.getSelectedItem(), false);

        List<Solicitacao> filtradas = filtrarPorPeriodo(chaveInicio, chaveFim);

        // Atualiza totalizadores, gráficos e tabela
        atualizarTotalizadores(filtradas);
        // Atualiza gráficos e tabela com base nas solicitações filtradas
        atualizarGraficos(filtradas);
        // Atualiza a tabela de resumo com base no período selecionado
        atualizarTabela(chaveInicio, chaveFim);
    }
    
    /**
     * Filtra as solicitações por período.
     */
        private List<Solicitacao> filtrarPorPeriodo(Integer chaveInicio, Integer chaveFim) {
            if (chaveInicio == null && chaveFim == null) {
                return new ArrayList<>(todasSolicitacoes);
            }
        return todasSolicitacoes.stream()
            .filter(s -> {
                Integer chave = chaveDoMesReferencia(s.getMesReferencia());
                if (chave == null) return false;
                if (chaveInicio != null && chave < chaveInicio) return false;
                if (chaveFim != null && chave > chaveFim) return false;
                return true;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Atualiza os totalizadores.
     */
    private void atualizarTotalizadores(List<Solicitacao> lista) {
        long total = lista.size();
        long adesoes = lista.stream()
            .filter(s -> s.getTipoSolicitacao() == TipoSolicitacao.ADESAO)
            .count();
        long renuncias = lista.stream()
            .filter(s -> s.getTipoSolicitacao() == TipoSolicitacao.RENUNCIA)
            .count();
        long alteracoes = lista.stream()
            .filter(s -> s.getTipoSolicitacao() == TipoSolicitacao.ALTERACAO)
            .count();
        
        lblTotalPeriodo.setText("📊 Total: " + total);
        lblAdesoesPeriodo.setText("✅ Adesões: " + adesoes);
        lblRenunciasPeriodo.setText("❌ Renúncias: " + renuncias);
        lblAlteracoesPeriodo.setText("🔄 Alterações: " + alteracoes);
    }
    
    /**
     * Atualiza os gráficos.
     */
    private void atualizarGraficos(List<Solicitacao> lista) {
        // Limpar painéis
        panelGraficoBarras.removeAll();
        panelGraficoPizza.removeAll();
        
        // Calcular dados
        long adesoes = lista.stream()
            .filter(s -> s.getTipoSolicitacao() == TipoSolicitacao.ADESAO)
            .count();
        long renuncias = lista.stream()
            .filter(s -> s.getTipoSolicitacao() == TipoSolicitacao.RENUNCIA)
            .count();
        long alteracoes = lista.stream()
            .filter(s -> s.getTipoSolicitacao() == TipoSolicitacao.ALTERACAO)
            .count();
        
        // Dataset para gráfico de barras
        DefaultCategoryDataset datasetBarras = new DefaultCategoryDataset();
        datasetBarras.addValue(adesoes, "Solicitações", "Adesões");
        datasetBarras.addValue(renuncias, "Solicitações", "Renúncias");
        datasetBarras.addValue(alteracoes, "Solicitações", "Alterações");
        
        // Criar gráfico de barras
        JFreeChart chartBarras = ChartFactory.createBarChart(
            "", // Título
            "Tipo de Solicitação", // Eixo X
            "Quantidade", // Eixo Y
            datasetBarras
        );
        
        // Personalizar gráfico de barras
        CategoryPlot plotBarras = chartBarras.getCategoryPlot();
        BarRenderer rendererBarras = (BarRenderer) plotBarras.getRenderer();
        rendererBarras.setSeriesPaint(0, new Color(52, 152, 219));
        rendererBarras.setMaximumBarWidth(0.5);
        
        // Adicionar cores individuais
        rendererBarras.setSeriesPaint(0, COR_PRIMARIA);
        
        // Dataset para gráfico de pizza
        DefaultPieDataset datasetPizza = new DefaultPieDataset();
        datasetPizza.setValue("Adesões", adesoes);
        datasetPizza.setValue("Renúncias", renuncias);
        datasetPizza.setValue("Alterações", alteracoes);
        
        // Criar gráfico de pizza
        JFreeChart chartPizza = ChartFactory.createPieChart(
            "", // Título
            datasetPizza,
            true, // Legend
            true,
            false
        );
        
        // Personalizar gráfico de pizza
        PiePlot plotPizza = (PiePlot) chartPizza.getPlot();
        plotPizza.setSectionPaint("Adesões", COR_SUCESSO);
        plotPizza.setSectionPaint("Renúncias", COR_PERIGO);
        plotPizza.setSectionPaint("Alterações", COR_AVISO);
        plotPizza.setLabelGenerator(new StandardPieSectionLabelGenerator(
            "{0}: {1} ({2})",
            new DecimalFormat("0"), 
            new DecimalFormat("0%")
        ));
        plotPizza.setExplodePercent("Adesões", 0.05);
        plotPizza.setExplodePercent("Renúncias", 0.05);
        plotPizza.setExplodePercent("Alterações", 0.05);
        
        // Adicionar gráficos aos painéis
        ChartPanel panelBarras = new ChartPanel(chartBarras);
        panelBarras.setPreferredSize(new Dimension(400, 300));
        panelBarras.setBackground(Color.WHITE);
        
        ChartPanel panelPizza = new ChartPanel(chartPizza);
        panelPizza.setPreferredSize(new Dimension(400, 300));
        panelPizza.setBackground(Color.WHITE);
        
        panelGraficoBarras.add(panelBarras, BorderLayout.CENTER);
        panelGraficoPizza.add(panelPizza, BorderLayout.CENTER);
        
        // Revalidar e repintar
        panelGraficoBarras.revalidate();
        panelGraficoBarras.repaint();
        panelGraficoPizza.revalidate();
        panelGraficoPizza.repaint();
    }
    
    /**
     * Atualiza a tabela de resumo.
     */
        private void atualizarTabela(Integer chaveInicio, Integer chaveFim) {
        modeloTabela.setRowCount(0);
        Set<String> meses = dadosConsolidados.keySet().stream()
            .sorted(Collections.reverseOrder())
            .collect(Collectors.toCollection(LinkedHashSet::new));

        for (String mes : meses) {
            Integer chaveMes = chaveDoMesReferencia(mes);
            if (chaveMes == null) continue;
            if (chaveInicio != null && chaveMes < chaveInicio) continue;
            if (chaveFim != null && chaveMes > chaveFim) continue;

            Map<String, Long> tipos = dadosConsolidados.get(mes);
            long adesoes = tipos.getOrDefault("Adesão", 0L);
            long renuncias = tipos.getOrDefault("Renúncia", 0L);
            long alteracoes = tipos.getOrDefault("Alteração", 0L);
            long total = adesoes + renuncias + alteracoes;

            Object[] row = {mes, adesoes, renuncias, alteracoes, total};
            modeloTabela.addRow(row);
        }
    }
    
    /**
     * Exporta o relatório resumido (respeitando o filtro de mês atual) para Excel.
     */
    private void exportarRelatorio() {
        if (dadosConsolidados == null || dadosConsolidados.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Não há dados para exportar.",
                "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Integer chaveInicio = calcularChavePeriodo(
            (String) cbMesInicio.getSelectedItem(), (String) cbAnoInicio.getSelectedItem(), true);
        Integer chaveFim = calcularChavePeriodo(
            (String) cbMesFim.getSelectedItem(), (String) cbAnoFim.getSelectedItem(), false);

        Map<String, Map<String, Long>> dadosParaExportar = dadosConsolidados;

        if (chaveInicio != null || chaveFim != null) {
            dadosParaExportar = new LinkedHashMap<>();
            for (Map.Entry<String, Map<String, Long>> entry : dadosConsolidados.entrySet()) {
                Integer chaveMes = chaveDoMesReferencia(entry.getKey());
                if (chaveMes == null) continue;
                if (chaveInicio != null && chaveMes < chaveInicio) continue;
                if (chaveFim != null && chaveMes > chaveFim) continue;
                dadosParaExportar.put(entry.getKey(), entry.getValue());
            }
        }

        if (dadosParaExportar.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Não há dados para o período selecionado.",
                "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Salvar Relatório Excel");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Arquivo Excel (*.xlsx)", "xlsx"));
        String nomePadrao = "relatorio_mensal_" +
            java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        fileChooser.setSelectedFile(new File(nomePadrao + ".xlsx"));
        
        int resultado = fileChooser.showSaveDialog(this);
        if (resultado != JFileChooser.APPROVE_OPTION) {
            return;
        }
        
        File arquivo = fileChooser.getSelectedFile();
        String caminho = arquivo.getAbsolutePath();
        if (!caminho.toLowerCase().endsWith(".xlsx")) {
            caminho += ".xlsx";
            arquivo = new File(caminho);
        }
        
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
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            ExcelUtil.exportarRelatorioExcel(dadosParaExportar, arquivo);
            
            // Só registra auditoria após o arquivo ter sido efetivamente gravado com sucesso.
            auditoriaService.registrarExportacao("RELATORIO",
                "Relatório mensal exportado para Excel: " + arquivo.getName());
            
            JOptionPane.showMessageDialog(this,
                "✅ Relatório exportado com sucesso!\n\n" +
                "📁 Arquivo salvo em:\n" + arquivo.getAbsolutePath(),
                "Sucesso", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            LOGGER.error("❌ Erro ao exportar relatório", e);
            JOptionPane.showMessageDialog(this,
                "Erro ao exportar relatório:\n" + e.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }
}
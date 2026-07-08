package com.bilhetagem.view;

import com.bilhetagem.dao.SolicitacaoDAO;
import com.bilhetagem.dao.SolicitacaoDAOImpl;
import com.bilhetagem.model.Solicitacao;
import com.bilhetagem.model.Solicitacao.TipoSolicitacao;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Tela principal do Sistema de Bilhetagem.
 * 
 * <p>Esta classe implementa a interface gráfica principal da aplicação,
 * contendo a tabela de solicitações, filtros de busca, totalizadores
 * e botões de ação.</p>
 * 
 * @author [Seu Nome]
 * @version 1.0.0
 * @since 2026-01-08
 */
public class TelaPrincipal extends JFrame {
    
    private static final Logger LOGGER = LogManager.getLogger(TelaPrincipal.class);
    
    // ===== COMPONENTES DA INTERFACE =====
    private JTable tabelaSolicitacoes;
    private DefaultTableModel modeloTabela;
    private TableRowSorter<DefaultTableModel> sorter;
    
    // Campos de busca
    private JTextField txtBusca;
    private JComboBox<String> cbTipoFiltro;
    private JComboBox<String> cbMesReferencia;
    private JComboBox<String> cbTipoSolicitacao;
    
    // Labels de totalizadores
    private JLabel lblTotalRegistros;
    private JLabel lblTotalAdesoes;
    private JLabel lblTotalRenuncias;
    private JLabel lblTotalAlteracoes;
    
    // DAO
    private SolicitacaoDAO dao;
    
    // Cores do tema
    private static final Color COR_PRIMARIA = new Color(52, 152, 219);
    private static final Color COR_SUCESSO = new Color(46, 204, 113);
    private static final Color COR_PERIGO = new Color(231, 76, 60);
    private static final Color COR_AVISO = new Color(241, 196, 15);
    
    /**
     * Construtor da tela principal.
     * Inicializa todos os componentes e carrega os dados.
     */
    public TelaPrincipal() {
        dao = new SolicitacaoDAOImpl();
        
        configurarJanela();
        criarMenuBar();
        criarPainelPrincipal();
        carregarDados();
        
        LOGGER.info("🖥️ Tela principal inicializada");
    }
    
    /**
     * Configura as propriedades da janela principal.
     */
    private void configurarJanela() {
        setTitle("Sistema de Bilhetagem - Vale Transporte");
        setSize(1280, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        
        // Ícone da aplicação (opcional)
        try {
            setIconImage(Toolkit.getDefaultToolkit().getImage(
                getClass().getResource("/icons/icon.png")));
        } catch (Exception e) {
            LOGGER.warn("Ícone não encontrado, continuando sem ícone");
        }
    }
    
    /**
     * Cria a barra de menu da aplicação.
     */
    private void criarMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // Menu Arquivo
        JMenu menuArquivo = new JMenu("📁 Arquivo");
        JMenuItem itemImportar = new JMenuItem("Importar Excel");
        JMenuItem itemExportar = new JMenuItem("Exportar Excel");
        JMenuItem itemSair = new JMenuItem("Sair");
        
        itemImportar.addActionListener(e -> abrirImportacao());
        itemExportar.addActionListener(e -> abrirExportacao());
        itemSair.addActionListener(e -> sairAplicacao());
        
        menuArquivo.add(itemImportar);
        menuArquivo.add(itemExportar);
        menuArquivo.addSeparator();
        menuArquivo.add(itemSair);
        
        // Menu Relatórios
        JMenu menuRelatorios = new JMenu("📊 Relatórios");
        JMenuItem itemRelatorioMensal = new JMenuItem("Relatório Mensal");
        JMenuItem itemGraficos = new JMenuItem("Gráficos");
        
        itemRelatorioMensal.addActionListener(e -> abrirRelatorioMensal());
        itemGraficos.addActionListener(e -> abrirGraficos());
        
        menuRelatorios.add(itemRelatorioMensal);
        menuRelatorios.add(itemGraficos);
        
        // Menu Ajuda
        JMenu menuAjuda = new JMenu("❓ Ajuda");
        JMenuItem itemSobre = new JMenuItem("Sobre");
        JMenuItem itemManual = new JMenuItem("Manual");
        
        itemSobre.addActionListener(e -> mostrarSobre());
        itemManual.addActionListener(e -> mostrarManual());
        
        menuAjuda.add(itemManual);
        menuAjuda.addSeparator();
        menuAjuda.add(itemSobre);
        
        // Adicionar menus
        menuBar.add(menuArquivo);
        menuBar.add(menuRelatorios);
        menuBar.add(menuAjuda);
        
        setJMenuBar(menuBar);
    }
    
    /**
     * Cria o painel principal com todos os componentes.
     */
    private void criarPainelPrincipal() {
        // Painel superior (busca e filtros)
        JPanel painelSuperior = criarPainelFiltros();
        add(painelSuperior, BorderLayout.NORTH);
        
        // Painel central (tabela)
        JPanel painelCentral = criarPainelTabela();
        add(painelCentral, BorderLayout.CENTER);
        
        // Painel inferior (totalizadores e ações)
        JPanel painelInferior = criarPainelRodape();
        add(painelInferior, BorderLayout.SOUTH);
    }
    
    /**
     * Cria o painel de filtros e busca.
     */
    private JPanel criarPainelFiltros() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(new Color(240, 244, 248));
        
        // Painel de busca
        JPanel painelBusca = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        painelBusca.setOpaque(false);
        
        JLabel lblBuscar = new JLabel("🔍 Buscar:");
        lblBuscar.setFont(new Font("Arial", Font.BOLD, 12));
        
        txtBusca = new JTextField(25);
        txtBusca.setToolTipText("Digite nome, matrícula ou CPF");
        txtBusca.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                aplicarFiltros();
            }
        });
        
        cbTipoFiltro = new JComboBox<>(new String[]{
            "Todos", "Nome", "Matrícula", "CPF"
        });
        cbTipoFiltro.addActionListener(e -> aplicarFiltros());
        
        JButton btnBuscar = new JButton("Buscar");
        btnBuscar.setBackground(COR_PRIMARIA);
        btnBuscar.setForeground(Color.WHITE);
        btnBuscar.addActionListener(e -> aplicarFiltros());
        
        painelBusca.add(lblBuscar);
        painelBusca.add(txtBusca);
        painelBusca.add(cbTipoFiltro);
        painelBusca.add(btnBuscar);
        
        // Painel de filtros
        JPanel painelFiltros = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        painelFiltros.setOpaque(false);
        
        JLabel lblMes = new JLabel("📅 Mês:");
        lblMes.setFont(new Font("Arial", Font.BOLD, 12));
        
        cbMesReferencia = new JComboBox<>();
        cbMesReferencia.addItem("Todos");
        carregarMeses();
        cbMesReferencia.addActionListener(e -> aplicarFiltros());
        
        JLabel lblTipo = new JLabel("📌 Tipo:");
        lblTipo.setFont(new Font("Arial", Font.BOLD, 12));
        
        cbTipoSolicitacao = new JComboBox<>(new String[]{
            "Todos", "Adesão", "Renúncia", "Alteração"
        });
        cbTipoSolicitacao.addActionListener(e -> aplicarFiltros());
        
        JButton btnLimpar = new JButton("Limpar Filtros");
        btnLimpar.setBackground(new Color(149, 165, 166));
        btnLimpar.setForeground(Color.WHITE);
        btnLimpar.addActionListener(e -> limparFiltros());
        
        painelFiltros.add(lblMes);
        painelFiltros.add(cbMesReferencia);
        painelFiltros.add(lblTipo);
        painelFiltros.add(cbTipoSolicitacao);
        painelFiltros.add(btnLimpar);
        
        // Montar painel superior
        panel.add(painelBusca, BorderLayout.WEST);
        panel.add(painelFiltros, BorderLayout.EAST);
        
        return panel;
    }
    
    /**
     * Cria o painel com a tabela de solicitações.
     */
    private JPanel criarPainelTabela() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        
        // Criar modelo da tabela
        String[] colunas = {
            "ID", "Data", "Mês Ref.", "Matrícula", "CPF", "Nome",
            "Nº Cartão", "Qtd. Vales", "Tipo", "Observação"
        };
        
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Tabela não editável
            }
        };
        
        tabelaSolicitacoes = new JTable(modeloTabela);
        tabelaSolicitacoes.setRowHeight(25);
        tabelaSolicitacoes.setFont(new Font("Arial", Font.PLAIN, 12));
        tabelaSolicitacoes.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        tabelaSolicitacoes.getTableHeader().setBackground(COR_PRIMARIA);
        tabelaSolicitacoes.getTableHeader().setForeground(Color.WHITE);
        tabelaSolicitacoes.setSelectionBackground(new Color(187, 222, 251));
        tabelaSolicitacoes.setSelectionForeground(Color.BLACK);
        
        // Configurar largura das colunas
        tabelaSolicitacoes.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        tabelaSolicitacoes.getColumnModel().getColumn(1).setPreferredWidth(80);  // Data
        tabelaSolicitacoes.getColumnModel().getColumn(2).setPreferredWidth(70);  // Mês
        tabelaSolicitacoes.getColumnModel().getColumn(3).setPreferredWidth(80);  // Matrícula
        tabelaSolicitacoes.getColumnModel().getColumn(4).setPreferredWidth(100); // CPF
        tabelaSolicitacoes.getColumnModel().getColumn(5).setPreferredWidth(250); // Nome
        tabelaSolicitacoes.getColumnModel().getColumn(6).setPreferredWidth(100); // Cartão
        tabelaSolicitacoes.getColumnModel().getColumn(7).setPreferredWidth(70);  // Qtd
        tabelaSolicitacoes.getColumnModel().getColumn(8).setPreferredWidth(90);  // Tipo
        tabelaSolicitacoes.getColumnModel().getColumn(9).setPreferredWidth(150); // Observação
        
        // Configurar renderização de cores por tipo
        tabelaSolicitacoes.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (!isSelected) {
                    String tipo = (String) table.getValueAt(row, 8);
                    if ("Adesão".equals(tipo)) {
                        c.setBackground(new Color(232, 245, 233));
                    } else if ("Renúncia".equals(tipo)) {
                        c.setBackground(new Color(253, 235, 236));
                    } else if ("Alteração".equals(tipo)) {
                        c.setBackground(new Color(255, 248, 225));
                    } else {
                        c.setBackground(Color.WHITE);
                    }
                }
                return c;
            }
        });
        
        // Adicionar clique duplo para editar
        tabelaSolicitacoes.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = tabelaSolicitacoes.getSelectedRow();
                    if (row >= 0) {
                        Long id = (Long) modeloTabela.getValueAt(row, 0);
                        editarSolicitacao(id);
                    }
                }
            }
        });
        
        // Configurar sorter
        sorter = new TableRowSorter<>(modeloTabela);
        tabelaSolicitacoes.setRowSorter(sorter);
        
        // Scroll pane
        JScrollPane scrollPane = new JScrollPane(tabelaSolicitacoes);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Cria o painel inferior com totalizadores e botões.
     */
    private JPanel criarPainelRodape() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(new Color(240, 244, 248));
        
        // Totalizadores
        JPanel painelTotalizadores = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
        painelTotalizadores.setOpaque(false);
        
        lblTotalRegistros = criarLabelTotalizador("📊 Total:", "0", Color.BLACK);
        lblTotalAdesoes = criarLabelTotalizador("✅ Adesões:", "0", COR_SUCESSO);
        lblTotalRenuncias = criarLabelTotalizador("❌ Renúncias:", "0", COR_PERIGO);
        lblTotalAlteracoes = criarLabelTotalizador("🔄 Alterações:", "0", COR_AVISO);
        
        painelTotalizadores.add(lblTotalRegistros);
        painelTotalizadores.add(Box.createHorizontalStrut(10));
        painelTotalizadores.add(lblTotalAdesoes);
        painelTotalizadores.add(lblTotalRenuncias);
        painelTotalizadores.add(lblTotalAlteracoes);
        
        // Botões de ação
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        painelBotoes.setOpaque(false);
        
        JButton btnNovo = criarBotao("➕ Novo", COR_PRIMARIA);
        JButton btnEditar = criarBotao("✏️ Editar", new Color(52, 73, 94));
        JButton btnExcluir = criarBotao("🗑️ Excluir", COR_PERIGO);
        JButton btnAtualizar = criarBotao("🔄 Atualizar", new Color(149, 165, 166));
        
        btnNovo.addActionListener(e -> novaSolicitacao());
        btnEditar.addActionListener(e -> editarSolicitacaoSelecionada());
        btnExcluir.addActionListener(e -> excluirSolicitacaoSelecionada());
        btnAtualizar.addActionListener(e -> carregarDados());
        
        painelBotoes.add(btnNovo);
        painelBotoes.add(btnEditar);
        painelBotoes.add(btnExcluir);
        painelBotoes.add(btnAtualizar);
        
        panel.add(painelTotalizadores, BorderLayout.WEST);
        panel.add(painelBotoes, BorderLayout.EAST);
        
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
     * Cria um botão estilizado.
     */
    private JButton criarBotao(String texto, Color cor) {
        JButton btn = new JButton(texto);
        btn.setBackground(cor);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Efeito hover
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(cor.darker());
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(cor);
            }
        });
        
        return btn;
    }
    
    /**
     * Carrega os meses disponíveis para o filtro.
     */
    private void carregarMeses() {
        try {
            List<Solicitacao> lista = dao.listarTodos();
            for (Solicitacao s : lista) {
                String mes = s.getMesReferencia();
                if (mes != null && !mes.isEmpty()) {
                    boolean existe = false;
                    for (int i = 0; i < cbMesReferencia.getItemCount(); i++) {
                        if (mes.equals(cbMesReferencia.getItemAt(i))) {
                            existe = true;
                            break;
                        }
                    }
                    if (!existe) {
                        cbMesReferencia.addItem(mes);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Erro ao carregar meses", e);
        }
    }
    
    /**
     * Carrega os dados na tabela.
     */
    private void carregarDados() {
        try {
            LOGGER.info("🔄 Carregando dados...");
            List<Solicitacao> lista = dao.listarTodos();
            atualizarTabela(lista);
            atualizarTotalizadores(lista);
            LOGGER.info("✅ Carregados {} registros", lista.size());
        } catch (SQLException e) {
            LOGGER.error("❌ Erro ao carregar dados", e);
            JOptionPane.showMessageDialog(this,
                "Erro ao carregar dados: " + e.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Atualiza a tabela com uma lista de solicitações.
     */
    private void atualizarTabela(List<Solicitacao> lista) {
        modeloTabela.setRowCount(0);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        for (Solicitacao s : lista) {
            Object[] row = {
                s.getId(),
                s.getDataEfetivacao() != null ? s.getDataEfetivacao().format(formatter) : "",
                s.getMesReferencia() != null ? s.getMesReferencia() : "",
                s.getMatricula() != null ? s.getMatricula() : "",
                s.getCpf() != null ? s.getCpf() : "",
                s.getNome() != null ? s.getNome() : "",
                s.getNumeroCartao() != null ? s.getNumeroCartao() : "",
                s.getQuantidadeValeTipoA() != null ? s.getQuantidadeValeTipoA() : 0,
                s.getTipoSolicitacao() != null ? s.getTipoSolicitacao().getDescricao() : "",
                s.getObservacao() != null ? s.getObservacao() : ""
            };
            modeloTabela.addRow(row);
        }
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
        
        lblTotalRegistros.setText("📊 Total: " + total);
        lblTotalAdesoes.setText("✅ Adesões: " + adesoes);
        lblTotalRenuncias.setText("❌ Renúncias: " + renuncias);
        lblTotalAlteracoes.setText("🔄 Alterações: " + alteracoes);
    }
    
    /**
     * Aplica os filtros na tabela.
     */
    private void aplicarFiltros() {
        String busca = txtBusca.getText().trim();
        String tipoFiltro = (String) cbTipoFiltro.getSelectedItem();
        String mes = (String) cbMesReferencia.getSelectedItem();
        String tipo = (String) cbTipoSolicitacao.getSelectedItem();
        
        RowFilter<DefaultTableModel, Object> filter = new RowFilter<>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Object> entry) {
                // Filtrar por busca
                if (!busca.isEmpty()) {
                    String valor = "";
                    int coluna = 0;
                    
                    switch (tipoFiltro) {
                        case "Nome":
                            coluna = 5;
                            break;
                        case "Matrícula":
                            coluna = 3;
                            break;
                        case "CPF":
                            coluna = 4;
                            break;
                        default:
                            // Busca em todas as colunas
                            for (int i = 0; i < entry.getValueCount(); i++) {
                                if (entry.getStringValue(i).toLowerCase().contains(busca.toLowerCase())) {
                                    return true;
                                }
                            }
                            return false;
                    }
                    
                    valor = entry.getStringValue(coluna);
                    if (!valor.toLowerCase().contains(busca.toLowerCase())) {
                        return false;
                    }
                }
                
                // Filtrar por mês
                if (mes != null && !mes.equals("Todos")) {
                    String mesTabela = entry.getStringValue(2);
                    if (!mes.equals(mesTabela)) {
                        return false;
                    }
                }
                
                // Filtrar por tipo
                if (tipo != null && !tipo.equals("Todos")) {
                    String tipoTabela = entry.getStringValue(8);
                    if (!tipo.equals(tipoTabela)) {
                        return false;
                    }
                }
                
                return true;
            }
        };
        
        sorter.setRowFilter(filter);
    }
    
    /**
     * Limpa todos os filtros.
     */
    private void limparFiltros() {
        txtBusca.setText("");
        cbTipoFiltro.setSelectedIndex(0);
        cbMesReferencia.setSelectedIndex(0);
        cbTipoSolicitacao.setSelectedIndex(0);
        aplicarFiltros();
    }
    
    /**
     * Abre a tela de nova solicitação.
     */
    private void novaSolicitacao() {
        TelaCadastro tela = new TelaCadastro(this);
        tela.setVisible(true);
        carregarDados(); // Recarregar após fechar
    }
    
    /**
     * Abre a tela de edição da solicitação selecionada.
     */
    private void editarSolicitacaoSelecionada() {
        int row = tabelaSolicitacoes.getSelectedRow();
        if (row >= 0) {
            Long id = (Long) modeloTabela.getValueAt(row, 0);
            editarSolicitacao(id);
        } else {
            JOptionPane.showMessageDialog(this,
                "Selecione uma solicitação para editar.",
                "Aviso", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    /**
     * Abre a tela de edição para uma solicitação específica.
     */
    private void editarSolicitacao(Long id) {
        try {
            var optional = dao.buscarPorId(id);
            if (optional.isPresent()) {
                TelaCadastro tela = new TelaCadastro(this, optional.get());
                tela.setVisible(true);
                carregarDados(); // Recarregar após fechar
            } else {
                JOptionPane.showMessageDialog(this,
                    "Solicitação não encontrada.",
                    "Erro", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            LOGGER.error("Erro ao buscar solicitação para edição", e);
            JOptionPane.showMessageDialog(this,
                "Erro ao carregar solicitação: " + e.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Exclui a solicitação selecionada.
     */
    private void excluirSolicitacaoSelecionada() {
        int row = tabelaSolicitacoes.getSelectedRow();
        if (row >= 0) {
            Long id = (Long) modeloTabela.getValueAt(row, 0);
            String nome = (String) modeloTabela.getValueAt(row, 5);
            
            int confirm = JOptionPane.showConfirmDialog(this,
                "Deseja realmente excluir a solicitação de\n" + nome + " (ID: " + id + ")?",
                "Confirmar Exclusão",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    if (dao.excluir(id)) {
                        JOptionPane.showMessageDialog(this,
                            "Solicitação excluída com sucesso!",
                            "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                        carregarDados();
                    } else {
                        JOptionPane.showMessageDialog(this,
                            "Erro ao excluir solicitação.",
                            "Erro", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException e) {
                    LOGGER.error("Erro ao excluir solicitação", e);
                    JOptionPane.showMessageDialog(this,
                        "Erro ao excluir: " + e.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this,
                "Selecione uma solicitação para excluir.",
                "Aviso", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    /**
     * Abre a tela de importação.
     */
    private void abrirImportacao() {
        SwingUtilities.invokeLater(() -> {
            TelaImportacao tela = new TelaImportacao(this);
            tela.setVisible(true);
            carregarDados(); // Recarregar dados após importação
        });
    }
    
    /**
     * Abre a tela de exportação.
     */
    private void abrirExportacao() {
        SwingUtilities.invokeLater(() -> {
            TelaExportacao tela = new TelaExportacao(this);
            tela.setVisible(true);
        });
    }
    
    /**
     * Abre o relatório mensal.
     */
    private void abrirRelatorioMensal() {
        SwingUtilities.invokeLater(() -> {
            TelaRelatorio tela = new TelaRelatorio();
            tela.setVisible(true);
        });
    }
    
    /**
     * Abre os gráficos.
     */
    private void abrirGraficos() {
        abrirRelatorioMensal();
        }
    
    
    /**
     * Mostra a tela sobre.
     */
    private void mostrarSobre() {
        JOptionPane.showMessageDialog(this,
            "Sistema de Bilhetagem - Vale Transporte\n" +
            "Versão: 1.0.0-SNAPSHOT\n" +
            "Desenvolvido em Java 17+\n" +
            "© 2026 - Todos os direitos reservados",
            "Sobre", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Mostra o manual do usuário.
     */
    private void mostrarManual() {
        JOptionPane.showMessageDialog(this,
            "📖 Manual do Sistema\n\n" +
            "🔍 Busca: Digite nome, matrícula ou CPF\n" +
            "📅 Filtros: Selecione mês e tipo\n" +
            "➕ Novo: Cadastrar nova solicitação\n" +
            "✏️ Editar: Duplo clique na linha\n" +
            "🗑️ Excluir: Selecione e clique em excluir\n" +
            "📊 Relatórios: Menu Relatórios",
            "Manual", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Sai da aplicação.
     */
    private void sairAplicacao() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Deseja realmente sair?",
            "Confirmar Saída",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }
}
package com.bilhetagem.view;

import com.bilhetagem.dao.SolicitacaoDAO;
import com.bilhetagem.dao.SolicitacaoDAOImpl;
import com.bilhetagem.model.Solicitacao;
import com.bilhetagem.model.Solicitacao.TipoSolicitacao;
import com.bilhetagem.model.Usuario.Permissao;
import com.bilhetagem.service.AuditoriaService;
import com.bilhetagem.util.SessaoUtil;
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
 * e botões de ação com controle de permissões.</p>
 * 
 * @author Equipe de Desenvolvimento
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
    
    // Botões
    private JButton btnNovo;
    private JButton btnEditar;
    private JButton btnExcluir;
    private JButton btnImportar;
    private JButton btnExportar;
    private JButton btnAtualizar;
    
    // DAO e Serviços
    private SolicitacaoDAO dao;
    private AuditoriaService auditoriaService;
    
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
        auditoriaService = new AuditoriaService();
        
        configurarJanela();
        criarMenuBar();
        criarPainelPrincipal();
        carregarDados();
        atualizarPermissoesUI();
        verificarSessao();
        
        LOGGER.info("🖥️ Tela principal inicializada");
    }
    
    /**
     * Atualiza a interface baseado nas permissões do usuário.
     */
    private void atualizarPermissoesUI() {
        btnNovo.setEnabled(SessaoUtil.temPermissao(Permissao.CADASTRAR_SOLICITACAO));
        btnEditar.setEnabled(SessaoUtil.temPermissao(Permissao.EDITAR_SOLICITACAO));
        btnExcluir.setEnabled(SessaoUtil.temPermissao(Permissao.EXCLUIR_SOLICITACAO));
        btnImportar.setEnabled(SessaoUtil.temPermissao(Permissao.IMPORTAR_DADOS));
        btnExportar.setEnabled(SessaoUtil.temPermissao(Permissao.EXPORTAR_DADOS));
        btnAtualizar.setEnabled(SessaoUtil.temPermissao(Permissao.CONSULTAR_SOLICITACAO));
    }
    
    /**
     * Verifica se a sessão ainda é válida.
     */
    private void verificarSessao() {
        if (SessaoUtil.sessaoExpirada()) {
            JOptionPane.showMessageDialog(this,
                "Sua sessão expirou. Faça login novamente.",
                "Sessão Expirada",
                JOptionPane.WARNING_MESSAGE);
            SessaoUtil.encerrarSessao();
            dispose();
            SwingUtilities.invokeLater(() -> {
                TelaLogin tela = new TelaLogin();
                tela.setVisible(true);
            });
        }
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
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (SessaoUtil.isLogado()) {
                    auditoriaService.registrarLogout(SessaoUtil.getUsuarioLogado());
                    SessaoUtil.encerrarSessao();
                }
            }
        });
        
        try {
            setIconImage(Toolkit.getDefaultToolkit().getImage(
                getClass().getResource("/icons/icon.png")));
        } catch (Exception e) {
            LOGGER.warn("Ícone não encontrado, continuando sem ícone");
        }
    }
    
    /**
     * Cria a barra de menu da aplicação com controle de permissões.
     */
    private void criarMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // Menu Arquivo
        JMenu menuArquivo = new JMenu("📁 Arquivo");
        
        JMenuItem itemImportar = new JMenuItem("Importar Excel");
        itemImportar.setEnabled(SessaoUtil.temPermissao(Permissao.IMPORTAR_DADOS));
        itemImportar.addActionListener(e -> abrirImportacao());
        
        JMenuItem itemExportar = new JMenuItem("Exportar Excel");
        itemExportar.setEnabled(SessaoUtil.temPermissao(Permissao.EXPORTAR_DADOS));
        itemExportar.addActionListener(e -> abrirExportacao());
        
        JMenuItem itemSair = new JMenuItem("Sair");
        itemSair.addActionListener(e -> sairAplicacao());
        
        menuArquivo.add(itemImportar);
        menuArquivo.add(itemExportar);
        menuArquivo.addSeparator();
        menuArquivo.add(itemSair);
        
        // Menu Relatórios
        JMenu menuRelatorios = new JMenu("📊 Relatórios");
        
        JMenuItem itemRelatorioMensal = new JMenuItem("Relatório Mensal");
        itemRelatorioMensal.setEnabled(SessaoUtil.temPermissao(Permissao.GERAR_RELATORIOS));
        itemRelatorioMensal.addActionListener(e -> abrirRelatorioMensal());
        
        JMenuItem itemGraficos = new JMenuItem("Gráficos");
        itemGraficos.setEnabled(SessaoUtil.temPermissao(Permissao.GERAR_RELATORIOS));
        itemGraficos.addActionListener(e -> abrirGraficos());
        
        menuRelatorios.add(itemRelatorioMensal);
        menuRelatorios.add(itemGraficos);
        
        // Menu Auditoria
        JMenu menuAuditoria = new JMenu("🔍 Auditoria");
        JMenuItem itemAuditoria = new JMenuItem("Consultar Logs");
        itemAuditoria.setEnabled(SessaoUtil.temPermissao(Permissao.VER_AUDITORIA));
        itemAuditoria.addActionListener(e -> abrirAuditoria());
        menuAuditoria.add(itemAuditoria);
        
        // Menu Usuário
        JMenu menuUsuario = new JMenu("👤 Usuário");
        JMenuItem itemInfo = new JMenuItem("Informações");
        JMenuItem itemLogout = new JMenuItem("Logout");
        
        itemInfo.addActionListener(e -> mostrarInfoUsuario());
        itemLogout.addActionListener(e -> fazerLogout());
        
        menuUsuario.add(itemInfo);
        menuUsuario.addSeparator();
        menuUsuario.add(itemLogout);
        
        // Menu Ajuda
        JMenu menuAjuda = new JMenu("❓ Ajuda");
        JMenuItem itemSobre = new JMenuItem("Sobre");
        JMenuItem itemManual = new JMenuItem("Manual");
        
        itemSobre.addActionListener(e -> mostrarSobre());
        itemManual.addActionListener(e -> mostrarManual());
        
        menuAjuda.add(itemManual);
        menuAjuda.addSeparator();
        menuAjuda.add(itemSobre);
        
        menuBar.add(menuArquivo);
        menuBar.add(menuRelatorios);
        if (SessaoUtil.temPermissao(Permissao.VER_AUDITORIA)) {
            menuBar.add(menuAuditoria);
        }
        menuBar.add(menuUsuario);
        menuBar.add(menuAjuda);
        
        setJMenuBar(menuBar);
    }
    
    /**
     * Cria o painel principal com todos os componentes.
     */
    private void criarPainelPrincipal() {
        JPanel painelSuperior = criarPainelFiltros();
        add(painelSuperior, BorderLayout.NORTH);
        
        JPanel painelCentral = criarPainelTabela();
        add(painelCentral, BorderLayout.CENTER);
        
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
        
        String[] colunas = {
            "ID", "Data", "Mês Ref.", "Matrícula", "CPF", "Nome",
            "Nº Cartão", "Qtd. Vales", "Tipo", "Observação"
        };
        
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
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
        
        tabelaSolicitacoes.getColumnModel().getColumn(0).setPreferredWidth(50);
        tabelaSolicitacoes.getColumnModel().getColumn(1).setPreferredWidth(80);
        tabelaSolicitacoes.getColumnModel().getColumn(2).setPreferredWidth(70);
        tabelaSolicitacoes.getColumnModel().getColumn(3).setPreferredWidth(80);
        tabelaSolicitacoes.getColumnModel().getColumn(4).setPreferredWidth(100);
        tabelaSolicitacoes.getColumnModel().getColumn(5).setPreferredWidth(250);
        tabelaSolicitacoes.getColumnModel().getColumn(6).setPreferredWidth(100);
        tabelaSolicitacoes.getColumnModel().getColumn(7).setPreferredWidth(70);
        tabelaSolicitacoes.getColumnModel().getColumn(8).setPreferredWidth(90);
        tabelaSolicitacoes.getColumnModel().getColumn(9).setPreferredWidth(150);
        
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
        
        tabelaSolicitacoes.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    if (!SessaoUtil.temPermissao(Permissao.EDITAR_SOLICITACAO)) {
                        JOptionPane.showMessageDialog(TelaPrincipal.this,
                            "Você não tem permissão para editar solicitações.",
                            "Acesso Negado", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    int row = tabelaSolicitacoes.getSelectedRow();
                    if (row >= 0) {
                        Long id = (Long) modeloTabela.getValueAt(row, 0);
                        editarSolicitacao(id);
                    }
                }
            }
        });
        
        sorter = new TableRowSorter<>(modeloTabela);
        tabelaSolicitacoes.setRowSorter(sorter);
        
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
        
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        painelBotoes.setOpaque(false);
        
        btnNovo = criarBotao("➕ Novo", COR_PRIMARIA);
        btnEditar = criarBotao("✏️ Editar", new Color(52, 73, 94));
        btnExcluir = criarBotao("🗑️ Excluir", COR_PERIGO);
        btnImportar = criarBotao("📥 Importar", new Color(46, 204, 113));
        btnExportar = criarBotao("📤 Exportar", new Color(155, 89, 182));
        btnAtualizar = criarBotao("🔄 Atualizar", new Color(149, 165, 166));
        
        btnNovo.addActionListener(e -> novaSolicitacao());
        btnEditar.addActionListener(e -> editarSolicitacaoSelecionada());
        btnExcluir.addActionListener(e -> excluirSolicitacaoSelecionada());
        btnImportar.addActionListener(e -> abrirImportacao());
        btnExportar.addActionListener(e -> abrirExportacao());
        btnAtualizar.addActionListener(e -> carregarDados());
        
        painelBotoes.add(btnNovo);
        painelBotoes.add(btnEditar);
        painelBotoes.add(btnExcluir);
        painelBotoes.add(btnImportar);
        painelBotoes.add(btnExportar);
        painelBotoes.add(btnAtualizar);
        
        panel.add(painelTotalizadores, BorderLayout.WEST);
        panel.add(painelBotoes, BorderLayout.EAST);
        
        return panel;
    }
    
    private JLabel criarLabelTotalizador(String label, String valor, Color cor) {
        JLabel lbl = new JLabel(label + " " + valor);
        lbl.setFont(new Font("Arial", Font.BOLD, 13));
        lbl.setForeground(cor);
        return lbl;
    }
    
    private JButton criarBotao(String texto, Color cor) {
        JButton btn = new JButton(texto);
        btn.setBackground(cor);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
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
    
    private void carregarDados() {
        try {
            LOGGER.info("🔄 Carregando dados...");
            List<Solicitacao> lista = dao.listarTodos();
            atualizarTabela(lista);
            atualizarTotalizadores(lista);
            LOGGER.info("✅ Carregados {} registros", lista.size());
            
            if (SessaoUtil.temPermissao(Permissao.CONSULTAR_SOLICITACAO)) {
                auditoriaService.registrarConsulta("SOLICITACAO", null, 
                    "Consulta geral: " + lista.size() + " registros");
            }
            
        } catch (SQLException e) {
            LOGGER.error("❌ Erro ao carregar dados", e);
            JOptionPane.showMessageDialog(this,
                "Erro ao carregar dados: " + e.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
    
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
    
    private void aplicarFiltros() {
        String busca = txtBusca.getText().trim();
        String tipoFiltro = (String) cbTipoFiltro.getSelectedItem();
        String mes = (String) cbMesReferencia.getSelectedItem();
        String tipo = (String) cbTipoSolicitacao.getSelectedItem();
        
        RowFilter<DefaultTableModel, Object> filter = new RowFilter<>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Object> entry) {
                if (!busca.isEmpty()) {
                    String valor = "";
                    int coluna = 0;
                    
                    switch (tipoFiltro) {
                        case "Nome": coluna = 5; break;
                        case "Matrícula": coluna = 3; break;
                        case "CPF": coluna = 4; break;
                        default:
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
                
                if (mes != null && !mes.equals("Todos")) {
                    String mesTabela = entry.getStringValue(2);
                    if (!mes.equals(mesTabela)) {
                        return false;
                    }
                }
                
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
    
    private void limparFiltros() {
        txtBusca.setText("");
        cbTipoFiltro.setSelectedIndex(0);
        cbMesReferencia.setSelectedIndex(0);
        cbTipoSolicitacao.setSelectedIndex(0);
        aplicarFiltros();
    }
    
    // ===== MÉTODOS DE AÇÃO =====
    
    private void novaSolicitacao() {
        try {
            SessaoUtil.verificarPermissao(Permissao.CADASTRAR_SOLICITACAO);
            TelaCadastro tela = new TelaCadastro(this);
            tela.setVisible(true);
            carregarDados();
            auditoriaService.registrarCriacao("SOLICITACAO", null, "Nova solicitação criada");
        } catch (SecurityException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Acesso Negado", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void editarSolicitacaoSelecionada() {
        try {
            SessaoUtil.verificarPermissao(Permissao.EDITAR_SOLICITACAO);
            int row = tabelaSolicitacoes.getSelectedRow();
            if (row >= 0) {
                Long id = (Long) modeloTabela.getValueAt(row, 0);
                editarSolicitacao(id);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Selecione uma solicitação para editar.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            }
        } catch (SecurityException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Acesso Negado", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void editarSolicitacao(Long id) {
        try {
            SessaoUtil.verificarPermissao(Permissao.EDITAR_SOLICITACAO);
            var optional = dao.buscarPorId(id);
            if (optional.isPresent()) {
                TelaCadastro tela = new TelaCadastro(this, optional.get());
                tela.setVisible(true);
                carregarDados();
                auditoriaService.registrarAtualizacao("SOLICITACAO", id, 
                    "Solicitação editada: " + optional.get().getNome());
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
        } catch (SecurityException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Acesso Negado", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Exclui a solicitação selecionada (soft delete).
     */
    private void excluirSolicitacaoSelecionada() {
        try {
            SessaoUtil.verificarPermissao(Permissao.EXCLUIR_SOLICITACAO);
            int row = tabelaSolicitacoes.getSelectedRow();
            if (row >= 0) {
                Long id = (Long) modeloTabela.getValueAt(row, 0);
                String nome = (String) modeloTabela.getValueAt(row, 5);
                
                int confirm = JOptionPane.showConfirmDialog(this,
                    "Deseja realmente excluir a solicitação de\n" + nome + " (ID: " + id + ")?\n\n" +
                    "A solicitação será movida para a lixeira e poderá ser restaurada.",
                    "Confirmar Exclusão",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    if (dao.excluir(id)) {
                        auditoriaService.registrarExclusao("SOLICITACAO", id, 
                            "Solicitação movida para lixeira: " + nome);
                        JOptionPane.showMessageDialog(this,
                            "Solicitação movida para lixeira!\n" +
                            "Use 'Restaurar' para recuperar.",
                            "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                        carregarDados();
                    } else {
                        JOptionPane.showMessageDialog(this,
                            "Erro ao excluir solicitação.",
                            "Erro", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this,
                    "Selecione uma solicitação para excluir.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            }
        } catch (SecurityException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Acesso Negado", 
                JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            LOGGER.error("Erro ao excluir solicitação", e);
            JOptionPane.showMessageDialog(this,
                "Erro ao excluir: " + e.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void abrirImportacao() {
        try {
            SessaoUtil.verificarPermissao(Permissao.IMPORTAR_DADOS);
            SwingUtilities.invokeLater(() -> {
                TelaImportacao tela = new TelaImportacao(this);
                tela.setVisible(true);
                carregarDados();
                auditoriaService.registrarImportacao("SOLICITACAO", "Importação de dados do Excel");
            });
        } catch (SecurityException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Acesso Negado", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void abrirExportacao() {
        try {
            SessaoUtil.verificarPermissao(Permissao.EXPORTAR_DADOS);
            SwingUtilities.invokeLater(() -> {
                TelaExportacao tela = new TelaExportacao(this);
                tela.setVisible(true);
                auditoriaService.registrarExportacao("SOLICITACAO", "Exportação de dados para Excel");
            });
        } catch (SecurityException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Acesso Negado", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void abrirRelatorioMensal() {
        try {
            SessaoUtil.verificarPermissao(Permissao.GERAR_RELATORIOS);
            SwingUtilities.invokeLater(() -> {
                TelaRelatorio tela = new TelaRelatorio();
                tela.setVisible(true);
                auditoriaService.registrarConsulta("RELATORIO", null, "Relatório mensal acessado");
            });
        } catch (SecurityException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Acesso Negado", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void abrirGraficos() {
        abrirRelatorioMensal();
    }
    
    private void abrirAuditoria() {
        try {
            SessaoUtil.verificarPermissao(Permissao.VER_AUDITORIA);
            SwingUtilities.invokeLater(() -> {
                TelaAuditoria tela = new TelaAuditoria(this);
                tela.setVisible(true);
            });
        } catch (SecurityException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Acesso Negado", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // ===== MÉTODOS DE UTILITÁRIO =====
    
    private void mostrarInfoUsuario() {
        var usuario = SessaoUtil.getUsuarioLogado();
        if (usuario != null) {
            String permissoes = usuario.getPermissoes().stream()
                .map(Permissao::getDescricao)
                .reduce((a, b) -> a + ", " + b)
                .orElse("Nenhuma");
            
            JOptionPane.showMessageDialog(this,
                "👤 Informações do Usuário\n\n" +
                "Nome: " + usuario.getNome() + "\n" +
                "Login: " + usuario.getLogin() + "\n" +
                "Email: " + (usuario.getEmail() != null ? usuario.getEmail() : "Não informado") + "\n" +
                "Perfil: " + usuario.getPerfil().getDescricao() + "\n" +
                "Permissões: " + permissoes + "\n" +
                "Tempo restante: " + SessaoUtil.getTempoRestanteSessaoMinutos() + " minutos",
                "Informações do Usuário",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void fazerLogout() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Deseja realmente sair do sistema?",
            "Confirmar Logout",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (SessaoUtil.isLogado()) {
                auditoriaService.registrarLogout(SessaoUtil.getUsuarioLogado());
                SessaoUtil.encerrarSessao();
            }
            dispose();
            SwingUtilities.invokeLater(() -> {
                TelaLogin tela = new TelaLogin();
                tela.setVisible(true);
            });
        }
    }
    
    private void mostrarSobre() {
        JOptionPane.showMessageDialog(this,
            "Sistema de Bilhetagem - Vale Transporte\n" +
            "Versão: 1.0.0-SNAPSHOT\n" +
            "Desenvolvido em Java 17+\n" +
            "© 2026 - Todos os direitos reservados",
            "Sobre", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void mostrarManual() {
        JOptionPane.showMessageDialog(this,
            "📖 Manual do Sistema\n\n" +
            "🔍 Busca: Digite nome, matrícula ou CPF\n" +
            "📅 Filtros: Selecione mês e tipo\n" +
            "➕ Novo: Cadastrar nova solicitação\n" +
            "✏️ Editar: Duplo clique na linha\n" +
            "🗑️ Excluir: Selecione e clique em excluir\n" +
            "📊 Relatórios: Menu Relatórios\n" +
            "🔍 Auditoria: Menu Auditoria (ADMIN/GERENTE)\n" +
            "👤 Usuário: Menu Usuário para informações e logout",
            "Manual", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void sairAplicacao() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Deseja realmente sair?",
            "Confirmar Saída",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (SessaoUtil.isLogado()) {
                auditoriaService.registrarLogout(SessaoUtil.getUsuarioLogado());
                SessaoUtil.encerrarSessao();
            }
            System.exit(0);
        }
    }
}
package com.bilhetagem.view;

import com.bilhetagem.dao.LogAuditoriaDAO;
import com.bilhetagem.dao.LogAuditoriaDAOImpl;
import com.bilhetagem.model.LogAuditoria;
import com.bilhetagem.model.Usuario.Permissao;
import com.bilhetagem.util.SessaoUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Tela de consulta de auditoria.
 * 
 * @author Equipe de desenvolvimento
 * @version 1.0.0
 * @since 2026-01-09
 */
public class TelaAuditoria extends JDialog {
    
    private static final Logger LOGGER = LogManager.getLogger(TelaAuditoria.class);
    
    private JTable tabelaLogs;
    private DefaultTableModel modeloTabela;
    private JComboBox<String> cbFiltro;
    private JTextField txtBusca;
    private JButton btnBuscar;
    private JButton btnAtualizar;
    
    private LogAuditoriaDAO logDAO;
    private JFrame parent;
    
    public TelaAuditoria(JFrame parent) {
        super(parent, "🔍 Auditoria - Logs do Sistema", true);
        this.parent = parent;
        this.logDAO = new LogAuditoriaDAOImpl();
        
        // Verificar permissão
        if (!SessaoUtil.temPermissao(Permissao.VER_AUDITORIA)) {
            JOptionPane.showMessageDialog(this,
                "Você não tem permissão para acessar a auditoria.",
                "Acesso Negado", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }
        
        configurarJanela();
        criarComponentes();
        carregarLogs();
    }
    
    private void configurarJanela() {
        setSize(1000, 600);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
    }
    
    private void criarComponentes() {
        // Painel de filtros
        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panelFiltros.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        cbFiltro = new JComboBox<>(new String[]{
            "Todos", "LOGIN", "LOGOUT", "CRIACAO", "ATUALIZACAO", 
            "EXCLUSAO", "CONSULTA", "EXPORTACAO", "IMPORTACAO"
        });
        
        txtBusca = new JTextField(20);
        txtBusca.setToolTipText("Buscar por usuário ou detalhes");
        
        btnBuscar = new JButton("🔍 Buscar");
        btnBuscar.addActionListener(e -> buscarLogs());
        
        btnAtualizar = new JButton("🔄 Atualizar");
        btnAtualizar.addActionListener(e -> carregarLogs());
        
        panelFiltros.add(new JLabel("Filtro:"));
        panelFiltros.add(cbFiltro);
        panelFiltros.add(new JLabel("Busca:"));
        panelFiltros.add(txtBusca);
        panelFiltros.add(btnBuscar);
        panelFiltros.add(btnAtualizar);
        
        add(panelFiltros, BorderLayout.NORTH);
        
        // Tabela de logs
        String[] colunas = {"ID", "Usuário", "Ação", "Entidade", "Detalhes", "Data/Hora"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tabelaLogs = new JTable(modeloTabela);
        tabelaLogs.setRowHeight(25);
        tabelaLogs.setFont(new Font("Arial", Font.PLAIN, 12));
        tabelaLogs.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        
        // Configurar largura das colunas
        tabelaLogs.getColumnModel().getColumn(0).setPreferredWidth(50);
        tabelaLogs.getColumnModel().getColumn(1).setPreferredWidth(100);
        tabelaLogs.getColumnModel().getColumn(2).setPreferredWidth(120);
        tabelaLogs.getColumnModel().getColumn(3).setPreferredWidth(100);
        tabelaLogs.getColumnModel().getColumn(4).setPreferredWidth(300);
        tabelaLogs.getColumnModel().getColumn(5).setPreferredWidth(150);
        
        JScrollPane scrollPane = new JScrollPane(tabelaLogs);
        scrollPane.setBorder(BorderFactory.createTitledBorder("📋 Logs de Auditoria"));
        add(scrollPane, BorderLayout.CENTER);
        
        // Painel inferior
        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnFechar = new JButton("❌ Fechar");
        btnFechar.addActionListener(e -> dispose());
        panelInferior.add(btnFechar);
        add(panelInferior, BorderLayout.SOUTH);
    }
    
    private void carregarLogs() {
        try {
            List<LogAuditoria> logs = logDAO.listarUltimos(100);
            atualizarTabela(logs);
        } catch (SQLException e) {
            LOGGER.error("Erro ao carregar logs", e);
            JOptionPane.showMessageDialog(this,
                "Erro ao carregar logs: " + e.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void buscarLogs() {
        String filtro = (String) cbFiltro.getSelectedItem();
        String busca = txtBusca.getText().trim();
        
        try {
            List<LogAuditoria> logs;
            
            if (!filtro.equals("Todos") && !busca.isEmpty()) {
                // Buscar por ação e filtrar por texto
                logs = logDAO.listarPorAcao(filtro);
                logs = logs.stream()
                    .filter(l -> l.getUsuarioId().toString().contains(busca) ||
                                (l.getDetalhes() != null && l.getDetalhes().toLowerCase().contains(busca.toLowerCase())))
                    .toList();
            } else if (!filtro.equals("Todos")) {
                logs = logDAO.listarPorAcao(filtro);
            } else if (!busca.isEmpty()) {
                logs = logDAO.listarUltimos(1000);
                logs = logs.stream()
                    .filter(l -> l.getUsuarioId().toString().contains(busca) ||
                                (l.getDetalhes() != null && l.getDetalhes().toLowerCase().contains(busca.toLowerCase())))
                    .toList();
            } else {
                logs = logDAO.listarUltimos(100);
            }
            
            atualizarTabela(logs);
            
        } catch (SQLException e) {
            LOGGER.error("Erro ao buscar logs", e);
            JOptionPane.showMessageDialog(this,
                "Erro ao buscar logs: " + e.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void atualizarTabela(List<LogAuditoria> logs) {
        modeloTabela.setRowCount(0);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        
        for (LogAuditoria log : logs) {
            Object[] row = {
                log.getId(),
                log.getUsuarioId(),
                log.getAcao(),
                log.getEntidade(),
                log.getDetalhes(),
                log.getDataHora() != null ? log.getDataHora().format(formatter) : ""
            };
            modeloTabela.addRow(row);
        }
    }
}
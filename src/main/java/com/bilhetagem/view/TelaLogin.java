package com.bilhetagem.view;

import com.bilhetagem.dao.UsuarioDAO;
import com.bilhetagem.dao.UsuarioDAOImpl;
import com.bilhetagem.model.Usuario;
import com.bilhetagem.service.AuditoriaService;
import com.bilhetagem.util.SessaoUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

/**
 * Tela de login do sistema.
 * 
 * @author [Seu Nome]
 * @version 1.0.0
 * @since 2026-01-08
 */
public class TelaLogin extends JFrame {
    
    private static final Logger LOGGER = LogManager.getLogger(TelaLogin.class);
    
    private JTextField txtLogin;
    private JPasswordField txtSenha;
    private JButton btnLogin;
    private JButton btnCancelar;
    private UsuarioDAO usuarioDAO;
    private AuditoriaService auditoriaService;
    
    private static final Color COR_PRIMARIA = new Color(52, 152, 219);
    
    public TelaLogin() {
        usuarioDAO = new UsuarioDAOImpl();
        auditoriaService = new AuditoriaService();
        configurarJanela();
        criarComponentes();
        LOGGER.info("🔐 Tela de login inicializada");
    }
    
    private void configurarJanela() {
        setTitle("🔐 Sistema de Bilhetagem - Login");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout());
    }
    
    private void criarComponentes() {
        // Painel principal
        JPanel panelPrincipal = new JPanel(new GridBagLayout());
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        panelPrincipal.setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Logo/Título
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel lblTitulo = new JLabel("🚌 Bilhetagem VT");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitulo.setForeground(COR_PRIMARIA);
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        panelPrincipal.add(lblTitulo, gbc);
        
        gbc.gridy = 1;
        JLabel lblSubtitulo = new JLabel("Sistema de Gerenciamento de Vale Transporte");
        lblSubtitulo.setFont(new Font("Arial", Font.PLAIN, 12));
        lblSubtitulo.setForeground(Color.GRAY);
        lblSubtitulo.setHorizontalAlignment(SwingConstants.CENTER);
        panelPrincipal.add(lblSubtitulo, gbc);
        
        // Separador
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        JSeparator separator = new JSeparator();
        panelPrincipal.add(separator, gbc);
        gbc.gridwidth = 1;
        
        // Login
        gbc.gridy = 3;
        gbc.gridx = 0;
        JLabel lblLogin = new JLabel("Usuário:");
        lblLogin.setFont(new Font("Arial", Font.BOLD, 12));
        panelPrincipal.add(lblLogin, gbc);
        
        gbc.gridx = 1;
        txtLogin = new JTextField(15);
        txtLogin.setFont(new Font("Arial", Font.PLAIN, 12));
        txtLogin.setPreferredSize(new Dimension(200, 30));
        txtLogin.addActionListener(e -> realizarLogin());
        panelPrincipal.add(txtLogin, gbc);
        
        // Senha
        gbc.gridy = 4;
        gbc.gridx = 0;
        JLabel lblSenha = new JLabel("Senha:");
        lblSenha.setFont(new Font("Arial", Font.BOLD, 12));
        panelPrincipal.add(lblSenha, gbc);
        
        gbc.gridx = 1;
        txtSenha = new JPasswordField(15);
        txtSenha.setFont(new Font("Arial", Font.PLAIN, 12));
        txtSenha.setPreferredSize(new Dimension(200, 30));
        txtSenha.addActionListener(e -> realizarLogin());
        panelPrincipal.add(txtSenha, gbc);
        
        // Botões
        gbc.gridy = 5;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JPanel panelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        panelBotoes.setOpaque(false);
        
        btnLogin = new JButton("🔑 Entrar");
        btnLogin.setBackground(COR_PRIMARIA);
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(new Font("Arial", Font.BOLD, 12));
        btnLogin.setPreferredSize(new Dimension(120, 35));
        btnLogin.addActionListener(e -> realizarLogin());
        
        btnCancelar = new JButton("❌ Cancelar");
        btnCancelar.setBackground(new Color(149, 165, 166));
        btnCancelar.setForeground(Color.WHITE);
        btnCancelar.setFont(new Font("Arial", Font.BOLD, 12));
        btnCancelar.setPreferredSize(new Dimension(120, 35));
        btnCancelar.addActionListener(e -> System.exit(0));
        
        panelBotoes.add(btnLogin);
        panelBotoes.add(btnCancelar);
        panelPrincipal.add(panelBotoes, gbc);
        
        // Versão
        gbc.gridy = 6;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JLabel lblVersao = new JLabel("Versão 1.0.0");
        lblVersao.setFont(new Font("Arial", Font.ITALIC, 10));
        lblVersao.setForeground(Color.GRAY);
        lblVersao.setHorizontalAlignment(SwingConstants.CENTER);
        panelPrincipal.add(lblVersao, gbc);
        
        add(panelPrincipal, BorderLayout.CENTER);
        
        // Foco no campo login
        txtLogin.requestFocus();
    }
    
    private void realizarLogin() {
        String login = txtLogin.getText().trim();
        String senha = new String(txtSenha.getPassword());
        
        if (login.isEmpty() || senha.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Preencha todos os campos!",
                "Campos Obrigatórios",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            btnLogin.setEnabled(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            
            // Autenticar
            boolean autenticado = usuarioDAO.autenticar(login, senha);
            
            if (autenticado) {
                var optional = usuarioDAO.buscarPorLogin(login);
                if (optional.isPresent()) {
                    Usuario usuario = optional.get();
                    
                    // Criar sessão
                    SessaoUtil.iniciarSessao(usuario);
                    
                    // Atualizar último acesso
                    usuarioDAO.atualizarUltimoAcesso(login);
                    
                    // Registrar login na auditoria
                    auditoriaService.registrarLogin(usuario);
                    
                    LOGGER.info("✅ Login realizado: {}", login);
                    
                    // Abrir tela principal
                    JOptionPane.showMessageDialog(this,
                        "Bem-vindo, " + usuario.getNome() + "!",
                        "Login Realizado",
                        JOptionPane.INFORMATION_MESSAGE);
                    
                    dispose();
                    SwingUtilities.invokeLater(() -> {
                        TelaPrincipal tela = new TelaPrincipal();
                        tela.setVisible(true);
                    });
                }
            } else {
                LOGGER.warn("⚠️ Tentativa de login falha: {}", login);
                JOptionPane.showMessageDialog(this,
                    "Usuário ou senha inválidos!",
                    "Erro de Autenticação",
                    JOptionPane.ERROR_MESSAGE);
                txtSenha.setText("");
                txtSenha.requestFocus();
            }
            
        } catch (SQLException e) {
            LOGGER.error("Erro ao autenticar", e);
            JOptionPane.showMessageDialog(this,
                "Erro ao conectar ao banco de dados!\n" + e.getMessage(),
                "Erro",
                JOptionPane.ERROR_MESSAGE);
        } finally {
            btnLogin.setEnabled(true);
            setCursor(Cursor.getDefaultCursor());
        }
    }
}
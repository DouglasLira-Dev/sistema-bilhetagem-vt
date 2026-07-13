package com.bilhetagem.view;

import com.bilhetagem.dao.UsuarioDAO;
import com.bilhetagem.dao.UsuarioDAOImpl;
import com.bilhetagem.model.Usuario;
import com.bilhetagem.model.Usuario.Perfil;
import com.bilhetagem.model.Usuario.Permissao;
import com.bilhetagem.service.AuditoriaService;
import com.bilhetagem.util.SessaoUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Tela de gerenciamento de usuários do sistema.
 * 
 * <p>Permite cadastrar, editar, ativar/desativar usuários.</p>
 * 
 * @author Equipe de Desenvolvimento
 * @version 1.0.0
 * @since 2026-01-13
 */
public class TelaUsuarios extends JDialog {
    
    private static final Logger LOGGER = LogManager.getLogger(TelaUsuarios.class);
    
    private JTable tabelaUsuarios;
    private DefaultTableModel modeloTabela;
    private JButton btnNovo;
    private JButton btnEditar;
    private JButton btnAtivarDesativar;
    private JButton btnAtualizar;
    
    private UsuarioDAO usuarioDAO;
    private AuditoriaService auditoriaService;
    private JFrame parent;
    
    public TelaUsuarios(JFrame parent) {
        super(parent, "👥 Gerenciar Usuários", true);
        this.parent = parent;
        this.usuarioDAO = new UsuarioDAOImpl();
        this.auditoriaService = new AuditoriaService();
        
        // Verificar permissão
        if (!SessaoUtil.temPermissao(Permissao.GERENCIAR_USUARIOS)) {
            JOptionPane.showMessageDialog(this,
                "Você não tem permissão para gerenciar usuários.",
                "Acesso Negado", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }
        
        configurarJanela();
        criarComponentes();
        carregarUsuarios();
    }
    
    private void configurarJanela() {
        setSize(800, 500);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
    }
    
    private void criarComponentes() {
        // Painel superior - botões
        JPanel panelSuperior = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panelSuperior.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        
        btnNovo = new JButton("➕ Novo Usuário");
        btnNovo.setBackground(new Color(52, 152, 219));
        btnNovo.setForeground(Color.WHITE);
        btnNovo.addActionListener(e -> novoUsuario());
        
        btnEditar = new JButton("✏️ Editar");
        btnEditar.setBackground(new Color(52, 73, 94));
        btnEditar.setForeground(Color.WHITE);
        btnEditar.addActionListener(e -> editarUsuario());
        
        btnAtivarDesativar = new JButton("🔒 Ativar/Desativar");
        btnAtivarDesativar.setBackground(new Color(241, 196, 15));
        btnAtivarDesativar.setForeground(Color.BLACK);
        btnAtivarDesativar.addActionListener(e -> ativarDesativarUsuario());
        
        btnAtualizar = new JButton("🔄 Atualizar");
        btnAtualizar.setBackground(new Color(149, 165, 166));
        btnAtualizar.setForeground(Color.WHITE);
        btnAtualizar.addActionListener(e -> carregarUsuarios());
        
        panelSuperior.add(btnNovo);
        panelSuperior.add(btnEditar);
        panelSuperior.add(btnAtivarDesativar);
        panelSuperior.add(btnAtualizar);
        
        add(panelSuperior, BorderLayout.NORTH);
        
        // Tabela de usuários
        String[] colunas = {"ID", "Login", "Nome", "Email", "Perfil", "Status", "Último Acesso"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tabelaUsuarios = new JTable(modeloTabela);
        tabelaUsuarios.setRowHeight(25);
        tabelaUsuarios.setFont(new Font("Arial", Font.PLAIN, 12));
        tabelaUsuarios.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        
        // Configurar largura das colunas
        tabelaUsuarios.getColumnModel().getColumn(0).setPreferredWidth(50);
        tabelaUsuarios.getColumnModel().getColumn(1).setPreferredWidth(100);
        tabelaUsuarios.getColumnModel().getColumn(2).setPreferredWidth(150);
        tabelaUsuarios.getColumnModel().getColumn(3).setPreferredWidth(150);
        tabelaUsuarios.getColumnModel().getColumn(4).setPreferredWidth(100);
        tabelaUsuarios.getColumnModel().getColumn(5).setPreferredWidth(80);
        tabelaUsuarios.getColumnModel().getColumn(6).setPreferredWidth(150);
        
        // Renderização de cores por status
        tabelaUsuarios.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (!isSelected && column == 5) {
                    String status = (String) table.getValueAt(row, 5);
                    if ("Ativo".equals(status)) {
                        c.setBackground(new Color(232, 245, 233));
                    } else {
                        c.setBackground(new Color(253, 235, 236));
                    }
                }
                return c;
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(tabelaUsuarios);
        scrollPane.setBorder(BorderFactory.createTitledBorder("📋 Usuários Cadastrados"));
        add(scrollPane, BorderLayout.CENTER);
        
        // Painel inferior
        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnFechar = new JButton("❌ Fechar");
        btnFechar.addActionListener(e -> dispose());
        panelInferior.add(btnFechar);
        add(panelInferior, BorderLayout.SOUTH);
    }
    
    private void carregarUsuarios() {
        try {
            List<Usuario> usuarios = usuarioDAO.listarTodos();
            modeloTabela.setRowCount(0);
            
            for (Usuario u : usuarios) {
                Object[] row = {
                    u.getId(),
                    u.getLogin(),
                    u.getNome(),
                    u.getEmail() != null ? u.getEmail() : "",
                    u.getPerfil().getDescricao(),
                    u.isAtivo() ? "Ativo" : "Inativo",
                    u.getUltimoAcesso() != null ? 
                        u.getUltimoAcesso().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : 
                        "Nunca"
                };
                modeloTabela.addRow(row);
            }
            
            LOGGER.info("✅ Carregados {} usuários", usuarios.size());
        } catch (SQLException e) {
            LOGGER.error("Erro ao carregar usuários", e);
            JOptionPane.showMessageDialog(this,
                "Erro ao carregar usuários: " + e.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void novoUsuario() {
        JDialog dialog = new JDialog(this, "Novo Usuário", true);
        dialog.setSize(400, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JTextField txtLogin = new JTextField(20);
        JPasswordField txtSenha = new JPasswordField(20);
        JTextField txtNome = new JTextField(20);
        JTextField txtEmail = new JTextField(20);
        JComboBox<Perfil> cbPerfil = new JComboBox<>(Perfil.values());
        
        int y = 0;
        gbc.gridy = y++;
        gbc.gridx = 0;
        dialog.add(new JLabel("Login:*"), gbc);
        gbc.gridx = 1;
        dialog.add(txtLogin, gbc);
        
        gbc.gridy = y++;
        gbc.gridx = 0;
        dialog.add(new JLabel("Senha:*"), gbc);
        gbc.gridx = 1;
        dialog.add(txtSenha, gbc);
        
        gbc.gridy = y++;
        gbc.gridx = 0;
        dialog.add(new JLabel("Nome:*"), gbc);
        gbc.gridx = 1;
        dialog.add(txtNome, gbc);
        
        gbc.gridy = y++;
        gbc.gridx = 0;
        dialog.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        dialog.add(txtEmail, gbc);
        
        gbc.gridy = y++;
        gbc.gridx = 0;
        dialog.add(new JLabel("Perfil:*"), gbc);
        gbc.gridx = 1;
        dialog.add(cbPerfil, gbc);
        
        gbc.gridy = y++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JLabel lblObrigatorio = new JLabel("* Campos obrigatórios");
        lblObrigatorio.setFont(new Font("Arial", Font.ITALIC, 10));
        lblObrigatorio.setForeground(Color.GRAY);
        dialog.add(lblObrigatorio, gbc);
        
        gbc.gridy = y++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JPanel panelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        JButton btnSalvar = new JButton("💾 Salvar");
        btnSalvar.setBackground(new Color(46, 204, 113));
        btnSalvar.setForeground(Color.WHITE);
        btnSalvar.addActionListener(e -> {
            String login = txtLogin.getText().trim();
            String senha = new String(txtSenha.getPassword());
            String nome = txtNome.getText().trim();
            String email = txtEmail.getText().trim();
            Perfil perfil = (Perfil) cbPerfil.getSelectedItem();
            
            if (login.isEmpty() || senha.isEmpty() || nome.isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                    "Preencha todos os campos obrigatórios!",
                    "Campos Obrigatórios", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            if (senha.length() < 6) {
                JOptionPane.showMessageDialog(dialog,
                    "A senha deve ter no mínimo 6 caracteres!",
                    "Senha Fraca", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            try {
                Usuario usuario = new Usuario(login, senha, nome, email, perfil);
                usuarioDAO.salvar(usuario);
                auditoriaService.registrarCriacao("USUARIO", usuario.getId(), 
                    "Usuário criado: " + login);
                JOptionPane.showMessageDialog(dialog,
                    "Usuário criado com sucesso!",
                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                carregarUsuarios();
            } catch (SQLException ex) {
                LOGGER.error("Erro ao criar usuário", ex);
                JOptionPane.showMessageDialog(dialog,
                    "Erro ao criar usuário: " + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        JButton btnCancelar = new JButton("❌ Cancelar");
        btnCancelar.addActionListener(e -> dialog.dispose());
        
        panelBotoes.add(btnSalvar);
        panelBotoes.add(btnCancelar);
        dialog.add(panelBotoes, gbc);
        
        dialog.setVisible(true);
    }
    
    private void editarUsuario() {
        int row = tabelaUsuarios.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                "Selecione um usuário para editar.",
                "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Long id = (Long) modeloTabela.getValueAt(row, 0);
        
        try {
            var optional = usuarioDAO.buscarPorId(id);
            if (optional.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Usuário não encontrado.",
                    "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            Usuario usuario = optional.get();
            
            JDialog dialog = new JDialog(this, "Editar Usuário", true);
            dialog.setSize(400, 380);
            dialog.setLocationRelativeTo(this);
            dialog.setLayout(new GridBagLayout());
            
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            
            JTextField txtLogin = new JTextField(usuario.getLogin(), 20);
            txtLogin.setEditable(false); // Login não pode ser alterado
            JTextField txtNome = new JTextField(usuario.getNome(), 20);
            JTextField txtEmail = new JTextField(usuario.getEmail() != null ? usuario.getEmail() : "", 20);
            JPasswordField txtNovaSenha = new JPasswordField(20);
            JComboBox<Perfil> cbPerfil = new JComboBox<>(Perfil.values());
            cbPerfil.setSelectedItem(usuario.getPerfil());
            
            int y = 0;
            gbc.gridy = y++;
            gbc.gridx = 0;
            dialog.add(new JLabel("Login:"), gbc);
            gbc.gridx = 1;
            dialog.add(txtLogin, gbc);
            
            gbc.gridy = y++;
            gbc.gridx = 0;
            dialog.add(new JLabel("Nome:*"), gbc);
            gbc.gridx = 1;
            dialog.add(txtNome, gbc);
            
            gbc.gridy = y++;
            gbc.gridx = 0;
            dialog.add(new JLabel("Email:"), gbc);
            gbc.gridx = 1;
            dialog.add(txtEmail, gbc);
            
            gbc.gridy = y++;
            gbc.gridx = 0;
            dialog.add(new JLabel("Nova Senha:"), gbc);
            gbc.gridx = 1;
            dialog.add(txtNovaSenha, gbc);
            
            gbc.gridy = y++;
            gbc.gridx = 0;
            dialog.add(new JLabel("Perfil:*"), gbc);
            gbc.gridx = 1;
            dialog.add(cbPerfil, gbc);
            
            gbc.gridy = y++;
            gbc.gridx = 0;
            gbc.gridwidth = 2;
            JLabel lblInfo = new JLabel("💡 Deixe a senha em branco para manter a atual");
            lblInfo.setFont(new Font("Arial", Font.ITALIC, 10));
            lblInfo.setForeground(Color.GRAY);
            dialog.add(lblInfo, gbc);
            
            gbc.gridy = y++;
            gbc.gridx = 0;
            gbc.gridwidth = 2;
            JPanel panelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
            
            JButton btnSalvar = new JButton("💾 Salvar");
            btnSalvar.setBackground(new Color(46, 204, 113));
            btnSalvar.setForeground(Color.WHITE);
            btnSalvar.addActionListener(e -> {
                String nome = txtNome.getText().trim();
                String email = txtEmail.getText().trim();
                String novaSenha = new String(txtNovaSenha.getPassword());
                Perfil perfil = (Perfil) cbPerfil.getSelectedItem();
                
                if (nome.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog,
                        "Preencha o nome!",
                        "Campo Obrigatório", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                try {
                    usuario.setNome(nome);
                    usuario.setEmail(email);
                    usuario.setPerfil(perfil);
                    
                    if (!novaSenha.isEmpty()) {
                        if (novaSenha.length() < 6) {
                            JOptionPane.showMessageDialog(dialog,
                                "A senha deve ter no mínimo 6 caracteres!",
                                "Senha Fraca", JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                        usuarioDAO.atualizarSenha(usuario.getId(), novaSenha);
                    }
                    
                    usuarioDAO.atualizar(usuario);
                    auditoriaService.registrarAtualizacao("USUARIO", usuario.getId(), 
                        "Usuário atualizado: " + usuario.getLogin());
                    JOptionPane.showMessageDialog(dialog,
                        "Usuário atualizado com sucesso!",
                        "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    carregarUsuarios();
                } catch (SQLException ex) {
                    LOGGER.error("Erro ao atualizar usuário", ex);
                    JOptionPane.showMessageDialog(dialog,
                        "Erro ao atualizar usuário: " + ex.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
                }
            });
            
            JButton btnCancelar = new JButton("❌ Cancelar");
            btnCancelar.addActionListener(e -> dialog.dispose());
            
            panelBotoes.add(btnSalvar);
            panelBotoes.add(btnCancelar);
            dialog.add(panelBotoes, gbc);
            
            dialog.setVisible(true);
            
        } catch (SQLException e) {
            LOGGER.error("Erro ao buscar usuário", e);
            JOptionPane.showMessageDialog(this,
                "Erro ao buscar usuário: " + e.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void ativarDesativarUsuario() {
        int row = tabelaUsuarios.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                "Selecione um usuário para ativar/desativar.",
                "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Long id = (Long) modeloTabela.getValueAt(row, 0);
        String nome = (String) modeloTabela.getValueAt(row, 2);
        String status = (String) modeloTabela.getValueAt(row, 5);
        boolean isAtivo = status.contains("Ativo");
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Deseja " + (isAtivo ? "desativar" : "ativar") + " o usuário " + nome + "?",
            "Confirmar", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                var optional = usuarioDAO.buscarPorId(id);
                if (optional.isPresent()) {
                    Usuario usuario = optional.get();
                    usuario.setAtivo(!isAtivo);
                    usuarioDAO.atualizar(usuario);
                    
                    auditoriaService.registrarAtualizacao("USUARIO", usuario.getId(), 
                        "Usuário " + (isAtivo ? "desativado" : "ativado") + ": " + nome);
                    
                    JOptionPane.showMessageDialog(this,
                        "Usuário " + (isAtivo ? "desativado" : "ativado") + " com sucesso!",
                        "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                    carregarUsuarios();
                }
            } catch (SQLException e) {
                LOGGER.error("Erro ao ativar/desativar usuário", e);
                JOptionPane.showMessageDialog(this,
                    "Erro ao ativar/desativar usuário: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
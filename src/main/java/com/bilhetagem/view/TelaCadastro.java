package com.bilhetagem.view;

import com.bilhetagem.dao.SolicitacaoDAO;
import com.bilhetagem.dao.SolicitacaoDAOImpl;
import com.bilhetagem.model.Solicitacao;
import com.bilhetagem.model.Solicitacao.TipoSolicitacao;
import com.bilhetagem.util.CpfUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Tela de cadastro/edição de solicitações.
 * 
 * <p>Esta classe implementa o formulário para criar ou editar
 * uma solicitação de vale transporte.</p>
 * 
 * @author [Seu Nome]
 * @version 1.0.0
 * @since 2026-01-08
 */
public class TelaCadastro extends JDialog {
    
    private static final Logger LOGGER = LogManager.getLogger(TelaCadastro.class);
    
    private JFrame parent;
    private Solicitacao solicitacaoEditando;
    private boolean isEditando;
    
    // Campos do formulário
    private JTextField txtDataEfetivacao;
    private JTextField txtMesReferencia;
    private JTextField txtMatricula;
    private JTextField txtCpf;
    private JTextField txtNome;
    private JTextField txtNumeroCartao;
    private JTextField txtQuantidadeVales;
    private JComboBox<String> cbTipoSolicitacao;
    private JTextArea txtObservacao;
    
    private SolicitacaoDAO dao;
    
    /**
     * Construtor para nova solicitação.
     */
    public TelaCadastro(JFrame parent) {
        super(parent, "Nova Solicitação", true);
        this.parent = parent;
        this.isEditando = false;
        this.dao = new SolicitacaoDAOImpl();
        inicializarComponentes();
        preencherDadosPadrao();
    }
    
    /**
     * Construtor para edição de solicitação existente.
     */
    public TelaCadastro(JFrame parent, Solicitacao solicitacao) {
        super(parent, "Editar Solicitação", true);
        this.parent = parent;
        this.solicitacaoEditando = solicitacao;
        this.isEditando = true;
        this.dao = new SolicitacaoDAOImpl();
        inicializarComponentes();
        preencherDados();
    }
    
    /**
     * Inicializa os componentes da tela.
     */
    private void inicializarComponentes() {
        setSize(600, 550);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        setResizable(false);
        
        // Painel de formulário
        JPanel panelForm = new JPanel(new GridBagLayout());
        panelForm.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Linha 0: Data de Efetivação
        gbc.gridx = 0;
        gbc.gridy = 0;
        panelForm.add(new JLabel("Data de Efetivação:*"), gbc);
        
        gbc.gridx = 1;
        txtDataEfetivacao = new JTextField(15);
        txtDataEfetivacao.setToolTipText("Formato: dd/MM/yyyy");
        panelForm.add(txtDataEfetivacao, gbc);
        
        JButton btnHoje = new JButton("Hoje");
        btnHoje.addActionListener(e -> {
            txtDataEfetivacao.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        });
        gbc.gridx = 2;
        panelForm.add(btnHoje, gbc);
        
        // Linha 1: Mês Referência
        gbc.gridx = 0;
        gbc.gridy = 1;
        panelForm.add(new JLabel("Mês Referência:*"), gbc);
        
        gbc.gridx = 1;
        txtMesReferencia = new JTextField(15);
        txtMesReferencia.setToolTipText("Formato: MM/yyyy");
        panelForm.add(txtMesReferencia, gbc);
        
        JButton btnMesAtual = new JButton("Atual");
        btnMesAtual.addActionListener(e -> {
            txtMesReferencia.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("MM/yyyy")));
        });
        gbc.gridx = 2;
        panelForm.add(btnMesAtual, gbc);
        
        // Linha 2: Matrícula
        gbc.gridx = 0;
        gbc.gridy = 2;
        panelForm.add(new JLabel("Matrícula:*"), gbc);
        gbc.gridx = 1;
        txtMatricula = new JTextField(20);
        panelForm.add(txtMatricula, gbc);
        
        // Linha 3: CPF
        gbc.gridx = 0;
        gbc.gridy = 3;
        panelForm.add(new JLabel("CPF:*"), gbc);
        gbc.gridx = 1;
        txtCpf = new JTextField(20);
        txtCpf.setToolTipText("Apenas números");
        panelForm.add(txtCpf, gbc);
        
        // Linha 4: Nome
        gbc.gridx = 0;
        gbc.gridy = 4;
        panelForm.add(new JLabel("Nome:*"), gbc);
        gbc.gridx = 1;
        txtNome = new JTextField(30);
        panelForm.add(txtNome, gbc);
        
        // Linha 5: Número do Cartão
        gbc.gridx = 0;
        gbc.gridy = 5;
        panelForm.add(new JLabel("Nº Cartão:"), gbc);
        gbc.gridx = 1;
        txtNumeroCartao = new JTextField(20);
        panelForm.add(txtNumeroCartao, gbc);
        
        // Linha 6: Quantidade de Vales
        gbc.gridx = 0;
        gbc.gridy = 6;
        panelForm.add(new JLabel("Qtd. Vales Tipo A:*"), gbc);
        gbc.gridx = 1;
        txtQuantidadeVales = new JTextField(10);
        txtQuantidadeVales.setToolTipText("Número inteiro");
        panelForm.add(txtQuantidadeVales, gbc);
        
        // Linha 7: Tipo de Solicitação
        gbc.gridx = 0;
        gbc.gridy = 7;
        panelForm.add(new JLabel("Tipo de Solicitação:*"), gbc);
        gbc.gridx = 1;
        cbTipoSolicitacao = new JComboBox<>(new String[]{
            "Adesão", "Renúncia", "Alteração"
        });
        panelForm.add(cbTipoSolicitacao, gbc);
        
        // Linha 8: Observação
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panelForm.add(new JLabel("Observação:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        txtObservacao = new JTextArea(3, 30);
        txtObservacao.setLineWrap(true);
        txtObservacao.setWrapStyleWord(true);
        JScrollPane scrollObs = new JScrollPane(txtObservacao);
        panelForm.add(scrollObs, gbc);
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        
        // Painel de botões
        JPanel panelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        panelBotoes.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        JButton btnSalvar = new JButton("💾 Salvar");
        btnSalvar.setBackground(new Color(52, 152, 219));
        btnSalvar.setForeground(Color.WHITE);
        btnSalvar.setFont(new Font("Arial", Font.BOLD, 12));
        btnSalvar.addActionListener(e -> salvar());
        
        JButton btnCancelar = new JButton("❌ Cancelar");
        btnCancelar.setBackground(new Color(149, 165, 166));
        btnCancelar.setForeground(Color.WHITE);
        btnCancelar.setFont(new Font("Arial", Font.BOLD, 12));
        btnCancelar.addActionListener(e -> dispose());
        
        panelBotoes.add(btnSalvar);
        panelBotoes.add(btnCancelar);
        
        // Adicionar painéis à janela
        add(panelForm, BorderLayout.CENTER);
        add(panelBotoes, BorderLayout.SOUTH);
        
        // Labels de campos obrigatórios
        JLabel lblObrigatorio = new JLabel("* Campos obrigatórios");
        lblObrigatorio.setFont(new Font("Arial", Font.ITALIC, 10));
        lblObrigatorio.setForeground(Color.GRAY);
        add(lblObrigatorio, BorderLayout.NORTH);
    }
    
    /**
     * Preenche os campos com dados padrão.
     */
    private void preencherDadosPadrao() {
        LocalDate hoje = LocalDate.now();
        txtDataEfetivacao.setText(hoje.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        txtMesReferencia.setText(hoje.format(DateTimeFormatter.ofPattern("MM/yyyy")));
        txtQuantidadeVales.setText("1");
    }
    
    /**
     * Preenche os campos com dados da solicitação existente.
     */
    private void preencherDados() {
        if (solicitacaoEditando == null) return;
        
        txtDataEfetivacao.setText(solicitacaoEditando.getDataEfetivacaoFormatada());
        txtMesReferencia.setText(solicitacaoEditando.getMesReferencia());
        txtMatricula.setText(solicitacaoEditando.getMatricula());
        txtCpf.setText(solicitacaoEditando.getCpf());
        txtNome.setText(solicitacaoEditando.getNome());
        txtNumeroCartao.setText(solicitacaoEditando.getNumeroCartao());
        txtQuantidadeVales.setText(
            solicitacaoEditando.getQuantidadeValeTipoA() != null ? 
            solicitacaoEditando.getQuantidadeValeTipoA().toString() : "0"
        );
        
        if (solicitacaoEditando.getTipoSolicitacao() != null) {
            cbTipoSolicitacao.setSelectedItem(
                solicitacaoEditando.getTipoSolicitacao().getDescricao()
            );
        }
        
        txtObservacao.setText(solicitacaoEditando.getObservacao());
    }
    
    /**
     * Salva a solicitação.
     */
    private void salvar() {
        try {
            // Validar campos obrigatórios
            if (!validarCampos()) {
                return;
            }
            
            // Criar objeto solicitação
            Solicitacao solicitacao = isEditando ? solicitacaoEditando : new Solicitacao();
            
            // Preencher dados
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            solicitacao.setDataEfetivacao(LocalDate.parse(txtDataEfetivacao.getText().trim(), formatter));
            solicitacao.setMesReferencia(txtMesReferencia.getText().trim());
            solicitacao.setMatricula(txtMatricula.getText().trim());
            solicitacao.setCpf(CpfUtil.limpar(txtCpf.getText().trim()));
            solicitacao.setNome(txtNome.getText().trim());
            solicitacao.setNumeroCartao(txtNumeroCartao.getText().trim());
            solicitacao.setQuantidadeValeTipoA(Integer.parseInt(txtQuantidadeVales.getText().trim()));
            
            String tipoSelecionado = (String) cbTipoSolicitacao.getSelectedItem();
            solicitacao.setTipoSolicitacao(TipoSolicitacao.fromDescricao(tipoSelecionado));
            solicitacao.setObservacao(txtObservacao.getText().trim());
            
            // Salvar
            if (isEditando) {
                dao.atualizar(solicitacao);
                JOptionPane.showMessageDialog(this,
                    "Solicitação atualizada com sucesso!",
                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } else {
                dao.salvar(solicitacao);
                JOptionPane.showMessageDialog(this,
                    "Solicitação cadastrada com sucesso!",
                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            }
            
            dispose();
            
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this,
                "Data inválida! Use o formato dd/MM/yyyy",
                "Erro de Validação", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                "Quantidade de vales deve ser um número inteiro!",
                "Erro de Validação", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this,
                e.getMessage(),
                "Erro de Validação", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            LOGGER.error("Erro ao salvar solicitação", e);
            JOptionPane.showMessageDialog(this,
                "Erro ao salvar: " + e.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Valida os campos do formulário.
     */
    private boolean validarCampos() {
        // Data
        String data = txtDataEfetivacao.getText().trim();
        if (data.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Data de efetivação é obrigatória!",
                "Campo Obrigatório", JOptionPane.WARNING_MESSAGE);
            txtDataEfetivacao.requestFocus();
            return false;
        }
        
        // Mês referência
        String mes = txtMesReferencia.getText().trim();
        if (mes.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Mês de referência é obrigatório!",
                "Campo Obrigatório", JOptionPane.WARNING_MESSAGE);
            txtMesReferencia.requestFocus();
            return false;
        }
        
        // Matrícula
        String matricula = txtMatricula.getText().trim();
        if (matricula.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Matrícula é obrigatória!",
                "Campo Obrigatório", JOptionPane.WARNING_MESSAGE);
            txtMatricula.requestFocus();
            return false;
        }
        
        // CPF
        String cpf = txtCpf.getText().trim();
        if (cpf.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "CPF é obrigatório!",
                "Campo Obrigatório", JOptionPane.WARNING_MESSAGE);
            txtCpf.requestFocus();
            return false;
        }
        if (!CpfUtil.isValid(cpf)) {
            JOptionPane.showMessageDialog(this,
                "CPF inválido! Verifique os dígitos informados.",
                "Erro de Validação", JOptionPane.WARNING_MESSAGE);
            txtCpf.requestFocus();
            return false;
        }
        
        // Nome
        String nome = txtNome.getText().trim();
        if (nome.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Nome é obrigatório!",
                "Campo Obrigatório", JOptionPane.WARNING_MESSAGE);
            txtNome.requestFocus();
            return false;
        }
        
        // Quantidade vales
        String qtd = txtQuantidadeVales.getText().trim();
        if (qtd.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Quantidade de vales é obrigatória!",
                "Campo Obrigatório", JOptionPane.WARNING_MESSAGE);
            txtQuantidadeVales.requestFocus();
            return false;
        }
        
        return true;
    }
}
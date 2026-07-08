package com.bilhetagem.util;

import com.bilhetagem.model.Solicitacao;
import com.bilhetagem.model.Solicitacao.TipoSolicitacao;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Classe utilitária para operações com arquivos Excel.
 * 
 * <p>Responsável por ler e escrever arquivos Excel (.xlsx e .xls),
 * mapeando os dados para objetos do sistema.</p>
 * 
 * @author [Seu Nome]
 * @version 1.0.0
 * @since 2026-01-08
 */
public class ExcelUtil {
    
    private static final Logger LOGGER = LogManager.getLogger(ExcelUtil.class);
    
    // ===== CONSTANTES =====
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_FORMATTER_ALT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    // Índices das colunas esperadas no Excel
    public static final int COL_DATA_EFETIVACAO = 0;
    public static final int COL_MES_REFERENCIA = 1;
    public static final int COL_MATRICULA = 2;
    public static final int COL_CPF = 3;
    public static final int COL_NOME = 4;
    public static final int COL_NUMERO_CARTAO = 5;
    public static final int COL_QUANTIDADE_VALES = 6;
    public static final int COL_TIPO_SOLICITACAO = 7;
    public static final int COL_OBSERVACAO = 8;
    
    // ===== MÉTODOS PÚBLICOS =====
    
    /**
     * Importa dados de um arquivo Excel.
     * 
     * @param arquivo Arquivo Excel a ser importado
     * @return Lista de solicitações extraídas do arquivo
     * @throws IOException Se houver erro na leitura do arquivo
     */
    public static List<Solicitacao> importarExcel(File arquivo) throws IOException {
        LOGGER.info("📥 Iniciando importação do arquivo: {}", arquivo.getName());
        
        List<Solicitacao> solicitacoes = new ArrayList<>();
        int linhaAtual = 0;
        int erros = 0;
        
        try (InputStream inputStream = new FileInputStream(arquivo);
             Workbook workbook = criarWorkbook(inputStream, arquivo.getName())) {
            
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            
            // Pular cabeçalho (primeira linha)
            if (rowIterator.hasNext()) {
                rowIterator.next();
            }
            
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                linhaAtual++;
                
                try {
                    Solicitacao solicitacao = mapearLinhaParaSolicitacao(row);
                    if (solicitacao != null && solicitacao.isValid()) {
                        solicitacoes.add(solicitacao);
                        LOGGER.debug("✅ Linha {} importada com sucesso", linhaAtual);
                    } else {
                        LOGGER.warn("⚠️ Linha {} ignorada (dados inválidos)", linhaAtual);
                        erros++;
                    }
                } catch (Exception e) {
                    LOGGER.error("❌ Erro na linha {}: {}", linhaAtual, e.getMessage());
                    erros++;
                }
            }
        }
        
        LOGGER.info("✅ Importação concluída: {} registros importados, {} erros", 
                   solicitacoes.size(), erros);
        
        return solicitacoes;
    }
    
    /**
     * Importa dados de um arquivo Excel com validação adicional.
     * 
     * @param arquivo Arquivo Excel a ser importado
     * @param validarDuplicatas Verifica se já existem registros duplicados
     * @return Lista de solicitações extraídas do arquivo
     * @throws IOException Se houver erro na leitura do arquivo
     */
    public static List<Solicitacao> importarExcel(File arquivo, boolean validarDuplicatas) 
            throws IOException {
        List<Solicitacao> solicitacoes = importarExcel(arquivo);
        
        if (validarDuplicatas && !solicitacoes.isEmpty()) {
            // Remover duplicatas baseado em campos chave
            List<Solicitacao> unicos = new ArrayList<>();
            for (Solicitacao s : solicitacoes) {
                boolean duplicado = unicos.stream().anyMatch(u ->
                    u.getMatricula().equals(s.getMatricula()) &&
                    u.getMesReferencia().equals(s.getMesReferencia()) &&
                    u.getTipoSolicitacao() == s.getTipoSolicitacao()
                );
                if (!duplicado) {
                    unicos.add(s);
                }
            }
            LOGGER.info("🔍 Removidas {} duplicatas", solicitacoes.size() - unicos.size());
            return unicos;
        }
        
        return solicitacoes;
    }
    
    /**
     * Exporta dados para um arquivo Excel.
     * 
     * @param solicitacoes Lista de solicitações a serem exportadas
     * @param arquivoDestino Arquivo de destino
     * @throws IOException Se houver erro na escrita do arquivo
     */
    public static void exportarExcel(List<Solicitacao> solicitacoes, File arquivoDestino) 
            throws IOException {
        LOGGER.info("📤 Exportando {} registros para: {}", solicitacoes.size(), arquivoDestino.getName());
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Solicitações");
            
            // Criar cabeçalho
            Row headerRow = sheet.createRow(0);
            String[] cabecalhos = {
                "Data Efetivação", "Mês Referência", "Matrícula", "CPF", "Nome",
                "Nº Cartão", "Qtd. Vales Tipo A", "Tipo Solicitação", "Observação"
            };
            
            // Estilo do cabeçalho
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            
            for (int i = 0; i < cabecalhos.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(cabecalhos[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 20 * 256); // Largura em caracteres
            }
            
            // Preencher dados
            int rowNum = 1;
            for (Solicitacao s : solicitacoes) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(COL_DATA_EFETIVACAO).setCellValue(
                    s.getDataEfetivacao() != null ? 
                    s.getDataEfetivacao().format(DATE_FORMATTER) : ""
                );
                row.createCell(COL_MES_REFERENCIA).setCellValue(
                    s.getMesReferencia() != null ? s.getMesReferencia() : ""
                );
                row.createCell(COL_MATRICULA).setCellValue(
                    s.getMatricula() != null ? s.getMatricula() : ""
                );
                row.createCell(COL_CPF).setCellValue(
                    s.getCpf() != null ? s.getCpf() : ""
                );
                row.createCell(COL_NOME).setCellValue(
                    s.getNome() != null ? s.getNome() : ""
                );
                row.createCell(COL_NUMERO_CARTAO).setCellValue(
                    s.getNumeroCartao() != null ? s.getNumeroCartao() : ""
                );
                row.createCell(COL_QUANTIDADE_VALES).setCellValue(
                    s.getQuantidadeValeTipoA() != null ? s.getQuantidadeValeTipoA() : 0
                );
                row.createCell(COL_TIPO_SOLICITACAO).setCellValue(
                    s.getTipoSolicitacao() != null ? 
                    s.getTipoSolicitacao().getDescricao() : ""
                );
                row.createCell(COL_OBSERVACAO).setCellValue(
                    s.getObservacao() != null ? s.getObservacao() : ""
                );
            }
            
            // Salvar arquivo
            try (FileOutputStream fileOut = new FileOutputStream(arquivoDestino)) {
                workbook.write(fileOut);
            }
        }
        
        LOGGER.info("✅ Exportação concluída com sucesso!");
    }
    
    /**
     * Exporta um relatório resumido para Excel.
     * 
     * @param dadosResumo Dados consolidados por mês
     * @param arquivoDestino Arquivo de destino
     * @throws IOException Se houver erro na escrita do arquivo
     */
    public static void exportarRelatorioExcel(java.util.Map<String, java.util.Map<String, Long>> dadosResumo, 
                                              File arquivoDestino) throws IOException {
        LOGGER.info("📤 Exportando relatório para: {}", arquivoDestino.getName());
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Relatório Mensal");
            
            // Criar cabeçalho
            Row headerRow = sheet.createRow(0);
            String[] cabecalhos = {"Mês Referência", "Adesões", "Renúncias", "Alterações", "Total"};
            
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_GREEN.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            for (int i = 0; i < cabecalhos.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(cabecalhos[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 20 * 256);
            }
            
            // Preencher dados
            int rowNum = 1;
            var mesesOrdenados = dadosResumo.keySet().stream()
                .sorted(java.util.Collections.reverseOrder())
                .collect(java.util.stream.Collectors.toList());
            
            for (String mes : mesesOrdenados) {
                var tipos = dadosResumo.get(mes);
                long adesoes = tipos.getOrDefault("Adesão", 0L);
                long renuncias = tipos.getOrDefault("Renúncia", 0L);
                long alteracoes = tipos.getOrDefault("Alteração", 0L);
                long total = adesoes + renuncias + alteracoes;
                
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(mes);
                row.createCell(1).setCellValue(adesoes);
                row.createCell(2).setCellValue(renuncias);
                row.createCell(3).setCellValue(alteracoes);
                row.createCell(4).setCellValue(total);
            }
            
            try (FileOutputStream fileOut = new FileOutputStream(arquivoDestino)) {
                workbook.write(fileOut);
            }
        }
        
        LOGGER.info("✅ Relatório exportado com sucesso!");
    }
    
    // ===== MÉTODOS PRIVADOS =====
    
    /**
     * Cria o Workbook apropriado baseado na extensão do arquivo.
     */
    private static Workbook criarWorkbook(InputStream inputStream, String nomeArquivo) 
            throws IOException {
        if (nomeArquivo.toLowerCase().endsWith(".xlsx")) {
            return new XSSFWorkbook(inputStream);
        } else if (nomeArquivo.toLowerCase().endsWith(".xls")) {
            return new HSSFWorkbook(inputStream);
        } else {
            throw new IllegalArgumentException("Formato de arquivo não suportado: " + nomeArquivo);
        }
    }
    
    /**
     * Mapeia uma linha do Excel para um objeto Solicitacao.
     */
    private static Solicitacao mapearLinhaParaSolicitacao(Row row) {
        try {
            Solicitacao solicitacao = new Solicitacao();
            
            // Data de efetivação
            String dataStr = obterValorCelula(row, COL_DATA_EFETIVACAO);
            if (dataStr != null && !dataStr.isEmpty()) {
                try {
                    solicitacao.setDataEfetivacao(LocalDate.parse(dataStr, DATE_FORMATTER));
                } catch (DateTimeParseException e) {
                    try {
                        solicitacao.setDataEfetivacao(LocalDate.parse(dataStr, DATE_FORMATTER_ALT));
                    } catch (DateTimeParseException e2) {
                        LOGGER.warn("Formato de data inválido: {}", dataStr);
                    }
                }
            }
            
            // Mês referência
            String mesRef = obterValorCelula(row, COL_MES_REFERENCIA);
            if (mesRef != null && !mesRef.isEmpty()) {
                solicitacao.setMesReferencia(mesRef.trim());
            }
            
            // Matrícula
            String matricula = obterValorCelula(row, COL_MATRICULA);
            if (matricula != null && !matricula.isEmpty()) {
                solicitacao.setMatricula(matricula.trim());
            }
            
            // CPF
            String cpf = obterValorCelula(row, COL_CPF);
            if (cpf != null && !cpf.isEmpty()) {
                solicitacao.setCpf(cpf.trim().replaceAll("[^0-9]", ""));
            }
            
            // Nome
            String nome = obterValorCelula(row, COL_NOME);
            if (nome != null && !nome.isEmpty()) {
                solicitacao.setNome(nome.trim());
            }
            
            // Número do cartão
            String cartao = obterValorCelula(row, COL_NUMERO_CARTAO);
            if (cartao != null && !cartao.isEmpty()) {
                solicitacao.setNumeroCartao(cartao.trim());
            }
            
            // Quantidade de vales
            String qtdStr = obterValorCelula(row, COL_QUANTIDADE_VALES);
            if (qtdStr != null && !qtdStr.isEmpty()) {
                try {
                    solicitacao.setQuantidadeValeTipoA(Integer.parseInt(qtdStr.trim()));
                } catch (NumberFormatException e) {
                    solicitacao.setQuantidadeValeTipoA(1);
                }
            } else {
                solicitacao.setQuantidadeValeTipoA(1);
            }
            
            // Tipo de solicitação
            String tipoStr = obterValorCelula(row, COL_TIPO_SOLICITACAO);
            if (tipoStr != null && !tipoStr.isEmpty()) {
                try {
                    solicitacao.setTipoSolicitacao(TipoSolicitacao.fromDescricao(tipoStr));
                } catch (IllegalArgumentException e) {
                    LOGGER.warn("Tipo de solicitação inválido: {}", tipoStr);
                }
            }
            
            // Observação
            String observacao = obterValorCelula(row, COL_OBSERVACAO);
            if (observacao != null && !observacao.isEmpty()) {
                solicitacao.setObservacao(observacao.trim());
            }
            
            return solicitacao;
            
        } catch (Exception e) {
            LOGGER.error("Erro ao mapear linha {}: {}", row.getRowNum(), e.getMessage());
            return null;
        }
    }
    
    /**
     * Obtém o valor de uma célula como String.
     */
    private static String obterValorCelula(Row row, int colIndex) {
        Cell cell = row.getCell(colIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) {
            return null;
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toInstant()
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDate()
                        .format(DATE_FORMATTER);
                }
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }
    
    /**
     * Valida se um arquivo Excel é válido.
     */
    public static boolean validarArquivoExcel(File arquivo) {
        if (arquivo == null || !arquivo.exists()) {
            return false;
        }
        
        String nome = arquivo.getName().toLowerCase();
        if (!nome.endsWith(".xlsx") && !nome.endsWith(".xls")) {
            return false;
        }
        
        // Verificar se o arquivo pode ser lido
        try (InputStream input = new FileInputStream(arquivo)) {
            Workbook workbook = criarWorkbook(input, arquivo.getName());
            workbook.close();
            return true;
        } catch (Exception e) {
            LOGGER.error("Arquivo Excel inválido: {}", e.getMessage());
            return false;
        }
    }
}
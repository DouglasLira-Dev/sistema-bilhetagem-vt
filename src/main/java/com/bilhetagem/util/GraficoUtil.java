package com.bilhetagem.util;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import java.awt.*;
import java.util.Map;

/**
 * Classe utilitária para criação de gráficos.
 * 
 * <p>Facilita a criação de gráficos de barras e pizza
 * com estilos padronizados para o sistema.</p>
 * 
 * @author [Seu Nome]
 * @version 1.0.0
 * @since 2026-01-08
 */
public class GraficoUtil {
    
    // Cores padrão para gráficos
    public static final Color COR_PRIMARIA = new Color(52, 152, 219);
    public static final Color COR_SUCESSO = new Color(46, 204, 113);
    public static final Color COR_PERIGO = new Color(231, 76, 60);
    public static final Color COR_AVISO = new Color(241, 196, 15);
    public static final Color COR_INFO = new Color(155, 89, 182);
    
    /**
     * Cria um gráfico de barras.
     * 
     * @param titulo Título do gráfico
     * @param eixoX Rótulo do eixo X
     * @param eixoY Rótulo do eixo Y
     * @param dados Mapa com os dados (chave = categoria, valor = quantidade)
     * @return ChartPanel pronto para exibição
     */
    public static ChartPanel criarGraficoBarras(String titulo, String eixoX, String eixoY, 
                                                Map<String, Number> dados) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        for (Map.Entry<String, Number> entry : dados.entrySet()) {
            dataset.addValue(entry.getValue(), "Valores", entry.getKey());
        }
        
        JFreeChart chart = ChartFactory.createBarChart(
            titulo,
            eixoX,
            eixoY,
            dataset
        );
        
        // Personalização
        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setMaximumBarWidth(0.6);
        
        // Cores para cada barra
        Color[] cores = {COR_PRIMARIA, COR_SUCESSO, COR_PERIGO, COR_AVISO, COR_INFO};
        int i = 0;
        for (String key : dados.keySet()) {
            renderer.setSeriesPaint(i, cores[i % cores.length]);
            i++;
        }
        
        ChartPanel panel = new ChartPanel(chart);
        panel.setBackground(Color.WHITE);
        panel.setPreferredSize(new Dimension(500, 350));
        
        return panel;
    }
    
    /**
     * Cria um gráfico de pizza.
     * 
     * @param titulo Título do gráfico
     * @param dados Mapa com os dados (chave = categoria, valor = quantidade)
     * @param mostrarPorcentagem Mostrar porcentagem no gráfico
     * @return ChartPanel pronto para exibição
     */
    public static ChartPanel criarGraficoPizza(String titulo, Map<String, Number> dados, 
                                               boolean mostrarPorcentagem) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        
        for (Map.Entry<String, Number> entry : dados.entrySet()) {
            dataset.setValue(entry.getKey(), entry.getValue().doubleValue());
        }
        
        JFreeChart chart = ChartFactory.createPieChart(
            titulo,
            dataset,
            true, // Legend
            true, // Tooltips
            false // URLs
        );
        
        // Personalização
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setLabelFont(new Font("Arial", Font.PLAIN, 12));
        plot.setLabelBackgroundPaint(new Color(255, 255, 255, 200));
        
        // Cores para cada fatia
        Color[] cores = {COR_SUCESSO, COR_PERIGO, COR_AVISO, COR_PRIMARIA, COR_INFO};
        int i = 0;
        for (String key : dados.keySet()) {
            plot.setSectionPaint(key, cores[i % cores.length]);
            i++;
        }
        
        if (mostrarPorcentagem) {
            plot.setLabelGenerator(
                new org.jfree.chart.labels.StandardPieSectionLabelGenerator(
                    "{0}: {2}"
                )
            );
        }
        
        ChartPanel panel = new ChartPanel(chart);
        panel.setBackground(Color.WHITE);
        panel.setPreferredSize(new Dimension(500, 350));
        
        return panel;
    }
}
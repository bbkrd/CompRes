/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bbk.concurreport;

import ec.ui.view.AutoCorrelationsView;
import org.jfree.chart.JFreeChart;

/**
 *
 * @author s4504ch
 */
public class BbkAutoCorrelationsView extends AutoCorrelationsView {
    
    public BbkAutoCorrelationsView() {
        super();
    }

    public JFreeChart getChart() {
        return chartPanel.getChart();
    }
    
    
    
}

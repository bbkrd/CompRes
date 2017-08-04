/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bbk.outputcustomized.util;

import ec.nbdemetra.ui.properties.l2fprod.ColorChooser;
import ec.tss.tsproviders.utils.Formatters;
import ec.tstoolkit.timeseries.regression.OutlierEstimation;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.ui.grid.JTsGrid;
import ec.ui.grid.TsGridObs;
import ec.util.grid.CellIndex;
import ec.util.various.swing.StandardSwingColor;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTable;
import javax.swing.JToolTip;
import static javax.swing.SwingConstants.TRAILING;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author Thomas Witthohn
 */
public class JTsOutlierGrid extends JTsGrid {

    private transient final List<OutlierEstimation[]> outliers;
    private transient TsData d9;

    public JTsOutlierGrid() {
        super();
        outliers = new ArrayList<>();
        setCellRenderer(new OutlierCellRenderer());
    }

    public void setOutliers(OutlierEstimation[] outliers) {
        this.outliers.clear();
        this.outliers.add(outliers);
    }

    public void setD9(TsData d9) {
        this.d9 = d9;
    }

    @Override
    protected void onDataFormatChange() {
        updateOutlierCellRenderer();
    }

    private void updateOutlierCellRenderer() {
        grid.setDefaultRenderer(TsGridObs.class, new OutlierCellRenderer());
        grid.repaint();
    }

    // <editor-fold defaultstate="collapsed" desc="Cell Renderer">
    /*
     * Renderer coloring the cells containing outliers
     */
    private class OutlierCellRenderer extends DefaultTableCellRenderer {

        private final JToolTip tooltip;
        private final Formatters.Formatter<? super Number> valueFormatter;

        public OutlierCellRenderer() {
            valueFormatter = JTsOutlierGrid.this.themeSupport.getDataFormat().numberFormatter();
            setHorizontalAlignment(TRAILING);
            setOpaque(true);
            tooltip = super.createToolTip();
        }

        @Override
        public JToolTip createToolTip() {
            tooltip.setBackground(getBackground());
            tooltip.setForeground(getForeground());
            return tooltip;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setFont(table.getFont());

            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            CellIndex hoveredCell = grid.getHoveredCell();
            if (hoveredCell.equals(row, column)) {
                setBackground(StandardSwingColor.TABLE_SELECTION_BACKGROUND.value());
                setForeground(StandardSwingColor.TABLE_SELECTION_FOREGROUND.value());
            } else {
                setBackground(null);
                setForeground(null);
            }

            if (value instanceof TsGridObs) {
                TsGridObs obs = (TsGridObs) value;

                switch (obs.getInfo()) {
                    case Empty:
                        setText("");
                        setToolTipText(null);
                        break;
                    case Missing:
                        setText(".");
                        setToolTipText(null);
                        break;
                    case Valid:
                        /*
                         * Try to find a match between the outlier and the current cell
                         * using the TsPeriods
                         */
                        StringBuilder tooltipText = new StringBuilder();
                        tooltipText.append("<html>");
                        String valueText = valueFormatter.formatAsString(obs.getValue());

                        if (outliers.size() > obs.getSeriesIndex() && outliers.get(obs.getSeriesIndex()) != null) {
                            OutlierEstimation[] est = outliers.get(obs.getSeriesIndex());
                            boolean found = false;
                            int i = 0;
                            OutlierEstimation outlier = null;
                            while (!found && i < est.length) {
                                if (est[i].getPosition().equals(obs.getPeriod())) {
                                    found = true;
                                    outlier = est[i];
                                }
                                i++;
                            }

                            if (found && outlier != null) {
                                tooltipText.append("Outlier Value : ")
                                        .append(valueFormatter.formatAsString(outlier.getValue())).append("<br>")
                                        .append("TStat : ")
                                        .append(valueFormatter.formatAsString(outlier.getTStat())).append("<br>")
                                        .append("Outlier type : ")
                                        .append(outlier.getCode()).append("<br>");
                                setBackground(ColorChooser.getColor(outlier.getCode()));
                                setForeground(ColorChooser.getForeColor(outlier.getCode()));
                            }
                        }
                        if (d9 != null && d9.getFrequency() == obs.getPeriod().getFrequency() && Double.isFinite(d9.get(obs.getPeriod()))) {
                            tooltipText.append("Extreme Value Replacement : ")
                                    .append(valueFormatter.formatAsString(d9.get(obs.getPeriod()))).append("<br>");
                            valueText = "* " + valueText;
                        }

                        tooltipText.append("Period : ").append(obs.getPeriod().toString());
                        setText(valueText);
                        setToolTipText(tooltipText.toString());
                }
            }

            return this;
        }
    }
// </editor-fold>
}

/*
 * Copyright 2017 Deutsche Bundesbank
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the
 * Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl.html
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */
package de.bbk.concur.util;

import de.bbk.concur.FixedOutlier;
import ec.nbdemetra.ui.properties.l2fprod.ColorChooser;
import ec.tss.tsproviders.utils.Formatters;
import ec.tstoolkit.timeseries.regression.OutlierEstimation;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.ui.grid.JTsGrid;
import ec.ui.grid.TsGridObs;
import ec.util.grid.CellIndex;
import ec.util.various.swing.StandardSwingColor;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.JToolTip;
import static javax.swing.SwingConstants.TRAILING;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author Thomas Witthohn
 */
public class JTsOutlierGrid extends JTsGrid {

    private transient OutlierEstimation[] outliers;
    private transient FixedOutlier[] fixedOutliers;
    private transient TsData d9;

    public JTsOutlierGrid() {
        super();
        setCellRenderer(new OutlierCellRenderer());
    }

    public void setOutliers(OutlierEstimation[] outliers) {
        if (outliers == null) {
            this.outliers = null;
        } else {
            this.outliers = outliers.clone();
        }
    }

    public void setFixedOutliers(FixedOutlier[] fixedOutliers) {
        if (fixedOutliers == null) {
            this.fixedOutliers = null;
        } else {
            this.fixedOutliers = fixedOutliers.clone();
        }
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
                    default:
                        /*
                         * Try to find a match between the outlier and the current cell
                         * using the TsPeriods
                         */
                        StringBuilder tooltipText = new StringBuilder();
                        tooltipText.append("<html>");
                        String valueText = valueFormatter.formatAsString(obs.getValue());

                        boolean found = false;
                        for (OutlierEstimation outlier : outliers) {
                            if (outlier == null) {
                                continue;
                            }
                            if (outlier.getPosition().equals(obs.getPeriod())) {
                                tooltipText.append("Outlier Value : ")
                                        .append(valueFormatter.formatAsString(outlier.getValue())).append("<br>")
                                        .append("TStat : ")
                                        .append(valueFormatter.formatAsString(outlier.getTStat())).append("<br>")
                                        .append("Outlier type : ")
                                        .append(outlier.getCode()).append("<br>");
                                setBackground(ColorChooser.getColor(outlier.getCode()));
                                setForeground(ColorChooser.getForeColor(outlier.getCode()));
                                found = true;
                                break;
                            }
                        }

                        if (!found) {
                            for (FixedOutlier fixedOutlier : fixedOutliers) {
                                if (fixedOutlier == null) {
                                    continue;
                                }
                                if (fixedOutlier.getPosition().equals(obs.getPeriod())) {
                                    tooltipText.append("Fixed Outlier Value : ")
                                            .append(valueFormatter.formatAsString(fixedOutlier.getValue())).append("<br>")
                                            .append("Outlier type : ")
                                            .append(fixedOutlier.getCode()).append("<br>");
                                    setBackground(ColorChooser.getColor(fixedOutlier.getCode()));
                                    setForeground(ColorChooser.getForeColor(fixedOutlier.getCode()));
                                    break;
                                }
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

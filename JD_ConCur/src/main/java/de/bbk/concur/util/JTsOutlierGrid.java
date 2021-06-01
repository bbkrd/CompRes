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
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import ec.ui.grid.JTsGrid;
import ec.ui.grid.TsGridObs;
import ec.util.grid.CellIndex;
import ec.util.various.swing.StandardSwingColor;
import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JTable;
import javax.swing.JToolTip;
import static javax.swing.SwingConstants.TRAILING;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author Thomas Witthohn
 */
public class JTsOutlierGrid extends JTsGrid {

    private transient TsData d9;
    private transient Map<TsPeriod, List<Object>> outlierMap = new HashMap<>();

    public JTsOutlierGrid() {
        super();
        setCellRenderer(new OutlierCellRenderer());
    }

    public void setOutliers(OutlierEstimation[] outliers) {
        if (outliers != null) {
            for (OutlierEstimation outlier : outliers) {
                TsPeriod position = outlier.getPosition();
                List<Object> list = outlierMap.getOrDefault(position, new ArrayList<>());
                list.add(outlier);
                outlierMap.put(position, list);
            }
        }
    }

    public void setFixedOutliers(FixedOutlier[] fixedOutliers) {
        if (fixedOutliers != null) {
            for (FixedOutlier fixedOutlier : fixedOutliers) {
                TsPeriod position = fixedOutlier.getPosition();
                List<Object> list = outlierMap.getOrDefault(position, new ArrayList<>());
                list.add(fixedOutlier);
                outlierMap.put(position, list);
            }
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

                        List<Object> list = outlierMap.get(obs.getPeriod());

                        if (list != null) {
                            for (Object object : list) {
                                if (object instanceof OutlierEstimation) {
                                    OutlierEstimation outlier = (OutlierEstimation) object;
                                    tooltipText.append("Outlier Value : ")
                                            .append(valueFormatter.formatAsString(outlier.getValue())).append("<br>")
                                            .append("TStat : ")
                                            .append(valueFormatter.formatAsString(outlier.getTStat())).append("<br>")
                                            .append("Outlier type : ")
                                            .append(outlier.getCode()).append("<br>");
                                } else if (object instanceof FixedOutlier) {
                                    FixedOutlier fixedOutlier = (FixedOutlier) object;
                                    tooltipText.append("Fixed Outlier Value : ")
                                            .append(valueFormatter.formatAsString(fixedOutlier.getValue())).append("<br>")
                                            .append("Outlier type : ")
                                            .append(fixedOutlier.getCode()).append("<br>");
                                }
                            }
                            if (list.size() > 1) {
                                setBackground(Color.YELLOW);
                                setForeground(Color.BLACK);
                            } else {
                                String code;
                                Object o = list.get(0);
                                if (o instanceof OutlierEstimation) {
                                    code = ((OutlierEstimation) o).getCode();
                                } else if (o instanceof FixedOutlier) {
                                    code = ((FixedOutlier) o).getCode();
                                } else {
                                    code = "No Code";
                                }
                                setBackground(ColorChooser.getColor(code));
                                setForeground(ColorChooser.getForeColor(code));
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

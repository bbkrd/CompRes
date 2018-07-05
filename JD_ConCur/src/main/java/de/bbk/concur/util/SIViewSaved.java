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

import static de.bbk.concur.util.InPercent.convertTsDataInPercentIfMult;
import ec.nbdemetra.ui.DemetraUI;
import ec.satoolkit.DecompositionMode;
import ec.satoolkit.x11.X11Results;
import ec.tss.Ts;
import ec.tss.TsInformation;
import ec.tss.sa.documents.X13Document;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.timeseries.simplets.*;
import ec.ui.ATsView;
import ec.ui.chart.BasicXYDataset;
import ec.ui.chart.TsCharts;
import ec.ui.interfaces.ITsChart;
import ec.ui.view.JChartPanel;
import ec.ui.view.TsFrequencyTickUnit;
import ec.util.chart.ColorScheme;
import ec.util.chart.swing.ChartCommand;
import ec.util.chart.swing.Charts;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.AbstractRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;

/**
 *
 *
 * @author Christiane Hofer
 */
public class SIViewSaved extends ATsView {

    // CONSTANTS
    private static final int S_INDEX = 0;
    private static final int T_INDEX = 1;
    private static final int SI_INDEX = 2;
    private static final Stroke MARKER_STROKE = new BasicStroke(0.5f);
    private static final Paint MARKER_PAINT = Color.DARK_GRAY;
    private static final float MARKER_ALPHA = .8f;
    // OTHER
    private final Map<Bornes, Graphs> graphs_;
    private final JChartPanel chartPanel;
    private final XYLineAndShapeRenderer sRenderer;
    private final XYLineAndShapeRenderer tRenderer;
    private final XYLineAndShapeRenderer siDetailRenderer;
    private final XYLineAndShapeRenderer siMasterRenderer;
    private final JFreeChart masterChart;

    public JFreeChart getJFreeChart() {
        return masterChart;
    }

    /**
     *
     * @param period starts with 0
     *
     * @return
     */
    public JFreeChart getDetailChart(int period) {
        // Sortieren, so dass man mit int periode das richtige Intervall erwischt
        TreeMap<Double, Bornes> map = new TreeMap<>();
        graphs_.keySet().stream().forEach((b) -> {
            map.put(b.min_, b);
        });
        int i = 0;
        double dd = 0;
        for (Double d : map.keySet()) {
            if (i >= period) {
                dd = d;
                break;
            }
            i = i + 1;
        }
        Bornes bperiod = map.get(dd);

        Graphs g = graphs_.get(bperiod);
        showDetail(g, bperiod);

        return detailChart;
    }
    private final JFreeChart detailChart;
    private final DecimalFormat format = new DecimalFormat("0");
    private NumberFormat numberFormat;

    private final RevealObs revealObs;

    private static XYItemEntity highlight;

    static class Bornes {

        static final Bornes ZERO = new Bornes(0, 0);
        final double min_;
        final double max_;

        Bornes(double min, double max) {
            min_ = min;
            max_ = max;
        }
    }

    static class Graphs {

        final BasicXYDataset.Series S1_;
        final BasicXYDataset.Series S2_;
        final BasicXYDataset.Series S3_;
        final String label_;

        Graphs(BasicXYDataset.Series s1, BasicXYDataset.Series s2, BasicXYDataset.Series s3, String label) {
            S1_ = s1;
            S2_ = s2;
            S3_ = s3;
            label_ = label;
        }

        static double minYear(BasicXYDataset.Series S) {
            return S.getXValue(0);
        }

        static double maxYear(BasicXYDataset.Series S) {
            return S.getXValue(S.getItemCount() - 1);
        }

        int getMinYear() {
            double year = 9999;
            if (S1_.getItemCount() > 0 && minYear(S1_) < year) {
                year = minYear(S1_);
            }
            if (S2_.getItemCount() > 0 && minYear(S2_) < year) {
                year = minYear(S2_);
            }
            if (S3_.getItemCount() > 0 && minYear(S3_) < year) {
                year = minYear(S3_);
            }
            return (int) year;
        }

        int getMaxYear() {
            double year = 0;
            if (S1_ != null && maxYear(S1_) > year) {
                year = maxYear(S1_);
            }
            if (S2_ != null && maxYear(S2_) > year) {
                year = maxYear(S2_);
            }
            if (S3_ != null && maxYear(S3_) > year) {
                year = maxYear(S3_);
            }
            return (int) year;
        }
    }

    public SIViewSaved() {
        this.graphs_ = new HashMap<>();
        highlight = null;
        this.revealObs = new RevealObs();
        this.sRenderer = new LineRenderer(S_INDEX, true, false);
        this.tRenderer = new LineRenderer(T_INDEX, true, false);
        this.siDetailRenderer = new LineRenderer(SI_INDEX, false, true);
        this.siMasterRenderer = new LineRenderer(SI_INDEX, false, true);
        this.masterChart = createMasterChart(sRenderer, tRenderer, siMasterRenderer);
        this.detailChart = createDetailChart(sRenderer, tRenderer, siDetailRenderer);
        this.chartPanel = new JChartPanel(null);
        this.numberFormat = DemetraUI.getDefault().getDataFormat().newNumberFormat();
        initComponents();
    }

    private void initComponents() {
        siDetailRenderer.setAutoPopulateSeriesShape(false);
        siDetailRenderer.setBaseShape(new Ellipse2D.Double(-2, -2, 4, 4));
        siDetailRenderer.setBaseShapesFilled(false);

        siMasterRenderer.setAutoPopulateSeriesShape(false);
        siMasterRenderer.setBaseShape(new Ellipse2D.Double(-1, -1, 2, 2));
        siMasterRenderer.setBaseShapesFilled(false);

        enableRescaleOnResize();
        enableObsHighlight();
        enableMasterDetailSwitch();
        Charts.enableFocusOnClick(chartPanel);

        onComponentPopupMenuChange();
        enableProperties();

        setLayout(new BorderLayout());
        add(chartPanel, BorderLayout.CENTER);
    }

    private void enableProperties() {
        addPropertyChangeListener(evt -> {
            switch (evt.getPropertyName()) {
                case "componentPopupMenu":
                    onComponentPopupMenuChange();
                    break;
            }
        });
    }

    private void enableObsHighlight() {
        chartPanel.addChartMouseListener(new HighlightChartMouseListener2());
        chartPanel.addKeyListener(revealObs);
    }

    private void enableMasterDetailSwitch() {
        chartPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                    if (masterChart.equals(chartPanel.getChart())) {
                        double x = chartPanel.getChartX(e.getX());
                        Graphs g = null;
                        Bornes fb = Bornes.ZERO;
                        for (Bornes b : graphs_.keySet()) {
                            if (x >= b.min_ && x <= b.max_) {
                                g = graphs_.get(b);
                                fb = b;
                                break;
                            }
                        }
                        if (g == null) {
                            return;
                        }

                        showDetail(g, fb);
                    } else if (detailChart.equals(chartPanel.getChart())) {
                        showMain();
                    }
                }
            }
        });
    }

    private void enableRescaleOnResize() {
        chartPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (chartPanel.getChart() != null) {
                    rescaleAxis((NumberAxis) chartPanel.getChart().getXYPlot().getRangeAxis());
                }
            }
        });
    }

    //<editor-fold defaultstate="collapsed" desc="Event handlers">
    private void onComponentPopupMenuChange() {
        JPopupMenu popupMenu = getComponentPopupMenu();
        chartPanel.setComponentPopupMenu(popupMenu != null ? popupMenu : buildMenu().getPopupMenu());
    }

    @Override
    protected void onDataFormatChange() {
        numberFormat = DemetraUI.getDefault().getDataFormat().newNumberFormat();
    }

    @Override
    protected void onTsChange() {
        TsInformation ts = getTsInformation();
        setData(ts != null ? ts.data : null);
    }

    @Override
    protected void onColorSchemeChange() {
        sRenderer.setBasePaint(themeSupport.getLineColor(ColorScheme.KnownColor.BLUE));
        tRenderer.setBasePaint(themeSupport.getLineColor(ColorScheme.KnownColor.RED));
        siDetailRenderer.setBasePaint(themeSupport.getLineColor(ColorScheme.KnownColor.GRAY));
        siMasterRenderer.setBasePaint(themeSupport.getLineColor(ColorScheme.KnownColor.GRAY));

        XYPlot mainPlot = masterChart.getXYPlot();
        mainPlot.setBackgroundPaint(themeSupport.getPlotColor());
        mainPlot.setDomainGridlinePaint(themeSupport.getGridColor());
        mainPlot.setRangeGridlinePaint(themeSupport.getGridColor());
        masterChart.setBackgroundPaint(themeSupport.getBackColor());

        XYPlot detailPlot = detailChart.getXYPlot();
        detailPlot.setBackgroundPaint(themeSupport.getPlotColor());
        detailPlot.setDomainGridlinePaint(themeSupport.getGridColor());
        detailPlot.setRangeGridlinePaint(themeSupport.getGridColor());
        detailChart.setBackgroundPaint(themeSupport.getBackColor());
    }
    //</editor-fold>

    private JMenu buildMenu() {
        JMenu result = new JMenu();

        result.add(new JMenuItem(new AbstractAction("Show main") {
            @Override
            public void actionPerformed(ActionEvent e) {
                showMain();
            }
        }));

        JMenu export = new JMenu("Export image to");
        export.add(ChartCommand.printImage().toAction(chartPanel)).setText("Printer...");
        export.add(ChartCommand.copyImage().toAction(chartPanel)).setText("Clipboard");
        export.add(ChartCommand.saveImage().toAction(chartPanel)).setText("File...");
        result.add(export);

        return result;
    }

    private void showMain() {
        chartPanel.setChart(masterChart);
        onColorSchemeChange();
    }

    private void showDetail(Graphs g, Bornes fb) {
        XYPlot plot = detailChart.getXYPlot();

        NumberAxis yAxis = new NumberAxis();
        yAxis.setTickLabelPaint(Color.GRAY);
        rescaleAxis(yAxis);
        plot.setRangeAxis(yAxis);

        NumberAxis xAxis = new NumberAxis();
        xAxis.setTickLabelPaint(Color.GRAY);
        xAxis.setTickUnit(new NumberTickUnit(1), true, false);
        xAxis.setNumberFormatOverride(new DecimalFormat("0000"));
        xAxis.setRange(g.getMinYear() - 1, g.getMaxYear() + 1);
        plot.setDomainAxis(xAxis);

        plot.setDataset(S_INDEX, new BasicXYDataset(Collections.singletonList(g.S2_)));
        plot.setDataset(T_INDEX, new BasicXYDataset(Collections.singletonList(g.S1_)));
        plot.setDataset(SI_INDEX, new BasicXYDataset(Collections.singletonList(g.S3_)));

        detailChart.setTitle(g.label_);
        chartPanel.setChart(detailChart);
        onColorSchemeChange();
    }

    public void setSiData(TsData seas, TsData si, TsData seas_saved) {

        setData(seas, si, seas_saved);
    }

    public void setData(TsData seas) {
        reset();
        if (seas != null) {
            displayData(seas, null, null);
        }
    }

    public void setDoc(X13Document doc) {

        if (doc == null) {
            this.reset();
            return;
        }

        Ts seasonalfactor = TsData_Saved.convertMetaDataToTs(doc.getMetaData(), SavedTables.SEASONALFACTOR);
        if (seasonalfactor == null) {
            this.reset();
            return;
        }
        X11Results x11 = doc.getDecompositionPart();
        DecompositionMode mode = doc.getDecompositionPart().getSeriesDecomposition().getMode();
        if (x11 != null) {

            TsData si = x11.getData("d8", TsData.class);
            TsData seas = x11.getData("d10", TsData.class);

            if (x11.getSeriesDecomposition().getMode() == DecompositionMode.LogAdditive) {
                si = si.exp();
            }
            seas = convertTsDataInPercentIfMult(seas, mode.isMultiplicative());
            si = convertTsDataInPercentIfMult(si, mode.isMultiplicative());
            this.setSiData(seas, si, seasonalfactor.getTsData());

        } else {
            this.reset();
        }

    }

    /**
     * This method needs a saved D10 else the wrong class is used
     *
     * @param seas D10
     * @param irr D8
     * @param seas_saved D10_saved
     */
    public void setData(TsData seas, TsData irr, TsData seas_saved) {
        reset();
        if (seas == null && irr == null) {
            return;
        }
        if (seas == null) {
            seas = new TsData(irr.getDomain(), 1);
        } else if (irr == null) {
            irr = new TsData(seas.getDomain(), 1);
        }
        displayData(seas, irr, seas_saved);

    }

    private void displayData(TsData seas, TsData irr, TsData seas_saved) {
        TsFrequency freq = seas.getFrequency();
        if (freq == TsFrequency.Undefined) {
            return;
        }

        TsPeriod end = seas.getEnd().minus(1);
        TsPeriod start = seas.getStart();
        int np = end.getYear() - start.getYear();

        BasicXYDataset sDataset = new BasicXYDataset();
        BasicXYDataset siDataset = new BasicXYDataset();
        BasicXYDataset tDataset = new BasicXYDataset();
        PeriodIterator speriods = new PeriodIterator(seas);
        double xstart = -0.4;
        double xend = 0.4;
        final double xstep = 0.8 / np;
        int il = 0;
        while (speriods.hasMoreElements()) {
            TsDataBlock datablock = speriods.nextElement();
            DataBlock src = datablock.data;
            int startyear = datablock.start.getYear();
//            int endyear = startyear + datablock.data.getLength() - 1;

            String key = "p" + Integer.toString(il);

            int n = src.getLength();
            if (n > 0) {
                //  double m = src.sum() / n;
//                double[] tX = {xstart, xend};
//                double[] tX2 = {startyear, endyear};
//                double[] tY = {m, m};

                double[] tX = new double[n];
                double[] tX2 = new double[n];
                double[] tY = new double[n];
                double[] sX = new double[n];
                double[] sX2 = new double[n];
                double[] sY = new double[n];
                double[] siY = new double[n];

                double x = xstart + xstep * (startyear - start.getYear());
                for (int i = 0; i < n; ++i, x += xstep, startyear++) {
                    sX[i] = x;
                    sX2[i] = startyear;
                    sY[i] = src.get(i);
                    if (irr != null) {
                        int pos = irr.getDomain().search(datablock.period(i));
                        siY[i] = irr.get(pos);
                    }

                    tX[i] = x;
                    tX2[i] = startyear;
                    if (seas_saved != null) {
                        if (seas_saved.getDomain().search(datablock.period(i)) != -1) {
                            int pos = seas_saved.getDomain().search(datablock.period(i));
                            tY[i] = seas_saved.get(pos);
                        } else {
                            tY[i] = 1;
                        }
                    } else {
                        tY[i] = Double.NaN;
                    }
                }

                BasicXYDataset.Series t = BasicXYDataset.Series.of(key, tX, tY);
                BasicXYDataset.Series t2 = BasicXYDataset.Series.of(key, tX2, tY);

                BasicXYDataset.Series s = BasicXYDataset.Series.of(key, sX, sY);
                BasicXYDataset.Series s2 = BasicXYDataset.Series.of(key, sX2, sY);

                BasicXYDataset.Series si = irr != null ? BasicXYDataset.Series.of(key, sX, siY) : BasicXYDataset.Series.empty(key);
                BasicXYDataset.Series si2 = irr != null ? BasicXYDataset.Series.of(key, sX2, siY) : BasicXYDataset.Series.empty(key);

                SIViewSaved.Bornes b = new SIViewSaved.Bornes(xstart, xend);
                SIViewSaved.Graphs g = new SIViewSaved.Graphs(t2, s2, si2, TsPeriod.formatPeriod(freq, il));
                graphs_.put(b, g);

                sDataset.addSeries(s);
                tDataset.addSeries(t);
                siDataset.addSeries(si);
            }

            xstart++;
            xend++;
            il++;
        }

        XYPlot plot = masterChart.getXYPlot();
        configureAxis(plot, freq);
        plot.setDataset(S_INDEX, sDataset);
        plot.setDataset(T_INDEX, tDataset);
        plot.setDataset(SI_INDEX, siDataset);

        showMain();
    }

    public void reset() {
        graphs_.clear();
        chartPanel.setChart(null);
    }

    static void configureAxis(XYPlot plot, TsFrequency freq) {
        NumberAxis yAxis = new NumberAxis();
        yAxis.setTickLabelPaint(Color.GRAY);
        rescaleAxis(yAxis);
        plot.setRangeAxis(yAxis);

        NumberAxis xAxis = new NumberAxis();
        xAxis.setTickLabelPaint(Color.GRAY);
        int periods = freq.intValue();
        xAxis.setTickUnit(new TsFrequencyTickUnit(freq));
        xAxis.setRange(-0.5, periods - 0.5);
        plot.setDomainAxis(xAxis);
        plot.setDomainGridlinesVisible(false);
        for (int i = 0; i < periods; i++) {
            ValueMarker marker = new ValueMarker(i + 0.5);
            marker.setStroke(MARKER_STROKE);
            marker.setPaint(MARKER_PAINT);
            marker.setAlpha(MARKER_ALPHA);
            plot.addDomainMarker(marker);
        }
    }

    static void rescaleAxis(NumberAxis axis) {
        axis.setAutoRangeIncludesZero(false);
    }

    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
    }

    static JFreeChart createMasterChart(XYLineAndShapeRenderer sRenderer, XYLineAndShapeRenderer tRenderer, XYLineAndShapeRenderer siRenderer2) {
        XYPlot plot = new XYPlot();

        plot.setDataset(S_INDEX, Charts.emptyXYDataset());
        plot.setRenderer(S_INDEX, sRenderer);
        plot.mapDatasetToDomainAxis(S_INDEX, 0);
        plot.mapDatasetToRangeAxis(S_INDEX, 0);

        plot.setDataset(T_INDEX, Charts.emptyXYDataset());
        plot.setRenderer(T_INDEX, tRenderer);
        plot.mapDatasetToDomainAxis(T_INDEX, 0);
        plot.mapDatasetToRangeAxis(T_INDEX, 0);

        plot.setDataset(SI_INDEX, Charts.emptyXYDataset());
        plot.setRenderer(SI_INDEX, siRenderer2);
        plot.mapDatasetToDomainAxis(SI_INDEX, 0);
        plot.mapDatasetToRangeAxis(SI_INDEX, 0);
        plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);

        JFreeChart result = new JFreeChart("", TsCharts.CHART_TITLE_FONT, plot, false);
        result.setPadding(TsCharts.CHART_PADDING);
        return result;
    }

    static JFreeChart createDetailChart(XYLineAndShapeRenderer sRenderer, XYLineAndShapeRenderer tRenderer, XYLineAndShapeRenderer siRenderer1) {
        XYPlot plot = new XYPlot();

        plot.setDataset(S_INDEX, Charts.emptyXYDataset());
        plot.setRenderer(S_INDEX, sRenderer);
        plot.mapDatasetToDomainAxis(S_INDEX, 0);
        plot.mapDatasetToRangeAxis(S_INDEX, 0);

        plot.setDataset(T_INDEX, Charts.emptyXYDataset());
        plot.setRenderer(T_INDEX, tRenderer);
        plot.mapDatasetToDomainAxis(T_INDEX, 0);
        plot.mapDatasetToRangeAxis(T_INDEX, 0);

        plot.setDataset(SI_INDEX, Charts.emptyXYDataset());
        plot.setRenderer(SI_INDEX, siRenderer1);
        plot.mapDatasetToDomainAxis(SI_INDEX, 0);
        plot.mapDatasetToRangeAxis(SI_INDEX, 0);

        JFreeChart result = new JFreeChart("", TsCharts.CHART_TITLE_FONT, plot, false);
        result.setPadding(TsCharts.CHART_PADDING);
        return result;
    }

    private final class HighlightChartMouseListener2 implements ChartMouseListener {

        @Override
        public void chartMouseClicked(ChartMouseEvent event) {
        }

        @Override
        public void chartMouseMoved(ChartMouseEvent event) {
            if (event.getEntity() instanceof XYItemEntity) {
                XYItemEntity xxx = (XYItemEntity) event.getEntity();
                setHighlightedObs(xxx);
            } else {
                setHighlightedObs(null);
            }
        }
    }

    private void setHighlightedObs(XYItemEntity item) {
        if (item == null || highlight != item) {
            highlight = item;
            chartPanel.getChart().fireChartChanged();
        }
    }

    private static final Shape ITEM_SHAPE = new Ellipse2D.Double(-2, -2, 4, 4);

    private final class LineRenderer extends XYLineAndShapeRenderer {

        private final int index;
        private final Color color;

        public LineRenderer(int index, boolean lines, boolean shapes) {
            setBaseLinesVisible(lines);
            setBaseShapesVisible(shapes);
            setBaseItemLabelsVisible(true);
            setAutoPopulateSeriesShape(false);
            setAutoPopulateSeriesFillPaint(false);
            setAutoPopulateSeriesOutlineStroke(false);
            setAutoPopulateSeriesPaint(false);
            setBaseShape(ITEM_SHAPE);
            setUseFillPaint(true);
            this.index = index;
            switch (index) {
                case S_INDEX:
                    this.color = themeSupport.getLineColor(ColorScheme.KnownColor.BLUE);
                    break;
                case T_INDEX:
                    this.color = themeSupport.getLineColor(ColorScheme.KnownColor.RED);
                    break;
                case SI_INDEX:
                    this.color = themeSupport.getLineColor(ColorScheme.KnownColor.GRAY);
                    break;
                default:
                    this.color = themeSupport.getLineColor(ColorScheme.KnownColor.GRAY);
            }
        }

        @Override
        public boolean getItemShapeVisible(int series, int item) {
            return index == SI_INDEX || revealObs.isEnabled() || isObsHighlighted(series, item);
        }

        private boolean isObsHighlighted(int series, int item) {
            XYPlot plot = (XYPlot) chartPanel.getChart().getPlot();
            if (highlight != null && highlight.getDataset().equals(plot.getDataset(index))) {
                return highlight.getSeriesIndex() == series && highlight.getItem() == item;
            } else {
                return false;
            }
        }

        @Override
        public boolean isItemLabelVisible(int series, int item) {
            return isObsHighlighted(series, item);
        }

        @Override
        public Paint getSeriesPaint(int series) {
            return color;
        }

        @Override
        public Paint getItemPaint(int series, int item) {
            return color;
        }

        @Override
        public Paint getItemFillPaint(int series, int item) {
            return chartPanel.getChart().getPlot().getBackgroundPaint();
        }

        @Override
        public Stroke getSeriesStroke(int series) {
            return TsCharts.getNormalStroke(ITsChart.LinesThickness.Thin);
        }

        @Override
        public Stroke getItemOutlineStroke(int series, int item) {
            return TsCharts.getNormalStroke(ITsChart.LinesThickness.Thin);
        }

        @Override
        protected void drawItemLabel(Graphics2D g2, PlotOrientation orientation, XYDataset dataset, int series, int item, double x, double y, boolean negative) {
            String label = generateLabel();
            Font font = chartPanel.getFont();
            Paint paint = chartPanel.getChart().getPlot().getBackgroundPaint();
            Paint fillPaint = color;
            Stroke outlineStroke = AbstractRenderer.DEFAULT_STROKE;
            Charts.drawItemLabelAsTooltip(g2, x, y, 3d, label, font, paint, fillPaint, paint, outlineStroke);
        }

        private String generateLabel() {
            XYDataset dataset = highlight.getDataset();
            int series = highlight.getSeriesIndex();
            int item = highlight.getItem();

            String x = "";
            if (dataset.getSeriesCount() == 1) {
                x = Integer.toString((int) dataset.getXValue(series, item));
            } else {
                for (Bornes b : graphs_.keySet()) {
                    if (dataset.getXValue(series, item) >= b.min_ && dataset.getXValue(series, item) <= b.max_) {
                        x = format.format(graphs_.get(b).S3_.getXValue(item));
                    }
                }
            }

            double y = dataset.getYValue(series, item);
            return x + "\nValue : " + numberFormat.format(y);
        }
    }

    private final class RevealObs implements KeyListener {

        private boolean enabled = false;

        @Override
        public void keyTyped(KeyEvent e) {
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyChar() == 'r') {
                setEnabled(true);
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            if (e.getKeyChar() == 'r') {
                setEnabled(false);
            }
        }

        private void setEnabled(boolean enabled) {
            if (this.enabled != enabled) {
                this.enabled = enabled;
                firePropertyChange("revealObs", !enabled, enabled);
                chartPanel.getChart().fireChartChanged();
            }
        }

        public boolean isEnabled() {
            return enabled;
        }
    }
}

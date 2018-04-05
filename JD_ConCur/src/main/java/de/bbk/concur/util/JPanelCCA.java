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

import ec.nbdemetra.ui.NbComponents;
import ec.nbdemetra.ui.properties.l2fprod.ColorChooser;
import ec.satoolkit.DecompositionMode;
import ec.satoolkit.x11.X11Results;
import ec.tss.Ts;
import ec.tss.TsFactory;
import ec.tss.documents.DocumentManager;
import ec.tss.html.CssProperty;
import ec.tss.html.CssStyle;
import ec.tss.html.HtmlStream;
import ec.tss.html.HtmlTag;
import ec.tss.sa.documents.X13Document;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.timeseries.regression.OutlierEstimation;
import ec.tstoolkit.timeseries.simplets.*;
import ec.ui.grid.JTsGrid;
import ec.ui.interfaces.IDisposable;
import ec.ui.interfaces.ITsCollectionView;
import ec.ui.interfaces.ITsGrid;
import java.awt.Color;
import java.awt.Dimension;
import java.io.IOException;
import java.util.Arrays;
import java.util.Formatter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

/**
 *
 * @author Christiane Hofer
 */
public class JPanelCCA extends JPanel implements IDisposable {

    private final static String FMT = "%.2f";
    private final JTsOutlierGrid d8Grid;
    private final JTsGrid d10Grid, d10SavedGrid;
    private final JSplitPane d8Pane;
    private final JSplitPane d10aPane;
    private final JSplitPane d10aOldPane;
    private transient X13Document doc;
    private static final int ROW_HEIGHT = 19;
    private static final int ROW_WIDTH = 70; //70 war mal gut, es muss ausrichend breit sein,
    // keine Ahnugn woraus man das erreichen muss
    private int freq;
    private Ts d8, d10, savedD10;
    private TsData d9;
    private OutlierEstimation[] both;

    public JPanelCCA() {

        this.d8Grid = new JTsOutlierGrid();
        d8Grid.setTsUpdateMode(ITsCollectionView.TsUpdateMode.None);
        d8Grid.setMode(ITsGrid.Mode.SINGLETS);

        this.d10Grid = new JTsGrid();
        d10Grid.setTsUpdateMode(ITsCollectionView.TsUpdateMode.None);
        d10Grid.setMode(ITsGrid.Mode.SINGLETS);

        this.d10SavedGrid = new JTsGrid();
        d10SavedGrid.setTsUpdateMode(ITsCollectionView.TsUpdateMode.None);
        d10SavedGrid.setMode(ITsGrid.Mode.SINGLETS);

        JLabel lblD8 = new JLabel("D8B", JLabel.CENTER);
        d8Pane = NbComponents.newJSplitPane(JSplitPane.VERTICAL_SPLIT, lblD8, d8Grid);

        d8Pane.setEnabled(false);
        d8Pane.setDividerSize(0);
        JLabel lblD10a = new JLabel(SavedTables.NAME_SEASONAL_FACTOR, JLabel.CENTER);
        d10aPane = NbComponents.newJSplitPane(JSplitPane.VERTICAL_SPLIT, lblD10a, d10Grid);

        d10aPane.setEnabled(false);
        d10aPane.setDividerSize(0);

        JLabel lblD10aOld = new JLabel(SavedTables.NAME_SEASONAL_FACTOR_SAVED, JLabel.CENTER);
        d10aOldPane = NbComponents.newJSplitPane(JSplitPane.VERTICAL_SPLIT, lblD10aOld, d10SavedGrid);

        d10aOldPane.setEnabled(false);
        d10aOldPane.setDividerSize(0);

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.add(d8Pane);
        this.add(d10aPane);
        this.add(d10aOldPane);
    }

    public JPanel getCCAPanel() {
        JPanel jPanel = new JPanel();
        jPanel.add(this);
        int width = d8Pane.getMaximumSize().width;
        int height = d8Pane.getMaximumSize().height + d10aPane.getMaximumSize().height * 2;
        jPanel.setSize(width, height);
        jPanel.setMaximumSize(new Dimension(width, height));
        jPanel.setMinimumSize(new Dimension(width, height));
        jPanel.setPreferredSize(new Dimension(width, height));
        return jPanel;
    }

    public void getTablesAsHtml(HtmlStream stream) {
        try {
            stream.write("<table style=\"table-layout:fixed\"");
            stream.open(HtmlTag.TABLEROW);
            stream.write("<th colspan=\"")
                    .write(String.valueOf(freq + 1))
                    .write("\">")
                    .write("D8B");
            stream.close(HtmlTag.TABLEHEADER);
            stream.close(HtmlTag.TABLEROW);
            d8b(stream);

            stream.open(HtmlTag.TABLEROW);
            stream.write("<th colspan=\"")
                    .write(String.valueOf(freq + 1))
                    .write("\" style=\"text-align:left\">")
                    .write("new");
            stream.close(HtmlTag.TABLEHEADER);
            stream.close(HtmlTag.TABLEROW);
            singleTsToHtml(stream, d10);

            stream.open(HtmlTag.TABLEROW);
            stream.write("<th colspan=\"")
                    .write(String.valueOf(freq + 1))
                    .write("\" style=\"text-align:left\">")
                    .write("current");

            stream.close(HtmlTag.TABLEHEADER);
            stream.close(HtmlTag.TABLEROW);
            singleTsToHtml(stream, savedD10);

            if (doc.getMetaData() != null) {
                String savedID = doc.getMetaData().get("prodebene.seasonalfactor.loadid");
                if (savedID != null) {
                    stream.open(HtmlTag.TABLEROW);
                    stream.write("<th colspan=\"")
                            .write(String.valueOf(freq + 1))
                            .write("\">")
                            .write("Loaded from ")
                            .write(savedID);
                    stream.close(HtmlTag.TABLEHEADER);
                    stream.close(HtmlTag.TABLEROW);
                }
            }
            stream.close(HtmlTag.TABLE);
        } catch (IOException ex) {
            Logger.getLogger(JPanelCCA.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void set(X13Document doc) {
        this.doc = doc;
        if (doc.getSeries() != null) {
            this.freq = doc.getSeries().getFrequency().intValue();
        }
        X11Results x11 = doc.getDecompositionPart();
        CompositeResults results = doc.getResults();

        //  boolean isLog = doc.getResults().getData("log", Boolean.class);
        DecompositionMode mode = results.getData("mode", DecompositionMode.class);
        if (x11 != null) {
            d8 = getMainSeries("decomposition.d-tables.d8");
            d8 = InPercent.convertTsInPercentIfMult(d8, mode.isMultiplicative());
            TsData d8Data = d8.getTsData();
            if (d8Data.getDomain().getYearsCount() > 10) {
                TsDomain domMax10years = new TsDomain(d8Data.getEnd().minus(d8Data.getFrequency().intValue() * 10), d8Data.getFrequency().intValue() * 10);
                d8Data = d8Data.fittoDomain(domMax10years);
            }
            TsDomain domain = new TsDomain(d8Data.getEnd().minus(d8Data.getFrequency().intValue()), d8Data.getFrequency().intValue());
            d8.set(d8Data);
            d8Grid.getTsCollection().replace(d8);
            d8Grid.setSelection(d8Grid.getTsCollection().toArray());
            fixSize(d8Pane, d8.getTsData().getDomain().getFrequency().intValue(), d8.getTsData().getDomain().getYearsCount());
            if (doc.getPreprocessingPart() != null) {
                OutlierEstimation[] prespecified = doc.getPreprocessingPart().outliersEstimation(true, true);
                OutlierEstimation[] estimations = doc.getPreprocessingPart().outliersEstimation(true, false);

                both = Arrays.copyOf(prespecified, prespecified.length + estimations.length);
                System.arraycopy(estimations, 0, both, prespecified.length, estimations.length);
                d8Grid.setOutliers(both);
            }

            Ts d9Ts = getMainSeries("decomposition.d-tables.d9");
            d9Ts = InPercent.convertTsInPercentIfMult(d9Ts, mode.isMultiplicative());
            d9 = d9Ts.getTsData();
            d8Grid.setD9(d9);

            d10 = getMainSeries("decomposition.d-tables.d10");
            d10 = InPercent.convertTsInPercentIfMult(d10, mode.isMultiplicative());
            d10.set(d10.getTsData().fittoDomain(domain));
            fixSize(d10aPane, d10.getTsData().getFrequency().intValue(), d10.getTsData().getDomain().getYearsCount());

            d10Grid.getTsCollection().replace(d10);
            d10Grid.setSelection(d10Grid.getTsCollection().toArray());

            savedD10 = TsData_Saved.convertMetaDataToTs(doc.getMetaData(), SavedTables.SEASONALFACTOR);
            if (savedD10 != null && savedD10.getTsData() != null && savedD10.getTsData().getFrequency().intValue() != d10.getTsData().getFrequency().intValue()) {
                savedD10 = TsFactory.instance.createTs(savedD10.getName());
                savedD10.setInvalidDataCause("Frequency mismatch");
            } else if (savedD10 != null && savedD10.getTsData() != null) {
                TsDomain intersectionDomain = savedD10.getTsData().getDomain().intersection(domain);
                savedD10 = TsFactory.instance.createTs(savedD10.getName(), savedD10.getMetaData(), savedD10.getTsData().fittoDomain(intersectionDomain));
                fixSize(d10aOldPane, domain.getFrequency().intValue(), domain.getYearsCount());
            }
            d10SavedGrid.getTsCollection().replace(savedD10);
            d10SavedGrid.setSelection(d10SavedGrid.getTsCollection().toArray());

        } else {
            d8Grid.getTsCollection().clear();
            d10Grid.getTsCollection().clear();
            d10SavedGrid.getTsCollection().clear();
        }

    }

    private void fixSize(JSplitPane pane, int periods, int years) {
        int widthcomplete = periods * ROW_WIDTH + 2 * ROW_WIDTH;
        int heightcomplete = (years + 1) * ROW_HEIGHT + 16;
        pane.setMaximumSize(new Dimension(widthcomplete, heightcomplete));
        pane.setMinimumSize(new Dimension(widthcomplete, heightcomplete));
        pane.setPreferredSize(new Dimension(widthcomplete, heightcomplete));

    }

    private Ts getMainSeries(String str) {
        return DocumentManager.instance.getTs(doc, str);
    }

    @Override
    public void dispose() {
        doc = null;
        d8Grid.dispose();
        d10Grid.dispose();
        d10SavedGrid.dispose();
        removeAll();
        setLayout(null);
    }

    private void singleTsToHtml(HtmlStream stream, Ts ts) throws IOException {
        TsData data = ts.getTsData();
        if (data == null) {
            return;
        }

        int nfreq = data.getFrequency().intValue();
        YearIterator iter = new YearIterator(data);
        while (iter.hasMoreElements()) {
            stream.open(HtmlTag.TABLEROW);
            TsDataBlock block = iter.nextElement();
            stream.write(HtmlTag.TABLEHEADER,
                         Integer.toString(block.start.getYear()));
            int start = block.start.getPosition();
            int n = block.data.getLength();
            for (int i = 0; i < start; ++i) {
                stream.write(HtmlTag.TABLECELL);
            }
            for (int i = 0; i < n; ++i) {
                if (Double.isFinite(block.data.get(i))) {
                    Formatter formatter = new Formatter();
                    formatter.format(FMT, block.data.get(i));
                    stream.write(HtmlTag.TABLECELL, formatter.toString());
                } else {
                    stream.write(HtmlTag.TABLECELL, ".");
                }
            }

            for (int i = start + n; i < nfreq; ++i) {
                stream.write(HtmlTag.TABLECELL);
            }
            stream.close(HtmlTag.TABLEROW);
        }
    }

    private void d8b(HtmlStream stream) throws IOException {
        TsData data = d8.getTsData();
        if (data == null) {
            return;
        }

        stream.open(HtmlTag.TABLEROW);
        stream.write(HtmlTag.TABLEHEADER);
        for (int i = 0; i < data.getFrequency().intValue(); ++i) {
            stream.write(HtmlTag.TABLEHEADER, TsPeriod.formatShortPeriod(data.getFrequency(), i));
        }
        stream.close(HtmlTag.TABLEROW);

        int nfreq = data.getFrequency().intValue();
        YearIterator iter = new YearIterator(data);
        while (iter.hasMoreElements()) {
            stream.open(HtmlTag.TABLEROW);
            TsDataBlock block = iter.nextElement();
            stream.write(HtmlTag.TABLEHEADER,
                         Integer.toString(block.start.getYear()));
            int start = block.start.getPosition();
            int n = block.data.getLength();
            for (int i = 0; i < start; ++i) {
                stream.write(HtmlTag.TABLECELL);
            }
            for (int i = 0; i < n; ++i) {
                if (Double.isFinite(block.data.get(i))) {
                    Formatter formatter = new Formatter();
                    formatter.format(FMT, block.data.get(i));

                    CssStyle outlierColor = new CssStyle();
                    if (both != null) {
                        boolean found = false;
                        int j = 0;
                        OutlierEstimation outlier = null;
                        while (!found && j < both.length) {
                            if (both[j].getPosition().equals(block.start.plus(i))) {
                                found = true;
                                outlier = both[j];
                            }
                            ++j;
                        }

                        if (found && outlier != null) {
                            Color bgColor = ColorChooser.getColor(outlier.getCode());
                            String bgColorString = new StringBuilder().append("rgb(")
                                    .append(bgColor.getRed()).append(",")
                                    .append(bgColor.getGreen()).append(",")
                                    .append(bgColor.getBlue()).append(")").toString();
                            outlierColor.add(CssProperty.BACKGROUND_COLOR, bgColorString);

                            Color textColor = ColorChooser.getForeColor(outlier.getCode());
                            String textColorString = new StringBuilder().append("rgb(")
                                    .append(textColor.getRed()).append(",")
                                    .append(textColor.getGreen()).append(",")
                                    .append(textColor.getBlue()).append(")").toString();
                            outlierColor.add(CssProperty.COLOR, textColorString);

                        }
                    }

                    stream.open(HtmlTag.TABLECELL, outlierColor);
                    if (d9 != null && d9.getFrequency() == block.start.getFrequency() && Double.isFinite(d9.get(block.start.plus(i)))) {
                        stream.write("*");
                    }
                    stream.write(formatter.toString());
                    stream.close(HtmlTag.TABLECELL);
                } else {
                    stream.write(HtmlTag.TABLECELL, ".");
                }
            }

            for (int i = start + n; i < nfreq; ++i) {
                stream.write(HtmlTag.TABLECELL);
            }
            stream.close(HtmlTag.TABLEROW);
        }

    }
}

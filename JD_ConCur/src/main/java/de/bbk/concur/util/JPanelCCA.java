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
import ec.satoolkit.DecompositionMode;
import ec.satoolkit.x11.X11Results;
import ec.tss.Ts;
import ec.tss.documents.DocumentManager;
import ec.tss.sa.documents.X13Document;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.timeseries.regression.OutlierEstimation;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.ui.grid.JTsGrid;
import ec.ui.interfaces.IDisposable;
import ec.ui.interfaces.ITsCollectionView;
import ec.ui.interfaces.ITsGrid;
import java.awt.Dimension;
import java.util.Arrays;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

/**
 *
 * @author Christiane Hofer
 */
public class JPanelCCA extends JPanel implements IDisposable {

    private final JTsOutlierGrid d8Grid;
    private final JTsGrid d10Grid, d10SavedGrid;
    private final JSplitPane d8Pane;
    private final JSplitPane d10aPane;
    private final JSplitPane d10aOldPane;
    private transient X13Document doc;
    private static final int ROW_HEIGHT = 19;
    private static final int ROW_WIDTH = 70; //70 war mal gut, es muss ausrichend breit sein,
    // keine Ahnugn woraus man das erreichen muss

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

    public void set(X13Document doc) {
        this.doc = doc;
        X11Results x11 = doc.getDecompositionPart();
        CompositeResults results = doc.getResults();

        //  boolean isLog = doc.getResults().getData("log", Boolean.class);
        DecompositionMode mode = results.getData("mode", DecompositionMode.class);
        if (x11 != null) {
            Ts d8 = getMainSeries("decomposition.d-tables.d8");
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

                OutlierEstimation[] both = Arrays.copyOf(prespecified, prespecified.length + estimations.length);
                System.arraycopy(estimations, 0, both, prespecified.length, estimations.length);
                d8Grid.setOutliers(both);
            }

            Ts d9 = getMainSeries("decomposition.d-tables.d9");
            d9 = InPercent.convertTsInPercentIfMult(d9, mode.isMultiplicative());
            d8Grid.setD9(d9.getTsData());

            Ts d10 = getMainSeries("decomposition.d-tables.d10");
            d10 = InPercent.convertTsInPercentIfMult(d10, mode.isMultiplicative());
            d10.set(d10.getTsData().fittoDomain(domain));
            fixSize(d10aPane, d10.getTsData().getFrequency().intValue(), d10.getTsData().getDomain().getYearsCount());

            d10Grid.getTsCollection().replace(d10);
            d10Grid.setSelection(d10Grid.getTsCollection().toArray());

            Ts savedD10 = TsData_Saved.convertMetaDataToTs(doc.getMetaData(), SavedTables.SEASONALFACTOR);
            if (savedD10 != null && savedD10.getTsData() != null && savedD10.getTsData().getFrequency().intValue() != d10.getTsData().getFrequency().intValue()) {
                savedD10.setInvalidDataCause("Frequency mismatch");
            }

            //TODO Möglicher Grund für große NoData-Area
            if (savedD10 != null && savedD10.getTsData() != null) {
                savedD10.set(savedD10.getTsData().fittoDomain(domain));
                d10SavedGrid.getTsCollection().replace(savedD10);
                d10SavedGrid.setSelection(d10SavedGrid.getTsCollection().toArray());
                fixSize(d10aOldPane, domain.getFrequency().intValue(), domain.getYearsCount());
            } else {
                d10SavedGrid.getTsCollection().replace(savedD10);
            }
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
    }
}

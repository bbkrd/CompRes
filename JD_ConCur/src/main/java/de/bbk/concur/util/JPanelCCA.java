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

import de.bbk.concur.TablesPercentageChange;
import ec.nbdemetra.ui.NbComponents;
import ec.tss.TsFactory;
import ec.tss.sa.documents.SaDocument;
import ec.tss.sa.documents.X13Document;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.ui.grid.JTsGrid;
import ec.ui.interfaces.IDisposable;
import ec.ui.interfaces.ITsCollectionView;
import ec.ui.interfaces.ITsGrid;
import java.awt.Dimension;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

/**
 *
 * @author Christiane Hofer
 */
public final class JPanelCCA extends JPanel implements IDisposable {

    private final JTsOutlierGrid d8Grid;
    private final JTsGrid d10Grid, d10SavedGrid, growthGrid, growthSavedGrid;
    private final JSplitPane d8Pane;
    private final JSplitPane d10aPane;
    private final JSplitPane d10aOldPane;
    private final JSplitPane growthPane;
    private final JSplitPane growthOldPane;
    private final JLabel lblD8;
    private static final int ROW_HEIGHT = 19;
    private static final int ROW_WIDTH = 70;
    private D8BInfos d8BInfos;
    private TablesPercentageChange tpc;

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

        this.growthGrid = new JTsGrid();
        growthGrid.setTsUpdateMode(ITsCollectionView.TsUpdateMode.None);
        growthGrid.setMode(ITsGrid.Mode.SINGLETS);

        this.growthSavedGrid = new JTsGrid();
        growthSavedGrid.setTsUpdateMode(ITsCollectionView.TsUpdateMode.None);
        growthSavedGrid.setMode(ITsGrid.Mode.SINGLETS);

        lblD8 = new JLabel("D8B", JLabel.CENTER);
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

        JLabel lblGrowth = new JLabel("new GR", JLabel.CENTER);
        growthPane = NbComponents.newJSplitPane(JSplitPane.VERTICAL_SPLIT, lblGrowth, growthGrid);

        growthPane.setEnabled(false);
        growthPane.setDividerSize(0);

        JLabel lblGrowthOld = new JLabel("old GR", JLabel.CENTER);
        growthOldPane = NbComponents.newJSplitPane(JSplitPane.VERTICAL_SPLIT, lblGrowthOld, growthSavedGrid);

        growthOldPane.setEnabled(false);
        growthOldPane.setDividerSize(0);

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.add(d8Pane);
        this.add(d10aPane);
        this.add(d10aOldPane);
        this.add(growthPane);
        this.add(growthOldPane);
    }

    public void set(SaDocument doc) {
        this.d8BInfos = new D8BInfos(doc);
        d10Grid.getTsCollection().clear();
        d10SavedGrid.getTsCollection().clear();
        if (!d8BInfos.isValid()) {
            fixSize(d8Pane, 12, 1);
            fixSize(d10aPane, 12, 1);
            fixSize(d10aOldPane, 12, 1);
            return;
        }
        d8Grid.setInfo(d8BInfos);
        fixSize(d8Pane, d8BInfos.getFrequency(), d8BInfos.getSi().getTsData().getDomain().getYearsCount());

        if (d8BInfos.getSeasonalFactor() != null) {
            d10Grid.getTsCollection().add(d8BInfos.getSeasonalFactor());
            d10Grid.setSelection(d10Grid.getTsCollection().toArray());
            fixSize(d10aPane, d8BInfos.getFrequency(), d8BInfos.getSeasonalFactor().getTsData().getDomain().getYearsCount());
        } else {
            fixSize(d10aPane, d8BInfos.getFrequency(), 1);
        }

        if (d8BInfos.getSavedSeasonalFactor() != null) {
            d10SavedGrid.getTsCollection().add(d8BInfos.getSavedSeasonalFactor());
            d10SavedGrid.setSelection(d10SavedGrid.getTsCollection().toArray());
            fixSize(d10aOldPane, d8BInfos.getFrequency(), d8BInfos.getSavedSeasonalFactor().getTsData().getDomain().getYearsCount());
        } else {
            fixSize(d10aOldPane, d8BInfos.getFrequency(), 1);
        }

        if (doc instanceof X13Document) {
            lblD8.setText("D8B");
        } else {
            lblD8.setText("Pseudo D8B");
        }

        /* Growth Rates */
        this.tpc = new TablesPercentageChange(doc);
        growthGrid.getTsCollection().clear();
        growthSavedGrid.getTsCollection().clear();

        int freq = doc.getSeries().getFrequency().intValue();

        if (tpc.getSeasonallyAdjusted() != null) {

            growthGrid.getTsCollection().add(
                    TsFactory.instance.createTs(
                            tpc.getSeasonallyAdjusted().getName(),
                            null,
                            lastYearOfSeries(
                                    doc.getSeries().getDomain(),
                                    tpc.getSeasonallyAdjusted().getTsData()
                            )
                    )
            );
            growthGrid.setSelection(growthGrid.getTsCollection().toArray());
            fixSize(growthPane, freq, 1);
        } else {
            fixSize(growthPane, freq, 1);
        }

        if (tpc.getSavedSeasonallyAdjusted() != null) {
            growthSavedGrid.getTsCollection().add(
                    TsFactory.instance.createTs(
                            tpc.getSavedSeasonallyAdjusted().getName(),
                            null,
                            lastYearOfSeries(
                                    doc.getSeries().getDomain(),
                                    tpc.getSavedSeasonallyAdjusted().getTsData()
                            )
                    )
            );
            growthSavedGrid.setSelection(growthSavedGrid.getTsCollection().toArray());
            if (tpc.getSavedSeasonallyAdjusted().getTsData() != null) {
                fixSize(growthOldPane, freq, tpc.getSavedSeasonallyAdjusted().getTsData().getDomain().getYearsCount());
            } else {
                tpc.getSavedSeasonallyAdjusted().setInvalidDataCause("NO DATA");
                fixSize(growthOldPane, freq, 1);
            }
        } else {
            tpc.getSavedSeasonallyAdjusted().setInvalidDataCause("NO DATA");
            fixSize(growthOldPane, freq, 1);
        }

    }

    private void fixSize(JSplitPane pane, int periods, int years) {
        int widthcomplete = periods * ROW_WIDTH + 2 * ROW_WIDTH;
        int heightcomplete = (years + 1) * ROW_HEIGHT + 16;
        pane.setMaximumSize(new Dimension(widthcomplete, heightcomplete));
        pane.setMinimumSize(new Dimension(widthcomplete, heightcomplete));
        pane.setPreferredSize(new Dimension(widthcomplete, heightcomplete));

    }

    @Override
    public void dispose() {
        d8Grid.dispose();
        d10Grid.dispose();
        d10SavedGrid.dispose();
        growthGrid.dispose();
        growthSavedGrid.dispose();
        removeAll();
        setLayout(null);
    }

    private TsData lastYearOfSeries(TsDomain dom, TsData tsData) {
        dom = FixTimeDomain.domLastYear(dom);

        if (tsData != null) {
            TsDomain intersection = tsData.getDomain().intersection(dom);
            return tsData.fittoDomain(intersection);
        }
        return new TsData(dom);
    }
}

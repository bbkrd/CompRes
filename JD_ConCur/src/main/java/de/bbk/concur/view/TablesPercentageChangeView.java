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
package de.bbk.concur.view;

import de.bbk.concur.TablesPercentageChange;
import ec.tss.TsCollection;
import ec.tss.sa.documents.SaDocument;
import ec.ui.grid.JTsGrid;
import ec.ui.interfaces.IDisposable;
import ec.ui.interfaces.ITsCollectionView;
import ec.ui.interfaces.ITsGrid;
import java.awt.BorderLayout;
import javax.swing.JComponent;

/**
 *
 * @author Christiane Hofer
 */
public class TablesPercentageChangeView extends JComponent implements IDisposable {

    private final TsCollection percentageChangeGridContent;
    private final JTsGrid percentageChangeGrid;

    public TablesPercentageChangeView() {
        setLayout(new BorderLayout());
        percentageChangeGrid = new JTsGrid();
        percentageChangeGrid.setTsUpdateMode(ITsCollectionView.TsUpdateMode.None);
        percentageChangeGrid.setMode(ITsGrid.Mode.MULTIPLETS);
        percentageChangeGridContent = percentageChangeGrid.getTsCollection();
        add(percentageChangeGrid, BorderLayout.CENTER);
    }

    public void set(SaDocument doc) {
        percentageChangeGridContent.clear();
        if (doc == null || doc.getResults() == null) {
            return;
        }

        TablesPercentageChange tablesPercentageChange = new TablesPercentageChange(doc);
        percentageChangeGridContent.add(tablesPercentageChange.getSeries());
        percentageChangeGridContent.add(tablesPercentageChange.getTrend());
        percentageChangeGridContent.add(tablesPercentageChange.getIrregular());
        percentageChangeGridContent.add(tablesPercentageChange.getSeasonallyAdjusted());
        percentageChangeGridContent.add(tablesPercentageChange.getSavedSeasonallyAdjusted());
        percentageChangeGridContent.add(tablesPercentageChange.getSeasonalFactor());
        percentageChangeGridContent.add(tablesPercentageChange.getSavedSeasonalFactor());
        percentageChangeGridContent.add(tablesPercentageChange.getCalendarFactor());
        percentageChangeGridContent.add(tablesPercentageChange.getSavedCalenderFactor());

    }

    @Override
    public void dispose() {
        percentageChangeGrid.dispose();
    }
}

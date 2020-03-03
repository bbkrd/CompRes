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

import de.bbk.concur.util.InPercent;
import de.bbk.concur.util.SavedTables;
import de.bbk.concur.util.SeasonallyAdjusted_Saved;
import de.bbk.concur.util.TsData_Saved;
import ec.tss.Ts;
import ec.tss.TsCollection;
import ec.tss.documents.DocumentManager;
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
public class TablesSeriesView extends JComponent implements IDisposable {

    private final TsCollection seriesGridContent;
    private final JTsGrid seriesGrid;

    public TablesSeriesView() {
        setLayout(new BorderLayout());
        seriesGrid = new JTsGrid();
        seriesGrid.setTsUpdateMode(ITsCollectionView.TsUpdateMode.None);
        seriesGrid.setMode(ITsGrid.Mode.MULTIPLETS);
        seriesGridContent = seriesGrid.getTsCollection();
        add(seriesGrid, BorderLayout.CENTER);
    }

    public void set(SaDocument doc) {
        seriesGridContent.clear();
        if (doc == null) {
            return;
        }

        boolean isMultiplicative = doc.getFinalDecomposition().getMode().isMultiplicative();
        Ts series = DocumentManager.instance.getTs(doc, SavedTables.COMPOSITE_RESULTS_SERIES_WITH_FORECAST, false);
        seriesGridContent.add(series);

        Ts trend = DocumentManager.instance.getTs(doc, SavedTables.COMPOSITE_RESULTS_TREND_WITH_FORECAST, false);
        seriesGridContent.add(trend);

        Ts irreg = DocumentManager.instance.getTs(doc, SavedTables.COMPOSITE_RESULTS_IRREGULAR_WITH_FORECAST, false);
        seriesGridContent.add(irreg);

        Ts seasonallyAdjusted = DocumentManager.instance.getTs(doc, SavedTables.COMPOSITE_RESULTS_SEASONALLY_ADJUSTED_WITH_FORECAST, false);
        seriesGridContent.add(seasonallyAdjusted.rename(SavedTables.NAME_SHORT_SEASONALLY_ADJUSTED));

        Ts savedSeasonallyAdjusted = SeasonallyAdjusted_Saved.calcSeasonallyAdjusted(doc);
        seriesGridContent.add(savedSeasonallyAdjusted.rename(SavedTables.NAME_SHORT_SEASONALLY_ADJUSTED_SAVED));

        Ts seasonalFactor = DocumentManager.instance.getTs(doc, SavedTables.COMPOSITE_RESULTS_SEASONAL_WITH_FORECAST, false);
        if (seasonalFactor.getTsData() != null) {
            seasonalFactor = InPercent.convertTsInPercentIfMult(seasonalFactor, isMultiplicative);
        }
        seriesGridContent.add(seasonalFactor.rename(SavedTables.NAME_SHORT_SEASONAL_FACTOR));

        Ts savedSeasonalFactors = TsData_Saved.convertMetaDataToTs(doc.getMetaData(), SavedTables.SEASONALFACTOR);
        seriesGridContent.add(savedSeasonalFactors.rename(SavedTables.NAME_SHORT_SEASONAL_FACTOR_SAVED));

        Ts calendarFactor = DocumentManager.instance.getTs(doc, SavedTables.COMPOSITE_RESULTS_CALENDAR_WITH_FORECAST);
        if (calendarFactor.getTsData() != null) {
            calendarFactor = InPercent.convertTsInPercentIfMult(calendarFactor, isMultiplicative);
        }
        seriesGridContent.add(calendarFactor.rename(SavedTables.NAME_SHORT_CALENDAR_FACTOR));

        Ts savedCalendarFactor = TsData_Saved.convertMetaDataToTs(doc.getMetaData(), SavedTables.CALENDARFACTOR);
        seriesGridContent.add(savedCalendarFactor.rename(SavedTables.NAME_SHORT_CALENDAR_FACTOR_SAVED));

    }

    @Override
    public void dispose() {
        seriesGrid.dispose();
    }

}

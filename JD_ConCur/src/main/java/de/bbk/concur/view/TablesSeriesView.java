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

import de.bbk.concur.actions.SelectionSaveCalendarfactorToWorkspace;
import de.bbk.concur.util.InPercent;
import de.bbk.concur.util.SavedTables;
import static de.bbk.concur.util.SavedTables.*;
import de.bbk.concur.util.SeasonallyAdjusted_Saved;
import de.bbk.concur.util.TsData_Saved;
import ec.satoolkit.DecompositionMode;
import ec.tss.Ts;
import ec.tss.TsCollection;
import ec.tss.TsFactory;
import ec.tss.documents.DocumentManager;
import ec.tss.sa.documents.X13Document;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.timeseries.simplets.TsData;
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

    public TablesSeriesView() {
        setLayout(new BorderLayout());
        JTsGrid seriesGrid = new JTsGrid();
        seriesGrid.setTsUpdateMode(ITsCollectionView.TsUpdateMode.None);
        seriesGrid.setMode(ITsGrid.Mode.MULTIPLETS);
        seriesGridContent = seriesGrid.getTsCollection();
        add(seriesGrid, BorderLayout.CENTER);
    }

    public void set(X13Document doc) {
        seriesGridContent.clear();
        if (doc == null) {
            return;
        }
        CompositeResults results = doc.getResults();
        if (results == null) {
            return;
        }

        DecompositionMode mode = doc.getDecompositionPart().getSeriesDecomposition().getMode();
        Ts x = DocumentManager.instance.getTs(doc, COMPOSITE_RESULTS_SERIES_WITH_FORECAST, false);
        seriesGridContent.add(x);

        Ts trend = DocumentManager.instance.getTs(doc, COMPOSITE_RESULTS_TREND_WITH_FORECAST, false);
        seriesGridContent.add(trend);

        Ts irreg = DocumentManager.instance.getTs(doc, COMPOSITE_RESULTS_IRREGULAR_WITH_FORECAST, false);
        seriesGridContent.add(irreg);

        Ts seasonallyAdjusted = DocumentManager.instance.getTs(doc, COMPOSITE_RESULTS_SEASONALLY_ADJUSTED_WITH_FORECAST, false);
        seriesGridContent.add(seasonallyAdjusted);

        Ts savedSeasonallyAdjusted = SeasonallyAdjusted_Saved.calcSeasonallyAdjusted(doc);
        seriesGridContent.add(savedSeasonallyAdjusted);

        Ts tsd10aAll = DocumentManager.instance.getTs(doc, DECOMPOSITION_D10_D10A, false);
        tsd10aAll = InPercent.convertTsInPercentIfMult(tsd10aAll, mode.isMultiplicative());
        seriesGridContent.add(tsd10aAll.rename(NAME_SEASONAL_FACTOR));

        Ts savedSeasonalFactors = TsData_Saved.convertMetaDataToTs(doc.getMetaData(), SavedTables.SEASONALFACTOR);
        seriesGridContent.add(savedSeasonalFactors.rename(NAME_SEASONAL_FACTOR_SAVED));

        Ts a6_7ts = calcA6_7(results, mode);

        seriesGridContent.add(a6_7ts);

        Ts savedCalendarFactor = TsData_Saved.convertMetaDataToTs(doc.getMetaData(), SavedTables.CALENDARFACTOR);
        seriesGridContent.add(savedCalendarFactor.rename(NAME_CALENDAR_FACTOR_SAVED));

    }

    public static Ts calcA6_7(CompositeResults results, DecompositionMode mode) {

        TsData a6_7 = SelectionSaveCalendarfactorToWorkspace.calcCalendarFactor(results, mode);
        Ts a6_7ts = TsFactory.instance.createTs("a6_7");
        if (a6_7 != null) {
            a6_7ts.set(InPercent.convertTsDataInPercentIfMult(a6_7, mode.isMultiplicative()));
        }

        return a6_7ts.rename(NAME_CALENDAR_FACTOR);

    }

    @Override
    public void dispose() {
    }

}
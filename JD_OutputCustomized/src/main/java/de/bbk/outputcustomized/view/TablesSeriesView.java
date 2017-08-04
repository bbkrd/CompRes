/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bbk.outputcustomized.view;

import de.bbk.outputcustomized.util.InPercent;
import de.bbk.outputcustomized.util.SavedTables;
import static de.bbk.outputcustomized.util.SavedTables.*;
import de.bbk.outputcustomized.util.SeasonallyAdjusted_Saved;
import de.bbk.outputcustomized.util.TsData_Saved;
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

        TsData a6 = results.getData("a-tables.a6", TsData.class); //forecast is included
        Ts a6ts = TsFactory.instance.createTs("a6");
        if (a6 != null) {
            a6ts.set(InPercent.convertTsDataInPercentIfMult(a6, mode.isMultiplicative()));
        }
        seriesGridContent.add(a6ts.rename(NAME_CALENDAR_FACTOR));

        Ts savedCalendarFactor = TsData_Saved.convertMetaDataToTs(doc.getMetaData(), SavedTables.CALENDARFACTOR);
        seriesGridContent.add(savedCalendarFactor.rename(NAME_CALENDAR_FACTOR_SAVED));

    }

    @Override
    public void dispose() {
    }

}

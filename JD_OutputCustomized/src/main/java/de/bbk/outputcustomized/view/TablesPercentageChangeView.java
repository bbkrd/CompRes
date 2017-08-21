/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bbk.outputcustomized.view;

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
public class TablesPercentageChangeView extends JComponent implements IDisposable {

    private final TsCollection percentageChangeGridContent;
    private Ts savedSeasonallyAdjusted;
    private Ts seasonallyadjusted;

    public TablesPercentageChangeView() {
        setLayout(new BorderLayout());
        JTsGrid percentageChangeGrid = new JTsGrid();
        percentageChangeGrid.setTsUpdateMode(ITsCollectionView.TsUpdateMode.None);
        percentageChangeGrid.setMode(ITsGrid.Mode.MULTIPLETS);
        percentageChangeGridContent = percentageChangeGrid.getTsCollection();
        add(percentageChangeGrid, BorderLayout.CENTER);
    }

    public void set(X13Document doc) {
        if (doc == null) {
            return;
        }
        CompositeResults results = doc.getResults();
        if (results == null) {
            return;
        }
        percentageChangeGridContent.clear();
        Ts series = DocumentManager.instance.getTs(doc, COMPOSITE_RESULTS_SERIES_WITH_FORECAST);
        series = percentageChange(series);
        percentageChangeGridContent.add(series);

        Ts trend = DocumentManager.instance.getTs(doc, COMPOSITE_RESULTS_TREND_WITH_FORECAST);
        trend = percentageChange(trend);
        percentageChangeGridContent.add(trend);

        Ts irreg = DocumentManager.instance.getTs(doc, COMPOSITE_RESULTS_IRREGULAR_WITH_FORECAST);
        irreg = percentageChange(irreg);
        percentageChangeGridContent.add(irreg);

        seasonallyadjusted = DocumentManager.instance.getTs(doc, COMPOSITE_RESULTS_SEASONALLY_ADJUSTED_WITH_FORECAST);
        seasonallyadjusted = percentageChange(seasonallyadjusted);
        percentageChangeGridContent.add(seasonallyadjusted);

        savedSeasonallyAdjusted = SeasonallyAdjusted_Saved.calcSeasonallyAdjusted(doc);
        savedSeasonallyAdjusted = percentageChange(savedSeasonallyAdjusted);
        percentageChangeGridContent.add(savedSeasonallyAdjusted);

        Ts tsD10 = DocumentManager.instance.getTs(doc, DECOMPOSITION_D10_D10A);
        tsD10 = percentageChange(tsD10.rename(NAME_SEASONAL_FACTOR));
        percentageChangeGridContent.add(tsD10);

        Ts savedSeasonalFactors = TsData_Saved.convertMetaDataToTs(doc.getMetaData(), SavedTables.SEASONALFACTOR);
        savedSeasonalFactors = percentageChange(savedSeasonalFactors.rename(NAME_SEASONAL_FACTOR_SAVED));
        percentageChangeGridContent.add(savedSeasonalFactors);


        Ts a6_7ts = TablesSeriesView.calcA6_7(results, doc.getDecompositionPart().getSeriesDecomposition().getMode());
        percentageChangeGridContent.add(percentageChange(a6_7ts));

        Ts savedCalenderfactorFactors = TsData_Saved.convertMetaDataToTs(doc.getMetaData(), SavedTables.CALENDARFACTOR);
        savedCalenderfactorFactors = percentageChange(savedCalenderfactorFactors.rename(NAME_CALENDAR_FACTOR_SAVED));
        percentageChangeGridContent.add(savedCalenderfactorFactors);

    }

    @Override
    public void dispose() {
    }

    private Ts percentageChange(Ts ts) {
        TsData tsData = ts.getTsData();
        tsData = percentageChange(tsData);
        Ts tsPercentageChange = TsFactory.instance.createTs(ts.getName()+" (PtP GR)");
        tsPercentageChange.set(tsData);
        return tsPercentageChange;
    }

    private TsData percentageChange(TsData tsData) {
        if (tsData == null || tsData.getLength() <= 1) {
            return tsData;
        }
        return tsData.pctVariation(1);
    }

    public Ts GetSavedSeasonallyAdjustedPercentageChange() {
        return savedSeasonallyAdjusted;
    }

    public Ts GetSeasonallyadjustedPercentageChange() {
        return seasonallyadjusted;
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bbk.concur;

import de.bbk.concur.util.SavedTables;
import de.bbk.concur.util.SeasonallyAdjusted_Saved;
import de.bbk.concur.util.TsData_Saved;
import ec.tss.Ts;
import ec.tss.TsFactory;
import ec.tss.documents.DocumentManager;
import ec.tss.sa.documents.SaDocument;
import ec.tstoolkit.timeseries.simplets.TsData;

/**
 *
 * @author s4504tw
 */
@lombok.Value
public class TablesPercentageChange {

    Ts series, trend, irregular, seasonallyAdjusted, savedSeasonallyAdjusted, seasonalFactor, savedSeasonalFactor, calendarFactor, savedCalenderFactor;

    public TablesPercentageChange(SaDocument doc) {
        if (doc == null || doc.getResults() == null) {
            this.series = null;
            this.trend = null;
            this.irregular = null;
            this.seasonallyAdjusted = null;
            this.savedSeasonallyAdjusted = null;
            this.seasonalFactor = null;
            this.savedSeasonalFactor = null;
            this.calendarFactor = null;
            this.savedCalenderFactor = null;
            return;
        }

        series = percentageChange(DocumentManager.instance.getTs(doc, SavedTables.COMPOSITE_RESULTS_SERIES_WITH_FORECAST));
        trend = percentageChange(DocumentManager.instance.getTs(doc, SavedTables.COMPOSITE_RESULTS_TREND_WITH_FORECAST));
        irregular = percentageChange(DocumentManager.instance.getTs(doc, SavedTables.COMPOSITE_RESULTS_IRREGULAR_WITH_FORECAST));
        seasonallyAdjusted = percentageChange(DocumentManager.instance.getTs(doc, SavedTables.COMPOSITE_RESULTS_SEASONALLY_ADJUSTED_WITH_FORECAST)).rename(SavedTables.NAME_SHORT_SEASONALLY_ADJUSTED);
        seasonalFactor = percentageChange(DocumentManager.instance.getTs(doc, SavedTables.COMPOSITE_RESULTS_SEASONAL_WITH_FORECAST)).rename(SavedTables.NAME_SHORT_SEASONAL_FACTOR);
        calendarFactor = percentageChange(DocumentManager.instance.getTs(doc, SavedTables.COMPOSITE_RESULTS_CALENDAR_WITH_FORECAST)).rename(SavedTables.NAME_SHORT_CALENDAR_FACTOR);

        savedSeasonallyAdjusted = percentageChange(SeasonallyAdjusted_Saved.calcSeasonallyAdjusted(doc)).rename(SavedTables.NAME_SHORT_SEASONALLY_ADJUSTED_SAVED);
        savedSeasonalFactor = percentageChange(TsData_Saved.convertMetaDataToTs(doc.getMetaData(), SavedTables.SEASONALFACTOR)).rename(SavedTables.NAME_SHORT_SEASONAL_FACTOR_SAVED);
        savedCalenderFactor = percentageChange(TsData_Saved.convertMetaDataToTs(doc.getMetaData(), SavedTables.CALENDARFACTOR)).rename(SavedTables.NAME_SHORT_CALENDAR_FACTOR_SAVED);
    }

    private Ts percentageChange(Ts ts) {
        Ts tsPercentageChange = TsFactory.instance.createTs(ts.getName());
        TsData tsData = ts.getTsData();
        tsPercentageChange.set(percentageChange(tsData));
        return tsPercentageChange;
    }

    private TsData percentageChange(TsData tsData) {
        if (tsData == null || tsData.getLength() <= 1) {
            return tsData;
        }
        return tsData.pctVariation(1);
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bbk.outputcustomized.util;

import ec.satoolkit.DecompositionMode;
import ec.tss.Ts;
import ec.tss.TsFactory;
import ec.tss.sa.documents.X13Document;
import ec.tstoolkit.MetaData;
import ec.tstoolkit.maths.linearfilters.SymmetricFilter;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;

/**
 *
 * @author Christiane Hofer
 */
public class SeasonallyAdjusted_Saved {

    public static final Ts calcSeasonallyAdjusted(X13Document doc) {
        DecompositionMode mode = doc.getDecompositionPart().getSeriesDecomposition().getMode();
        TsData tsdA1_all = doc.getResults().getData("a-tables.a1", TsData.class).update(doc.getResults().getData("a-tables.a1a", TsData.class));
        return calcSeasonallyAdjusted(doc.getMetaData(), mode, tsdA1_all);
    }

    public static final Ts calcSeasonallyAdjusted(MetaData meta, DecompositionMode mode, TsData tsdSeriesWithForecast) {
        Ts tsSeasonsalFactor = TsData_Saved.convertMetaDataToTs(meta, SavedTables.SEASONALFACTOR);
        Ts tsCalendarFactor = TsData_Saved.convertMetaDataToTs(meta, SavedTables.CALENDARFACTOR);
        TsData tsdSeasonsalFactor = tsSeasonsalFactor.getTsData();
        TsData tsdCalendarFactor = tsCalendarFactor.getTsData();
        return calcSeasonallyAdjusted(tsdSeasonsalFactor, tsdCalendarFactor, tsdSeriesWithForecast, mode);

    }

   

    private static Ts calcSeasonallyAdjusted(TsData tsDataSeasonsalFactor, TsData tsDataCalendarFactor, TsData tsDataSeriesWithForecast, DecompositionMode mode) {
        //if Timeseries is saved in Percent then multiply if exits with 100
        Ts tsSeasonallyAdjusted = TsFactory.instance.createTs(SavedTables.NAME_SEASONALLY_ADJUSTED_SAVED);
        if (tsDataSeriesWithForecast == null) {
            tsSeasonallyAdjusted.setInvalidDataCause("No Series with forecast available.");
            return tsSeasonallyAdjusted;
        }
        if (tsDataSeasonsalFactor == null) {
            tsSeasonallyAdjusted.setInvalidDataCause("No Seasonal factor available.");
            return tsSeasonallyAdjusted;
        }

        TsFrequency frequency= tsDataSeriesWithForecast.getDomain().getFrequency();
        if(!frequency.equals(tsDataSeasonsalFactor.getDomain().getFrequency())){
           tsSeasonallyAdjusted.setInvalidDataCause("The frequency of the Series("+ frequency + ") differs from the frequency of the seasonal factor("+ tsDataSeasonsalFactor.getDomain().getFrequency() +").");
            return tsSeasonallyAdjusted;
        }
        
           if(tsDataCalendarFactor!=null && !frequency.equals(tsDataSeasonsalFactor.getDomain().getFrequency())){
           tsSeasonallyAdjusted.setInvalidDataCause("The frequency of the Series("+ frequency + ") differs from the frequency of the calendar factor("+ tsDataCalendarFactor.getDomain().getFrequency() +").");
            return tsSeasonallyAdjusted;
        }
        
        boolean isMult = mode.isMultiplicative();
        if (isMult) {
            if (tsDataCalendarFactor != null) {
                tsDataCalendarFactor = tsDataCalendarFactor.div(100);
            }
                tsDataSeasonsalFactor = tsDataSeasonsalFactor.div(100);
        }

        if (tsDataSeasonsalFactor != null ) {
            TsData tsDataSeasonallyAdjusted;
            switch (mode) {
                case Additive:
                case Undefined:
                    tsDataSeasonallyAdjusted = tsDataSeriesWithForecast.minus(tsDataSeasonsalFactor).minus(tsDataCalendarFactor);
                    break;
                case Multiplicative:
                case LogAdditive:
                    tsDataSeasonallyAdjusted = tsDataSeriesWithForecast.div(tsDataSeasonsalFactor).div(tsDataCalendarFactor);
                    break;
                default:
                    tsDataSeasonallyAdjusted = new TsData(tsDataSeriesWithForecast.getStart(), 0);
                    break;
            }

            tsSeasonallyAdjusted.set(tsDataSeasonallyAdjusted);
        }
        return tsSeasonallyAdjusted;
    }

}

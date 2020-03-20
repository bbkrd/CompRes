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

import static de.bbk.concur.util.SavedTables.COMPOSITE_RESULTS_SERIES_WITH_FORECAST;
import ec.satoolkit.DecompositionMode;
import ec.tss.Ts;
import ec.tss.TsFactory;
import ec.tss.documents.DocumentManager;
import ec.tss.sa.documents.SaDocument;
import ec.tstoolkit.MetaData;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;

/**
 *
 * @author Christiane Hofer
 */
public class SeasonallyAdjusted_Saved {

    public static final Ts calcSeasonallyAdjusted(SaDocument doc) {
        if (doc.getFinalDecomposition() != null) {
            DecompositionMode mode = doc.getFinalDecomposition().getMode();
            TsData tsdA1_all = DocumentManager.instance.getTs(doc, COMPOSITE_RESULTS_SERIES_WITH_FORECAST).getTsData();
            return calcSeasonallyAdjusted(doc.getMetaData(), mode, tsdA1_all);
        }
        Ts createTs = TsFactory.instance.createTs(SavedTables.NAME_SEASONALLY_ADJUSTED_SAVED);
        createTs.setInvalidDataCause("Invalid Adjustment");
        return createTs;
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

        TsFrequency frequency = tsDataSeriesWithForecast.getDomain().getFrequency();
        if (!frequency.equals(tsDataSeasonsalFactor.getDomain().getFrequency())) {
            tsSeasonallyAdjusted.setInvalidDataCause("The frequency of the Series(" + frequency + ") differs from the frequency of the seasonal factor(" + tsDataSeasonsalFactor.getDomain().getFrequency() + ").");
            return tsSeasonallyAdjusted;
        }

        if (tsDataCalendarFactor != null && !frequency.equals(tsDataSeasonsalFactor.getDomain().getFrequency())) {
            tsSeasonallyAdjusted.setInvalidDataCause("The frequency of the Series(" + frequency + ") differs from the frequency of the calendar factor(" + tsDataCalendarFactor.getDomain().getFrequency() + ").");
            return tsSeasonallyAdjusted;
        }

        boolean isMult = mode.isMultiplicative();
        if (isMult) {
            if (tsDataCalendarFactor != null) {
                tsDataCalendarFactor = tsDataCalendarFactor.div(100);
            }
            tsDataSeasonsalFactor = tsDataSeasonsalFactor.div(100);
        }

        if (tsDataSeasonsalFactor != null) {
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

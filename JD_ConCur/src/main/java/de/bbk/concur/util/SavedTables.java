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

/**
 *
 * @author s4504ch
 */
public class SavedTables {
// Strings used for Properties

    public static final String SEASONALFACTOR = "seasonalfactor";
    public static final String CALENDARFACTOR = "calendarfactor";
//    public static final String FORECAST = "forecast";

    public enum TABLES {
        SEASONALFACTOR, CALENDARFACTOR, FORECAST
    }

//Label for  time series on CCA
    public static final String NAME_SEASONALLY_ADJUSTED_SAVED = "Seasonally Adjusted (current)";
    public static final String NAME_SEASONAL_FACTOR_SAVED = "Seasonal Factor (current)";
    public static final String NAME_CALENDAR_FACTOR_SAVED = "Calendar Factor (current)";
    public static final String NAME_ONLY_SEASONALLY_ADJUSTED = "Only Seasonally Adjusted";
    public static final String NAME_SEASONAL_FACTOR = "Seasonal Factor";
    public static final String NAME_CALENDAR_FACTOR = "Calendar Factor";
    public static final String NAME_TREND = "Trend";
    public static final String NAME_SEASONALLY_ADJUSTED = "Seasonally Adjusted";
    public static final String NAME_SERIES = "Series";
    public static final String NAME_IRREGULAR = "Irregular";

    public static final String NAME_SHORT_SEASONALLY_ADJUSTED_SAVED = "SA (old)";
    public static final String NAME_SHORT_SEASONAL_FACTOR_SAVED = "S (old)";
    public static final String NAME_SHORT_CALENDAR_FACTOR_SAVED = "C (old)";
    public static final String NAME_SHORT_SEASONALLY_ADJUSTED = "SA (new)";
    public static final String NAME_SHORT_SEASONAL_FACTOR = "S (new)";
    public static final String NAME_SHORT_CALENDAR_FACTOR = "C (new)";

    public static final String COMPOSITE_RESULTS_SERIES = "@composite@" + NAME_SERIES + "=,final.y,";
    public static final String COMPOSITE_RESULTS_TREND = "@composite@" + NAME_TREND + "=,final.t,";
    public static final String COMPOSITE_RESULTS_IRREGULAR = "@composite@" + NAME_IRREGULAR + "=,final.i,";
    public static final String COMPOSITE_RESULTS_SEASONALLY_ADJUSTED = "@composite@" + NAME_SEASONALLY_ADJUSTED + "=,final.sa,";
    public static final String COMPOSITE_RESULTS_SERIES_WITH_FORECAST = "@composite@" + NAME_SERIES + "=,final.y,final.y_f";
    public static final String COMPOSITE_RESULTS_TREND_WITH_FORECAST = "@composite@" + NAME_TREND + "=,final.t,final.t_f";
    public static final String COMPOSITE_RESULTS_IRREGULAR_WITH_FORECAST = "@composite@" + NAME_IRREGULAR + "=,final.i,final.i_f";
    public static final String COMPOSITE_RESULTS_SEASONALLY_ADJUSTED_WITH_FORECAST = "@composite@" + NAME_SEASONALLY_ADJUSTED + "=,final.sa,final.sa_f";
    public static final String COMPOSITE_RESULTS_CALENDAR = "@composite@" + NAME_CALENDAR_FACTOR + "=,cal,";
    public static final String COMPOSITE_RESULTS_CALENDAR_WITH_FORECAST = "@composite@" + NAME_CALENDAR_FACTOR + "=,cal,cal_f";
    public static final String COMPOSITE_RESULTS_SEASONAL = "@composite@" + NAME_SEASONAL_FACTOR + "=,s_cmp,";
    public static final String COMPOSITE_RESULTS_SEASONAL_WITH_FORECAST = "@composite@" + NAME_SEASONAL_FACTOR + "=,s_cmp,s_cmp_f";
}

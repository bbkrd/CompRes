/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bbk.outputcustomized.util;

import ec.tss.Ts;
import ec.tss.documents.DocumentManager;
import ec.tstoolkit.modelling.ModellingDictionary;

/**
 *
 * @author s4504ch
 */
public class SavedTables {
// Strings used for Properties

    public static final String SEASONALFACTOR = "seasonalfactor";
    public static final String CALENDARFACTOR = "calendarfactor";
    public static final String FORECAST = "forecast";

    public static enum TABLES {
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
    public static final String NAME_IRREGULAR="Irregular";
    

    //   Ts x = DocumentManager.instance.getTs(doc, "@composite@Series=,final.y,final.y_f", false);
   // public static final String COMPOSITE_RESULTS_SERIES = ModellingDictionary.Y;
       public static final String COMPOSITE_RESULTS_SERIES = "@composite@"+NAME_SERIES+"=,final.y,";
    public static final String COMPOSITE_RESULTS_TREND = "@composite@"+NAME_TREND+"=,final.t,";
    public static final String COMPOSITE_RESULTS_IRREGULAR = "@composite@"+NAME_IRREGULAR+"=,final.i,";
    public static final String COMPOSITE_RESULTS_SEASONALLY_ADJUSTED = "@composite@"+NAME_SEASONALLY_ADJUSTED+"=,final.sa,";
    public static final String DECOMPOSITION_D10 = "@composite@"+NAME_SEASONAL_FACTOR+"=,decomposition.d10";
    public static final String COMPOSITE_RESULTS_SERIES_WITH_FORECAST = "@composite@"+NAME_SERIES+"=,final.y,final.y_f";
    public static final String COMPOSITE_RESULTS_TREND_WITH_FORECAST = "@composite@"+NAME_TREND+"=,final.t,final.t_f";
    public static final String COMPOSITE_RESULTS_IRREGULAR_WITH_FORECAST = "@composite@"+NAME_IRREGULAR+"=,final.i,final.i_f";
    public static final String COMPOSITE_RESULTS_SEASONALLY_ADJUSTED_WITH_FORECAST = "@composite@"+NAME_SEASONALLY_ADJUSTED+"=,final.sa,final.sa_f";
    public static final String DECOMPOSITION_D10_D10A = "@composite@"+NAME_SEASONAL_FACTOR+"=,decomposition.d10,decomposition.d10a";
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bbk.concurreport;

import de.bbk.concur.util.SavedTables;
import ec.tss.sa.documents.SaDocument;
import java.util.function.Function;

/**
 *
 * @author s4504tw
 */
public enum MainTable {
    SERIES("Series", t -> SavedTables.COMPOSITE_RESULTS_SERIES),
    SERIES_WITH_FORECAST("Series with forecast", t -> SavedTables.COMPOSITE_RESULTS_SERIES_WITH_FORECAST),
    SEASONALLY_ADJUSTED("Seasonally adjusted", t -> SavedTables.COMPOSITE_RESULTS_SEASONALLY_ADJUSTED),
    SEASONALLY_ADJUSTED_WITH_FORECAST("Seasonally adjusted with forecast", t -> SavedTables.COMPOSITE_RESULTS_SEASONALLY_ADJUSTED_WITH_FORECAST),
    TREND("Trend", t -> SavedTables.COMPOSITE_RESULTS_TREND),
    TREND_WITH_FORECAST("Trend with forecast", t -> SavedTables.COMPOSITE_RESULTS_TREND_WITH_FORECAST),
    SEASONAL("Seasonal", t -> SavedTables.pickSeasonalCompositeFor(t)),
    SEASONAL_WITH_FORECAST("Seasonal with forecast", t -> SavedTables.pickSeasonalWithForecastCompositeFor(t)),
    CALENDAR("Calendar", t -> SavedTables.COMPOSITE_RESULTS_CALENDAR),
    CALENDAR_WITH_FORECAST("Calendar with forecast", t -> SavedTables.COMPOSITE_RESULTS_CALENDAR_WITH_FORECAST),
    IRREGULAR("Irregular", t -> SavedTables.COMPOSITE_RESULTS_IRREGULAR),
    IRREGULAR_WITH_FORECAST("Irregular with forecast", t -> SavedTables.COMPOSITE_RESULTS_IRREGULAR_WITH_FORECAST);

    private final String displayName;
    private final Function<SaDocument, String> function;

    private MainTable(String displayName, Function<SaDocument, String> compositeFormula) {
        this.displayName = displayName;
        this.function = compositeFormula;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public Function<SaDocument, String> getFunction() {
        return function;
    }

    public static MainTable fromDisplayName(String displayName) {
        for (MainTable value : MainTable.values()) {
            if (value.displayName.equalsIgnoreCase(displayName)) {
                return value;
            }
        }
        return null;
    }

}

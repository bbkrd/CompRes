/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bbk.concurreport;

import de.bbk.concur.util.SavedTables;

/**
 *
 * @author s4504tw
 */
public enum MainTable {
    SERIES("Series", SavedTables.COMPOSITE_RESULTS_SERIES),
    SERIES_WITH_FORECAST("Series with forecast", SavedTables.COMPOSITE_RESULTS_SERIES_WITH_FORECAST),
    SEASONALLY_ADJUSTED("Seasonally adjusted", SavedTables.COMPOSITE_RESULTS_SEASONALLY_ADJUSTED),
    SEASONALLY_ADJUSTED_WITH_FORECAST("Seasonally adjusted with forecast", SavedTables.COMPOSITE_RESULTS_SEASONALLY_ADJUSTED_WITH_FORECAST),
    TREND("Trend", SavedTables.COMPOSITE_RESULTS_TREND),
    TREND_WITH_FORECAST("Trend with forecast", SavedTables.COMPOSITE_RESULTS_TREND_WITH_FORECAST),
    SEASONAL("Seasonal", SavedTables.COMPOSITE_RESULTS_SEASONAL),
    SEASONAL_WITH_FORECAST("Seasonal with forecast", SavedTables.COMPOSITE_RESULTS_SEASONAL_WITH_FORECAST),
    CALENDAR("Calendar", SavedTables.COMPOSITE_RESULTS_CALENDAR),
    CALENDAR_WITH_FORECAST("Calendar with forecast", SavedTables.COMPOSITE_RESULTS_CALENDAR_WITH_FORECAST),
    IRREGULAR("Irregular", SavedTables.COMPOSITE_RESULTS_IRREGULAR),
    IRREGULAR_WITH_FORECAST("Irregular with forecast", SavedTables.COMPOSITE_RESULTS_IRREGULAR_WITH_FORECAST);

    private final String displayName;
    private final String compositeFormula;

    private MainTable(String displayName, String compositeFormula) {
        this.displayName = displayName;
        this.compositeFormula = compositeFormula;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public String getCompositeFormula() {
        return compositeFormula;
    }

    public MainTable fromDisplayName(String displayName) {
        for (MainTable value : MainTable.values()) {
            if (value.displayName.equalsIgnoreCase(displayName)) {
                return value;
            }
        }
        return null;
    }

}

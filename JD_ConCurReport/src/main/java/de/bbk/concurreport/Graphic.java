/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bbk.concurreport;

import de.bbk.concurreport.html.graphic.HTMLBBKAutoRegressiveSpectrumView;
import de.bbk.concurreport.html.graphic.HTMLBBKChartAutocorrelations;
import de.bbk.concurreport.html.graphic.HTMLBBKPeriodogram;
import de.bbk.concurreport.html.graphic.HTMLBBKSIRatioLastTwoPeriodView;
import de.bbk.concurreport.html.graphic.HTMLBBKSIRatioView;
import ec.satoolkit.ISaSpecification;
import ec.tss.html.IHtmlElement;
import ec.tss.sa.documents.SaDocument;

/**
 *
 * @author s4504tw
 */
public enum Graphic {
    AUTOCORRELATION("Autocorrelations (Full Residuals)"),
    PARTIAL_AUTOCORRELATION("Partial Autocorrelations (Full Residuals)"),
    AUTOREGRESSIVE_SPECTRUM("Auto-regressive spectrum (Series stationary)"),
    PERIODOGRAM("Periodogram (Seasonally Adjusted stationary)"),
    S_I_RATIO("S-I-Ratio"),
    S_I_RATIO_LAST_TWO_PERIODS("S-I-Ratio (last two periods)");

    private final String displayName;

    private Graphic(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public static Graphic fromDisplayName(String displayName) {
        for (Graphic value : Graphic.values()) {
            if (value.displayName.equalsIgnoreCase(displayName)) {
                return value;
            }
        }
        return null;
    }

    public IHtmlElement createFromDoc(SaDocument<? extends ISaSpecification> doc) {
        IHtmlElement element;
        switch (this) {
            case AUTOCORRELATION:
                element = new HTMLBBKChartAutocorrelations(doc, false);
                break;
            case PARTIAL_AUTOCORRELATION:
                element = new HTMLBBKChartAutocorrelations(doc, true);
                break;
            case AUTOREGRESSIVE_SPECTRUM:
                element = new HTMLBBKAutoRegressiveSpectrumView(doc);
                break;
            case PERIODOGRAM:
                element = new HTMLBBKPeriodogram(doc);
                break;
            case S_I_RATIO:
                element = new HTMLBBKSIRatioView(doc);
                break;
            case S_I_RATIO_LAST_TWO_PERIODS:
                element = new HTMLBBKSIRatioLastTwoPeriodView(doc);
                break;
            default:
                throw new AssertionError(this.name());
        }
        return element;
    }

}

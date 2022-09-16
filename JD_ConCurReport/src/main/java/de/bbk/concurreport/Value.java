/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bbk.concurreport;

import de.bbk.concurreport.html.HTMLOutOfSampleTest;
import de.bbk.concurreport.html.HTMLRegressionModel;
import de.bbk.concurreport.html.HTMLWrapperCCA;
import de.bbk.concurreport.html.HtmlSpecification;
import de.bbk.concurreport.html.HtmlWrapperARIMA;
import ec.satoolkit.ISaSpecification;
import ec.tss.html.IHtmlElement;
import ec.tss.html.implementation.HtmlMstatistics;
import ec.tss.html.implementation.HtmlRegArima;
import ec.tss.sa.documents.SaDocument;
import ec.tss.sa.documents.X13Document;
import ec.tss.tsproviders.utils.MultiLineNameUtil;

/**
 *
 * @author s4504tw
 */
public enum Value {
    SPECIFICATION("Specification"),
    SUMMARY("Summary"),
    REGRESSION("Regression model"),
    ARIMA("ARIMA model"),
    OUT_OF_SAMPLE("Out of sample test"),
    CCA_VALUES("I/C Ratio, F-Tests and Final Trend Filter"),
    M_Q_STATISTICS("M-Q-Statistics");

    private final String displayName;

    private Value(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public static Value fromDisplayName(String displayName) {
        for (Value value : Value.values()) {
            if (value.displayName.equalsIgnoreCase(displayName)) {
                return value;
            }
        }
        return null;
    }

    public IHtmlElement createFromDoc(SaDocument<? extends ISaSpecification> doc) {
        IHtmlElement element;
        if (doc == null) {
            return null;
        }
        switch (this) {
            case SPECIFICATION:
                element = new HtmlSpecification(doc);
                break;
            case SUMMARY:
                element = new HtmlRegArima(doc.getPreprocessingPart(), true);
                break;
            case REGRESSION:
                element = new HTMLRegressionModel(doc.getPreprocessingPart());
                break;
            case ARIMA:
                element = new HtmlWrapperARIMA(doc.getPreprocessingPart());
                break;
            case CCA_VALUES:
                element = new HTMLWrapperCCA(MultiLineNameUtil.join(doc.getInput().getName()), doc);
                break;
            case OUT_OF_SAMPLE:
                element = new HTMLOutOfSampleTest(doc.getPreprocessingPart());
                break;
            case M_Q_STATISTICS:
                if (doc instanceof X13Document) {
                    element = new HtmlMstatistics(((X13Document) doc).getMStatistics());
                } else {
                    element = null;
                }
                break;
            default:
                throw new AssertionError(this.name());
        }
        return element;
    }
}

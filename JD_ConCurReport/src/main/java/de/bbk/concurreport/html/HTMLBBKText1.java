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
package de.bbk.concurreport.html;

import ec.satoolkit.x11.SeasonalFilterOption;
import ec.satoolkit.x11.X11Specification;
import ec.tss.html.AbstractHtmlElement;
import ec.tss.html.HtmlStream;
import ec.tss.html.HtmlTag;
import ec.tss.html.IHtmlElement;
import ec.tss.html.implementation.HtmlRegArima;
import ec.tss.sa.documents.X13Document;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import static ec.tstoolkit.modelling.arima.x13.OutlierSpec.DEF_VA;
import ec.tstoolkit.modelling.arima.x13.RegArimaSpecification;
import ec.tstoolkit.modelling.arima.x13.SingleOutlierSpec;
import ec.tstoolkit.timeseries.regression.OutlierDefinition;
import java.io.IOException;
import java.util.Arrays;

/**
 *
 * @author Christiane Hofer
 */
public class HTMLBBKText1 extends AbstractHtmlElement implements IHtmlElement {

    private final X13Document x13Document;
    private final PreprocessingModel model;

    public HTMLBBKText1(X13Document x13Document) {
        this.x13Document = x13Document;
        this.model = this.x13Document.getPreprocessingPart();
    }

    @Override
    public void write(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER2, h2, "Specification");
        if (model != null) {
            RegArimaSpecification regSpec = x13Document.getSpecification().getRegArimaSpecification();
            stream.write("Transform: ");
            stream.write(regSpec.getTransform().getFunction().toString()).newLine();

            //Outliers
            if (regSpec.getOutliers().isUsed()) {
                stream.write("Outlier detection: ");
                for (SingleOutlierSpec type : regSpec.getOutliers().getTypes()) {
                    stream.write(type.getType().name() + " ");
                }
                stream.newLine();

                double criticalValue = regSpec.getOutliers().getDefaultCriticalValue();
                if (criticalValue == 0) {
                    criticalValue = DEF_VA;
                }
                stream.write("Outliers critical value: " + criticalValue).newLine();
            }

            if (regSpec.getRegression().getTradingDays().isUsed()) {

                stream.write("Regression variable: Trading days").newLine();

//                if (regSpec.getRegression().getTradingDays().getTradingDaysType() != TradingDaysType.None) {
//                    stream.write("Trading days td: " + regSpec.getRegression().getTradingDays().getTradingDaysType().name()).newLine(); //td
//                }
                if (regSpec.getRegression().getTradingDays().getUserVariables() != null && regSpec.getRegression().getTradingDays().getUserVariables().length > 0) {
                    for (String userVariable : regSpec.getRegression().getTradingDays().getUserVariables()) {
                        stream.write("Trading days user-defined variable: " + userVariable).newLine();
                    }
                }
                if (regSpec.getRegression().getTradingDays().isStockTradingDays()) {
                    stream.write("Trading days: stock trading days").newLine();
                }

            }

            if (regSpec.getRegression().getEaster() != null) {
                stream.write("Regression variable: " + regSpec.getRegression().getEaster().getType()).newLine();
            }

            for (OutlierDefinition variable : regSpec.getRegression().getOutliers()) {
                stream.write("Regression variable: " + variable.toString()).newLine();
            }

            if (regSpec.isUsingAutoModel()) {
                stream.write("ARIMA model: auto").newLine();
            } else {
                stream.write("ARIMA model: (" + regSpec.getArima().getP());
                stream.write(" " + regSpec.getArima().getD());
                stream.write(" " + regSpec.getArima().getQ() + ")");
                stream.write("(" + regSpec.getArima().getBP());
                stream.write(" " + regSpec.getArima().getBD());
                stream.write(" " + regSpec.getArima().getBQ() + ")").newLine();
            }

        }
        X11Specification x11Spec = x13Document.getSpecification().getX11Specification();
        stream.write("Forecast horizon: " + x11Spec.getForecastHorizon()).newLine();
        stream.write("Sigmalimit: [" + x11Spec.getLowerSigma() + ";" + x11Spec.getUpperSigma() + "]").newLine();

        if (x11Spec.isSeasonal() && x11Spec.getSeasonalFilters() != null) {
            SeasonalFilterOption first = x11Spec.getSeasonalFilters()[0];
            boolean isSameSeasonalFilter = Arrays.stream(x11Spec.getSeasonalFilters()).allMatch(x -> x.equals(first));

            if (isSameSeasonalFilter) {
                stream.write("Seasonal filters:" + first.name()).newLine();
            } else {

                stream.write("Seasonal filters:" + x11Spec.getSeasonalFilters()[0].name() + ",");
                for (int i = 1; i < x11Spec.getSeasonalFilters().length - 1; i++) {
                    stream.write(x11Spec.getSeasonalFilters()[i].name() + ",");
                    if (i == 5) {
                        stream.newLine();
                        stream.write("Seasonal filters:");
                    }
                }

                if (x11Spec.getSeasonalFilters().length > 1) {
                    stream.write(x11Spec.getSeasonalFilters()[x11Spec.getSeasonalFilters().length - 1].name()).newLine();
                }
            }
        } else {
            stream.write("Seasonal filters: Msr").newLine();
        }

        if (x11Spec.isAutoHenderson()) {
            stream.write("Trendfilter: auto").newLine();
        } else {
            stream.write("Trendfilter: " + x11Spec.getHendersonFilterLength()).newLine();
        }
        stream.write("Calendarsigma: " + x11Spec.getCalendarSigma().name()).newLine();
        stream.write("Excludefcst: " + x11Spec.isExcludefcst()).newLines(2);

        if (model != null) {

            HtmlRegArima htmlRegArimaSummary = new HtmlRegArima(model, true);
            stream.write(htmlRegArimaSummary).newLine(); //H1 muss hier auf 100 gesetzt werden, sonst copy and paste
            //Arima Model
            HtmlRegArima htmlRegArima = new HtmlRegArima(model, false);

            stream.write(HtmlTag.HEADER2, h2, "Regression model:");
            htmlRegArima.writeRegression(stream, true);
            stream.newLines(1);
            stream.write(HtmlTag.HEADER2, h2, "Arima model: ");
            htmlRegArima.writeArima(stream);
            stream.newLines(1);

        }
    }
}

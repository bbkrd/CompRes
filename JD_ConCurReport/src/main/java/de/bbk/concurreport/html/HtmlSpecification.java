/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bbk.concurreport.html;

import ec.satoolkit.seats.SeatsSpecification;
import ec.satoolkit.tramoseats.TramoSeatsSpecification;
import ec.satoolkit.x11.SeasonalFilterOption;
import ec.satoolkit.x11.X11Specification;
import ec.satoolkit.x13.X13Specification;
import ec.tss.html.AbstractHtmlElement;
import ec.tss.html.HtmlStream;
import ec.tss.html.HtmlTag;
import ec.tss.sa.documents.SaDocument;
import ec.tss.sa.documents.TramoSeatsDocument;
import ec.tss.sa.documents.X13Document;
import ec.tstoolkit.modelling.arima.tramo.CalendarSpec;
import ec.tstoolkit.modelling.arima.tramo.TramoSpecification;
import ec.tstoolkit.modelling.arima.x13.RegArimaSpecification;
import ec.tstoolkit.modelling.arima.x13.SingleOutlierSpec;
import ec.tstoolkit.timeseries.regression.OutlierType;
import java.io.IOException;
import java.util.Arrays;

/**
 *
 * @author s4504tw
 */
public class HtmlSpecification extends AbstractHtmlElement {

    private final SaDocument doc;

    public HtmlSpecification(SaDocument doc) {
        this.doc = doc;
    }

    @Override
    public void write(HtmlStream stream) throws IOException {
        if (doc instanceof X13Document) {
            writeX13Specification(stream);
        } else if (doc instanceof TramoSeatsDocument) {
            writeTramoSeatsSpecification(stream);
        }

    }

    private void writeX13Specification(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER2, "Specification");
        X13Specification specification = ((X13Document) doc).getSpecification();
        RegArimaSpecification regSpec = specification.getRegArimaSpecification();
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
                stream.write("Outliers critical value: auto").newLine();
            } else {
                stream.write("Outliers critical value: " + criticalValue).newLine();
            }
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

//            not included because also in regression model
//            for (OutlierDefinition variable : regSpec.getRegression().getOutliers()) {
//                stream.write("Regression variable: " + variable.toString()).newLine();
//            }
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

        X11Specification x11Spec = specification.getX11Specification();
        stream.write("Forecast horizon: " + x11Spec.getForecastHorizon()).newLine();
        stream.write("Sigmalimit: [" + x11Spec.getLowerSigma() + ";" + x11Spec.getUpperSigma() + "]").newLine();

        if (x11Spec.isSeasonal() && x11Spec.getSeasonalFilters() != null) {
            SeasonalFilterOption first = x11Spec.getSeasonalFilters()[0];
            boolean isSameSeasonalFilter = Arrays.stream(x11Spec.getSeasonalFilters()).allMatch(x -> x.equals(first));

            if (isSameSeasonalFilter) {
                stream.write("Seasonal filter:" + first.name()).newLine();
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
        stream.write("Excludefcst: " + x11Spec.isExcludefcst());
    }

    private void writeTramoSeatsSpecification(HtmlStream stream) throws IOException {
        TramoSeatsSpecification specification = ((TramoSeatsDocument) doc).getSpecification();

        stream.write(HtmlTag.HEADER2, "Specification");
        TramoSpecification regSpec = specification.getTramoSpecification();
        stream.write("Transform: ");
        stream.write(regSpec.getTransform().getFunction().toString()).newLine();

        //Outliers
        if (regSpec.getOutliers().isUsed()) {
            stream.write("Outlier detection: ");
            for (OutlierType type : regSpec.getOutliers().getTypes()) {
                stream.write(type.name() + " ");
            }
            stream.newLine();

            double criticalValue = regSpec.getOutliers().getCriticalValue();
            if (criticalValue == 0) {
                stream.write("Outliers critical value: auto").newLine();
            } else {
                stream.write("Outliers critical value: " + criticalValue).newLine();
            }
        }
        CalendarSpec calendar = regSpec.getRegression().getCalendar();

        if (calendar.getTradingDays().isUsed()) {

            stream.write("Regression variable: Trading days").newLine();

            if (calendar.getTradingDays().getUserVariables() != null && calendar.getTradingDays().getUserVariables().length > 0) {
                for (String userVariable : calendar.getTradingDays().getUserVariables()) {
                    stream.write("Trading days user-defined variable: " + userVariable).newLine();
                }
            }
            if (calendar.getTradingDays().isStockTradingDays()) {
                stream.write("Trading days: stock trading days").newLine();
            }

        }

        if (calendar.getEaster() != null) {
            stream.write("Regression variable: ").write(calendar.getEaster().getOption().toString()).newLine();
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

        SeatsSpecification seatsSpecification = specification.getSeatsSpecification();
        if (seatsSpecification != null) {
            stream.write("Prediction length: ").write(seatsSpecification.getPredictionLength()).newLine()
                    .write("Approximation mode: ").write(seatsSpecification.getApproximationMode().toString()).newLine()
                    .write("MA unit root boundary: ").write(seatsSpecification.getXlBoundary()).newLine()
                    .write("Trend boundary: ").write(seatsSpecification.getTrendBoundary()).newLine()
                    .write("Seasonal tolerance: ").write(seatsSpecification.getSeasTolerance()).newLine()
                    .write("Seasonal boundary: ").write(seatsSpecification.getSeasBoundary()).newLine()
                    .write("Seas. boundary (unique): ").write(seatsSpecification.getSeasBoundary1()).newLine()
                    .write("Method: ").write(seatsSpecification.getMethod().toString());
        }

    }

}

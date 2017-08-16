/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bbk.outputpdf.html;

import ec.satoolkit.x11.X11Specification;
import ec.satoolkit.x13.X13Specification;
import ec.tss.html.AbstractHtmlElement;
import ec.tss.html.HtmlStream;
import ec.tss.html.HtmlStyle;
import ec.tss.html.HtmlTable;
import ec.tss.html.HtmlTableCell;
import ec.tss.html.HtmlTag;
import ec.tss.html.IHtmlElement;
import ec.tss.html.implementation.HtmlRegArima;
import ec.tss.html.implementation.HtmlX13Summary;
import ec.tss.sa.diagnostics.ResidualsDiagnosticsConfiguration;
import java.io.IOException;
import ec.tss.sa.documents.X13Document;
import ec.tstoolkit.Parameter;
import ec.tstoolkit.arima.estimation.LikelihoodStatistics;
import ec.tstoolkit.dstats.T;
import ec.tstoolkit.eco.ConcentratedLikelihood;
import ec.tstoolkit.modelling.DefaultTransformationType;
import ec.tstoolkit.modelling.ModellingDictionary;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.modelling.arima.tramo.SeasonalityTests;
import static ec.tstoolkit.modelling.arima.x13.OutlierSpec.DEF_VA;
import ec.tstoolkit.modelling.arima.x13.RegArimaSpecification;
import ec.tstoolkit.modelling.arima.x13.SingleOutlierSpec;
import ec.tstoolkit.sarima.SarimaComponent;
import ec.tstoolkit.sarima.SarimaSpecification;
import ec.tstoolkit.stats.LjungBoxTest;
import ec.tstoolkit.stats.NiidTests;
import ec.tstoolkit.timeseries.regression.OutlierDefinition;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.util.Map;

/**
 *
 * @author Christiane Hofer
 */
public class HTMLBBKText1 extends AbstractHtmlElement implements IHtmlElement {

    private final X13Document x13Document;
    private final PreprocessingModel model;
    private LikelihoodStatistics statistics;
    private SeasonalityTests seasonalityTests;
    private NiidTests niidTests;
    private TsData tsDataSeries;

    public HTMLBBKText1(X13Document x13Document) {
        this.x13Document = x13Document;
        this.model = this.x13Document.getPreprocessingPart();
        this.tsDataSeries = x13Document.getResults().getData(ModellingDictionary.Y, TsData.class);

        if (model != null) {
            this.statistics = model.estimation.getStatistics();
            this.seasonalityTests = SeasonalityTests.seasonalityTest(model.description.transformedOriginal(), 1, true, false);
            this.niidTests = new NiidTests(model.getFullResiduals(), model.getFullResiduals().getFrequency().getAsInt(), model.description.getArimaComponent().getFreeParametersCount(), true);
        }
    }

    @Override
    public void write(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER2, h2, "Specification");
        if (model != null) {
            RegArimaSpecification regSpec = x13Document.getSpecification().getRegArimaSpecification();
            stream.write("Transform: ");
            stream.write(regSpec.getTransform().getFunction().toString()).newLine();
//Outlier   
            for (SingleOutlierSpec type : regSpec.getOutliers().getTypes()) {
                stream.write("Outlier detection: " + type.getType().name()).newLine();
            }
            double criticalValue = regSpec.getOutliers().getDefaultCriticalValue();
            if (criticalValue == 0) {
                criticalValue = DEF_VA;
            }
            stream.write("Outliers critical value is: " + criticalValue).newLine();

            if (regSpec.getRegression().getTradingDays().isUsed()) {
                stream.write("Regression variables: Trading days").newLine();
            }
            for (OutlierDefinition variable : regSpec.getRegression().getOutliers()) {
                stream.write("Regression variable: " + variable.toString()).newLine();
            }

            if (regSpec.isUsingAutoModel()) {
                stream.write("ARIMA model: auto");
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
            stream.write("Seasonal filters:" + x11Spec.getSeasonalFilters()[0].name() + ",");
            for (int i = 1; i < x11Spec.getSeasonalFilters().length - 1; i++) {
                stream.write(x11Spec.getSeasonalFilters()[i].name() + ",");
            }
            if (x11Spec.getSeasonalFilters().length > 1) {
                stream.write(x11Spec.getSeasonalFilters()[x11Spec.getSeasonalFilters().length - 1].name()).newLine();
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

            stream.write(HtmlTag.HEADER2, h2, "Regresssion model:");
            htmlRegArima.writeRegression(stream, true);
            stream.newLines(1);
            stream.write(HtmlTag.HEADER2, h2, "Arima model: ");
            writeArima(stream);
            stream.newLines(1);
           
        }
    }

    private void writeACF(HtmlStream stream) throws IOException {
        if (seasonalityTests != null && niidTests.getLjungBox() != null) {

            boolean first = true;
            LjungBoxTest lb = niidTests.getLjungBox();
            int ifreq = seasonalityTests.getDifferencing().getOriginal().getFrequency().intValue();
            stream.write("ACF-Ljung-Box tests on residuals: ");

            for (int i = 1; i < ifreq / 2 + 1; i++) {
                lb.setK(i);
                if (lb.isValid()) {

                    stream.write("P-Value(").write(i).write(")=").write(df4.format(lb.getPValue()), PValue(lb.getPValue()));
                    first = false;
                    if (!first) {
                        stream.write("; ");
                    }
                }
            }
            lb.setK(ifreq);
            if (lb.isValid()) {
                stream.write("P-Value(").write(ifreq).write(")=").write(df4.format(lb.getPValue()), PValue(lb.getPValue()));
            }
            lb.setK(2 * ifreq);
            if (lb.isValid()) {
                stream.write("; P-Value(").write(2 * ifreq).write(")=").write(df4.format(lb.getPValue()), PValue(lb.getPValue()));
            }
        }
    }

    /*
    Copy and Paste aus HtmlRegArima, because not pubic, for me this ist too much copy method should be public
     */
    private void writeArima(HtmlStream stream) throws IOException {
        SarimaComponent arima = model.description.getArimaComponent();
        SarimaSpecification sspec = arima.getSpecification();
        ConcentratedLikelihood ll = model.estimation.getLikelihood();
        int nhp_ = model.description.getArimaComponent().getFreeParametersCount();

        stream.write('[').write(sspec.toString()).write(']').newLines(2);
        if (sspec.getParametersCount() == 0) {
            return;
        }
        stream.open(new HtmlTable(0, 400));
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("", 100));
        stream.write(new HtmlTableCell("Coefficients", 100, HtmlStyle.Bold));
        stream.write(new HtmlTableCell("T-Stat", 100, HtmlStyle.Bold));
        stream.write(new HtmlTableCell("P[|T| &gt t]", 100, HtmlStyle.Bold));
        stream.close(HtmlTag.TABLEROW);
        int P = sspec.getP();
        Parameter[] p = arima.getPhi();
        T t = new T();
        t.setDegreesofFreedom(ll.getDegreesOfFreedom(true, nhp_));
        for (int j = 0; j < P; ++j) {
            stream.open(HtmlTag.TABLEROW);
            StringBuilder header = new StringBuilder();
            header.append("Phi(").append(j + 1).append(')');
            stream.write(new HtmlTableCell(header.toString(), 100));
            double val = p[j].getValue(), stde = p[j].getStde();
            stream.write(new HtmlTableCell(df4.format(val), 100));
            if (stde > 0) {
                double tval = val / stde;
                stream.write(new HtmlTableCell(formatT(tval), 100));
                double prob = 1 - t.getProbabilityForInterval(-tval, tval);
                stream.write(new HtmlTableCell(df4.format(prob), 100));
            }
            stream.close(HtmlTag.TABLEROW);
        }
        int Q = sspec.getQ();
        p = arima.getTheta();
        for (int j = 0; j < Q; ++j) {
            stream.open(HtmlTag.TABLEROW);
            StringBuilder header = new StringBuilder();
            header.append("Theta(").append(j + 1).append(')');
            stream.write(new HtmlTableCell(header.toString(), 100));
            double val = p[j].getValue(), stde = p[j].getStde();
            stream.write(new HtmlTableCell(df4.format(val), 100));
            if (stde > 0) {
                double tval = val / stde;
                stream.write(new HtmlTableCell(formatT(tval), 100));
                double prob = 1 - t.getProbabilityForInterval(-tval, tval);
                stream.write(new HtmlTableCell(df4.format(prob), 100));
            }
            stream.close(HtmlTag.TABLEROW);
        }
        int BP = sspec.getBP();
        p = arima.getBPhi();
        for (int j = 0; j < BP; ++j) {
            stream.open(HtmlTag.TABLEROW);
            StringBuilder header = new StringBuilder();
            header.append("BPhi(").append(j + 1).append(')');
            stream.write(new HtmlTableCell(header.toString(), 100));
            double val = p[j].getValue(), stde = p[j].getStde();
            stream.write(new HtmlTableCell(df4.format(val), 100));
            if (stde > 0) {
                double tval = val / stde;
                stream.write(new HtmlTableCell(formatT(tval), 100));
                double prob = 1 - t.getProbabilityForInterval(-tval, tval);
                stream.write(new HtmlTableCell(df4.format(prob), 100));
            }
            stream.close(HtmlTag.TABLEROW);
        }
        int BQ = sspec.getBQ();
        p = arima.getBTheta();
        for (int j = 0; j < BQ; ++j) {
            stream.open(HtmlTag.TABLEROW);
            StringBuilder header = new StringBuilder();
            header.append("BTheta(").append(j + 1).append(')');
            stream.write(new HtmlTableCell(header.toString(), 100));
            double val = p[j].getValue(), stde = p[j].getStde();
            stream.write(new HtmlTableCell(df4.format(val), 100));
            if (stde > 0) {
                double tval = val / stde;
                stream.write(new HtmlTableCell(formatT(tval), 100));
                double prob = 1 - t.getProbabilityForInterval(-tval, tval);
                stream.write(new HtmlTableCell(df4.format(prob), 100));
            }
            stream.close(HtmlTag.TABLEROW);
        }

        stream.close(HtmlTag.TABLE);
    }

    private HtmlStyle PValue(double val) {
        double badthreshold_ = ResidualsDiagnosticsConfiguration.NBAD;
        double goodthreshold_ = ResidualsDiagnosticsConfiguration.NUNC;

        if (val < badthreshold_) {
            return HtmlStyle.Danger;
        } else if (val < goodthreshold_) {
            return HtmlStyle.Warning;
        } else {
            return HtmlStyle.Success;
        }
    }

}

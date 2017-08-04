/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bbk.outputpdf.html;

import ec.tss.html.AbstractHtmlElement;
import ec.tss.html.HtmlStream;
import ec.tss.html.HtmlStyle;
import ec.tss.html.HtmlTable;
import ec.tss.html.HtmlTableCell;
import ec.tss.html.HtmlTag;
import ec.tss.html.IHtmlElement;
import ec.tss.html.implementation.HtmlRegArima;
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
import ec.tstoolkit.sarima.SarimaComponent;
import ec.tstoolkit.sarima.SarimaSpecification;
import ec.tstoolkit.stats.LjungBoxTest;
import ec.tstoolkit.stats.NiidTests;
import ec.tstoolkit.timeseries.simplets.TsData;

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
        if (model != null) {
            if (model.description.getTransformation() == DefaultTransformationType.Log) {
                stream.write("Series has been log-transformed").newLine();
            } else {
                stream.write("Series has not been transformed").newLine();
            }
            if (statistics != null) {
                stream.write("AIC = ").write(df4.format(statistics.AIC)).newLines(1);
            }

            //Arima Model
            HtmlRegArima htmlRegArima = new HtmlRegArima(model, false);
            stream.write(HtmlTag.HEADER2, h2, "Arima model: ");
            writeArima(stream);
            stream.newLines(1);
            stream.write(HtmlTag.HEADER2, h2, "Regresssion model:");
            htmlRegArima.writeRegression(stream, true);
            stream.newLines(1);
            
            double criticalValue=x13Document.getSpecification().getRegArimaSpecification().getOutliers().getDefaultCriticalValue();
                 if (criticalValue == 0) {
               criticalValue=DEF_VA;
            }
            
            
            stream.write("Outliers critical value is: " + criticalValue);
            stream.newLines(1);
            writeACF(stream);
        }
    }

    private void writeACF(HtmlStream stream) throws IOException {
        if (seasonalityTests != null && niidTests.getLjungBox() != null) {

            boolean first=true;
            LjungBoxTest lb = niidTests.getLjungBox();
            int ifreq = seasonalityTests.getDifferencing().getOriginal().getFrequency().intValue();
            stream.write("ACF-Ljung-Box tests on residuals: ");


            for (int i = 1; i < ifreq / 2 + 1; i++) {
                lb.setK(i);
                if (lb.isValid()) {
                  
                    stream.write("P-Value(").write(i).write(")=").write(df4.format(lb.getPValue()),PValue(lb.getPValue()));
                  first=false;
                  if(!first)
                      stream.write("; ");
                }
            }
            lb.setK(ifreq);
            if (lb.isValid()) {
                stream.write("P-Value(").write(ifreq).write(")=").write(df4.format(lb.getPValue()),PValue(lb.getPValue()));
            }
            lb.setK(2 * ifreq);
            if (lb.isValid()) {
                stream.write("; P-Value(").write(2 * ifreq).write(")=").write(df4.format(lb.getPValue()),PValue(lb.getPValue()));
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

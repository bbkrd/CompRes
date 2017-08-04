/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bbk.outputcustomized.html;

import ec.satoolkit.diagnostics.CombinedSeasonalityTest;
import ec.satoolkit.x11.X11Kernel;
import ec.satoolkit.x11.X11Results;
import ec.tss.html.AbstractHtmlElement;
import ec.tss.html.HtmlStream;
import ec.tss.html.HtmlTag;
import ec.tss.html.implementation.HtmlRegArima;
import ec.tss.sa.documents.X13Document;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.io.IOException;

/**
 *
 * @author Thomas Witthohn
 */
public class HtmlBBKSummary extends AbstractHtmlElement {

    private final String title;
    private final PreprocessingModel model;
    private final X11Results decomposition;

    public HtmlBBKSummary(String title, X13Document doc) {
        this.title = title;
        this.model = doc.getPreprocessingPart();
        this.decomposition = doc.getDecompositionPart();
    }

    @Override
    public void write(HtmlStream stream) throws IOException {
        writeTitle(stream);
        if (model != null) {
            writeOutliers(stream);
            writeDetails(stream);
        }
        if (decomposition != null) {
            writeCombinedSeasonalityTest(stream, decomposition);
        }

    }

    private void writeTitle(HtmlStream stream) throws IOException {
        if (title != null) {
            stream.write(HtmlTag.HEADER1, h1, title).newLine();
        }
    }

    private void writeOutliers(HtmlStream stream) throws IOException {
        int no = model.description.getOutliers().size();
        int npo = model.description.getPrespecifiedOutliers().size();

        if (npo > 1) {
            stream.write(Integer.toString(npo)).write(" pre-specified outliers").newLine();
        } else if (npo == 1) {
            stream.write(Integer.toString(npo)).write(" pre-specified outlier").newLine();
        }
        if (no > 1) {
            stream.write(Integer.toString(no)).write(" detected outliers").newLine();
        } else if (no == 1) {
            stream.write(Integer.toString(no)).write(" detected outlier").newLine();
        }
    }

    private void writeDetails(HtmlStream stream) throws IOException {
        HtmlRegArima htmlRegArima = new HtmlRegArima(model, true);
        stream.write(HtmlTag.HEADER2, h2, "Arima model");
        htmlRegArima.writeArima(stream);
        stream.write(HtmlTag.LINEBREAK);
        stream.write(HtmlTag.HEADER2, h2, "Regression model");
        htmlRegArima.writeRegression(stream);
        stream.write(HtmlTag.LINEBREAK);
    }

    public static void writeCombinedSeasonalityTest(HtmlStream stream, X11Results decomposition) throws IOException {
        CombinedSeasonalityTest test = new CombinedSeasonalityTest(decomposition.getData(X11Kernel.D8, TsData.class),
                                                                   decomposition.getSeriesDecomposition().getMode().isMultiplicative());

        stream.write(HtmlTag.HEADER2, h2, "F-Test for stable seasonality").newLine();
        stream.write("Stable Value: " + df4.format(test.getStableSeasonality().getValue())).newLine();
        stream.write("Stable PValue: " + df4.format(test.getStableSeasonality().getPValue())).newLines(2);

        stream.write(HtmlTag.HEADER2, h2, "F-Test for moving seasonality").newLine();
        stream.write("Evolutive Value: " + df4.format(test.getEvolutiveSeasonality().getValue())).newLine();
        stream.write("Evolutive PValue: " + df4.format(test.getEvolutiveSeasonality().getPValue())).newLines(2);
    }

}

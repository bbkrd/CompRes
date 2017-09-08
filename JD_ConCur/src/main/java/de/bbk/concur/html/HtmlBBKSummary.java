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
package de.bbk.concur.html;

import ec.satoolkit.diagnostics.CombinedSeasonalityTest;
import ec.satoolkit.x11.X11Kernel;
import ec.satoolkit.x11.X11Results;
import ec.tss.html.AbstractHtmlElement;
import ec.tss.html.HtmlStream;
import ec.tss.html.HtmlTag;
import ec.tss.html.implementation.HtmlProcessingInformation;
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
        writeMainResultsSummary(stream);
        if (model != null) {
            writeDetails(stream);
        }
        if (decomposition != null) {
            writeCombinedSeasonalityTest(stream, decomposition);
        }

    }

    private void writeMainResultsSummary(HtmlStream stream) throws IOException {
        HtmlProcessingInformation hpi = new HtmlProcessingInformation(this.decomposition);
        hpi.write(stream);

        if (model == null) {
            stream.write(HtmlTag.HEADER2, h2, "No pre-processing").newLine();
        } else {
            stream.write(HtmlTag.HEADER2, h2, "Pre-processing (RegArima)").newLine();
            stream.write(new HtmlRegArima(model, true));
        }
    }

    private void writeTitle(HtmlStream stream) throws IOException {
        if (title != null) {
            stream.write(HtmlTag.HEADER1, h1, title).newLine();
        }
    }

    private void writeDetails(HtmlStream stream) throws IOException {
        HtmlRegArima htmlRegArima = new HtmlRegArima(model, true);
        stream.write(HtmlTag.HEADER2, h2, "Regression model");
        htmlRegArima.writeRegression(stream);
        stream.write(HtmlTag.LINEBREAK);
        stream.write(HtmlTag.HEADER2, h2, "Arima model");
        htmlRegArima.writeArima(stream);
        stream.write(HtmlTag.LINEBREAK);

    }

    public static void writeCombinedSeasonalityTest(HtmlStream stream, X11Results decomposition) throws IOException {
        CombinedSeasonalityTest test = new CombinedSeasonalityTest(decomposition.getData(X11Kernel.D8, TsData.class),
                                                                   decomposition.getSeriesDecomposition().getMode().isMultiplicative());

        stream.write(HtmlTag.HEADER2, h2, "F-Test for stable seasonality");
        stream.write("Stable Value: " + df4.format(test.getStableSeasonality().getValue())).newLine();
        stream.write("Stable PValue: " + df4.format(test.getStableSeasonality().getPValue())).newLines(2);

        stream.write(HtmlTag.HEADER2, h2, "F-Test for moving seasonality");
        stream.write("Evolutive Value: " + df4.format(test.getEvolutiveSeasonality().getValue())).newLine();
        stream.write("Evolutive PValue: " + df4.format(test.getEvolutiveSeasonality().getPValue())).newLines(2);
    }

}

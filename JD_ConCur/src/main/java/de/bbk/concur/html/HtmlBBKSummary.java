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
import ec.tss.sa.documents.SaDocument;
import ec.tstoolkit.algorithm.IProcResults;
import ec.tstoolkit.modelling.ModellingDictionary;
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
    private final IProcResults decomposition;
    private final boolean multiplicative;

    public HtmlBBKSummary(String title, SaDocument doc) {
        this.title = title;
        this.model = doc.getPreprocessingPart();
        this.decomposition = doc.getDecompositionPart();
        if (doc.getFinalDecomposition() != null) {
            this.multiplicative = doc.getFinalDecomposition().getMode().isMultiplicative();
        } else {
            this.multiplicative = false;
        }
    }

    @Override
    public void write(HtmlStream stream) throws IOException {
        writeTitle(stream);
        writeMainResultsSummary(stream);
        if (model != null) {
            writeDetails(stream);
        }
        if (decomposition != null) {
            writeCombinedSeasonalityTest(stream);
        }

    }

    private void writeMainResultsSummary(HtmlStream stream) throws IOException {
        HtmlProcessingInformation hpi = new HtmlProcessingInformation(decomposition);
        hpi.write(stream);

        if (model == null) {
            stream.write(HtmlTag.HEADER2, "No pre-processing").newLine();
        } else {
            stream.write(HtmlTag.HEADER2, "Pre-processing").newLine();
            stream.write(new HtmlRegArima(model, true));
        }
    }

    private void writeTitle(HtmlStream stream) throws IOException {
        if (title != null) {
            stream.write(HtmlTag.HEADER1, title).newLine();
        }
    }

    private void writeDetails(HtmlStream stream) throws IOException {
        HtmlRegArima htmlRegArima = new HtmlRegArima(model, true);
        stream.write(HtmlTag.HEADER2, "Regression model");
        htmlRegArima.writeRegression(stream);
        stream.write(HtmlTag.LINEBREAK);
        stream.write(HtmlTag.HEADER2, "Arima model");
        htmlRegArima.writeArima(stream);
        stream.write(HtmlTag.LINEBREAK);

    }

    public void writeCombinedSeasonalityTest(HtmlStream stream) throws IOException {
        TsData si;
        if (decomposition instanceof X11Results) {
            si = decomposition.getData(X11Kernel.D8, TsData.class);
        } else {
            TsData seas = decomposition.getData(ModellingDictionary.S_CMP, TsData.class);
            TsData i = decomposition.getData(ModellingDictionary.I_CMP, TsData.class);

            if (multiplicative) {
                si = TsData.multiply(seas, i);
            } else {
                si = TsData.add(seas, i);
            }
        }

        CombinedSeasonalityTest test = new CombinedSeasonalityTest(si, multiplicative);

        stream.write(HtmlTag.HEADER2, "F-Test for stable seasonality");
        stream.write("Stable Value: " + df4.format(test.getStableSeasonality().getValue())).newLine();
        stream.write("Stable PValue: " + df4.format(test.getStableSeasonality().getPValue())).newLines(2);

        stream.write(HtmlTag.HEADER2, "F-Test for moving seasonality");
        stream.write("Evolutive Value: " + df4.format(test.getEvolutiveSeasonality().getValue())).newLine();
        stream.write("Evolutive PValue: " + df4.format(test.getEvolutiveSeasonality().getPValue())).newLines(2);
    }

}

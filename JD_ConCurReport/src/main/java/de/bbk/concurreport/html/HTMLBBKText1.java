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

import ec.tss.html.AbstractHtmlElement;
import ec.tss.html.HtmlStream;
import ec.tss.html.implementation.HtmlRegArima;
import ec.tss.sa.documents.SaDocument;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import java.io.IOException;

/**
 *
 * @author Christiane Hofer
 */
public class HTMLBBKText1 extends AbstractHtmlElement {

    private final SaDocument doc;
    private final PreprocessingModel model;

    public HTMLBBKText1(SaDocument doc) {
        this.doc = doc;
        this.model = doc.getPreprocessingPart();
    }

    @Override
    public void write(HtmlStream stream) throws IOException {
        if (model != null) {
            stream.write(new HtmlSpecification(doc)).newLines(2)
                    .write(new HtmlRegArima(model, true)).newLine()
                    .write(new HTMLRegressionModel(model)).newLine()
                    .write(new HtmlWrapperARIMA(model)).newLine()
                    .write(new HTMLOutOfSampleTest(model));
        }
    }
}

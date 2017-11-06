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
import ec.tss.html.IHtmlElement;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.modelling.arima.diagnostics.OneStepAheadForecastingTest;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import java.io.IOException;

/**
 *
 * @author s4504ch
 */
public class HTMLOutOfSampleTest extends AbstractHtmlElement implements IHtmlElement {

    PreprocessingModel model_;

    public HTMLOutOfSampleTest(PreprocessingModel model) {

        model_ = model;

    }

    @Override
    public void write(HtmlStream stream) throws IOException {
        int lback;
            TsFrequency freq = model_.description.getSeriesDomain().getFrequency();
            switch (freq) {
                case Monthly:
                    lback = 18;
                    break;
                case Quarterly:
                    lback = 6;
                    break;
                case BiMonthly:
                    lback = 9;
                    break;
                default:
                    lback = 5;
                    break;
            }
            OneStepAheadForecastingTest test = new OneStepAheadForecastingTest(lback);
            test.test(model_.estimation.getRegArima());
            stream.write("MSE Out of Sample ");
            stream.write(df4.format(test.getOutOfSampleMSE()));
    }

}

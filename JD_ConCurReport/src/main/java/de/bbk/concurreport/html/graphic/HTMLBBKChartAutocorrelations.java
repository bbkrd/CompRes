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
package de.bbk.concurreport.html.graphic;

import de.bbk.concurreport.BbkAutoCorrelationsView;
import ec.tss.html.AbstractHtmlElement;
import ec.tss.html.HtmlStream;
import ec.tss.sa.documents.SaDocument;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.ui.view.AutoCorrelationsView;
import ec.util.chart.swing.Charts;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 *
 * @author Christiane Hofer
 */
public class HTMLBBKChartAutocorrelations extends AbstractHtmlElement {

    private final SaDocument doc;
    private final boolean partial;
    private final BbkAutoCorrelationsView acv;
    private static final int WIDTH = 450, HEIGHT = 450;

    public HTMLBBKChartAutocorrelations(SaDocument doc, boolean partial) {
        this.doc = doc;
        this.partial = partial;
        this.acv = new BbkAutoCorrelationsView();

    }

    @Override
    public void write(HtmlStream stream) throws IOException {

        if (doc.getPreprocessingPart() == null) {
            return;
        }
        TsData tsFullRes = doc.getPreprocessingPart().getFullResiduals();
        if (tsFullRes != null && !tsFullRes.isEmpty()) {
            acv.setDataBlock(tsFullRes);

            if (partial) {
                acv.setKind(AutoCorrelationsView.ACKind.Partial);
            }

            acv.setSize(WIDTH, HEIGHT);
            acv.doLayout();
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            acv.extendTitle(" (Full Residuals)");
            Charts.writeChartAsSVG(os, acv.getChart(), WIDTH, HEIGHT);
            stream.write(os.toString());
        }

    }
}

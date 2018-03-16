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

import de.bbk.concur.util.SIViewSaved;
import ec.tss.html.AbstractHtmlElement;
import ec.tss.html.HtmlStream;
import ec.tss.html.HtmlTag;
import ec.tss.html.IHtmlElement;
import ec.tss.sa.documents.X13Document;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.util.chart.swing.Charts;
import java.awt.Dimension;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.swing.JPanel;

/**
 *
 * @author Christiane Hofer
 */
public class HTMLBBKSIRatioView extends AbstractHtmlElement implements IHtmlElement {

    private final X13Document x13doc;
    private static final int WIDTH = 450, HEIGHT = 250;
    int lastPeriod = 0;
    int periodbeforLastPeriod = 0;

    public HTMLBBKSIRatioView(X13Document x13doc) {
        this.x13doc = x13doc;
        lastPeriod = x13doc.getSeries().getDomain().getLast().getPosition();
        if (lastPeriod == 0) {
            if (x13doc.getSeries().getDomain().getFrequency() == TsFrequency.Monthly) {
                periodbeforLastPeriod = 11;
            } else if (x13doc.getSeries().getDomain().getFrequency() == TsFrequency.Monthly) {
                periodbeforLastPeriod = 3;
            }
        } else {
            periodbeforLastPeriod = lastPeriod - 1;
        }
    }

    @Override
    public void write(HtmlStream stream) throws IOException {

        SIViewSaved sIViewSaved = new SIViewSaved();
        sIViewSaved.setDoc(x13doc);

        sIViewSaved.setSize(new Dimension(WIDTH, HEIGHT));
        sIViewSaved.setMaximumSize(new Dimension(WIDTH, HEIGHT));
        sIViewSaved.setMinimumSize(new Dimension(WIDTH, HEIGHT));
        sIViewSaved.setPreferredSize(new Dimension(WIDTH, HEIGHT));

        JPanel jPanel = new JPanel();
        jPanel.add(sIViewSaved);

        jPanel.setSize(WIDTH, HEIGHT);
        stream.write(HtmlTag.HEADER2, h2, "S-I-Ratio");
        sIViewSaved.doLayout();
        ByteArrayOutputStream os_last = new ByteArrayOutputStream();
        //      Charts.writeChartAsSVG(os, sIViewSaved.getJFreeChart(), WIDTH, HEIGHT);
        Charts.writeChartAsSVG(os_last, sIViewSaved.getDetailChart(lastPeriod), WIDTH, HEIGHT);
        ByteArrayOutputStream os_beforlast = new ByteArrayOutputStream();
        //      Charts.writeChartAsSVG(os, sIViewSaved.getJFreeChart(), WIDTH, HEIGHT);
        Charts.writeChartAsSVG(os_beforlast, sIViewSaved.getDetailChart(periodbeforLastPeriod), WIDTH, HEIGHT);

        HTMLByteArrayOutputStream lastArrayOutputStream = new HTMLByteArrayOutputStream(os_last);
        HTMLByteArrayOutputStream beforlastArrayOutputStream = new HTMLByteArrayOutputStream(os_beforlast);

        HTML2Div div = new HTML2Div(beforlastArrayOutputStream, lastArrayOutputStream);
        div.write(stream);

        stream.write("<p style='text-align:center; '>");
        stream.write("Dots - D8, red - current D10, blue - D10");
        stream.write("</p>");
        stream.newLine();
        sIViewSaved.dispose();
        jPanel.removeAll();
    }
}

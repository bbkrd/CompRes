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
import ec.tss.sa.documents.X13Document;
import ec.util.chart.swing.Charts;
import java.awt.Dimension;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.swing.JPanel;

/**
 *
 * @author Christiane Hofer
 */
public class HTMLBBKSIRatioView extends AbstractHtmlElement {

    private final X13Document x13doc;
    private static final int WIDTH = 450, HEIGHT = 250;
    int lastPeriod = 0;
    int forelastPeriod = 0;

    public HTMLBBKSIRatioView(X13Document x13doc) {
        this.x13doc = x13doc;
        lastPeriod = x13doc.getSeries().getDomain().getLast().getPosition();
        if (lastPeriod == 0) {
            forelastPeriod = x13doc.getSeries().getFrequency().intValue() - 1;
        } else {
            forelastPeriod = lastPeriod - 1;
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
        stream.write(HtmlTag.HEADER2, "S-I-Ratio");
        sIViewSaved.doLayout();
        ByteArrayOutputStream osLast = new ByteArrayOutputStream();
        Charts.writeChartAsSVG(osLast, sIViewSaved.getDetailChart(lastPeriod), WIDTH, HEIGHT);
        ByteArrayOutputStream osForeast = new ByteArrayOutputStream();
        Charts.writeChartAsSVG(osForeast, sIViewSaved.getDetailChart(forelastPeriod), WIDTH, HEIGHT);

        HTMLByteArrayOutputStream lastArrayOutputStream = new HTMLByteArrayOutputStream(osLast);
        HTMLByteArrayOutputStream forelastArrayOutputStream = new HTMLByteArrayOutputStream(osForeast);

        HTML2Div div = new HTML2Div(forelastArrayOutputStream, lastArrayOutputStream);
        div.write(stream);

        stream.write("<p style='text-align:center; '>")
                .write("Dots - D8, red - current D10, blue - D10")
                .write("</p>")
                .newLine();
        sIViewSaved.dispose();
        jPanel.removeAll();
    }
}

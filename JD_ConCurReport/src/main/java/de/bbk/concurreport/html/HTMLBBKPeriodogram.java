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

import de.bbk.concurreport.BbkPeriodogramView;
import ec.tss.html.AbstractHtmlElement;
import ec.tss.html.HtmlStream;
import ec.tss.html.IHtmlElement;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.util.chart.swing.Charts;
import java.awt.Dimension;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.jfree.chart.JFreeChart;

/**
 *
 * @author Christiane Hofer
 */
@Deprecated
public class HTMLBBKPeriodogram extends AbstractHtmlElement implements IHtmlElement {

    private final TsData tsData;
    private final int width = 900;
    private final int height = 450;

    public HTMLBBKPeriodogram(TsData tsData) {
        this.tsData = tsData;

    }

    @Override
    public void write(HtmlStream stream) throws IOException {

        BbkPeriodogramView pView = new BbkPeriodogramView();
        int freq = this.tsData.getFrequency().intValue();
        pView.setLimitVisible(false);
        pView.setDifferencingOrder(0);
        pView.setData("Periodogram", freq, tsData);
        pView.setSize(width, height);

        pView.setMaximumSize(new Dimension(width, height));
        pView.setMinimumSize(new Dimension(width, height));
        pView.setMaximumSize(new Dimension(width, height));
        pView.setPreferredSize(new Dimension(width, height));
        pView.doLayout();

        ByteArrayOutputStream os;
        os = new ByteArrayOutputStream();

        JFreeChart jfc = pView.getChartPanel().getChart();
        Charts.writeChartAsSVG(os, jfc, width, height);
        stream.write(os.toString());
//        JPanel jPanel = new JPanel();
//        jPanel.add(m_pView);
//
//        jPanel.setSize(width, height);
//        jPanel.setMaximumSize(new Dimension(width, height));
//        jPanel.setMinimumSize(new Dimension(width, height));
//        jPanel.setPreferredSize(new Dimension(width, height));
//        jPanel.doLayout();
//
//        svgJComponent = new SVGJComponent(jPanel);
//        svgJComponent.write(stream);

    }
}

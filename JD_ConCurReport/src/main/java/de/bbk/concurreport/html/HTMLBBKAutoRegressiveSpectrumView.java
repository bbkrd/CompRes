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

import de.bbk.concurreport.BbkAutoRegressiveSpectrumView;
import ec.satoolkit.ISeriesDecomposition;
import ec.tss.html.AbstractHtmlElement;
import ec.tss.html.HtmlStream;
import ec.tss.html.IHtmlElement;
import ec.tss.sa.documents.X13Document;
import ec.tstoolkit.modelling.ComponentInformation;
import ec.tstoolkit.modelling.ComponentType;
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
public class HTMLBBKAutoRegressiveSpectrumView extends AbstractHtmlElement implements IHtmlElement {

    private final X13Document x13doc;
    private final int width = 450;
    private final int height = 220; //450

    /**
     *
     * @param tsData the time series is differenced in the write
     */
    public HTMLBBKAutoRegressiveSpectrumView(X13Document x13doc) {

        this.x13doc = x13doc;
    }

    @Override
    public void write(HtmlStream stream) throws IOException {

        ISeriesDecomposition finals = x13doc.getFinalDecomposition();
        if (finals == null) {
            return;
        }

        TsData tsData = finals.getSeries(ComponentType.Series, ComponentInformation.Value);

        BbkAutoRegressiveSpectrumView pView = new BbkAutoRegressiveSpectrumView();
        int freq = tsData.getFrequency().intValue();
        pView.setDifferencingOrder(1);
        pView.setData("Auto-regressive spectrum (" + ComponentType.Series + " stationary)", freq, tsData);
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
        pView.dispose();

    }
}

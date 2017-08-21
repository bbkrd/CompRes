/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bbk.outputpdf.html;

import de.bbk.outputpdf.BbkAutoCorrelationsView;
import ec.tss.html.AbstractHtmlElement;
import ec.tss.html.HtmlStream;
import ec.tss.html.IHtmlElement;
import ec.tss.sa.documents.X13Document;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.ui.view.AutoCorrelationsView;
import ec.util.chart.swing.Charts;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 *
 * @author Christiane Hofer
 */
public class HTMLBBKChartAutocorrelations extends AbstractHtmlElement implements IHtmlElement {

    private final X13Document x13doc;
    private final boolean partial;
    private final BbkAutoCorrelationsView acv;
    private static final int WIDTH = 450, HEIGHT = 450;

    public HTMLBBKChartAutocorrelations(X13Document x13doc, boolean partial) {
        this.x13doc = x13doc;
        this.partial = partial;
        this.acv = new BbkAutoCorrelationsView();
    
    }

    @Override
    public void write(HtmlStream stream) throws IOException {

        if (x13doc.getPreprocessingPart() == null) {
            return;
        }
        TsData tsFullRes = x13doc.getPreprocessingPart().getFullResiduals();
        if (tsFullRes != null && !tsFullRes.isEmpty()) {
            acv.setDataBlock(tsFullRes);

            if (partial) {
                acv.setKind(AutoCorrelationsView.ACKind.Partial);
            }
            x13doc.getPreprocessingPart().getFullResiduals();

            acv.setSize(WIDTH, HEIGHT);
            acv.doLayout();
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            Charts.writeChartAsSVG(os, acv.getChart(), WIDTH, HEIGHT);
            stream.write(os.toString());
        }

    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bbk.outputpdf.html;

//import de.bbk.outputcustomized.util.TsData_MetaDataConverter;
import de.bbk.outputcustomized.util.SIViewSaved;
import de.bbk.outputcustomized.view.SIRatioView;
import de.bbk.outputpdf.SVGJComponent;
import ec.tss.html.AbstractHtmlElement;
import ec.tss.html.HtmlStream;
import ec.tss.html.HtmlTag;
import ec.tss.html.IHtmlElement;
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
public class HTMLBBKSIRatioView extends AbstractHtmlElement implements IHtmlElement {

    private X13Document x13doc;
    private static final int WIDTH = 2 * 450, HEIGHT = 450;
    private SVGJComponent bBKSIRATioview;

    public HTMLBBKSIRatioView(X13Document x13doc) {
        this.x13doc = x13doc;

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
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Charts.writeChartAsSVG(os, sIViewSaved.getJFreeChart(), WIDTH, HEIGHT);

        stream.write(os.toString()).newLine();
        stream.write(" <p style='text-align:center; '>");
        stream.write("Dots - D8, red – current D10, blue – D10");
        stream.write("</p>");
        stream.newLine();
    }

    public void dispose() {
        this.bBKSIRATioview.dispose();
        this.x13doc = null;

    }
}

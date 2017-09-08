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
package de.bbk.concurreport;

import ec.tss.html.HtmlStream;
import ec.ui.interfaces.IDisposable;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.io.IOException;
import java.io.StringWriter;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.svg.SVGDocument;

/**
 *
 * @author Christiane Hofer compare with
 * https://github.com/compomics/mitraq/blob/master/src/main/java/no/uib/mitraq/util/Export.java
 */
public class SVGJComponent implements IDisposable {

    private JPanel jPanel;
    private SVGGraphics2D gGraphics2D;

    public SVGJComponent(JPanel jp) {
        this.jPanel = jp;
    }

    public void write(HtmlStream stream) throws IOException {

        // jPanel.setSize(WIDTH, HEIGHT); //muss gemacht werden sonst exception
        jPanel.doLayout();
        int width, hight;
        width = jPanel.getMaximumSize().width + 1; //+1 is needed for monthly
        hight = jPanel.getMaximumSize().height;

        gGraphics2D = drawSvgGraphics(jPanel, new Rectangle(width, hight));
        StringWriter sWriter = new StringWriter();
        gGraphics2D.stream(sWriter, true /* use css */);
        stream.write(sWriter.getBuffer().toString());
    }

    private static SVGGraphics2D drawSvgGraphics(JPanel component, Rectangle bounds) {

        // Get a SVGDOMImplementation and create an XML document
        DOMImplementation domImpl = SVGDOMImplementation.getDOMImplementation();
        String svgNS = "http://www.w3.org/2000/svg";
        SVGDocument svgDocument = (SVGDocument) domImpl.createDocument(svgNS, "svg", null);

        // Create an instance of the SVG Generator
        SVGGraphics2D svgGenerator = new SVGGraphics2D(svgDocument);
        svgGenerator.setSVGCanvasSize(bounds.getSize());

        // draw the panel in the SVG generator
        component.setVisible(true);

        JFrame jp = new JFrame();
        jp.setLayout(new FlowLayout()); // needed to get all on the screen otherwise it is centeresd
        jp.setVisible(true);  // is needed otherwise no information from the top component
        jp.add(component);
        //   jp.setSize(component.getSize());
        jp.validate();
        component.update(svgGenerator);
        jp.dispose(); //workaround to get rid of the jp
        return svgGenerator;
    }

    @Override
    public void dispose() {
        this.jPanel = null;
        this.gGraphics2D.dispose();
    }
}

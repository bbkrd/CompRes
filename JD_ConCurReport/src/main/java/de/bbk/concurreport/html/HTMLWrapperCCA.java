/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bbk.concurreport.html;

import de.bbk.concur.html.HtmlCCA;
import ec.tss.html.HtmlStream;
import ec.tss.sa.documents.X13Document;
import java.io.IOException;

public class HTMLWrapperCCA extends HtmlCCA {

    private String trend = "";

    public HTMLWrapperCCA(String title, X13Document doc) {
        super(title, doc);
        if (doc != null && doc.getDecompositionPart() != null) {
            trend = doc.getDecompositionPart().getFinalTrendFilter();
        }
    }

    @Override
    public void write(HtmlStream stream) throws IOException {
        writeTextForHTML(stream);
        if (!trend.isEmpty()) {
            stream.write("<b>Final trend filter:</b> " + trend).newLine();
        }
    }

}

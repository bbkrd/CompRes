/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bbk.concurreport.report.tramo;

import de.bbk.concurreport.html.HTMLBBKTableD8B;
import de.bbk.concurreport.html.HTMLBBkHeader;
import ec.tss.html.HtmlStream;
import ec.tss.html.IHtmlElement;
import ec.tss.sa.SaItem;
import ec.tss.sa.documents.SaDocument;
import ec.tss.sa.documents.TramoSeatsDocument;
import java.io.IOException;

/**
 *
 */
public class D8BReport implements IHtmlElement {

    private final SaItem item;

    public D8BReport(SaItem item) {
        this.item = item;
    }

    @Override
    public void write(HtmlStream stream) throws IOException {
        final HTMLBBkHeader headerbbk = new HTMLBBkHeader(item.getRawName(), item.getTs());
        stream.write(headerbbk)
                .newLine();
        SaDocument<?> doc = item.toDocument();
        if (doc instanceof TramoSeatsDocument) {
            TramoSeatsDocument tramoSeatsDocument = (TramoSeatsDocument) doc;
            stream.write(new HTMLBBKTableD8B(tramoSeatsDocument));
        } else {
            stream.write("The item doesn't contain a TramoSeatsSpecification!");
        }
    }

}

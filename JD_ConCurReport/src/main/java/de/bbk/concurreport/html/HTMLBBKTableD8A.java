/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bbk.concurreport.html;

//import de.bbk.concur.util.TsData_MetaDataConverter;
import de.bbk.concur.util.JPanelCCA;
import de.bbk.concurreport.SVGJComponent;
import ec.tss.html.AbstractHtmlElement;
import ec.tss.html.HtmlStream;
import ec.tss.html.IHtmlElement;
import ec.tss.sa.documents.X13Document;
import java.io.IOException;

/**
 *
 * @author Christiane Hofer
 */
public class HTMLBBKTableD8A extends AbstractHtmlElement implements IHtmlElement {

    private final X13Document x13doc;

    public HTMLBBKTableD8A(X13Document x13doc) {
        this.x13doc = x13doc;
    }

    @Override
    public void write(HtmlStream stream) throws IOException {

        JPanelCCA jpcca = new JPanelCCA();
        jpcca.set(x13doc);

        SVGJComponent bBKTableD8B = new SVGJComponent(jpcca.getCCAPanel());
        bBKTableD8B.write(stream);

        stream.newLine();
    }
}

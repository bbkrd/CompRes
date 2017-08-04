/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bbk.outputpdf.html;

//import de.bbk.outputcustomized.util.TsData_MetaDataConverter;
import de.bbk.outputcustomized.util.JPanelCCA;
import de.bbk.outputpdf.SVGJComponent;
import ec.tss.html.AbstractHtmlElement;
import ec.tss.html.HtmlStream;
import ec.tss.html.IHtmlElement;
import ec.tss.sa.documents.X13Document;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.ui.interfaces.IDisposable;
import java.io.IOException;

/**
 *
 * @author Christiane Hofer
 */
public class HTMLBBKTableD8A extends AbstractHtmlElement implements IHtmlElement {

    private  X13Document x13doc;
    private  TsDomain domMax5;
    private SVGJComponent bBKTableD8B;

    public HTMLBBKTableD8A(X13Document x13doc, TsDomain tsDom) {
        this.x13doc = x13doc;
        this.domMax5 = tsDom;
    }

    @Override
    public void write(HtmlStream stream) throws IOException {

        JPanelCCA jpcca = new JPanelCCA();
        jpcca.set(x13doc);

        bBKTableD8B = new SVGJComponent(jpcca.getCCPanel());
//ersetzte alle 3 scroll panels durch panels
        bBKTableD8B.write(stream);

        stream.newLine();
    }

 
    public void dispose() {
        this.bBKTableD8B.dispose(); 
        this.x13doc=null;
        this.domMax5=null;
    }
}

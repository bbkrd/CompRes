/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bbk.outputpdf.html;

import ec.nbdemetra.ws.WorkspaceFactory;
import ec.tss.html.AbstractHtmlElement;
import ec.tss.html.HtmlStream;
import ec.tss.html.HtmlTag;
import ec.tss.html.IHtmlElement;
import java.io.IOException;
import java.time.LocalDateTime;
import de.bbk.outputpdf.util.Frozen;

/**
 *
 * @author s4504ch
 */
public class HTMLBBkHeader extends AbstractHtmlElement implements IHtmlElement {

    private final String nameSaProcessing;
    private String nameSAItem;
    private String nameSeries;

    public HTMLBBkHeader(String nameSAProcessing, String nameSaItem, String nameSeries) {
        this.nameSaProcessing = nameSAProcessing;
        this.nameSAItem = nameSaItem;
        this.nameSeries = nameSeries;
    }

    @Override
    public void write(HtmlStream stream) throws IOException {
        //WorkspaceFactory.getInstance().getActiveWorkspace().getName(); // Name von  aktiven WS
        nameSAItem = Frozen.removeFrozen(nameSAItem);
        nameSeries = Frozen.removeFrozen(nameSeries);

        stream.write(HtmlTag.HEADER1, h1, WorkspaceFactory.getInstance().getActiveWorkspace().getName()
                + "- " + nameSaProcessing).newLines(1);
        stream.write(nameSAItem);
        stream.write("-");
        stream.write(nameSeries);
        stream.newLines(1);

    }

}

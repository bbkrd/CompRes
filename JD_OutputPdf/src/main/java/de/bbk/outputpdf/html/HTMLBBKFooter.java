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
import ec.tss.Ts;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author s4504ch
 */
public class HTMLBBKFooter extends AbstractHtmlElement implements IHtmlElement {

    private final Ts ts;
    private String nameSeries;

    public HTMLBBKFooter(Ts ts) {
        this.ts = ts;
    }

    @Override
    public void write(HtmlStream stream) throws IOException {
        //WorkspaceFactory.getInstance().getActiveWorkspace().getName(); // Name von  aktiven WS
        stream.newLines(1);
        nameSeries = Frozen.removeFrozen(ts.getRawName());
        stream.write(nameSeries).newLine();
        stream.write("From " + ts.getTsData().getStart() + " to " + ts.getTsData().getEnd() + " ").newLine();
        stream.write(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + " " + System.getProperty("user.name"));
    }

}

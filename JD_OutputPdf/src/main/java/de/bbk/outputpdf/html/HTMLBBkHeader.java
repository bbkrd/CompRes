/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bbk.outputpdf.html;

import ec.tss.html.AbstractHtmlElement;
import ec.tss.html.HtmlStream;
import ec.tss.html.IHtmlElement;
import java.io.IOException;
import java.time.LocalDateTime;
import de.bbk.outputpdf.util.Frozen;
import ec.tss.Ts;
import ec.tss.html.HtmlStyle;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author s4504ch
 */
public class HTMLBBkHeader extends AbstractHtmlElement implements IHtmlElement {

    private String nameSAItem;
    private String nameSeries;
    private final Ts ts;

    
    public HTMLBBkHeader(String nameSaItem, Ts ts) {

        this.nameSAItem = nameSaItem;
        this.nameSeries = ts.getRawName();
        this.ts = ts;
    
    }

    @Override
    public void write(HtmlStream stream) throws IOException {
        //WorkspaceFactory.getInstance().getActiveWorkspace().getName(); // Name von  aktiven WS
        stream.write(" <p style='text-align:right; '>");
        stream.write(  LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + " " + System.getProperty("user.name"));
        stream.write("</p>");
        nameSAItem = Frozen.removeFrozen(nameSAItem);
        nameSeries = Frozen.removeFrozen(nameSeries);
        stream.write( nameSAItem, HtmlStyle.Bold).newLine();
        stream.write(nameSeries).newLine();
        stream.write("From " + ts.getTsData().getStart() + " to " + ts.getTsData().getLastPeriod() + " ").newLine(); // ToDo Series Span?
    }

   
}

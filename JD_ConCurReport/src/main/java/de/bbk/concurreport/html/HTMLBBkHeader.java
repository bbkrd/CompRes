/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bbk.concurreport.html;

import de.bbk.concurreport.util.Frozen;
import ec.tss.Ts;
import ec.tss.html.AbstractHtmlElement;
import ec.tss.html.HtmlStream;
import ec.tss.html.HtmlStyle;
import ec.tss.html.IHtmlElement;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 *
 * @author s4504ch
 */
public class HTMLBBkHeader extends AbstractHtmlElement implements IHtmlElement {

    private final String nameSAItem;
    private final String nameSeries;
    private final TsData tsData;

    public HTMLBBkHeader(String nameSaItem, Ts ts) {

        this.nameSAItem = Frozen.removeFrozen(nameSaItem);
        this.nameSeries = ts.getRawName();
        this.tsData = ts.getTsData();

    }

    @Override
    public void write(HtmlStream stream) throws IOException {
        //WorkspaceFactory.getInstance().getActiveWorkspace().getName(); // Name von  aktiven WS
        stream.write(" <p style='text-align:right; '>");
        stream.write(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + " " + System.getProperty("user.name"));
        stream.write("</p>");
        stream.write(nameSAItem, HtmlStyle.Bold).newLine();
        stream.write(nameSeries).newLine();
        stream.write("From " + tsData.getStart() + " to " + tsData.getLastPeriod()).newLine(); // ToDo Series Span?
    }

}

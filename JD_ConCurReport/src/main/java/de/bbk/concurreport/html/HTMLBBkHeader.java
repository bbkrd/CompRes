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
package de.bbk.concurreport.html;

import de.bbk.concurreport.util.Frozen;
import ec.nbdemetra.ws.WorkspaceFactory;
import ec.tss.Ts;
import ec.tss.html.AbstractHtmlElement;
import ec.tss.html.HtmlStream;
import ec.tss.html.HtmlStyle;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 *
 * @author s4504ch
 */
public class HTMLBBkHeader extends AbstractHtmlElement {

    private final String saProcessingName;
    private final String nameSAItem;
    private final String nameSeries;
    private final TsData tsData;

    public HTMLBBkHeader(String saProcessingName, String nameSaItem, Ts ts) {
        this.saProcessingName = saProcessingName;
        this.nameSAItem = Frozen.removeFrozen(nameSaItem);
        this.nameSeries = ts.getRawName();
        this.tsData = ts.getTsData();
    }

    @Override
    public void write(HtmlStream stream) throws IOException {
        String workspaceName = WorkspaceFactory.getInstance().getActiveWorkspace().getName(); // Name von  aktiven WS
        stream.write(" <p style='text-align:right; '>");
        stream.write(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + " " + System.getProperty("user.name"));
        stream.write("</p>");
        if (!nameSAItem.isEmpty()) {
            stream.write(nameSAItem, HtmlStyle.Bold).write(" - ");
        }
        stream.write(workspaceName).write(" - ").write(saProcessingName).newLine();
        stream.write(nameSeries).newLine();
        stream.write("From " + tsData.getStart() + " to " + tsData.getLastPeriod()).newLine(); // ToDo Series Span?
    }

}

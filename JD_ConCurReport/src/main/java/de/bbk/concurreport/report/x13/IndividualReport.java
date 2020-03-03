/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bbk.concurreport.report.x13;

import de.bbk.concurreport.html.HTMLBBkHeader;
import de.bbk.concurreport.html.HtmlTsData;
import de.bbk.concurreport.options.ConCurReportOptionsPanel;
import static de.bbk.concurreport.options.ConCurReportOptionsPanel.*;
import de.bbk.concurreport.util.Pagebreak;
import ec.tss.Ts;
import ec.tss.documents.DocumentManager;
import ec.tss.html.HtmlStream;
import ec.tss.html.IHtmlElement;
import ec.tss.sa.SaItem;
import ec.tss.sa.documents.SaDocument;
import ec.tss.sa.documents.X13Document;
import java.io.IOException;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 *
 * @author s4504tw
 */
public class IndividualReport implements IHtmlElement {

    private final SaItem item;

    public IndividualReport(SaItem item) {
        this.item = item;
    }

    @Override
    public void write(HtmlStream stream) throws IOException {
        final HTMLBBkHeader headerbbk = new HTMLBBkHeader(item.getRawName(), item.getTs());
        stream.write(headerbbk)
                .newLine();
        SaDocument<?> doc = item.toDocument();
        if (doc instanceof X13Document) {
//            X13Document x13doc = (X13Document) doc;
            Preferences preferences = NbPreferences.forModule(ConCurReportOptionsPanel.class);
            int tableTimespan = preferences.getInt(TIMESPAN_TABLE, DEFAULT_TIMESPAN_TABLE);
            int graphicTimespan = preferences.getInt(TIMESPAN_GRAPHIC, DEFAULT_TIMESPAN_GRAPHIC);

            String x13 = preferences.get(USER_DEFINED_REPORT_CONTENT_X13, "");
            for (String name : x13.split(";")) {
                Ts ts = DocumentManager.instance.getTs(doc, name);
                if (ts.getTsData() != null) {
                    stream.write(HtmlTsData.builder()
                            .data(ts.getTsData())
                            .title(name.toUpperCase())
                            .build())
                            .newLine();
                }
            }
        } else {
            stream.write("The item doesn't contain a X13Specification!");
        }
        stream.write(new Pagebreak());
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bbk.concurreport.report.x13;

import de.bbk.concurreport.Graphic;
import de.bbk.concurreport.MainTable;
import de.bbk.concurreport.Value;
import de.bbk.concurreport.html.HTMLBBKTableD8B;
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
            Preferences preferences = NbPreferences.forModule(ConCurReportOptionsPanel.class);

            String x13 = preferences.get(USER_DEFINED_REPORT_CONTENT_X13, "");
            if (!x13.isEmpty()) {
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
            }
            String main = preferences.get(USER_DEFINED_REPORT_CONTENT_MAIN, "");
            if (!main.isEmpty()) {
                for (String name : main.split(";")) {
                    MainTable table = MainTable.fromDisplayName(name);
                    if (table == null) {
                        continue;
                    }
                    Ts ts = DocumentManager.instance.getTs(doc, table.getCompositeFormula());
                    if (ts.getTsData() != null) {
                        stream.write(HtmlTsData.builder()
                                .data(ts.getTsData())
                                .title(name)
                                .build())
                                .newLine();
                    }
                }
            }

            String d8b = preferences.get(USER_DEFINED_REPORT_CONTENT_D8B, "");
            if (!d8b.isEmpty()) {
                stream.write(new HTMLBBKTableD8B(doc));
            }
            String values = preferences.get(USER_DEFINED_REPORT_CONTENT_VALUE, "");
            if (!values.isEmpty()) {
                for (String name : values.split(";")) {
                    Value value = Value.fromDisplayName(name);
                    if (value == null) {
                        continue;
                    }
                    stream.write(value.createFromDoc(doc)).newLine();
                }
            }

            String graphics = preferences.get(USER_DEFINED_REPORT_CONTENT_GRAPHIC, "");
            if (!graphics.isEmpty()) {
                for (String name : graphics.split(";")) {
                    Graphic graphic = Graphic.fromDisplayName(name);
                    if (graphic == null) {
                        continue;
                    }
                    stream.write(graphic.createFromDoc(doc));
                }
            }
        } else {
            stream.write("The item doesn't contain a X13Specification!");
        }
        stream.write(new Pagebreak());
    }

}

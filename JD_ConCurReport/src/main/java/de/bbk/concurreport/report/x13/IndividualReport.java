/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bbk.concurreport.report.x13;

import de.bbk.concur.util.FixTimeDomain;
import de.bbk.concurreport.BBKMainChart;
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
import ec.tss.TsCollection;
import ec.tss.TsCollectionInformation;
import ec.tss.TsFactory;
import ec.tss.TsInformationType;
import ec.tss.documents.DocumentManager;
import ec.tss.html.HtmlStream;
import ec.tss.html.IHtmlElement;
import ec.tss.sa.SaItem;
import ec.tss.sa.documents.SaDocument;
import ec.tss.sa.documents.X13Document;
import ec.tstoolkit.timeseries.simplets.TsDomain;
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
        Preferences preferences = NbPreferences.forModule(ConCurReportOptionsPanel.class);

        if (preferences.getBoolean(INCLUDE_SHORT_REPORT, DEFAULT_INCLUDE_SHORT_REPORT)) {
            new ShortReport(item).write(stream);
        }

        int timespanGraphic = preferences.getInt(TIMESPAN_GRAPHIC, DEFAULT_TIMESPAN_GRAPHIC);
        final HTMLBBkHeader headerbbk = new HTMLBBkHeader(item.getRawName(), item.getTs());
        stream.write(headerbbk)
                .newLine();
        SaDocument<?> doc = item.toDocument();
        if (doc instanceof X13Document) {
            boolean multiplicative = doc.getFinalDecomposition().getMode().isMultiplicative();

            String x13 = preferences.get(USER_DEFINED_REPORT_CONTENT_X13, "");
            if (!x13.isEmpty()) {
                for (String name : x13.split(";")) {
                    writeTsData(doc, name, stream, name.toUpperCase(), 1);
                }
            }

            String x13Transformed = preferences.get(USER_DEFINED_REPORT_CONTENT_X13_TRANSFORMED, "");
            if (!x13Transformed.isEmpty()) {
                String suffix = multiplicative ? " transformed" : "";
                int multiplier = multiplicative ? 100 : 1;

                for (String name : x13Transformed.split(";")) {
                    writeTsData(doc, name, stream, name.toUpperCase() + suffix, multiplier);
                }
            }

            String x13ChartOne = preferences.get(USER_DEFINED_REPORT_CONTENT_X13_CHART_ONE, "");
            if (!x13ChartOne.isEmpty()) {
                writeChart(x13ChartOne, doc, timespanGraphic, stream);
            }

            String x13ChartTwo = preferences.get(USER_DEFINED_REPORT_CONTENT_X13_CHART_TWO, "");
            if (!x13ChartTwo.isEmpty()) {
                writeChart(x13ChartTwo, doc, timespanGraphic, stream);
            }

            String main = preferences.get(USER_DEFINED_REPORT_CONTENT_MAIN, "");
            if (!main.isEmpty()) {
                for (String name : main.split(";")) {
                    MainTable table = MainTable.fromDisplayName(name);
                    if (table == null) {
                        continue;
                    }
                    writeTsData(doc, table.getCompositeFormula(), stream, name, 1);
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

    private void writeTsData(SaDocument<?> doc, String key, HtmlStream stream, String name, int multiplier) throws IOException {
        Ts ts = DocumentManager.instance.getTs(doc, key);
        if (ts.getTsData() != null) {
            stream.write(HtmlTsData.builder()
                    .data(ts.getTsData().times(multiplier))
                    .title(name)
                    .build())
                    .newLine();
        } else {
            stream.write(name + " is not available.").newLine();
        }
    }

    private void writeChart(String input, SaDocument<?> doc, int timespanGraphic, HtmlStream stream) throws IOException {
        TsCollection tc = TsFactory.instance.createTsCollection();
        for (String name : input.split(";")) {
            Ts ts = DocumentManager.instance.getTs(doc, name);
            if (ts.getTsData() != null) {
                TsDomain domLastYears = FixTimeDomain.domLastYears(ts, timespanGraphic);
                tc.add(TsFactory.instance.createTs(name.toUpperCase(), null, ts.getTsData().fittoDomain(domLastYears)));
            }
        }

        TsCollectionInformation collectionInformation = tc.toInfo(TsInformationType.Data);
        BBKMainChart tschart = new BBKMainChart();
        tschart.writeChart(collectionInformation, stream);
        tschart.dispose();
    }

}

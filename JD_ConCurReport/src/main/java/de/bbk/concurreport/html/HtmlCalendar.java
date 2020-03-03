/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bbk.concurreport.html;

import de.bbk.concur.util.InPercent;
import static de.bbk.concur.util.SavedTables.COMPOSITE_RESULTS_CALENDAR_WITH_FORECAST;
import ec.tss.documents.DocumentManager;
import ec.tss.html.HtmlStream;
import ec.tss.html.HtmlTag;
import ec.tss.html.IHtmlElement;
import ec.tss.sa.documents.SaDocument;
import ec.tss.sa.documents.X13Document;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import java.io.IOException;

/**
 *
 * @author s4504tw
 */
public class HtmlCalendar implements IHtmlElement {

    private final SaDocument doc;

    public HtmlCalendar(SaDocument doc) {
        this.doc = doc;
    }

    @Override
    public void write(HtmlStream stream) throws IOException {
        boolean multiplicative = doc.getFinalDecomposition().getMode().isMultiplicative();
        TsData calendarFactor = DocumentManager.instance.getTs(doc, COMPOSITE_RESULTS_CALENDAR_WITH_FORECAST).getTsData();
        String header;
        if (doc instanceof X13Document) {
            header = multiplicative ? "A6 * A7" : "A6 + A7";
        } else {
            header = "Calendar Factor";
        }

        if (calendarFactor != null) {
            int frequency = calendarFactor.getFrequency().intValue();
            calendarFactor = InPercent.convertTsDataInPercentIfMult(calendarFactor, multiplicative);
            if (calendarFactor.getDomain().getYearsCount() > 10) {
                TsDomain domainLastTenYears = new TsDomain(calendarFactor.getEnd().minus(frequency * 10), frequency * 10);
                calendarFactor = calendarFactor.fittoDomain(domainLastTenYears);
            }

            stream.write("<table style=\"table-layout:fixed\" >")
                    .write(HtmlTsData.builder()
                            .data(calendarFactor)
                            .title(header)
                            .includeTableTags(false)
                            .build())
                    .close(HtmlTag.TABLE);
        }
    }

}

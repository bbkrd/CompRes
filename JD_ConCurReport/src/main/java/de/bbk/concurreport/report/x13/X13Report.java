/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bbk.concurreport.report.x13;

import de.bbk.concurreport.ReportStyle;
import de.bbk.concurreport.options.ConCurReportOptionsPanel;
import static de.bbk.concurreport.options.ConCurReportOptionsPanel.REPORT_STYLE;
import ec.tss.html.HtmlStream;
import ec.tss.html.IHtmlElement;
import ec.tss.sa.SaItem;
import java.io.IOException;
import org.openide.util.NbPreferences;

/**
 *
 * @author s4504tw
 */
public class X13Report implements IHtmlElement {

    private final SaItem item;

    public X13Report(SaItem item) {
        this.item = item;
    }

    @Override
    public void write(HtmlStream stream) throws IOException {
        IHtmlElement report;
        String reportStyleName = NbPreferences.forModule(ConCurReportOptionsPanel.class).get(REPORT_STYLE, ReportStyle.SHORT.name());
        ReportStyle reportStyle = ReportStyle.valueOf(reportStyleName);

        switch (reportStyle) {
            case SHORT:
                report = new ShortReport(item);
                break;
            case LONG:
                report = new LongReport(item);
                break;
            case D8B:
                report = new D8BReport(item);
                break;
            case INDIVIDUAL:
                report = new IndividualReport(item);
                break;
            default:
                throw new AssertionError(reportStyle.name());

        }

        report.write(stream);

    }

}

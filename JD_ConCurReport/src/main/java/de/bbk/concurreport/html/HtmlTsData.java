/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bbk.concurreport.html;

import de.bbk.concurreport.options.ConCurReportOptionsPanel;
import static de.bbk.concurreport.options.ConCurReportOptionsPanel.DEFAULT_TIMESPAN_TABLE;
import static de.bbk.concurreport.options.ConCurReportOptionsPanel.TIMESPAN_TABLE;
import ec.tss.html.AbstractHtmlElement;
import ec.tss.html.HtmlStream;
import ec.tss.html.HtmlTag;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDataBlock;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import ec.tstoolkit.timeseries.simplets.YearIterator;
import java.io.IOException;
import java.util.Formatter;
import org.openide.util.NbPreferences;

/**
 *
 * @author Deutsche Bundesbank
 */
@lombok.Builder
public class HtmlTsData extends AbstractHtmlElement {

    private final TsData data;
    @lombok.Builder.Default
    private final String title = "";
    @lombok.Builder.Default
    private final boolean includeHeader = true;
    @lombok.Builder.Default
    private final boolean includeTableTags = true;
    @lombok.Builder.Default
    private final boolean dataItalic = false;
    @lombok.Builder.Default
    private final String numberFormat = "%.2f";
    @lombok.Builder.Default
    private final int maxTimeInYears = NbPreferences.forModule(ConCurReportOptionsPanel.class).getInt(TIMESPAN_TABLE, DEFAULT_TIMESPAN_TABLE);

    @Override
    public void write(HtmlStream stream) throws IOException {
        if (data == null) {
            return;
        }
        int frequency = data.getFrequency().intValue();

        if (includeTableTags) {
            stream.write("<table style=\"table-layout:fixed\" >");
        }

        if (!title.isEmpty()) {
            stream.open(HtmlTag.TABLEROW);
            stream.write("<th colspan=\"")
                    .write(String.valueOf(frequency + 1))
                    .write("\" style=\"text-align:left\">")
                    .write(title);
            stream.close(HtmlTag.TABLEHEADER);
            stream.close(HtmlTag.TABLEROW);
        }

        if (includeHeader) {
            stream.open(HtmlTag.TABLEROW);
            stream.write(HtmlTag.TABLEHEADER);
            for (int i = 0; i < frequency; ++i) {
                stream.write(HtmlTag.TABLEHEADER, TsPeriod.formatShortPeriod(data.getFrequency(), i));
            }
            stream.close(HtmlTag.TABLEROW);
        }

        int maxDomainLength = data.getEnd().getPosition() + maxTimeInYears * frequency;
        TsDomain domain = new TsDomain(data.getEnd().minus(maxDomainLength), maxDomainLength + 1).intersection(data.getDomain());

        YearIterator iter = new YearIterator(data.fittoDomain(domain));
        while (iter.hasMoreElements()) {
            stream.open(HtmlTag.TABLEROW);
            TsDataBlock block = iter.nextElement();
            stream.write(HtmlTag.TABLEHEADER,
                    Integer.toString(block.start.getYear()));
            int start = block.start.getPosition();
            int n = block.data.getLength();
            for (int i = 0; i < start; ++i) {
                stream.write(HtmlTag.TABLECELL);
            }
            for (int i = 0; i < n; ++i) {
                if (Double.isFinite(block.data.get(i))) {
                    Formatter formatter = new Formatter();
                    formatter.format(numberFormat, block.data.get(i));
                    String tmp;
                    if (dataItalic) {
                        tmp = "<i>" + formatter.toString() + "</i>";
                    } else {
                        tmp = formatter.toString();
                    }
                    stream.write(HtmlTag.TABLECELL, tmp);
                } else {
                    stream.write(HtmlTag.TABLECELL, ".");
                }
            }

            for (int i = start + n; i < frequency; ++i) {
                stream.write(HtmlTag.TABLECELL);
            }
            stream.close(HtmlTag.TABLEROW);
        }

        if (includeTableTags) {
            stream.close(HtmlTag.TABLE);
        }
    }

}

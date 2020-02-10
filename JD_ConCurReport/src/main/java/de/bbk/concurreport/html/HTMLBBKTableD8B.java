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

import de.bbk.concur.html.HtmlTsData;
import de.bbk.concur.util.FixTimeDomain;
import de.bbk.concur.util.JPanelCCA;
import de.bbk.concur.view.TablesPercentageChangeView;
import ec.satoolkit.x11.DefaultSeasonalFilteringStrategy;
import ec.satoolkit.x11.MsrTable;
import ec.satoolkit.x11.Mstatistics;
import ec.satoolkit.x11.X11Results;
import ec.tss.html.AbstractHtmlElement;
import ec.tss.html.HtmlStream;
import ec.tss.html.HtmlTableCell;
import ec.tss.html.HtmlTag;
import ec.tss.sa.documents.X13Document;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import java.io.IOException;

/**
 *
 * @author Christiane Hofer
 */
public class HTMLBBKTableD8B extends AbstractHtmlElement {

    private final X13Document x13doc;
    private final int frequency;

    public HTMLBBKTableD8B(X13Document x13doc) {
        this.x13doc = x13doc;
        this.frequency = x13doc.getInput().getTsData().getFrequency().intValue();
    }

    @Override
    public void write(HtmlStream stream) throws IOException {
        stream.write("<table style=\"table-layout:fixed\" >");
        stream.open(HtmlTag.TABLEROW);
        stream.write("<th colspan=\"")
                .write(String.valueOf(frequency + 1))
                .write("\" style=\"text-align:left\">")
                .write("D8B");
        stream.close(HtmlTag.TABLEHEADER);
        stream.close(HtmlTag.TABLEROW);

        writeSeasonalFilter(stream);
        writeMSR(stream);

        JPanelCCA jpcca = new JPanelCCA();
        jpcca.set(x13doc);
        jpcca.getTablesAsHtml(stream, false, false);
        jpcca.dispose();

        writeGrowthRates(stream);

        stream.close(HtmlTag.TABLE);
        stream.newLine();
    }

    private void writeGrowthRates(HtmlStream stream) throws IOException {
        TablesPercentageChangeView tpcv = new TablesPercentageChangeView();
        tpcv.set(x13doc);
        TsData seasonallyAdjustedPercentageChange = tpcv.getSeasonallyAdjustedPercentageChange().getTsData();
        TsData savedSeasonallyAdjustedPercentageChange = tpcv.getSavedSeasonallyAdjustedPercentageChange().getTsData();
        tpcv.dispose();

        TsDomain domain = x13doc.getSeries().getDomain();

        stream.open(HtmlTag.TABLEROW);
        stream.write("<th colspan=\"")
                .write(String.valueOf(frequency + 1))
                .write("\" style=\"text-align:left; border-top: 3px solid\">")
                .write("new GR");
        stream.close(HtmlTag.TABLEHEADER);
        stream.close(HtmlTag.TABLEROW);
        stream.write(HtmlTsData.builder()
                .data(lastYearOfSeries(domain, seasonallyAdjustedPercentageChange))
                .includeHeader(false)
                .includeTableTags(false)
                .dataItalic(true)
                .build());

        stream.open(HtmlTag.TABLEROW);
        stream.write("<th colspan=\"")
                .write(String.valueOf(frequency + 1))
                .write("\" style=\"text-align:left\">")
                .write("current GR");
        stream.close(HtmlTag.TABLEHEADER);
        stream.close(HtmlTag.TABLEROW);
        stream.write(HtmlTsData.builder()
                .data(lastYearOfSeries(domain, savedSeasonallyAdjustedPercentageChange))
                .includeHeader(false)
                .includeTableTags(false)
                .dataItalic(true)
                .build());
    }

    private void writeMSR(HtmlStream stream) throws IOException {
        Mstatistics stats = x13doc.getMStatistics();
        if (stats != null) {
            stream.open(HtmlTag.TABLEROW)
                    .open(HtmlTag.TABLEHEADER)
                    .write("MSR")
                    .close(HtmlTag.TABLEHEADER);
            MsrTable msrTable = stats.getRms();
            for (int j = 0; j < frequency; ++j) {
                double rms = msrTable.getRMS(j);
                if (Double.isFinite(rms)) {
                    stream.write(new HtmlTableCell(df2.format(rms)));
                } else {
                    stream.write(new HtmlTableCell("."));
                }
            }
        }
    }

    private void writeSeasonalFilter(HtmlStream stream) throws IOException {
        X11Results decomposition = x13doc.getDecompositionPart();

        if (decomposition != null) {
            stream.open(HtmlTag.TABLEROW);
            stream.open(HtmlTag.TABLEHEADER).write("Seas. Filter").close(HtmlTag.TABLEHEADER);
            if (decomposition.getFinalSeasonalFilterComposit() != null) {
                for (DefaultSeasonalFilteringStrategy dsfs : decomposition.getFinalSeasonalFilterComposit()) {
                    String filter = dsfs == null ? "Stable" : dsfs.getDescription();
                    stream.open(HtmlTag.TABLECELL).write(filter).close(HtmlTag.TABLECELL);
                }
            } else {
                stream.write("<td colspan=\"")
                        .write(String.valueOf(frequency))
                        .write("\" style=\"text-align:center\">")
                        .write(decomposition.getFinalSeasonalFilter())
                        .close(HtmlTag.TABLECELL);
            }
            stream.close(HtmlTag.TABLEROW);
        }
    }

    private TsData lastYearOfSeries(TsDomain dom, TsData tsData) {
        dom = FixTimeDomain.domLastYear(dom);

        if (tsData != null) {
            TsDomain intersection = tsData.getDomain().intersection(dom);
            return tsData.fittoDomain(intersection);
        }
        return new TsData(dom);

    }
}

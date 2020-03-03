/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bbk.concurreport.report.x13;

import de.bbk.concurreport.html.HtmlTsData;
import ec.satoolkit.x11.X11Kernel;
import ec.tss.html.HtmlStream;
import ec.tss.html.HtmlTag;
import ec.tss.html.IHtmlElement;
import ec.tss.html.implementation.HtmlMstatistics;
import ec.tss.sa.SaItem;
import ec.tss.sa.documents.SaDocument;
import ec.tss.sa.documents.X13Document;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.io.IOException;

/**
 *
 * @author s4504tw
 */
public class LongReport implements IHtmlElement {

    private final SaItem item;
    private final CompositeResults compositeResults;

    public LongReport(SaItem item) {
        this.item = item;
        this.compositeResults = item.toDocument().getResults();
    }

    @Override
    public void write(HtmlStream stream) throws IOException {
        stream.write(new ShortReport(item));
        SaDocument<?> doc = item.toDocument();
        if (compositeResults == null) {
            return;
        }
        if (doc instanceof X13Document) {
            write(stream, X11Kernel.A1, true);
            write(stream, X11Kernel.A8, false);
            write(stream, X11Kernel.B1, false);
            write(stream, X11Kernel.D7, false);
            write(stream, X11Kernel.D9, false);
            write(stream, X11Kernel.D10, true);
            write(stream, X11Kernel.D11, false);
            write(stream, X11Kernel.D11a, false);
            write(stream, X11Kernel.D12, false);
            write(stream, X11Kernel.D13, false);

            stream.write(new HtmlMstatistics(((X13Document) doc).getMStatistics()));

        }
    }

    private void write(HtmlStream stream, String tsName, boolean withForecast) throws IOException {
        TsData tsData = compositeResults.getData(tsName, TsData.class);
        if (withForecast) {
            TsData forecast = compositeResults.getData(tsName + "a", TsData.class);
            tsData = tsData.update(forecast);
        }

        stream.write("<table style=\"table-layout:fixed\" >")
                .write(HtmlTsData.builder()
                        .data(tsData)
                        .title(tsName.toUpperCase())
                        .includeTableTags(false)
                        .build())
                .close(HtmlTag.TABLE)
                .newLine();
    }
}

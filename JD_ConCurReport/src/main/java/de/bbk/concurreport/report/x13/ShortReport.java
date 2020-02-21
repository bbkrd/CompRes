/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bbk.concurreport.report.x13;

import de.bbk.concur.util.FixTimeDomain;
import de.bbk.concurreport.html.HTML2Div;
import de.bbk.concurreport.html.HTMLBBKAutoRegressiveSpectrumView;
import de.bbk.concurreport.html.HTMLBBKBox;
import de.bbk.concurreport.html.HTMLBBKChartAutocorrelations;
import de.bbk.concurreport.html.HTMLBBKChartMain;
import de.bbk.concurreport.html.HTMLBBKPeriodogram;
import de.bbk.concurreport.html.HTMLBBKSIRatioView;
import de.bbk.concurreport.html.HTMLBBKTableD8B;
import de.bbk.concurreport.html.HTMLBBKText1;
import de.bbk.concurreport.html.HTMLBBkHeader;
import de.bbk.concurreport.html.HTMLWrapperCCA;
import de.bbk.concurreport.html.HtmlCalendar;
import de.bbk.concurreport.html.HtmlComments;
import de.bbk.concurreport.html.HtmlSavedSeasonalFactor;
import de.bbk.concurreport.util.Pagebreak;
import ec.tss.Ts;
import ec.tss.documents.DocumentManager;
import ec.tss.html.HtmlStream;
import ec.tss.html.IHtmlElement;
import ec.tss.sa.SaItem;
import ec.tss.sa.documents.SaDocument;
import ec.tss.sa.documents.X13Document;
import ec.tss.tsproviders.utils.MultiLineNameUtil;
import ec.tstoolkit.modelling.ModellingDictionary;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import java.io.IOException;

/**
 *
 * @author s4504tw
 */
public class ShortReport implements IHtmlElement {

    private final SaItem item;

    public ShortReport(SaItem item) {
        this.item = item;
    }

    @Override
    public void write(HtmlStream stream) throws IOException {
        final HTMLBBkHeader headerbbk = new HTMLBBkHeader(item.getRawName(), item.getTs());
        stream.write(headerbbk)
                .newLine();
        SaDocument<?> doc = item.toDocument();
        if (doc instanceof X13Document) {
            X13Document x13doc = (X13Document) doc;
            stream.write(createDiv(x13doc))
                    .write(new Pagebreak())
                    .write(headerbbk)
                    .write(new HtmlCalendar(x13doc))
                    .newLine()
                    .write(new HTMLBBKTableD8B(x13doc))
                    .newLine()
                    .write(new HtmlSavedSeasonalFactor(x13doc))
                    .write(new HTMLBBKSIRatioView(x13doc))
                    .newLine()
                    .write(new HtmlComments(x13doc, headerbbk));
        } else {
            stream.write("The item doesn't contain a X13Specification!");
        }
    }

    private HTML2Div createDiv(X13Document x13doc) {
        Ts tsY = DocumentManager.instance.getTs(x13doc, ModellingDictionary.Y);
        TsDomain domCharMax5years = FixTimeDomain.domLastFiveYears(tsY);

        HTMLBBKBox leftBox = new HTMLBBKBox();
        leftBox.add(new HTMLBBKText1(x13doc));
        leftBox.add(new HTMLWrapperCCA(MultiLineNameUtil.join(x13doc.getInput().getName()), x13doc));

        HTMLBBKBox rightBox = new HTMLBBKBox();
        rightBox.add(new HTMLBBKChartMain(x13doc, domCharMax5years));
        rightBox.add(new HTMLBBKChartAutocorrelations(x13doc, false));
        rightBox.add(new HTMLBBKAutoRegressiveSpectrumView(x13doc));
        rightBox.add(new HTMLBBKPeriodogram(x13doc));

        return new HTML2Div(leftBox, rightBox);
    }

}

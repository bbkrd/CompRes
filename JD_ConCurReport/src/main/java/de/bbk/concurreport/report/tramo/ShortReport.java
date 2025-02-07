/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bbk.concurreport.report.tramo;

import de.bbk.concur.util.FixTimeDomain;
import de.bbk.concurreport.html.HTML2Div;
import de.bbk.concurreport.html.HTMLBBKBox;
import de.bbk.concurreport.html.HTMLBBKTableD8B;
import de.bbk.concurreport.html.HTMLBBKText1;
import de.bbk.concurreport.html.HTMLBBkHeader;
import de.bbk.concurreport.html.HtmlCalendar;
import de.bbk.concurreport.html.HtmlComments;
import de.bbk.concurreport.html.HtmlSavedSeasonalFactor;
import de.bbk.concurreport.html.graphic.HTMLBBKAutoRegressiveSpectrumView;
import de.bbk.concurreport.html.graphic.HTMLBBKChartAutocorrelations;
import de.bbk.concurreport.html.graphic.HTMLBBKChartMain;
import de.bbk.concurreport.html.graphic.HTMLBBKPeriodogram;
import de.bbk.concurreport.html.graphic.HTMLBBKSIRatioLastTwoPeriodView;
import de.bbk.concurreport.options.ConCurReportOptionsPanel;
import static de.bbk.concurreport.options.ConCurReportOptionsPanel.*;
import de.bbk.concurreport.util.Pagebreak;
import ec.tss.Ts;
import ec.tss.documents.DocumentManager;
import ec.tss.html.HtmlStream;
import ec.tss.html.HtmlTag;
import ec.tss.html.IHtmlElement;
import ec.tss.sa.SaItem;
import ec.tss.sa.documents.SaDocument;
import ec.tss.sa.documents.TramoSeatsDocument;
import ec.tstoolkit.modelling.ModellingDictionary;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import java.io.IOException;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 *
 * @author s4504tw
 */
public class ShortReport implements IHtmlElement {

    private final String saProcessingName;
    private final SaItem item;

    public ShortReport(String saProcessingName, SaItem item) {
        this.saProcessingName = saProcessingName;
        this.item = item;
    }

    @Override
    public void write(HtmlStream stream) throws IOException {
        final HTMLBBkHeader headerbbk = new HTMLBBkHeader(saProcessingName, item.getRawName(), item.getTs());
        if (!item.getRawName().trim().isEmpty()) {
            stream.open(HtmlTag.DIV, "id", item.getRawName()).close(HtmlTag.DIV);
        }
        stream.write(headerbbk)
                .newLine();
        SaDocument<?> doc = item.toDocument();
        if (doc instanceof TramoSeatsDocument) {
            TramoSeatsDocument tramoSeatsDocument = (TramoSeatsDocument) doc;
            stream.write(createDiv(tramoSeatsDocument, item.getRawName()))
                    .write(new Pagebreak())
                    .write(headerbbk)
                    .write(new HtmlCalendar(tramoSeatsDocument))
                    .newLine()
                    .write(new HTMLBBKTableD8B(tramoSeatsDocument))
                    .newLine()
                    .write(new HtmlSavedSeasonalFactor(tramoSeatsDocument))
                    .write(new HTMLBBKSIRatioLastTwoPeriodView(tramoSeatsDocument))
                    .newLine()
                    .write(new HtmlComments(tramoSeatsDocument, headerbbk));
        } else {
            stream.write("The item doesn't contain a TramoSeatsSpecification!");
        }
    }

    private HTML2Div createDiv(TramoSeatsDocument doc, String rawName) {
        Preferences preferences = NbPreferences.forModule(ConCurReportOptionsPanel.class);
        Ts tsY = DocumentManager.instance.getTs(doc, ModellingDictionary.Y);
        TsDomain domain = FixTimeDomain.domLastYears(tsY, preferences.getInt(TIMESPAN_GRAPHIC, DEFAULT_TIMESPAN_GRAPHIC));

        HTMLBBKBox leftBox = new HTMLBBKBox();
        leftBox.add(new HTMLBBKText1(doc, rawName));
        //leftBox.add(new HTMLWrapperCCA(MultiLineNameUtil.join(doc.getInput().getName()), doc));

        HTMLBBKBox rightBox = new HTMLBBKBox();
        rightBox.add(new HTMLBBKChartMain(doc, domain));
        rightBox.add(new HTMLBBKChartAutocorrelations(doc, false));
        rightBox.add(new HTMLBBKAutoRegressiveSpectrumView(doc));
        rightBox.add(new HTMLBBKPeriodogram(doc));

        return new HTML2Div(leftBox, rightBox);
    }

}

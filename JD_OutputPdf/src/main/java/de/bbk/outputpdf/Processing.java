/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bbk.outputpdf;

import de.bbk.outputcustomized.html.HtmlCCA;
import de.bbk.outputcustomized.util.FixTimeDomain;
import de.bbk.outputcustomized.view.TablesPercentageChangeView;
import de.bbk.outputpdf.files.HTMLFiles;
import de.bbk.outputpdf.html.*;
import de.bbk.outputpdf.util.Frozen;
import de.bbk.outputpdf.util.Pagebreak;
import ec.satoolkit.ISaSpecification;
import ec.tss.Ts;
import ec.tss.documents.DocumentManager;
import ec.tss.documents.TsDocument;
import ec.tss.html.AbstractHtmlElement;
import ec.tss.html.HtmlStream;
import ec.tss.html.implementation.HtmlSingleTsData;
import ec.tss.sa.SaItem;
import ec.tss.sa.documents.SaDocument;
import ec.tss.sa.documents.X13Document;
import ec.tss.tsproviders.utils.MultiLineNameUtil;
import ec.tstoolkit.modelling.ModellingDictionary;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JOptionPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Christieane Hofer
 */
public class Processing {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    private HTMLFiles htmlf;

    public void start(SaItem[] selection, String name) {
        htmlf = HTMLFiles.getInstance();
        if (htmlf.selectFolder()) {
            startWithOutFileSelection(selection, name);
        } else {
            JOptionPane.showMessageDialog(null, "The HTML is not generated, you haven't selected a folder. ");
        }

    }

    public void start(Map<String, List<SaItem>> map) {

        htmlf = HTMLFiles.getInstance();
        if (htmlf.selectFolder()) {
            Set<String> keySet = map.keySet();
            keySet.stream().forEach((singleKey) -> {
                SaItem[] selection = (SaItem[]) map.get(singleKey).toArray();
                startWithOutFileSelection(selection, singleKey);
            });
        } else {
            JOptionPane.showMessageDialog(null, "The HTML is not generated, you haven't selected a folder. ");
        }

    }

    private void startWithOutFileSelection(SaItem[] selection, String name) {
        Thread testThread = new Thread(new Processing.MyRun(selection, name), "Html" + name);
        testThread.start();
    }

    class MyRun implements Runnable {

        private final SaItem[] items;
        private final String saProcessingName;

        MyRun(SaItem[] items, String saProcessingName) {
            this.items = items;
            this.saProcessingName = saProcessingName;
        }

        @Override
        public void run() {
            StringBuilder sbError = new StringBuilder();
            StringBuilder sbSuccessful = new StringBuilder();

            for (SaItem item : items) {

                item.getTs().getName();// Name SAItem
                //   int index = cur.getCurrentProcessing().indexOf(selection[i]);
                SaDocument<ISaSpecification> doc = item.toDocument();
                TsDocument t = item.toDocument();
                String str = Frozen.removeFrozen(item.getName())
                        + "in Multi-doc " + this.saProcessingName;
                str = str.replace("\n", "-");
                if (t.getClass() == X13Document.class) {
                    X13Document x13doc = (X13Document) t;
                    //   CompositeResults results = doc.getResults();

                    TsDomain domCharMax5years;
                    Ts tsY;
                    tsY = DocumentManager.instance.getTs(x13doc, ModellingDictionary.Y);

                    domCharMax5years = FixTimeDomain.domLastFiveYears(tsY);
                    HtmlStream stream;
                    StringWriter writer = new StringWriter();
                    try {
                        //Open the stream
                        stream = new HtmlStream(writer);
                        stream.open();

                        stream.write(HTMLStyle.STYLE);

                        final HTMLBBkHeader headerbbk = new HTMLBBkHeader(item.getRawName(), item.getTs());
                        headerbbk.write(stream);
                        stream.newLine();

                        HTMLBBKChartMain chartMain = new HTMLBBKChartMain(x13doc, domCharMax5years);
                        final HTMLBBKText1 bBKText1 = new HTMLBBKText1(x13doc);

                        AbstractHtmlElement[] htmlElements = new AbstractHtmlElement[3];
                        htmlElements[0] = chartMain;
                        final HTMLBBKBox bBKBox = new HTMLBBKBox(htmlElements);

                        HTMLBBKChartAutocorrelations autocorrelation = new HTMLBBKChartAutocorrelations(x13doc, false);
                        htmlElements[1] = autocorrelation;

                        HTMLBBKChartAutocorrelations partialautocorrelation = new HTMLBBKChartAutocorrelations(x13doc, true);
                        htmlElements[2] = partialautocorrelation;

                        final HTML2Div hTML2Div = new HTML2Div(bBKText1, bBKBox);
                        hTML2Div.write(stream);
//                        stream.write("Irregular ??").newLine();
//                        final HTMLBBKPeriodogram htmlBBKPeriodogram = new HTMLBBKPeriodogram(x13doc.getDecompositionPart().getSeriesDecomposition().getSeries(ComponentType.Irregular, ComponentInformation.Value));
//                        htmlBBKPeriodogram.write(stream);

                        final Pagebreak p = new Pagebreak();
                        p.write(stream);

                        headerbbk.write(stream);

                        final HTMLBBKTableD8A hTMLBBKTableD8B = new HTMLBBKTableD8A(x13doc);
                        hTMLBBKTableD8B.write(stream);
                        stream.newLine();

                        TablesPercentageChangeView tpcv = new TablesPercentageChangeView();
                        tpcv.set(x13doc);
                        TsDomain domain = x13doc.getSeries().getDomain();
                        Ts SeasonallyadjustedPercentageChange = tpcv.GetSeasonallyadjustedPercentageChange();

                        HtmlSingleTsData htmlSingleTsData = new HtmlSingleTsData(
                                lastYearOfSeries(domain, SeasonallyadjustedPercentageChange.getTsData()), SeasonallyadjustedPercentageChange.getName());
                        htmlSingleTsData.write(stream);
                        stream.newLine();
                        htmlSingleTsData = new HtmlSingleTsData(lastYearOfSeries(domain, tpcv.GetSavedSeasonallyAdjustedPercentageChange().getTsData()), tpcv.GetSavedSeasonallyAdjustedPercentageChange().getName());
                        htmlSingleTsData.write(stream);
                        stream.newLine();

                        HTMLBBKSIRatioView sIRatioView = new HTMLBBKSIRatioView(x13doc);
                        sIRatioView.write(stream);

                        stream.newLine();

                        HtmlCCA htmlCCA = new HtmlCCA(MultiLineNameUtil.join(doc.getInput().getName()), x13doc);
                        htmlCCA.writeTextForHTML(stream);

                        stream.close();

                        String output = writer.getBuffer().toString();
                        String old = "<h1 style=\"font-weight:bold;font-size:110%;text-decoration:underline;\">";
                        String corrected = "<h1 style=\"font-weight:bold;font-size:100%;text-decoration:underline;\">";
                        output = output.replace(old, corrected);
                        output = output.replace("<hr />", "");
                        htmlf.creatHTMLFile(output, item.getName());

                        // hTMLBBKTableD8B.dispose();
                    } catch (IOException ex) {
                        LOGGER.error(ex.getMessage());
                    }

                    sbSuccessful.append(str);
                    sbSuccessful.append("\n");
                } else {

                    sbError.append(str);
                    sbError.append("\n");

                }
            }
            if (!sbError.toString().isEmpty()) {
                String str = "These documents are not X13: \n";
                sbError.insert(0, str);
                JOptionPane.showMessageDialog(null, sbError.toString(), "This output is not available ", JOptionPane.ERROR_MESSAGE);
            }

            if (!sbSuccessful.toString().isEmpty()) {

                JOptionPane.showMessageDialog(null, sbSuccessful.toString(), "This output is available for: ", JOptionPane.INFORMATION_MESSAGE);
            }
        }

    }

    private TsData lastYearOfSeries(TsDomain dom, TsData tsData) {
        dom = FixTimeDomain.domLastYear(dom);
        if (tsData != null) {
            return tsData.fittoDomain(dom);
        }
        return new TsData(dom);

    }

}

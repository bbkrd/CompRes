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
package de.bbk.concurreport;

import de.bbk.concur.html.HtmlCCA;
import de.bbk.concur.util.FixTimeDomain;
import de.bbk.concur.util.SavedTables;
import de.bbk.concur.util.TsData_Saved;
import de.bbk.concur.view.TablesPercentageChangeView;
import de.bbk.concurreport.files.HTMLFiles;
import de.bbk.concurreport.html.*;
import de.bbk.concurreport.options.ConCurReportOptionsOptionsPanelController;
import de.bbk.concurreport.options.ConCurReportOptionsPanel;
import de.bbk.concurreport.util.Frozen;
import de.bbk.concurreport.util.Pagebreak;
import ec.tss.Ts;
import ec.tss.documents.DocumentManager;
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
import java.awt.Dimension;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.*;
import org.openide.util.NbPreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Christieane Hofer
 */
public class Processing {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    private HTMLFiles htmlf;

    private boolean makeHtmlf() {
        htmlf = HTMLFiles.getInstance();
        boolean checkHtmlf = false;
        if (htmlf.selectFolder()) {
            checkHtmlf = true;
        } else if (!"".equals(htmlf.getErrorMessage())) {
            JOptionPane.showMessageDialog(null, htmlf.getErrorMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null, "The HTML is not generated, you haven't selected a folder. ");
        }
        return checkHtmlf;
    }

    private boolean checkLookAndFeel() {
        LookAndFeel lookAndFeel = UIManager.getLookAndFeel();
        if (lookAndFeel.getName() != "Windows") {
            int n = JOptionPane.showConfirmDialog(
                    null, "You have selected the LookAndFeel Option " + lookAndFeel.getName() + ".\nTherefore the ConCur Report is"
                    + " not optimized. \nThis might couse problems. \nWould you like to continue anyway?",
                    "Warning",
                    JOptionPane.YES_NO_OPTION);
            switch (n) {
                case JOptionPane.YES_OPTION:
                    return true;
                case JOptionPane.NO_OPTION:
                    return false;
                default:
                    return false;
            }

        }
        return true;
    }

    public void start(SaItem[] selection, String name) {

        if (checkLookAndFeel() && makeHtmlf()) {
            startWithOutFileSelection(selection, name);
        }

    }

    public void start(Map<String, List<SaItem>> map) {
        if (makeHtmlf()) {
            Set<String> keySet = map.keySet();
            keySet.stream().forEach((singleKey) -> {
                SaItem[] selection = (SaItem[]) map.get(singleKey).toArray();
                startWithOutFileSelection(selection, singleKey);
            });
        }
    }

    private void startWithOutFileSelection(SaItem[] selection, String name) {
        Thread testThread = new Thread(new Processing.MyRun(selection, name), "Html" + name);
        testThread.start();
    }

    private void createOutput(StringWriter writer, SaItem item) {
        SaDocument<?> doc = item.toDocument();
        X13Document x13doc = (X13Document) doc;
        //   CompositeResults results = doc.getResults();

        TsDomain domCharMax5years;
        Ts tsY;
        tsY = DocumentManager.instance.getTs(x13doc, ModellingDictionary.Y);

        domCharMax5years = FixTimeDomain.domLastFiveYears(tsY);
        HtmlStream stream;

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

            AbstractHtmlElement[] htmlElements = new AbstractHtmlElement[4];
            htmlElements[0] = chartMain;
            final HTMLBBKBox bBKBox = new HTMLBBKBox(htmlElements);

            HTMLBBKChartAutocorrelations autocorrelation = new HTMLBBKChartAutocorrelations(x13doc, false);
            htmlElements[1] = autocorrelation;

//                        HTMLBBKChartAutocorrelations partialautocorrelation = new HTMLBBKChartAutocorrelations(x13doc, true);
//                        htmlElements[2] = partialautocorrelation;
            HTMLBBKAutoRegressiveSpectrumView autoRegressiveSpectrumView = new HTMLBBKAutoRegressiveSpectrumView(x13doc);
            htmlElements[2] = autoRegressiveSpectrumView;

            HTMLBBKPeriodogram bBKPeriodogram = new HTMLBBKPeriodogram(x13doc);
            htmlElements[3] = bBKPeriodogram;

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
            Ts savedD10 = TsData_Saved.convertMetaDataToTs(doc.getMetaData(), SavedTables.SEASONALFACTOR);
            if (savedD10 != null && savedD10.getTsData() != null) {
                TsDomain savedD10dom = TsData_Saved.convertMetaDataToTs(doc.getMetaData(), SavedTables.SEASONALFACTOR).getTsData().getDomain();
                stream.write("Last available forecast for the " + SavedTables.NAME_SEASONAL_FACTOR_SAVED + " is " + savedD10dom.getLast() + " .");
                stream.newLine();
            }

            TablesPercentageChangeView tpcv = new TablesPercentageChangeView();
            tpcv.set(x13doc);
            TsDomain domain = x13doc.getSeries().getDomain();
            Ts SeasonallyadjustedPercentageChange = tpcv.getSeasonallyAdjustedPercentageChange();

            HtmlSingleTsData htmlSingleTsData = new HtmlSingleTsData(
                    lastYearOfSeries(domain, SeasonallyadjustedPercentageChange.getTsData()), SeasonallyadjustedPercentageChange.getName());
            htmlSingleTsData.write(stream);
            stream.newLine();
            htmlSingleTsData = new HtmlSingleTsData(lastYearOfSeries(domain, tpcv.getSavedSeasonallyAdjustedPercentageChange().getTsData()), tpcv.getSavedSeasonallyAdjustedPercentageChange().getName());
            htmlSingleTsData.write(stream);
            stream.newLine();

            HTMLBBKSIRatioView sIRatioView = new HTMLBBKSIRatioView(x13doc);
            sIRatioView.write(stream);

            stream.newLine();

            HtmlCCA htmlCCA = new HtmlCCA(MultiLineNameUtil.join(doc.getInput().getName()), x13doc);
            htmlCCA.writeTextForHTML(stream);

            HtmlComments htmlComments = new HtmlComments(x13doc, headerbbk);
            htmlComments.write(stream);

            stream.close();

        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
        }
    }

    public void process(Map<String, List<SaItem>> map) {

        makeHtmlf();
        
        Preferences node = NbPreferences.forModule(ConCurReportOptionsPanel.class);
        boolean just_one_html = node.getBoolean(ConCurReportOptionsPanel.JUST_ONE_HTML, true);

        StringBuilder sbError = new StringBuilder();
        StringBuilder sbSuccessful = new StringBuilder();

        String old = "<h1 style=\"font-weight:bold;font-size:110%;text-decoration:underline;\">";
        String corrected = "<h1 style=\"font-weight:bold;font-size:100%;text-decoration:underline;\">";

        if (just_one_html) {
            // alle
            StringWriter writer = new StringWriter(); //gleicher fuer alle

            map.keySet().stream().forEach((saProcessingName) -> {
                List<SaItem> items = map.get(saProcessingName);
                StringBuilder str = new StringBuilder();

                for (int i = 0; i < items.size(); i++) {
                    SaItem item = items.get(i);
                    item.getTs().getName();// Name SAItem
                    SaDocument<?> doc = item.toDocument();
                    str.append(Frozen.removeFrozen(item.getName()))
                            .append("in Multi-doc ")
                            .append(saProcessingName);
                    str = new StringBuilder(str.toString().replace("\n", "-"));

                    if (doc instanceof X13Document) {

                        createOutput(writer, item);
                        writer.append("\n");

                        sbSuccessful.append(str)
                                .append("\n");

                    } else {
                        sbError.append(str)
                                .append(":\n")
                                .append("- Is is not possible to create the output ")
                                .append("because it is not a X13 specification\n");
                    }
                }
            });
            
            String output = writer.getBuffer().toString();

                output = output.replaceAll(old, corrected);
                output = output.replaceAll("<hr />", "");
                //Create HTML hier
                if (!htmlf.createHTML(output, "WS")) {
                    sbError.append(":\n")
                            .append("- It is not possible to create the file:\n")
                            .append(htmlf.getFileName())
                            .append("\n because ")
                            .append(htmlf.getErrorMessage())
                            .append("\n");
                }
            
        } else {
            // einzeln
            map.keySet().stream().forEach((saProcessingName) -> {
                List<SaItem> items = map.get(saProcessingName);

                for (int i = 0; i < items.size(); i++) {
                    SaItem item = items.get(i);
                    item.getTs().getName();// Name SAItem
                    SaDocument<?> doc = item.toDocument();
                    String str = Frozen.removeFrozen(item.getName())
                            + "in Multi-doc " + saProcessingName;
                    str = str.replace("\n", "-");

                    if (doc instanceof X13Document) {
                        StringWriter writer = new StringWriter(); // immer neuen
                        createOutput(writer, item);
                        String output = writer.getBuffer().toString();
                        output = output.replace(old, corrected);
                        output = output.replace("<hr />", "");
                        if (!htmlf.createHTMLFile(output, item.getName())) {
                            sbError.append(str)
                                    .append(":\n")
                                    .append("- It is not possible to create the file:\n")
                                    .append(htmlf.getFileName())
                                    .append("\n because ")
                                    .append(htmlf.getErrorMessage())
                                    .append("\n");
                        }

                        sbSuccessful.append(str)
                                .append("\n");
                    } else {
                        sbError.append(str)
                                .append(":\n")
                                .append("- Is is not possible to create the output ")
                                .append("because it is not a X13 specification\n");
                    }
                }
            });

        }
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
                SaDocument<?> doc = item.toDocument();
                String str = Frozen.removeFrozen(item.getName())
                        + "in Multi-doc " + this.saProcessingName;
                str = str.replace("\n", "-");
                if (doc instanceof X13Document) {
                    X13Document x13doc = (X13Document) doc;
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

                        AbstractHtmlElement[] htmlElements = new AbstractHtmlElement[4];
                        htmlElements[0] = chartMain;
                        final HTMLBBKBox bBKBox = new HTMLBBKBox(htmlElements);

                        HTMLBBKChartAutocorrelations autocorrelation = new HTMLBBKChartAutocorrelations(x13doc, false);
                        htmlElements[1] = autocorrelation;

//                        HTMLBBKChartAutocorrelations partialautocorrelation = new HTMLBBKChartAutocorrelations(x13doc, true);
//                        htmlElements[2] = partialautocorrelation;
                        HTMLBBKAutoRegressiveSpectrumView autoRegressiveSpectrumView = new HTMLBBKAutoRegressiveSpectrumView(x13doc);
                        htmlElements[2] = autoRegressiveSpectrumView;

                        HTMLBBKPeriodogram bBKPeriodogram = new HTMLBBKPeriodogram(x13doc);
                        htmlElements[3] = bBKPeriodogram;

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
                        Ts savedD10 = TsData_Saved.convertMetaDataToTs(doc.getMetaData(), SavedTables.SEASONALFACTOR);
                        if (savedD10 != null && savedD10.getTsData() != null) {
                            TsDomain savedD10dom = TsData_Saved.convertMetaDataToTs(doc.getMetaData(), SavedTables.SEASONALFACTOR).getTsData().getDomain();
                            stream.write("Last available forecast for the " + SavedTables.NAME_SEASONAL_FACTOR_SAVED + " is " + savedD10dom.getLast() + " .");
                            stream.newLine();
                        }

                        TablesPercentageChangeView tpcv = new TablesPercentageChangeView();
                        tpcv.set(x13doc);
                        TsDomain domain = x13doc.getSeries().getDomain();
                        Ts SeasonallyadjustedPercentageChange = tpcv.getSeasonallyAdjustedPercentageChange();

                        HtmlSingleTsData htmlSingleTsData = new HtmlSingleTsData(
                                lastYearOfSeries(domain, SeasonallyadjustedPercentageChange.getTsData()), SeasonallyadjustedPercentageChange.getName());
                        htmlSingleTsData.write(stream);
                        stream.newLine();
                        htmlSingleTsData = new HtmlSingleTsData(lastYearOfSeries(domain, tpcv.getSavedSeasonallyAdjustedPercentageChange().getTsData()), tpcv.getSavedSeasonallyAdjustedPercentageChange().getName());
                        htmlSingleTsData.write(stream);
                        stream.newLine();

                        HTMLBBKSIRatioView sIRatioView = new HTMLBBKSIRatioView(x13doc);
                        sIRatioView.write(stream);

                        stream.newLine();

                        HtmlCCA htmlCCA = new HtmlCCA(MultiLineNameUtil.join(doc.getInput().getName()), x13doc);
                        htmlCCA.writeTextForHTML(stream);

                        HtmlComments htmlComments = new HtmlComments(x13doc, headerbbk);
                        htmlComments.write(stream);

                        stream.close();

                        String output = writer.getBuffer().toString();
                        String old = "<h1 style=\"font-weight:bold;font-size:110%;text-decoration:underline;\">";
                        String corrected = "<h1 style=\"font-weight:bold;font-size:100%;text-decoration:underline;\">";
                        output = output.replace(old, corrected);
                        output = output.replace("<hr />", "");
                        if (!htmlf.createHTMLFile(output, item.getName())) {
                            sbError.append(str)
                                    .append(":\n")
                                    .append("- It is not possible to create the file:\n")
                                    .append(htmlf.getFileName())
                                    .append("\n because ")
                                    .append(htmlf.getErrorMessage())
                                    .append("\n");
                        }

                    } catch (IOException ex) {
                        LOGGER.error(ex.getMessage());
                    }

                    sbSuccessful.append(str)
                            .append("\n");
                } else {

                    sbError.append(str)
                            .append(":\n")
                            .append("- Is is not possible to create the output ")
                            .append("because it is not a X13 specification\n");

                }
            }
            if (!sbError.toString().isEmpty()) {
                JTextArea jta = new JTextArea(sbError.toString());
                JScrollPane jsp = new JScrollPane(jta);
                jsp.setPreferredSize(new Dimension(480, 120));
                JOptionPane.showMessageDialog(null, jsp, "Error", JOptionPane.ERROR_MESSAGE);
            }

            if (!sbSuccessful.toString().isEmpty()) {
                JOptionPane.showMessageDialog(null, sbSuccessful.toString(), "The output is available for: ", JOptionPane.INFORMATION_MESSAGE);
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

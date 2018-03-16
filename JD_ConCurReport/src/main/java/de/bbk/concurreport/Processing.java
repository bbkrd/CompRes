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
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.swing.*;
import org.openide.util.NbPreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Christieane Hofer
 */
public class Processing {

    private static final String OLD_STYLE = "<h1 style=\"font-weight:bold;font-size:110%;text-decoration:underline;\">";
    private static final String NEW_STYLE = "<h1 style=\"font-weight:bold;font-size:100%;text-decoration:underline;\">";

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    private HTMLFiles htmlf;

    private boolean makeHtmlf() {
        htmlf = HTMLFiles.getInstance();
        if (htmlf.selectFolder()) {
            return true;
        } else if (!htmlf.getErrorMessage().isEmpty()) {
            JOptionPane.showMessageDialog(null, htmlf.getErrorMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null, "The HTML is not generated, you haven't selected a folder. ");
        }
        return false;
    }

    private boolean checkLookAndFeel() {
        LookAndFeel lookAndFeel = UIManager.getLookAndFeel();
        if (!"Windows".equals(lookAndFeel.getName())) {
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

    public String createOutput(SaItem item) {
        StringWriter writer = new StringWriter();
        SaDocument<?> doc = item.toDocument();
        X13Document x13doc = (X13Document) doc;

        Ts tsY = DocumentManager.instance.getTs(x13doc, ModellingDictionary.Y);
        TsDomain domCharMax5years = FixTimeDomain.domLastFiveYears(tsY);

        try {
            //Open the stream
            HtmlStream stream = new HtmlStream(writer);
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

            HTMLBBKAutoRegressiveSpectrumView autoRegressiveSpectrumView = new HTMLBBKAutoRegressiveSpectrumView(x13doc);
            htmlElements[2] = autoRegressiveSpectrumView;

            HTMLBBKPeriodogram bBKPeriodogram = new HTMLBBKPeriodogram(x13doc);
            htmlElements[3] = bBKPeriodogram;

            final HTML2Div hTML2Div = new HTML2Div(bBKText1, bBKBox);
            hTML2Div.write(stream);

            final Pagebreak p = new Pagebreak();
            p.write(stream);

            headerbbk.write(stream);

            final HTMLBBKTableD8A hTMLBBKTableD8B = new HTMLBBKTableD8A(x13doc);
            hTMLBBKTableD8B.write(stream);

            stream.newLine();
            Ts savedD10 = TsData_Saved.convertMetaDataToTs(doc.getMetaData(), SavedTables.SEASONALFACTOR);
            if (savedD10 != null && savedD10.getTsData() != null) {
                TsDomain savedD10dom = savedD10.getTsData().getDomain();
                stream.write("Last available forecast for the ")
                        .write(SavedTables.NAME_SEASONAL_FACTOR_SAVED)
                        .write(" is ").write(savedD10dom.getLast().toString())
                        .write(".").newLine();
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
            tpcv.dispose();

        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
        }
        return writer.toString();
    }

    public void process(Map<String, List<SaItem>> map) {

        if (!makeHtmlf() || !checkLookAndFeel()) {
            return;
        }

        boolean just_one_html = NbPreferences.forModule(ConCurReportOptionsPanel.class).getBoolean(ConCurReportOptionsPanel.JUST_ONE_HTML, true);

        StringBuilder sbError = new StringBuilder();
        StringBuilder sbSuccessful = new StringBuilder();

        if (just_one_html) {
            try (FileWriter writer = new FileWriter(htmlf.createHtmlFile("WS"), true);) {
                // alle
                for (Map.Entry<String, List<SaItem>> entry : map.entrySet()) {
                    String saProcessingName = entry.getKey();
                    List<SaItem> items = entry.getValue();
                    for (int i = 0; i < items.size(); i++) {
                        SaItem item = items.get(i);
                        SaDocument<?> doc = item.toDocument();
                        StringBuilder str = new StringBuilder();
                        str.append(Frozen.removeFrozen(item.getName()))
                                .append("in Multi-doc ")
                                .append(saProcessingName);
                        String name = str.toString().replace("\n", "-");

                        if (doc instanceof X13Document) {
                            String output = createOutput(item).replace(OLD_STYLE, NEW_STYLE).replaceAll("<\\s*hr\\s*\\/\\s*>", "");
                            item.compress();
                            writer.append(output).append('\n');
                            writer.flush();
                            sbSuccessful.append(name).append("\n");
                        } else {
                            item.compress();
                            sbError.append(name)
                                    .append(":\n- Is is not possible to create the output because it is not a X13 specification\n");
                        }

                    }
                }
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(Processing.class.getName()).log(Level.SEVERE, null, ex);
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
                    SaDocument<?> doc = item.toDocument();
                    String str = Frozen.removeFrozen(item.getName())
                            + "in Multi-doc " + saProcessingName;
                    str = str.replace("\n", "-");

                    if (doc instanceof X13Document) {
                        String output = createOutput(item);
                        output = output.replace(OLD_STYLE, NEW_STYLE);
                        output = output.replaceAll("<\\s*hr\\s*\\/\\s*>", "");
                        if (!htmlf.writeHTMLFile(output, item.getName())) {
                            sbError.append(str)
                                    .append(":\n")
                                    .append("- It is not possible to create the file:\n")
                                    .append(htmlf.getFileName())
                                    .append("\n because ")
                                    .append(htmlf.getErrorMessage())
                                    .append("\n");
                        }

                        sbSuccessful.append(str).append("\n");
                    } else {
                        sbError.append(str)
                                .append(":\n- Is is not possible to create the output because it is not a X13 specification\n");
                    }
                    item.compress();
                }
            });

        }
        if (!sbError.toString().isEmpty()) {
            JTextArea jta = new JTextArea(sbError.toString());
            jta.setEditable(false);
            JScrollPane jsp = new JScrollPane(jta);
            jsp.setPreferredSize(new Dimension(480, 120));
            JOptionPane.showMessageDialog(null, jsp, "Error", JOptionPane.ERROR_MESSAGE);
        }

        if (!sbSuccessful.toString().isEmpty()) {
            JTextArea jta = new JTextArea(sbSuccessful.toString());
            jta.setEditable(false);
            JScrollPane jsp = new JScrollPane(jta);
            jsp.setPreferredSize(new Dimension(480, 120));
            JOptionPane.showMessageDialog(null, jsp, "The output is available for: ", JOptionPane.INFORMATION_MESSAGE);
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

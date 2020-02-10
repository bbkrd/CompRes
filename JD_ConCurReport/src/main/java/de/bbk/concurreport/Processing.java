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

import de.bbk.concur.html.HtmlTsData;
import de.bbk.concur.util.FixTimeDomain;
import de.bbk.concur.util.InPercent;
import de.bbk.concur.util.SavedTables;
import de.bbk.concur.util.TsData_Saved;
import de.bbk.concurreport.files.HTMLFiles;
import de.bbk.concurreport.html.*;
import de.bbk.concurreport.options.ConCurReportOptionsPanel;
import de.bbk.concurreport.util.Frozen;
import de.bbk.concurreport.util.Pagebreak;
import ec.satoolkit.DecompositionMode;
import ec.satoolkit.x11.X11Kernel;
import ec.tss.Ts;
import ec.tss.documents.DocumentManager;
import ec.tss.html.HtmlStream;
import ec.tss.html.HtmlTag;
import ec.tss.sa.SaItem;
import ec.tss.sa.documents.SaDocument;
import ec.tss.sa.documents.X13Document;
import ec.tss.tsproviders.utils.MultiLineNameUtil;
import ec.tstoolkit.algorithm.CompositeResults;
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
import org.netbeans.api.progress.ProgressHandle;
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
    private final ProgressHandle progressHandle = ProgressHandle.createHandle("Creating report");
    private HTMLFiles htmlf;

    private boolean makeHtmlf() {
        htmlf = new HTMLFiles();
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
            return n == JOptionPane.YES_OPTION;
        }
        return true;
    }

    private String createOutput(SaItem item) {
        StringWriter writer = new StringWriter();
        SaDocument<?> doc = item.toDocument();
        X13Document x13doc = (X13Document) doc;

        try {
            //Open the stream
            HtmlStream stream = new HtmlStream(writer);
            stream.open();

            stream.write(HTMLStyle.STYLE);

            final HTMLBBkHeader headerbbk = new HTMLBBkHeader(item.getRawName(), item.getTs());
            stream.write(headerbbk)
                    .newLine()
                    .write(createDiv(x13doc));

            stream.write(new Pagebreak())
                    .write(headerbbk);

            writeCalendar(x13doc, stream);

            stream.newLine()
                    .write(new HTMLBBKTableD8B(x13doc))
                    .newLine();

            Ts savedD10 = TsData_Saved.convertMetaDataToTs(x13doc.getMetaData(), SavedTables.SEASONALFACTOR);
            if (savedD10 != null && savedD10.getTsData() != null) {
                TsDomain savedD10dom = savedD10.getTsData().getDomain();
                stream.write("Last available forecast for the ")
                        .write(SavedTables.NAME_SEASONAL_FACTOR_SAVED)
                        .write(" is ").write(savedD10dom.getLast().toString())
                        .write(".").newLine();
            }

            stream.write(new HTMLBBKSIRatioView(x13doc))
                    .newLine()
                    .write(new HtmlComments(x13doc, headerbbk))
                    .close();
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
        }
        return writer.toString();
    }

    private void writeCalendar(X13Document x13doc, HtmlStream stream) throws IOException {
        CompositeResults results = x13doc.getResults();
        if (results == null) {
            return;
        }
        TsData a6 = results.getData(X11Kernel.A6, TsData.class);
        TsData a7 = results.getData(X11Kernel.A7, TsData.class);
        DecompositionMode mode = results.getData("mode", DecompositionMode.class);
        TsData tsData;
        if (a6 == null) {
            tsData = a7;
        } else {
            if (mode.isMultiplicative()) {
                tsData = a6.times(a7);
            } else {
                tsData = a6.plus(a7);
            }
        }

        if (tsData != null) {
            int frequency = tsData.getFrequency().intValue();
            tsData = InPercent.convertTsDataInPercentIfMult(tsData, mode.isMultiplicative());
            if (tsData.getDomain().getYearsCount() > 10) {
                TsDomain domainLastTenYears = new TsDomain(tsData.getEnd().minus(frequency * 10), frequency * 10);
                tsData = tsData.fittoDomain(domainLastTenYears);
            }

            stream.write("<table style=\"table-layout:fixed\" >")
                    .open(HtmlTag.TABLEROW)
                    .write("<th colspan=\"")
                    .write(String.valueOf(frequency + 1))
                    .write("\" style=\"text-align:left\">")
                    .write(mode.isMultiplicative() ? "A6 * A7" : "A6 + A7")
                    .close(HtmlTag.TABLEHEADER)
                    .close(HtmlTag.TABLEROW)
                    .write(HtmlTsData.builder()
                            .data(tsData)
                            .includeTableTags(false)
                            .build())
                    .close(HtmlTag.TABLE);
        }
    }

    public void process(Map<String, List<SaItem>> map) {
        progressHandle.start();

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
                                .append(" in Multi-doc ")
                                .append(saProcessingName);
                        String name = str.toString().replace("\n", "-");

                        if (doc instanceof X13Document) {
                            if (item.getStatus() == SaItem.Status.Valid) {
                                String output = createOutput(item).replace(OLD_STYLE, NEW_STYLE)
                                        .replaceAll("<\\s*hr\\s*\\/\\s*>", "")
                                        .replace("▶", "&#9654;");
                                writer.append(output).append('\n');
                                writer.flush();
                                sbSuccessful.append(name).append("\n");
                            } else {
                                sbError.append(name)
                                        .append(":\nIt is not possible to create the output because it is not valid\n");
                            }
                        } else {
                            sbError.append(name)
                                    .append(":\nIt is not possible to create the output because it is not a X13 specification\n");
                        }
                        item.compress();

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
                        if (item.getStatus() == SaItem.Status.Valid) {
                            String output = createOutput(item);
                            output = output.replace(OLD_STYLE, NEW_STYLE)
                                    .replaceAll("<\\s*hr\\s*\\/\\s*>", "")
                                    .replace("▶", "&#9654;");
                            if (!htmlf.writeHTMLFile(output, item.getName())) {
                                sbError.append(str)
                                        .append(":\n")
                                        .append("It is not possible to create the file\n");
                                if (!htmlf.getFileName().isEmpty()) {
                                    sbError.append(htmlf.getFileName())
                                            .append("\n");
                                }
                                sbError.append("Reason: ")
                                        .append(htmlf.getErrorMessage())
                                        .append("\n");
                            } else {
                                sbSuccessful.append(str).append("\n");
                            }
                        } else {
                            sbError.append(str)
                                    .append(":\nIt is not possible to create the output because it is not valid\n");
                        }
                    } else {
                        sbError.append(str)
                                .append(":\nIt is not possible to create the output because it is not a X13 specification\n");
                    }
                    item.compress();
                }
            });

        }
        progressHandle.finish();
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

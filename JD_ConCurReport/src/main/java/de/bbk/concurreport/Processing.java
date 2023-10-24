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

import de.bbk.concurreport.files.HTMLFiles;
import de.bbk.concurreport.html.*;
import de.bbk.concurreport.options.ConCurReportOptionsPanel;
import de.bbk.concurreport.report.tramo.TramoSeatsReport;
import de.bbk.concurreport.report.x13.X13Report;
import de.bbk.concurreport.util.Frozen;
import ec.nbdemetra.sa.MultiProcessingDocument;
import ec.nbdemetra.sa.MultiProcessingManager;
import ec.nbdemetra.sa.SaBatchUI;
import ec.nbdemetra.ws.IWorkspaceItemManager;
import ec.nbdemetra.ws.Workspace;
import ec.nbdemetra.ws.WorkspaceFactory;
import ec.nbdemetra.ws.WorkspaceItem;
import ec.nbdemetra.ws.nodes.ItemWsNode;
import ec.tss.html.HtmlStream;
import ec.tss.sa.SaItem;
import ec.tss.sa.SaProcessing;
import ec.tss.sa.documents.SaDocument;
import ec.tss.sa.documents.TramoSeatsDocument;
import ec.tss.sa.documents.X13Document;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import javax.swing.*;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.nodes.Node;
import org.openide.util.NbPreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Christieane Hofer
 */
public class Processing implements Callable<ReportMessages> {

    private static final String OLD_STYLE = "<h1 style=\"font-weight:bold;font-size:110%;text-decoration:underline;\">";
    private static final String NEW_STYLE = "<h1 style=\"font-weight:bold;font-size:100%;text-decoration:underline;\">";

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    private final ProgressHandle progressHandle = ProgressHandle.createHandle("Creating report");
    private HTMLFiles htmlf;
    private final Map<String, List<SaItem>> map;

    private static final Class<MultiProcessingDocument> ITEM_TYPE = MultiProcessingDocument.class;

    public Processing() {
        this.map = new TreeMap<>();
        Workspace workspace = WorkspaceFactory.getInstance().getActiveWorkspace();
        IWorkspaceItemManager mgr = WorkspaceFactory.getInstance().getManager(MultiProcessingManager.ID);
        if (mgr != null) {
            List<WorkspaceItem<MultiProcessingDocument>> list = workspace.searchDocuments(mgr.getItemClass());
            list.stream().forEach((item) -> {
                SaProcessing saProcessing = item.getElement().getCurrent();
                map.put(item.getDisplayName(), saProcessing);
            });
        }
    }

    public Processing(Map<String, List<SaItem>> map) {
        this.map = map;
    }

    public Processing(Node[] activatedNodes) {
        this.map = new TreeMap<>();

        for (Node activatedNode : activatedNodes) {
            WorkspaceItem<MultiProcessingDocument> item = ((ItemWsNode) activatedNode).getItem(ITEM_TYPE);
            SaProcessing saProcessing = item.getElement().getCurrent();
            map.put(item.getDisplayName(), saProcessing);
        }
    }

    public Processing(SaBatchUI cur) {
        this.map = new TreeMap<>();
        map.put(cur.getName(), Arrays.asList(cur.getSelection()));
    }

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
                    + " not optimized.\nThis might cause problems.\nWould you like to continue anyway?",
                    "Warning",
                    JOptionPane.YES_NO_OPTION);
            return n == JOptionPane.YES_OPTION;
        }
        return true;
    }

    private String createOutput(SaItem item) {
        StringWriter writer = new StringWriter();
        SaDocument<?> doc = item.toDocument();
        try {
            //Open the stream
            HtmlStream stream = new HtmlStream(writer);
            stream.open();
            stream.write(HTMLStyle.STYLE);
            if (doc instanceof X13Document) {
                stream.write(new X13Report(item));
            } else if (doc instanceof TramoSeatsDocument) {
                stream.write(new TramoSeatsReport(item));
            }

            stream.close();
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
        }
        return writer.toString();
    }

    @Override
    public ReportMessages call() {
        try {
            progressHandle.start();
            if (!makeHtmlf() || !checkLookAndFeel()) {
                return ReportMessages.EMPTY;
            }

            boolean just_one_html = NbPreferences.forModule(ConCurReportOptionsPanel.class).getBoolean(ConCurReportOptionsPanel.JUST_ONE_HTML, true);

            StringBuilder sbError = new StringBuilder();
            StringBuilder sbSuccessful = new StringBuilder();

            if (just_one_html) {
                Workspace workspace = WorkspaceFactory.getInstance().getActiveWorkspace();
                try (FileWriter writer = new FileWriter(htmlf.createHtmlFile(workspace.getName()), true)) {
                    // alle
                    for (Map.Entry<String, List<SaItem>> entry : map.entrySet()) {
                        String saProcessingName = entry.getKey();
                        List<SaItem> items = entry.getValue();
                        for (SaItem item : items) {
                            item.process();
                            StringBuilder str = new StringBuilder();
                            str.append(Frozen.removeFrozen(item.getName()))
                                    .append("in Multi-doc ")
                                    .append(saProcessingName);
                            String name = str.toString().replace("\n", "-");

                            if (item.getStatus() == SaItem.Status.Valid) {
                                try {
                                    String output = createOutput(item).replace(OLD_STYLE, NEW_STYLE)
                                            .replaceAll("<\\s*hr\\s*\\/\\s*>", "")
                                            .replace("▶", "&#9654;");
                                    writer.append(output).append('\n');
                                    writer.flush();
                                    sbSuccessful.append(name).append("\n");
                                } catch (ReportException ex) {
                                    sbError.append(name).append(": ").append(ex.getMessage()).append("\n");
                                } catch (RuntimeException ex) {
                                    sbError.append(name).append(": Critical Error! ").append(ex.getMessage()).append("\n");
                                    LOGGER.error(name, ex);
                                }
                            } else {
                                sbError.append(name)
                                        .append(":\nIt is not possible to create the output because it is not valid\n");
                            }
                            item.compress();
                        }
                    }
                } catch (IOException ex) {
                    java.util.logging.Logger.getLogger(Processing.class.getName()).log(Level.SEVERE, null, ex);
                    sbError.append(":\n")
                            .append("- It is not possible to create the file:\n")
                            .append(htmlf.getFilePath())
                            .append("\n because ")
                            .append(htmlf.getErrorMessage())
                            .append("\n");
                }

            } else {
                // einzeln
                map.keySet().stream().forEach((saProcessingName) -> {
                    List<SaItem> items = map.get(saProcessingName);

                    for (SaItem item : items) {
                        item.process();
                        String str = Frozen.removeFrozen(item.getName())
                                + "in Multi-doc " + saProcessingName;
                        str = str.replace("\n", "-");

                        if (item.getStatus() == SaItem.Status.Valid) {
                            try {
                                String output = createOutput(item);
                                output = output.replace(OLD_STYLE, NEW_STYLE)
                                        .replaceAll("<\\s*hr\\s*\\/\\s*>", "")
                                        .replace("▶", "&#9654;");
                                if (!htmlf.writeHTMLFile(output, item.getName())) {
                                    sbError.append(str)
                                            .append(":\n")
                                            .append("It is not possible to create the file\n");
                                    if (!htmlf.getFilePath().isEmpty()) {
                                        sbError.append(htmlf.getFilePath())
                                                .append("\n");
                                    }
                                    sbError.append("Reason: ")
                                            .append(htmlf.getErrorMessage())
                                            .append("\n");
                                } else {
                                    sbSuccessful.append(str).append("\n");
                                }
                            } catch (ReportException ex) {
                                sbError.append(str).append(": ").append(ex.getMessage()).append("\n");
                            } catch (RuntimeException ex) {
                                sbError.append(str).append(": Critical Error! ").append(ex.getMessage()).append("\n");
                                LOGGER.error(str, ex);
                            }
                        } else {
                            sbError.append(str)
                                    .append(":\nIt is not possible to create the output because it is not valid\n");
                        }
                        item.compress();
                    }
                });

            }

            return new ReportMessages(sbSuccessful.toString(), sbError.toString());
        } finally {
            progressHandle.finish();
        }
    }

}

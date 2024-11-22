/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bbk.autoconcurreport;

import de.bbk.autoconcur.AutoConCur;
import de.bbk.autoconcur.BeanCollector;
import de.bbk.autoconcur.Decision;
import de.bbk.autoconcur.DecisionBean;
import de.bbk.concurreport.Processing;
import de.bbk.concurreport.ReportMessages;
import de.bbk.concurreport.util.Frozen;
import ec.nbdemetra.ws.WorkspaceFactory;
import ec.tss.html.HtmlStream;
import ec.tss.html.HtmlStyle;
import ec.tss.html.HtmlTable;
import ec.tss.html.HtmlTableCell;
import ec.tss.html.HtmlTableHeader;
import ec.tss.html.HtmlTag;
import java.awt.Dimension;
import java.io.FileWriter;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;
import java.util.logging.Level;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.openide.windows.WindowManager;

/**
 *
 * @author Jan Gerhardt
 */
public class AutoConCurReport {

//    private final Map<String, List<SaItem>> map;
    private static HTMLAutoConCurSummary htmlf;
    private static final DecimalFormat DF = new DecimalFormat("0.00");

    private static final String[] HEADSNEW = {"Series",
        "Series Name",
        "Recommendations",
        "Plausibility checks",
        "Max Revision %",
        "GrowthOld[t]",
        "Update History",
        "Info"};

    private static final String[] HEADSCHECKS = {"classic",
        "growth rate",
        "sign change",
        "extreme value",
        "fix outlier"
    };

    private static final String STYLE = "<style>\n" + "body{\n"
            + "         font: normal 12px Arial,sans-serif;\n"
            + "         text-align: left;        }\n"
            + "	table{\n"
            + "		width:100%;\n"
            + "		font-size:100%;\n"
            + "		table-layout:auto;\n"
            + "		border-collapse:collapse;}\n"
            + "	th, td{\n"
            + "		padding:3px 5px;\n"
            + "		border:.1px solid #000;\n"
            + "		vertical-align:top;}\n"
            + "	td{\n"
            + "		text-align:right;}\n"
            + "	tr:hover{\n"
            + "		background-color: #f5f5f5;}\n"
            + "	th { position: sticky;\n"
            + "  top: 0px;\n"
            + "  background: lightgrey; }"
            + "</style>\n";

    private static final HtmlStyle[] WHITESTYLE = new HtmlStyle[]{HtmlStyle.Center, HtmlStyle.Bold, HtmlStyle.White};
    private static final HtmlStyle[] BLACKSTYLE = new HtmlStyle[]{HtmlStyle.Center, HtmlStyle.Bold, HtmlStyle.Black};

    static {
        DF.setRoundingMode(RoundingMode.HALF_UP);
    }

    private static boolean makeHtmlf() {
        htmlf = new HTMLAutoConCurSummary();
        if (htmlf.selectFolder()) {
            return true;
        } else if (!htmlf.getErrorMessage().isEmpty()) {
            JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(), htmlf.getErrorMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(), "The HTML is not generated, you haven't selected a folder. ");
        }
        return false;
    }

    public static ReportMessages call2() {
        return process(true);
    }

    public static void call() {
        ReportMessages messages = process(false);
        showMessages(messages);
    }

    private static ReportMessages process(boolean master) {
        
        List<DecisionBean> beans;
        if (master) {
            beans = BeanCollector.getBeans();
        } else {
            AutoConCur acc = new AutoConCur();
            beans = acc.makeDecisions();
        }

        StringBuilder sbError = new StringBuilder();
        StringBuilder sbSuccessful = new StringBuilder();
        if (beans.isEmpty()) {
            sbError.append("No recommendations are given.");
            return new ReportMessages(sbSuccessful.toString(), sbError.toString());
        }
        //HTMLAutoConCurSummary autoConCurfile = new HTMLAutoConCurSummary();
        
        if(master){
             htmlf = new HTMLAutoConCurSummary();
        }else if (!makeHtmlf()) {
            return ReportMessages.EMPTY;
        }

        String filename;
        String headline = "compRes Recommendations";
        if (master) {
            filename = "Masterfile_" + WorkspaceFactory.getInstance().getActiveWorkspace().getName();
            headline = headline.concat(": Master overview");
        } else {
            filename = "Summary_" + WorkspaceFactory.getInstance().getActiveWorkspace().getName();
            headline = headline.concat(" for WS " + WorkspaceFactory.getInstance().getActiveWorkspace().getName());
        }

        try (FileWriter writer = new FileWriter(htmlf.createHTMLAutoConCurSummaryFile(filename), true); HtmlStream stream = new HtmlStream(writer)) {
            stream.open();
            stream.write(STYLE);
            stream.write(HtmlTag.HEADER2, headline);
            stream.open(new HtmlTable());
            initTable(stream);

            writeTable(stream, beans, true, sbSuccessful, sbError);
            writeTable(stream, beans, false, sbSuccessful, sbError);

            stream.close(HtmlTag.TABLE);
            stream.write('\n');
            stream.newLines(1);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(Processing.class.getName()).log(Level.SEVERE, null, ex);
            sbError.append(":")
                    .append(System.lineSeparator())
                    .append("- It is not possible to create the file:")
                    .append(System.lineSeparator())
                    .append(htmlf.getFilePath())
                    .append(System.lineSeparator())
                    .append(" because ")
                    .append(htmlf.getErrorMessage())
                    .append(System.lineSeparator());
        }
        return new ReportMessages(sbSuccessful.toString(), sbError.toString());
    }

    public static void showMessages(ReportMessages o) {
        if (!o.getErrorMessages().isEmpty()) {
            JTextArea jta = new JTextArea(o.getErrorMessages());
            jta.setEditable(false);
            JScrollPane jsp = new JScrollPane(jta);
            jsp.setPreferredSize(new Dimension(480, 120));
            JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(), jsp, "Error", JOptionPane.ERROR_MESSAGE);
        }

        if (!o.getSuccessMessages().isEmpty()) {
            JTextArea jta = new JTextArea(o.getSuccessMessages());
            jta.setEditable(false);
            JScrollPane jsp = new JScrollPane(jta);
            jsp.setPreferredSize(new Dimension(480, 120));
            JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(), jsp, "The output is available for: ", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private static void initTable(HtmlStream stream) throws IOException {
        stream.open(HtmlTag.TABLEROW);
        for (int j = 0; j < HEADSNEW.length; ++j) {
            if (!"Plausibility checks".equals(HEADSNEW[j])) {
                stream.write(new HtmlTableHeader(HEADSNEW[j]));
            } else {
                stream.write((HtmlTableHeader) (new HtmlTableHeader(HEADSNEW[j]).withColSpan(HEADSCHECKS.length)));
            }
        }
        stream.close(HtmlTag.TABLEROW).newLine();
        stream.write('\n');
        stream.open(HtmlTag.TABLEROW);
        for (int j = 0; j < HEADSNEW.length; ++j) {
            if (!"Plausibility checks".equals(HEADSNEW[j])) {
                stream.write(new HtmlTableHeader(""));
            } else {
                for (String check : HEADSCHECKS) {
                    stream.write(new HtmlTableHeader(check));
                }
            }
        }
        stream.close(HtmlTag.TABLEROW);
        stream.write('\n');
    }

    private static void writeTable(HtmlStream stream, DecisionBean bean, StringBuilder sbSuccessful, StringBuilder sbError) {
        try {
            stream.open(HtmlTag.TABLEROW);
            // bean.setFile(bean.getTitle());
            if (bean.getFile() == null || bean.getFile().isBlank()) {
                stream.write(new HtmlTableCell(""));
            } else {
                stream.write(new HtmlTableCell("<a href=\"" + bean.getFile() + "#" + bean.getTitle() + "\" target=\"_blank\">" + bean.getFile() + "</a>"));
            }

            stream.write(new HtmlTableCell(Frozen.removeFrozen(bean.getTitle())));
            if (bean.isManual()) {
                stream.write(new HtmlTableCell(String.valueOf(bean.getDecision()), WHITESTYLE), "Black");
                if (bean.getDecision() == Decision.UNKNOWN) {
                    fillUnknownRow(stream, bean, sbSuccessful);
                    return;
                }
            } else {
                switch (bean.getDecision()) {
                    case UNKNOWN:
                        stream.write(new HtmlTableCell(String.valueOf(Decision.UNKNOWN), WHITESTYLE), "Red");
                        fillUnknownRow(stream, bean, sbSuccessful);
                        return;
                    case CHECK:
                        stream.write(new HtmlTableCell(String.valueOf(Decision.CHECK), WHITESTYLE), "Red");
                        break;
                    case UPDATE:
                        stream.write(new HtmlTableCell(String.valueOf(Decision.UPDATE), BLACKSTYLE), "Yellow");
                        break;
                    case KEEP:
                        stream.write(new HtmlTableCell(String.valueOf(Decision.KEEP), WHITESTYLE), "Green");
                        break;
                }
            }
            if (bean.isClassic()) {
                stream.write(new HtmlTableCell("X", HtmlStyle.Center));
            } else {
                stream.write(new HtmlTableCell(""));
            }
            if (bean.isGrowthRate()) {
                stream.write(new HtmlTableCell("X", HtmlStyle.Center));
            } else {
                stream.write(new HtmlTableCell(""));
            }
            if (bean.isSignChange()) {
                stream.write(new HtmlTableCell("X", HtmlStyle.Center));
            } else {
                stream.write(new HtmlTableCell(""));
            }
            if (bean.isExtremevalue()) {
                stream.write(new HtmlTableCell("X", HtmlStyle.Center));
            } else {
                stream.write(new HtmlTableCell(""));
            }
            if (bean.isFixOutlier()) {
                stream.write(new HtmlTableCell("X", HtmlStyle.Center));
            } else {
                stream.write(new HtmlTableCell(""));
            }

            //ToDo: MaxRevision
            stream.write(new HtmlTableCell(String.valueOf(Double.NaN)));
            //Current growth rate (old factor)
            stream.write(new HtmlTableCell(Double.isNaN(bean.getGrowthOld()) ? "NOT CALCULATED" : DF.format(bean.getGrowthOld())));
            //ToDo: Update History
            stream.write(new HtmlTableCell(""));

            if (bean.getErrortext() == null) {
                stream.write(new HtmlTableCell(""));
            } else {
                stream.write(new HtmlTableCell(bean.getErrortext()));
            }
            stream.close(HtmlTag.TABLEROW);
            stream.write('\n');
            sbSuccessful.append(Frozen.removeFrozen(bean.getTitle()))
                    .append(" contained in Masterfile")
                    .append(System.lineSeparator());
        } catch (IOException ex) {
            sbError.append(bean.getTitle()
            ).append(": ").append(ex.getMessage()).append(System.lineSeparator());
        }
    }

    private static void fillUnknownRow(HtmlStream stream, DecisionBean bean, StringBuilder sbSuccessful) throws IOException {
        for (String string : HEADSCHECKS) {
            stream.write(new HtmlTableCell(""));
        }
        for (int i = 3; i < HEADSNEW.length - 1; i++) {
            stream.write(new HtmlTableCell(""));
        }
        if (bean.getErrortext() == null) {
            stream.write(new HtmlTableCell(""));
        } else {
            stream.write(new HtmlTableCell(bean.getErrortext()));
        }
        sbSuccessful.append(Frozen.removeFrozen(bean.getTitle()))
                .append(System.lineSeparator());
    }

    private static void writeTable(HtmlStream stream, List<DecisionBean> beans, boolean manual, StringBuilder sbSuccessful, StringBuilder sbError) {
        beans.stream().filter(b -> b.isManual() == manual && b.getDecision() == Decision.UNKNOWN).forEach((bean -> {
            AutoConCurReport.writeTable(stream, bean, sbSuccessful, sbError);
        }));
        beans.stream().filter(b -> b.isManual() == manual && b.getDecision() == Decision.CHECK).forEach((bean -> {
            AutoConCurReport.writeTable(stream, bean, sbSuccessful, sbError);
        }));
        beans.stream().filter(b -> b.isManual() == manual && b.getDecision() == Decision.UPDATE).forEach((bean -> {
            AutoConCurReport.writeTable(stream, bean, sbSuccessful, sbError);
        }));
        beans.stream().filter(b -> b.isManual() == manual && b.getDecision() == Decision.KEEP).forEach((bean -> {
            AutoConCurReport.writeTable(stream, bean, sbSuccessful, sbError);
        }));
    }
}

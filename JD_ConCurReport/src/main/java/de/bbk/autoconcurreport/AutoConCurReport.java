/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bbk.autoconcurreport;

import de.bbk.autoconcur.AutoConCur;
import de.bbk.autoconcur.DecisionBeanCollector;
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
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
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
    private static final DecimalFormat DF = new DecimalFormat("0.0");

    private static final String[] HEADS = {//"Series",
        "Series Name",
        "Recommendations",
        "Plausibility checks",
        //"Max Revision %",
        "GrowthOld[t]",
        "GrowthNew[t]",
        //"Update History",
        "Info"};

    private static final String[] HEADSCHECKS = {"seasonal factor",
        "fix outlier",
        "extreme value",
        "growth rate",
        "sign change"
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

    public static ReportMessages call() {
        return process();
    }

    public static void callAndShowMessages() {
        ReportMessages messages = process();
        showMessages(messages);
    }

    private static ReportMessages process() {

        List<DecisionBean> beans = DecisionBeanCollector.getBeans();
        Collections.sort(beans, (DecisionBean bean1, DecisionBean bean2) -> bean1.getTitle().compareToIgnoreCase(bean2.getTitle()));

        StringBuilder sbError = new StringBuilder();
        StringBuilder sbSuccessful = new StringBuilder();
        if (beans.isEmpty()) {
            sbError.append("No recommendations are given.");
            return new ReportMessages(sbSuccessful.toString(), sbError.toString());
        }
        //HTMLAutoConCurSummary autoConCurfile = new HTMLAutoConCurSummary();
        htmlf = new HTMLAutoConCurSummary();

        String filename = "Masterfile_" + WorkspaceFactory.getInstance().getActiveWorkspace().getName();
        String filenameTemp = filename + "_tmp";
        String headline = "compRes Recommendations: Overview for WS " + WorkspaceFactory.getInstance().getActiveWorkspace().getName();
        File file = null;
        File fileTemp = null;

        try (FileWriter writer = new FileWriter(fileTemp = htmlf.createHTMLAutoConCurSummaryFile(filenameTemp), true); HtmlStream stream = new HtmlStream(writer)) {
            file = htmlf.createHTMLAutoConCurSummaryFile(filename);
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
        } finally {
            if (file != null && fileTemp != null) {
                file.delete();
                fileTemp.renameTo(file);
            }
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
        for (int j = 0; j < HEADS.length; ++j) {
            if (!"Plausibility checks".equals(HEADS[j])) {
                stream.write(new HtmlTableHeader(HEADS[j]));
            } else {
                stream.write((HtmlTableHeader) (new HtmlTableHeader(HEADS[j]).withColSpan(HEADSCHECKS.length)));
            }
        }
        stream.close(HtmlTag.TABLEROW).newLine();
        stream.write('\n');
        stream.open(HtmlTag.TABLEROW);
        for (int j = 0; j < HEADS.length; ++j) {
            if (!"Plausibility checks".equals(HEADS[j])) {
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
            if (bean.getFile() == null || bean.getFile().trim().isEmpty()) {
                stream.write(new HtmlTableCell(Frozen.removeFrozen(bean.getTitle())));
            } else {
                stream.write(new HtmlTableCell("<a href=\"" + bean.getFile() + "#" + bean.getTitle() + "\" target=\"_blank\">" + Frozen.removeFrozen(bean.getTitle()) + "</a>"));
            }
            if (bean.getDecision() == Decision.UNKNOWN && bean.getErrortext().contains(AutoConCur.NOSEASONALFACTORS)) {
                stream.write(new HtmlTableCell(String.valueOf(bean.getDecision()), BLACKSTYLE));
                fillUnknownRow(stream, bean, sbSuccessful);
                return;
            }
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

            stream.write(new HtmlTableCell(isBeanProperty(bean, DecisionBean::isSeasonalFactor), HtmlStyle.Center));
            stream.write(new HtmlTableCell(isBeanProperty(bean, DecisionBean::isFixOutlier), HtmlStyle.Center));
            stream.write(new HtmlTableCell(isBeanProperty(bean, DecisionBean::isExtremevalue), HtmlStyle.Center));
            stream.write(new HtmlTableCell(isBeanProperty(bean, DecisionBean::isGrowthRate), HtmlStyle.Center));
            if (bean.isCheckSign()) {
                stream.write(new HtmlTableCell(isBeanProperty(bean, DecisionBean::isSignChange), HtmlStyle.Center));
            } else {
                stream.write(new HtmlTableCell(isBeanProperty(bean, DecisionBean::isSignChange)), "lightgrey\" style=\"text-align:center");
            }
//            //ToDo: MaxRevision
//            stream.write(new HtmlTableCell(String.valueOf(Double.NaN)));
            //Current growth rate (old factor)
            stream.write(new HtmlTableCell(Double.isNaN(bean.getGrowthOld()) ? "NOT CALCULATED" : DF.format(bean.getGrowthOld())));
            stream.write(new HtmlTableCell(Double.isNaN(bean.getGrowthNew()) ? "NOT CALCULATED" : DF.format(bean.getGrowthNew())));
//            //ToDo: Update History
//            stream.write(new HtmlTableCell(""));

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
        for (String entry : HEADSCHECKS) {
            stream.write(new HtmlTableCell(""));
        }

        for (int i = java.util.Arrays.asList(HEADS).indexOf("Plausibility checks") + 1; i < HEADS.length - 1; i++) {
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
        beans.stream().filter(b -> b.isManual() == manual && b.getDecision() == Decision.UNKNOWN && !b.getErrortext().contains(AutoConCur.NOSEASONALFACTORS)).forEach((bean -> {
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
        beans.stream().filter(b -> manual == false && b.getDecision() == Decision.UNKNOWN && b.getErrortext().contains(AutoConCur.NOSEASONALFACTORS)).forEach((bean -> {
            AutoConCurReport.writeTable(stream, bean, sbSuccessful, sbError);
        }));
    }

    private static String isBeanProperty(DecisionBean bean, Function<DecisionBean, Boolean> isProperty) {
        StringBuilder output = new StringBuilder();
        if (bean != null) {
            if (isProperty.apply(bean)) {
                output.append("X");
            }
            if (bean.getPreperiodbean() != null && isProperty.apply(bean.getPreperiodbean())) {
                output.append("(X)");
            }
        }
        return output.toString();
    }
}

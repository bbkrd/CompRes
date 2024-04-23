/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package autoconcurreport;

import de.bbk.autoconcur.AutoConCur;
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

/**
 *
 * @author Jan Gerhardt
 */
public class AutoConCurReport {

//    private final Map<String, List<SaItem>> map;
    private static HTMLAutoConCurSummary htmlf;
    private static final DecimalFormat DF = new DecimalFormat("0.00");

    private static final String[] HEADS = {"Series",
        "Recommendations",
        "Large Movement",
        "Extreme Value",
        "nD8",
        "nGrowth",
        "Trim",
        "tolGrowth",
        "SF[t]",
        "SFSpan[t]",
        "SF[t-1]",
        "SFSpan[t-1]",
        "GrowthNew[t]",
        "GrowthOld[t]",
        "Info"};
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
            JOptionPane.showMessageDialog(null, htmlf.getErrorMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null, "The HTML is not generated, you haven't selected a folder. ");
        }
        return false;
    }

    public static void call() {
        ReportMessages messages = process();
        showMessages(messages);
    }

    private static ReportMessages process() {
        AutoConCur acc = new AutoConCur();
        List<DecisionBean> beans = acc.makeDecisions();

        StringBuilder sbError = new StringBuilder();
        StringBuilder sbSuccessful = new StringBuilder();
        if (beans.isEmpty()) {
            sbError.append("No recommendations are given.");
            return new ReportMessages(sbSuccessful.toString(), sbError.toString());
        }
        HTMLAutoConCurSummary autoConCurfile = new HTMLAutoConCurSummary();
        if (!makeHtmlf()) {
            return ReportMessages.EMPTY;
        }
        try (FileWriter writer = new FileWriter(autoConCurfile.createHTMLAutoConCurSummaryFile("Summary_" + WorkspaceFactory.getInstance().getActiveWorkspace().getName()), true)) {
            HtmlStream stream = new HtmlStream(writer);
            stream.open();
            stream.write(STYLE);
            stream.write(HtmlTag.HEADER2, "compRes Recommendations for WS " + WorkspaceFactory.getInstance().getActiveWorkspace().getName());
            stream.open(new HtmlTable());
            initTable(stream);

            beans.stream().filter(b -> b.getDecision() == Decision.UNKNOWN).forEach((bean -> {
                writeTable(stream, bean, sbSuccessful, sbError);
            }));
            beans.stream().filter(b -> b.getDecision() == Decision.CHECK).forEach((bean -> {
                writeTable(stream, bean, sbSuccessful, sbError);
            }));
            beans.stream().filter(b -> b.getDecision() == Decision.UPDATE).forEach((bean -> {
                writeTable(stream, bean, sbSuccessful, sbError);
            }));
            beans.stream().filter(b -> b.getDecision() == Decision.KEEP).forEach((bean -> {
                writeTable(stream, bean, sbSuccessful, sbError);
            }));
            stream.close(HtmlTag.TABLE);
            stream.newLines(1);
            stream.close();
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(Processing.class.getName()).log(Level.SEVERE, null, ex);
            sbError.append(":")
                    .append(System.lineSeparator())
                    .append("- It is not possible to create the file:")
                    .append(System.lineSeparator())
                    .append(autoConCurfile.getFilePath())
                    .append(System.lineSeparator())
                    .append(" because ")
                    .append(autoConCurfile.getErrorMessage())
                    .append(System.lineSeparator());
        }
        return new ReportMessages(sbSuccessful.toString(), sbError.toString());
    }

    private static void showMessages(ReportMessages o) {
        if (!o.getErrorMessages().isEmpty()) {
            JTextArea jta = new JTextArea(o.getErrorMessages());
            jta.setEditable(false);
            JScrollPane jsp = new JScrollPane(jta);
            jsp.setPreferredSize(new Dimension(480, 120));
            JOptionPane.showMessageDialog(null, jsp, "Error", JOptionPane.ERROR_MESSAGE);
        }

        if (!o.getSuccessMessages().isEmpty()) {
            JTextArea jta = new JTextArea(o.getSuccessMessages());
            jta.setEditable(false);
            JScrollPane jsp = new JScrollPane(jta);
            jsp.setPreferredSize(new Dimension(480, 120));
            JOptionPane.showMessageDialog(null, jsp, "The output is available for: ", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private static void initTable(HtmlStream stream) throws IOException {
        stream.open(HtmlTag.TABLEROW);
        for (int j = 0; j < HEADS.length; ++j) {
            stream.write(new HtmlTableHeader(HEADS[j]));
        }
        stream.close(HtmlTag.TABLEROW);
    }

    private static void writeTable(HtmlStream stream, DecisionBean bean, StringBuilder sbSuccessful, StringBuilder sbError) {
        try {
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell(Frozen.removeFrozen(bean.getTitle())));
            switch (bean.getDecision()) {
                case UNKNOWN:
                    stream.write(new HtmlTableCell(String.valueOf(Decision.UNKNOWN), WHITESTYLE), "Red");
                    for (int i = 2; i < HEADS.length - 1; i++) {
                        stream.write(new HtmlTableCell(""));
                    }
                    if (bean.getErrortext() == null) {
                        stream.write(new HtmlTableCell(""));
                    } else {
                        stream.write(new HtmlTableCell(bean.getErrortext()));
                    }
                    sbSuccessful.append(Frozen.removeFrozen(bean.getTitle()))
                            .append(System.lineSeparator());
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
            if (bean.isDevelopment()) {
                stream.write(new HtmlTableCell("X", HtmlStyle.Center));
            } else {
                stream.write(new HtmlTableCell(""));
            }
            if (bean.isExtremevalue()) {
                stream.write(new HtmlTableCell("X", HtmlStyle.Center));
            } else {
                stream.write(new HtmlTableCell(""));
            }
            stream.write(new HtmlTableCell(String.valueOf(bean.getND8())));
            stream.write(new HtmlTableCell(String.valueOf(bean.getNGrowth())));
            stream.write(new HtmlTableCell(DF.format((bean.getTrim()))));
            stream.write(new HtmlTableCell(DF.format((bean.getToleranceGrowth()))));
            stream.write(new HtmlTableCell(DF.format(bean.getLastSF())));
            if (bean.getIntervalSF() != null && bean.getIntervalSF().length == 2) {
                stream.write(new HtmlTableCell("[" + DF.format(bean.getIntervalSF()[0]) + " ; " + DF.format(bean.getIntervalSF()[1]) + "]"));
            } else {
                stream.write(new HtmlTableCell("NO DATA"));
            }

            if (bean.getPreperiodbean() != null) {
                stream.write(new HtmlTableCell(DF.format(bean.getPreperiodbean().getLastSF())));
                if (bean.getPreperiodbean() != null && bean.getPreperiodbean().getIntervalSF() != null && bean.getPreperiodbean().getIntervalSF().length == 2) {
                    stream.write(new HtmlTableCell("[" + DF.format(bean.getPreperiodbean().getIntervalSF()[0]) + " ; " + DF.format(bean.getPreperiodbean().getIntervalSF()[1]) + "]"));
                } else {
                    stream.write(new HtmlTableCell("NO DATA"));
                }
            } else {
                stream.write(new HtmlTableCell("NO DATA"));
                stream.write(new HtmlTableCell("NO DATA"));
            }

            stream.write(new HtmlTableCell(Double.isNaN(bean.getGrowthNew()) ? "NOT CALCULATED" : DF.format(bean.getGrowthNew())));
            stream.write(new HtmlTableCell(Double.isNaN(bean.getGrowthOld()) ? "NOT CALCULATED" : DF.format(bean.getGrowthNew())));
            if (bean.getErrortext() == null) {
                stream.write(new HtmlTableCell(""));
            } else {
                stream.write(new HtmlTableCell(bean.getErrortext()));
            }
            stream.close(HtmlTag.TABLEROW);
            sbSuccessful.append(Frozen.removeFrozen(bean.getTitle()))
                    .append(System.lineSeparator());
        } catch (IOException ex) {
            sbError.append(bean.getTitle()
            ).append(": ").append(ex.getMessage()).append(System.lineSeparator());
        }
    }
}

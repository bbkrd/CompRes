/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bbk.outputcustomized.html;

import ec.satoolkit.x11.DefaultSeasonalFilteringStrategy;
import ec.satoolkit.x11.MsrTable;
import ec.satoolkit.x11.Mstatistics;
import ec.satoolkit.x11.SeasonalFilterOption;
import ec.satoolkit.x11.X11Results;
import ec.satoolkit.x13.X13Specification;
import ec.tss.html.*;
import ec.tss.sa.documents.X13Document;
import static ec.tstoolkit.modelling.arima.x13.OutlierSpec.DEF_VA;
import java.io.IOException;

/**
 *
 * @author Thomas Witthohn
 */
public class HtmlCCA extends AbstractHtmlElement {

    private final String title;
    private final X13Specification spec;
    private final X11Results decomposition;
    private final Mstatistics stats;

    public HtmlCCA(String title, X13Document doc) {
        this.title = title;
        this.spec = doc.getSpecification();
        this.decomposition = doc.getDecompositionPart();
        this.stats = doc.getMStatistics();

    }

    @Override
    public void write(HtmlStream stream) throws IOException {
        writeTitle(stream);
        if (spec != null) {
            writeSpecification(stream);
        }
        writeTextForHTML(stream);

        if (stats != null) {
            writeCochranResult(stream);
        }
    }

    public void writeTextForHTML(HtmlStream stream) throws IOException {

        if (decomposition != null) {
            writeFinalFilters(stream);
        }

        if (stats != null) {
            if (stats.getRms() != null) {
                writeMovingSeasonalityRatios(stream);
            }
            writeICRatio(stream);
        }

        if (decomposition != null) {
            HtmlBBKSummary.writeCombinedSeasonalityTest(stream, decomposition);
        }

    }

    private void writeTitle(HtmlStream stream) throws IOException {
        if (title != null) {
            stream.write(HtmlTag.HEADER1, h1, title).newLine();
        }
    }

    private void writeFinalFilters(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER2, h2, "Final filters").newLine();
        stream.write("Seasonal filters:");
        if (decomposition.getFinalSeasonalFilterComposit() != null) {

            for (DefaultSeasonalFilteringStrategy dsfs : decomposition.getFinalSeasonalFilterComposit()) {
                stream.write(" ").write(dsfs.getDescription());
            }

        } else {
            stream.write(" ").write(decomposition.getFinalSeasonalFilter());
        }
        stream.newLine();

        //     stream.write("Seasonal filter: " + decomposition.getFinalSeasonalFilter()).newLine();
        stream.write("Trend filter: " + decomposition.getFinalTrendFilter()).newLine();

        stream.newLine();
    }

    private void writeSpecification(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER2, h2, "Specifications").newLine();
        //<editor-fold defaultstate="collapsed" desc="Sigmalimit">
        stream.write("Sigmalimit: ")
                .write(spec.getX11Specification().getLowerSigma())
                .write("; ")
                .write(spec.getX11Specification().getUpperSigma())
                .newLine();
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="Seasonalfilters">
        {
            stream.write("Seasonalfilters:");
            if (spec.getX11Specification().getSeasonalFilters() != null) {

                for (SeasonalFilterOption sfo : spec.getX11Specification().getSeasonalFilters()) {
                    stream.write(" ").write(sfo.toString());
                }

            } else {
                stream.write(" ").write(SeasonalFilterOption.Msr.toString());
            }
            stream.newLine();
        }
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="Outlier critical value">
        {
            //Strange behavior in Outlier spec || 0 means Default
            double criticalValue = spec.getRegArimaSpecification().getOutliers().getDefaultCriticalValue();
            stream.write("Outliers critical value: ");
            if (criticalValue != 0) {
                stream.write(criticalValue);
            } else {
                stream.write(DEF_VA);
            }
            stream.newLine();
        }
        //</editor-fold>

        stream.write("Calendarsigma: ").write(spec.getX11Specification().getCalendarSigma().toString()).newLine();
        stream.newLine();

    }

    private void writeICRatio(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER2, h2, "I/C Ratio");
        if (Double.isFinite(stats.getIcr())) {
            stream.write(df4.format(stats.getIcr()));
        }
        stream.newLine();
    }

    private void writeMovingSeasonalityRatios(HtmlStream stream) throws IOException {
        String header = "Moving Seasonality Ratios (MSR)";
        String[] tableHeaders = new String[]{"Period", "MSR"};

        MsrTable msrTable = stats.getRms();

        stream.write(HtmlTag.HEADER2, h2, header);
        int len = msrTable.getMeanIrregularEvolutions().length;
        double[] Q = new double[len];

        for (int i = 0; i < len; ++i) {
            Q[i] = msrTable.getRMS(i);
        }

        stream.open(
                new HtmlTable(0, 50 * (len + 1)));

        //Period row
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableHeader(tableHeaders[0], 50));
        for (int j = 1; j <= len; ++j) {
            stream.write(new HtmlTableCell(String.valueOf(j), 50));
        }
        stream.close(HtmlTag.TABLEROW);

        //MSR row
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableHeader(tableHeaders[1], 50));
        for (int j = 0; j < len; ++j) {
            if (Double.isFinite(Q[j])) {
                stream.write(new HtmlTableCell(df4.format(Q[j]), 50));
            } else {
                stream.write(new HtmlTableCell(".", 50));
            }
        }
        stream.close(HtmlTag.TABLEROW);

        stream.close(HtmlTag.TABLE);

        stream.newLines(2);
    }

    private void writeCochranResult(HtmlStream stream) throws IOException {

        String header = "Heteroskedasticity (Cochran test on equal variances within each period)";
        String[] tableHeaders = new String[]{"Test statistic", "Critical value (5% level)", "Decision"};

        stream.write(HtmlTag.HEADER2, h2, header);
        //  stream.write("Cochran Test Result:");
        boolean testResultCochran = stats.getCochranResult();
        stream.open(new HtmlTable(0, 30 + 120 * tableHeaders.length));
        stream.open(HtmlTag.TABLEROW);
        for (int j = 0; j < tableHeaders.length; ++j) {
            stream.write(new HtmlTableCell(tableHeaders[j], 120));
        }
        stream.close(HtmlTag.TABLEROW);
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell(Double.toString(Math.round(stats.getTestValue() * 10000d) / 10000d), 120));
        stream.write(new HtmlTableCell(Double.toString(Math.round(stats.getCriticalValue() * 10000d) / 10000d), 120));

        if (testResultCochran) {
            stream.write(new HtmlTableCell("Null hypothesis is not rejected.", 150));
        } else {
            stream.write(new HtmlTableCell("Null hypothesis is rejected.", 150));
        }

        stream.close(HtmlTag.TABLEROW);

        stream.close(HtmlTag.TABLE);
    }
}

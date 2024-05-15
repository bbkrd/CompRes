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
package de.bbk.concur.html;

import ec.satoolkit.x11.*;
import ec.satoolkit.x13.X13Specification;
import ec.tss.html.*;
import ec.tss.sa.documents.SaDocument;
import ec.tss.sa.documents.X13Document;
import ec.tstoolkit.algorithm.IProcResults;
import ec.tstoolkit.algorithm.IProcSpecification;
import ec.tstoolkit.modelling.arima.x13.RegArimaSpecification;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import org.openide.util.Exceptions;

/**
 *
 * @author Thomas Witthohn
 */
public class HtmlCCA extends AbstractHtmlElement {

    private final String title;
    private final IProcSpecification spec;
    private final IProcResults decomposition;
    private final Mstatistics stats;
    private final SaDocument doc;

    public HtmlCCA(String title, SaDocument doc) {
        this.title = title;
        this.spec = doc.getSpecification();
        this.decomposition = doc.getDecompositionPart();
        if (doc instanceof X13Document) {
            this.stats = ((X13Document) doc).getMStatistics();
        } else {
            this.stats = null;
        }
        this.doc = doc;
    }

    @Override
    public void write(HtmlStream stream) throws IOException {
        writeTitle(stream);
        new HtmlAutomised(title, doc).write(stream);
        if (spec != null) {
            writeSpecification(stream);
        }
        if (decomposition != null) {
            writeFinalFilters(stream);
        }

        if (stats != null && stats.getRms() != null) {
            writeMovingSeasonalityRatios(stream);
        }

        writeTextForHTML(stream);

        if (stats != null) {
            writeCochranResult(stream);
        }
    }

    public void writeTextForHTML(HtmlStream stream) throws IOException {
        if (stats != null) {
            writeICRatio(stream);
            stream.newLine();
        }

        if (decomposition != null) {
            new HtmlBBKSummary(title, doc).writeCombinedSeasonalityTest(stream);
        }

    }

    private void writeTitle(HtmlStream stream) throws IOException {
        if (title != null) {
            stream.write(HtmlTag.HEADER1, title).newLine();
        }
    }

    public String writeFilters() {
        StringWriter sbuilder = new StringWriter();
        HtmlStream stream = new HtmlStream(sbuilder);

        try {
            stream.open();
            writeFinalFilters(stream);
            stream.close();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        return sbuilder.toString();
    }

    private void writeFinalFilters(HtmlStream stream) throws IOException {
        if (decomposition instanceof X11Results) {
            X11Results x11Results = (X11Results) decomposition;

            stream.write(HtmlTag.HEADER2, "Final filters");
            stream.write("Seasonal filters:");
            if (x11Results.getFinalSeasonalFilterComposit() != null) {

                for (DefaultSeasonalFilteringStrategy dsfs : x11Results.getFinalSeasonalFilterComposit()) {
                    String filter = dsfs == null ? "Stable" : dsfs.getDescription();
                    stream.write(" ").write(filter);
                }

            } else {
                stream.write(" ").write(x11Results.getFinalSeasonalFilter());
            }
            stream.newLine();

            stream.write("Trend filter: " + x11Results.getFinalTrendFilter()).newLine();

            stream.newLine();
        }
    }

    private void writeSpecification(HtmlStream stream) throws IOException {
        if (spec instanceof X13Specification) {
            X11Specification x11Spec = ((X13Specification) spec).getX11Specification();

            stream.write(HtmlTag.HEADER2, "Specifications");
            //<editor-fold defaultstate="collapsed" desc="Sigmalimit">
            stream.write("Sigmalimit: ")
                    .write(x11Spec.getLowerSigma())
                    .write("; ")
                    .write(x11Spec.getUpperSigma())
                    .newLine();
            //</editor-fold>

            //<editor-fold defaultstate="collapsed" desc="Seasonalfilters">
            {
                stream.write("Seasonalfilters:");
                if (x11Spec.getSeasonalFilters() != null) {
                    SeasonalFilterOption first = x11Spec.getSeasonalFilters()[0];
                    boolean isSameSeasonalFilter = Arrays.stream(x11Spec.getSeasonalFilters()).allMatch(x -> x.equals(first));

                    if (isSameSeasonalFilter) {
                        stream.write("Seasonal filters:" + first.name()).newLine();
                    } else {
                        for (SeasonalFilterOption sfo : x11Spec.getSeasonalFilters()) {
                            stream.write(" ").write(sfo.toString());
                        }

                    }
                } else {
                    stream.write(" ").write(SeasonalFilterOption.Msr.toString());
                }
                stream.newLine();
            }
            //</editor-fold>

            //<editor-fold defaultstate="collapsed" desc="Outlier critical value">
            {
                RegArimaSpecification regArimaSpecification = ((X13Specification) spec).getRegArimaSpecification();
                //Strange behavior in Outlier spec || 0 means Default
                if (regArimaSpecification.getOutliers().isUsed()) {
                    double criticalValue = regArimaSpecification.getOutliers().getDefaultCriticalValue();
                    stream.write("Outliers critical value: ");
                    if (criticalValue != 0) {
                        stream.write(criticalValue);
                    } else {
                        stream.write("auto");
                    }
                    stream.newLine();
                }
            }
            //</editor-fold>

            stream.write("Calendarsigma: ").write(x11Spec.getCalendarSigma().toString()).newLine();
            stream.newLine();
        }
    }

    private void writeICRatio(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER2, "I/C Ratio");
        if (Double.isFinite(stats.getIcr())) {
            stream.write(df4.format(stats.getIcr()));
        }
        stream.newLine();
    }

    private void writeMovingSeasonalityRatios(HtmlStream stream) throws IOException {
        String header = "Moving Seasonality Ratios (MSR)";
        String[] tableHeaders = new String[]{"Period", "MSR"};

        MsrTable msrTable = stats.getRms();

        stream.write(HtmlTag.HEADER2, header);
        int len = msrTable.getMeanIrregularEvolutions().length;
        double[] q = new double[len];

        for (int i = 0; i < len; ++i) {
            q[i] = msrTable.getRMS(i);
        }

        stream.open(
                new HtmlTable());

        //Period row
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableHeader(tableHeaders[0]));
        for (int j = 1; j <= len; ++j) {
            stream.write(new HtmlTableCell(String.valueOf(j)));
        }
        stream.close(HtmlTag.TABLEROW);

        //MSR row
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableHeader(tableHeaders[1]));
        for (int j = 0; j < len; ++j) {
            if (Double.isFinite(q[j])) {
                stream.write(new HtmlTableCell(df4.format(q[j])));
            } else {
                stream.write(new HtmlTableCell("."));
            }
        }
        stream.close(HtmlTag.TABLEROW);

        stream.close(HtmlTag.TABLE);

        stream.newLines(1);
    }

    private void writeCochranResult(HtmlStream stream) throws IOException {

        String header = "Heteroskedasticity (Cochran test on equal variances within each period)";
        String[] tableHeaders = new String[]{"Test statistic", "Critical value (5% level)", "Decision"};

        stream.write(HtmlTag.HEADER2, header);
        boolean testResultCochran = stats.getCochranResult();
        stream.open(new HtmlTable());
        stream.open(HtmlTag.TABLEROW);
        for (int j = 0; j < tableHeaders.length; ++j) {
            stream.write(new HtmlTableCell(tableHeaders[j]));
        }
        stream.close(HtmlTag.TABLEROW);
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell(Double.toString(Math.round(stats.getTestValue() * 10000d) / 10000d)));
        stream.write(new HtmlTableCell(Double.toString(Math.round(stats.getCriticalValue() * 10000d) / 10000d)));

        if (testResultCochran) {
            stream.write(new HtmlTableCell("Null hypothesis is not rejected."));
        } else {
            stream.write(new HtmlTableCell("Null hypothesis is rejected."));
        }

        stream.close(HtmlTag.TABLEROW);

        stream.close(HtmlTag.TABLE);
    }
}

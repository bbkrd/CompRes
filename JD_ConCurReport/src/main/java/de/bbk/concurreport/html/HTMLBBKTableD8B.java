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
package de.bbk.concurreport.html;

import de.bbk.concur.FixedOutlier;
import de.bbk.concur.TablesPercentageChange;
import de.bbk.concur.util.D8BInfos;
import de.bbk.concur.util.FixTimeDomain;
import de.bbk.concurreport.options.ConCurReportOptionsPanel;
import static de.bbk.concurreport.options.ConCurReportOptionsPanel.DECIMAL_PLACES;
import static de.bbk.concurreport.options.ConCurReportOptionsPanel.DEFAULT_DECIMAL_PLACES;
import ec.nbdemetra.ui.properties.l2fprod.ColorChooser;
import ec.satoolkit.x11.DefaultSeasonalFilteringStrategy;
import ec.satoolkit.x11.MsrTable;
import ec.satoolkit.x11.Mstatistics;
import ec.satoolkit.x11.X11Results;
import ec.tss.html.AbstractHtmlElement;
import ec.tss.html.CssProperty;
import ec.tss.html.CssStyle;
import ec.tss.html.HtmlStream;
import ec.tss.html.HtmlTableCell;
import ec.tss.html.HtmlTag;
import ec.tss.sa.documents.SaDocument;
import ec.tss.sa.documents.X13Document;
import ec.tstoolkit.timeseries.regression.OutlierEstimation;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDataBlock;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import ec.tstoolkit.timeseries.simplets.YearIterator;
import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.NbPreferences;

/**
 *
 * @author Christiane Hofer
 */
public class HTMLBBKTableD8B extends AbstractHtmlElement {

    private final String FMT = "%." + NbPreferences.forModule(ConCurReportOptionsPanel.class).getInt(DECIMAL_PLACES, DEFAULT_DECIMAL_PLACES) + "f";

    private final SaDocument document;
    private final D8BInfos d8BInfos;
    private final int frequency;

    public HTMLBBKTableD8B(SaDocument doc) {
        this.document = doc;
        this.frequency = doc.getSeries().getFrequency().intValue();
        this.d8BInfos = new D8BInfos(doc);
    }

    @Override
    public void write(HtmlStream stream) throws IOException {
        String headerSi;
        if (document instanceof X13Document) {
            headerSi = "D8B";
        } else {
            headerSi = "Pseudo D8B";
        }
        stream.write("<table style=\"table-layout:fixed\" >");
        stream.open(HtmlTag.TABLEROW);
        stream.write("<th colspan=\"")
                .write(String.valueOf(frequency + 1))
                .write("\" style=\"text-align:left\">")
                .write(headerSi);
        stream.close(HtmlTag.TABLEHEADER);
        stream.close(HtmlTag.TABLEROW);

        writeSeasonalFilter(stream);
        writeMSR(stream);

        writeD8B(stream);

        writeGrowthRates(stream);

        stream.close(HtmlTag.TABLE);
        stream.newLine();
    }

    private void writeGrowthRates(HtmlStream stream) throws IOException {

        TsDomain domain = document.getSeries().getDomain();
        TablesPercentageChange tpc = new TablesPercentageChange(document);

        TsData seasonallyAdjustedPercentageChange = tpc.getSeasonallyAdjusted() != null ? tpc.getSeasonallyAdjusted().getTsData() : new TsData(domain);
        stream.write(HtmlTsData.builder()
                .data(lastYearOfSeries(domain, seasonallyAdjustedPercentageChange))
                .title("new GR")
                .includeHeader(false)
                .includeTableTags(false)
                .dataItalic(true)
                .build());

        TsData savedSeasonallyAdjustedPercentageChange = tpc.getSavedSeasonallyAdjusted() != null ? tpc.getSavedSeasonallyAdjusted().getTsData() : new TsData(domain);
        stream.write(HtmlTsData.builder()
                .data(lastYearOfSeries(domain, savedSeasonallyAdjustedPercentageChange))
                .title("current GR")
                .includeHeader(false)
                .includeTableTags(false)
                .dataItalic(true)
                .build());
    }

    private void writeMSR(HtmlStream stream) throws IOException {
        if (document instanceof X13Document) {
            Mstatistics stats = ((X13Document) document).getMStatistics();
            if (stats != null) {
                stream.open(HtmlTag.TABLEROW)
                        .open(HtmlTag.TABLEHEADER)
                        .write("MSR")
                        .close(HtmlTag.TABLEHEADER);
                MsrTable msrTable = stats.getRms();
                for (int j = 0; j < frequency; ++j) {
                    double rms = msrTable.getRMS(j);
                    if (Double.isFinite(rms)) {
                        stream.write(new HtmlTableCell(df2.format(rms)));
                    } else {
                        stream.write(new HtmlTableCell("."));
                    }
                }
            }
        }
    }

    private void writeSeasonalFilter(HtmlStream stream) throws IOException {
        if (document instanceof X13Document) {
            X11Results decomposition = ((X13Document) document).getDecompositionPart();

            if (decomposition != null) {
                stream.open(HtmlTag.TABLEROW);
                stream.open(HtmlTag.TABLEHEADER).write("Seas. Filter").close(HtmlTag.TABLEHEADER);
                if (decomposition.getFinalSeasonalFilterComposit() != null) {
                    for (DefaultSeasonalFilteringStrategy dsfs : decomposition.getFinalSeasonalFilterComposit()) {
                        String filter = dsfs == null ? "Stable" : dsfs.getDescription();
                        stream.open(HtmlTag.TABLECELL).write(filter).close(HtmlTag.TABLECELL);
                    }
                } else {
                    stream.write("<td colspan=\"")
                            .write(String.valueOf(frequency))
                            .write("\" style=\"text-align:center\">")
                            .write(decomposition.getFinalSeasonalFilter())
                            .close(HtmlTag.TABLECELL);
                }
                stream.close(HtmlTag.TABLEROW);
            }
        }
    }

    private TsData lastYearOfSeries(TsDomain dom, TsData tsData) {
        dom = FixTimeDomain.domLastYear(dom);

        if (tsData != null) {
            TsDomain intersection = tsData.getDomain().intersection(dom);
            return tsData.fittoDomain(intersection);
        }
        return new TsData(dom);

    }

    private void writeD8B(HtmlStream stream) {
        try {
            d8b(stream);
            TsDomain domain = document.getSeries().getDomain();
            TsData seasonalFactor = d8BInfos.getSeasonalFactor() != null ? d8BInfos.getSeasonalFactor().getTsData() : new TsData(domain);
            stream.write(HtmlTsData.builder()
                    .data(lastYearOfSeries(domain, seasonalFactor))
                    .title("new")
                    .includeHeader(false)
                    .includeTableTags(false)
                    .build());

            TsData savedSeasonalFactor = d8BInfos.getSavedSeasonalFactor() != null ? d8BInfos.getSavedSeasonalFactor().getTsData() : new TsData(domain);
            stream.write(HtmlTsData.builder()
                    .data(lastYearOfSeries(domain, savedSeasonalFactor))
                    .title("current")
                    .includeHeader(false)
                    .includeTableTags(false)
                    .build());

            if (document.getMetaData() != null) {
                String savedID = document.getMetaData().get("prodebene.seasonalfactor.loadid");
                if (savedID != null) {
                    stream.open(HtmlTag.TABLEROW);
                    stream.write("<th colspan=\"")
                            .write(String.valueOf(frequency + 1))
                            .write("\">")
                            .write("Loaded from ")
                            .write(savedID);
                    stream.close(HtmlTag.TABLEHEADER);
                    stream.close(HtmlTag.TABLEROW);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(HTMLBBKTableD8B.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void d8b(HtmlStream stream) throws IOException {

        if (d8BInfos.getSi() == null || d8BInfos.getSi().getTsData() == null) {
            return;
        }
        TsData data = d8BInfos.getSi().getTsData();
        stream.open(HtmlTag.TABLEROW);
        stream.write(HtmlTag.TABLEHEADER);
        for (int i = 0; i < data.getFrequency().intValue(); ++i) {
            stream.write(HtmlTag.TABLEHEADER, TsPeriod.formatShortPeriod(data.getFrequency(), i));
        }
        stream.close(HtmlTag.TABLEROW);

        int nfreq = data.getFrequency().intValue();
        TsData replacementValues = d8BInfos.getReplacementValues();
        Map<TsPeriod, List<Object>> outlierMap = new HashMap<>();

        if (d8BInfos.getBoth() != null) {
            for (OutlierEstimation outlier : d8BInfos.getBoth()) {
                TsPeriod position = outlier.getPosition();
                List<Object> list = outlierMap.getOrDefault(position, new ArrayList<>());
                list.add(outlier);
                outlierMap.put(position, list);
            }
        }
        if (d8BInfos.getFixedOutliers() != null) {
            for (FixedOutlier fixedOutlier : d8BInfos.getFixedOutliers()) {
                TsPeriod position = fixedOutlier.getPosition();
                List<Object> list = outlierMap.getOrDefault(position, new ArrayList<>());
                list.add(fixedOutlier);
                outlierMap.put(position, list);
            }
        }

        YearIterator iter = new YearIterator(data);
        while (iter.hasMoreElements()) {
            stream.open(HtmlTag.TABLEROW);
            TsDataBlock block = iter.nextElement();
            stream.write(HtmlTag.TABLEHEADER,
                    Integer.toString(block.start.getYear()));
            int start = block.start.getPosition();
            int n = block.data.getLength();
            for (int i = 0; i < start; ++i) {
                stream.write(HtmlTag.TABLECELL);
            }
            for (int i = 0; i < n; ++i) {
                if (Double.isFinite(block.data.get(i))) {
                    Formatter formatter = new Formatter();
                    formatter.format(FMT, block.data.get(i));
                    TsPeriod pointInTime = block.start.plus(i);
                    List<Object> list = outlierMap.get(pointInTime);

                    CssStyle outlierColor = new CssStyle();
                    if (list != null && !list.isEmpty()) {
                        Color bgColor;
                        Color textColor;
                        if (list.size() > 1) {
                            bgColor = Color.YELLOW;
                            textColor = Color.BLACK;
                        } else {
                            String code;
                            Object o = list.get(0);
                            if (o instanceof OutlierEstimation) {
                                code = ((OutlierEstimation) o).getCode();
                            } else if (o instanceof FixedOutlier) {
                                code = ((FixedOutlier) o).getCode();
                            } else {
                                code = "No Code";
                            }
                            bgColor = ColorChooser.getColor(code);
                            textColor = ColorChooser.getForeColor(code);
                        }
                        String bgColorString = new StringBuilder().append("rgb(")
                                .append(bgColor.getRed()).append(",")
                                .append(bgColor.getGreen()).append(",")
                                .append(bgColor.getBlue()).append(")").toString();
                        outlierColor.add(CssProperty.BACKGROUND_COLOR, bgColorString);
                        String textColorString = new StringBuilder().append("rgb(")
                                .append(textColor.getRed()).append(",")
                                .append(textColor.getGreen()).append(",")
                                .append(textColor.getBlue()).append(")").toString();
                        outlierColor.add(CssProperty.COLOR, textColorString);
                    }
                    stream.open(HtmlTag.TABLECELL, outlierColor);
                    if (replacementValues != null && replacementValues.getFrequency() == block.start.getFrequency() && Double.isFinite(replacementValues.get(block.start.plus(i)))) {
                        stream.write("*");
                    }
                    stream.write(formatter.toString());
                    stream.close(HtmlTag.TABLECELL);
                } else {
                    stream.write(HtmlTag.TABLECELL, ".");
                }
            }

            for (int i = start + n; i < nfreq; ++i) {
                stream.write(HtmlTag.TABLECELL);
            }
            stream.close(HtmlTag.TABLEROW);
        }

    }
}

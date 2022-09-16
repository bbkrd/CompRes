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

import ec.nbdemetra.ui.DemetraUI;
import ec.tss.TsCollectionInformation;
import ec.tss.TsInformation;
import ec.tss.html.HtmlStream;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.ui.chart.JTsChart;
import ec.ui.chart.TsXYDatasets;
import ec.util.chart.ColorScheme;
import ec.util.chart.ObsFunction;
import ec.util.chart.SeriesFunction;
import ec.util.chart.swing.JTimeSeriesChart;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import org.jfree.data.xy.IntervalXYDataset;

/**
 *
 * @author Christiane Hofer
 */
public class BBKMainChart extends JTsChart {

    private static final int DEFAULT_WIDTH = 450, DEFAULT_HEIGHT = 450;

    public void writeChart(TsCollectionInformation col, HtmlStream stream) throws IOException {

        JTimeSeriesChart chart = new JTimeSeriesChart();
        applyContent(chart, col);
        ColorScheme colorScheme = DemetraUI.getDefault().getColorScheme();
        themeSupport.setLocalColorScheme(colorScheme);
        chart.setColorSchemeSupport(themeSupport);
        chart.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT); //muss gemacht werden sonst exception
        chart.doLayout();
        NumberFormat valueFormat = chart.getValueFormat();
        valueFormat.setMaximumFractionDigits(2);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        // chart.writeImage("image/jpeg", stream);
        chart.writeImage("image/svg+xml", os);

        stream.write(os.toString());

    }

    private void applyContent(JTimeSeriesChart chart, TsCollectionInformation info) {
        chart.setTitle(info.name);
        chart.setDataset(getDataset(info));
        chart.setSeriesFormatter(getSeriesFormatter(info));
        chart.setObsFormatter(getObsFormatter(info));
    }

    private ObsFunction<String> getObsFormatter(final TsCollectionInformation info) {
        return new ObsFunction<String>() {
            @Override
            public String apply(int series, int obs) {
                TsData data = info.items.get(series).data;
                return data.getDomain().get(obs) + " : " + data.get(obs);
            }
        };
    }

    private SeriesFunction<String> getSeriesFormatter(final TsCollectionInformation info) {
        return new SeriesFunction<String>() {
            @Override
            public String apply(int series) {
                return info.items.get(series).name;
            }
        };
    }

    private IntervalXYDataset getDataset(TsCollectionInformation info) {
        TsXYDatasets.Builder result = TsXYDatasets.builder();
        info.items.stream().filter(TsInformation::hasData).forEach((o) -> {
            if (o.data != null) {
                result.add(o.name, o.data);
            }

        });
        return result.build();
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bbk.outputpdf;

import ec.tss.TsCollectionInformation;
import ec.tss.TsInformation;
import ec.tss.html.HtmlStream;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.ui.chart.JTsChart;

import ec.ui.chart.TsXYDatasets;
import ec.ui.interfaces.IDisposable;
import ec.util.chart.ObsFunction;
import ec.util.chart.SeriesFunction;
import ec.util.chart.swing.JTimeSeriesChart;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.jfree.data.xy.IntervalXYDataset;

/**
 *
 * @author Christiane Hofer
 */
public class BBKChart extends JTsChart implements IDisposable {

    private static final int WIDTH=450, HEIGHT=450;


    public void writeChart(TsCollectionInformation col, HtmlStream stream) throws IOException {

        JTimeSeriesChart chart = new JTimeSeriesChart();
        applyContent(chart, col);

        chart.setSize(WIDTH, HEIGHT); //muss gemacht werden sonst exception
        chart.doLayout();
        ByteArrayOutputStream os;
        os = new ByteArrayOutputStream();
        // chart.writeImage("image/jpeg", stream);
        chart.writeImage("image/svg+xml", os);

        stream.write(os.toString());

    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
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
            if (o.data!=null) {
                     result.add(o.name, o.data);   
            }
    
        });
        return result.build();
    }

}

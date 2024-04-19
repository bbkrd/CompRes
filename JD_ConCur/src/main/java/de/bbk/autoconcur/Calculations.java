/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bbk.autoconcur;

import ec.tstoolkit.timeseries.simplets.TsData;
import java.util.Arrays;

/**
 *
 * @author Jan Gerhardt
 */
public class Calculations {

    public static double[] quantiles(TsData tsdata, double w) {
        return quantiles(tsdata.internalStorage(), w);
    }

    public static double[] quantiles(double[] values, double w) {
        double[] quants = new double[2];
        if (values != null) {
            double[] valuelist = Arrays.stream(values.clone()).filter(d -> !Double.isNaN(d)).toArray();
            if (valuelist.length != 0) {
                Arrays.sort(valuelist);
                if (w > 0.5) {
                    w = 1 - w;
                }
                quants[0] = quantile(valuelist, w);
                quants[1] = quantile(valuelist, 1 - w);
                return quants;
            }
        }
        quants[0] = Double.NaN;
        quants[1] = Double.NaN;
        return quants;
    }

    private static double quantile(double[] valuelist, double w) {
        double index = (valuelist.length - 1) * w;
        int low = (int) Math.max(Math.floor(index), 0.0);
        int high = (int) Math.ceil(index);
        double quantile = valuelist[low];
        if (index > low && quantile != valuelist[high]) {
            double weight = index - low;
            quantile = (1 - weight) * quantile + weight * valuelist[high];
        }
        return quantile;
    }

    public static TsData growthRate(TsData ts) {
        return ts.pctVariation(1);
    }
}

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

    public static double[] quantiles(TsData tsdata, double trim) {
        return quantiles(tsdata.internalStorage(), trim);
    }

    public static double[] quantiles(double[] values, double trim) {
        double[] quants = new double[2];
        if (values != null) {
            double[] valuelist = Arrays.stream(values.clone()).filter(d -> !Double.isNaN(d)).toArray();
            if (valuelist.length != 0) {
                Arrays.sort(valuelist);
                if (trim > 0.5) {
                    trim = 1 - trim;
                }
                quants[0] = quantile(valuelist, trim);
                quants[1] = quantile(valuelist, 1 - trim);
                return quants;
            }
        }
        quants[0] = Double.NaN;
        quants[1] = Double.NaN;
        return quants;
    }

    private static double quantile(double[] valuelist, double trim) {
        double index = (valuelist.length - 1) * trim;
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

    public static double truncMean(TsData tsdata, double trim) {
        return Arrays.stream(truncDouble(tsdata.internalStorage().clone(), trim)).average().orElse(Double.NaN);
    }

    public static double truncStDev(TsData tsdata, double trim) {
        double stDev = 0.0;
        double[] truncated = truncDouble(tsdata.internalStorage().clone(), trim);
        if (truncated.length > 1) {
            double mean = truncMean(tsdata, trim);
            for (double summand : truncated) {
                stDev += Math.pow(summand - mean, 2);
            }
            stDev = Math.sqrt(stDev / (truncated.length - 1));
        }
        return stDev;
    }

    public static double[] truncDouble(double[] values, double trim) {
        double[] valuelist = values;
        if (values != null) {
            valuelist = Arrays.stream(valuelist.clone()).filter(d -> !Double.isNaN(d)).toArray();
            if(trim==0.0){
                return valuelist;
            }
            if (valuelist.length != 0) {
                Arrays.sort(valuelist);
                if (trim > 0.5) {
                    trim = 1 - trim;
                }
                double indexFrom = (valuelist.length - 1) * trim;
                int from = (int) Math.max(Math.floor(indexFrom), 0.0);
                double indexTo = (valuelist.length - 1) * (1 - trim);
                int to = (int) Math.ceil(indexTo);
                valuelist = Arrays.copyOfRange(valuelist, from, to);
                if (from < to) {
                    //interpolate
                    valuelist[0] = interpolate(valuelist[0], valuelist[1], indexFrom);
                    valuelist[valuelist.length - 1] = interpolate(valuelist[valuelist.length - 2], valuelist[valuelist.length - 1], indexTo);
                }
            }
        }
        return valuelist;
    }

    private static double interpolate(double value1, double value2, double index) {
        int low = (int) Math.max(Math.floor(index), 0.0);
        if (index > low && value1 != value2) {
            double weight = index - low;
            return (1 - weight) * value1 + weight * value2;
        }
        return value1;
    }

}

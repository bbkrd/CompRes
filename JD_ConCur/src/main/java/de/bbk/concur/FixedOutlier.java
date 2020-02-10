/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bbk.concur;

import ec.tstoolkit.modelling.arima.x13.RegressionSpec;
import ec.tstoolkit.timeseries.regression.OutlierDefinition;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Thomas Witthohn
 */
@lombok.Value
public class FixedOutlier {

    double value;
    String code;
    TsPeriod position;

    public static FixedOutlier[] extractFixedOutliers(RegressionSpec regression, int frequency) {
        TsFrequency tsFrequency = TsFrequency.valueOf(frequency);
        return extractFixedOutliers(regression, tsFrequency);
    }

    public static FixedOutlier[] extractFixedOutliers(RegressionSpec regression, TsFrequency tsFrequency) {
        if (regression == null || tsFrequency == null) {
            return new FixedOutlier[0];
        }
        List<FixedOutlier> fixedOutliersList = new ArrayList<>();
        OutlierDefinition[] outliers = regression.getOutliers();
        for (OutlierDefinition outlier : outliers) {
            String name = outlier.toString().replaceAll("\\.(\\d{4}(-\\d{2}){2})", " \\($1\\)");
            double[] fixedCoefficients = regression.getFixedCoefficients(name);
            if (fixedCoefficients != null) {
                fixedOutliersList.add(new FixedOutlier(fixedCoefficients[0], outlier.getCode(), new TsPeriod(tsFrequency, outlier.getPosition())));
            }
        }
        return fixedOutliersList.toArray(new FixedOutlier[fixedOutliersList.size()]);
    }
}

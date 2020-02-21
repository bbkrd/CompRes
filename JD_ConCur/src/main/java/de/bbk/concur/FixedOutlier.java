/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bbk.concur;

import ec.tss.sa.documents.SaDocument;
import ec.tss.sa.documents.TramoSeatsDocument;
import ec.tss.sa.documents.X13Document;
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

    public static FixedOutlier[] extractFixedOutliers(SaDocument doc, int frequency) {
        TsFrequency tsFrequency = TsFrequency.valueOf(frequency);

        return extractFixedOutliers(doc, tsFrequency);
    }

    public static FixedOutlier[] extractFixedOutliers(SaDocument doc, TsFrequency tsFrequency) {
        if (doc == null || tsFrequency == null) {
            return new FixedOutlier[0];
        }
        if (doc instanceof X13Document) {
            ec.tstoolkit.modelling.arima.x13.RegressionSpec regression = ((X13Document) doc).getSpecification().getRegArimaSpecification().getRegression();
            return extractFixedOutliers(regression, tsFrequency);
        } else if (doc instanceof TramoSeatsDocument) {
            ec.tstoolkit.modelling.arima.tramo.RegressionSpec regression = ((TramoSeatsDocument) doc).getSpecification().getTramoSpecification().getRegression();
            return extractFixedOutliers(regression, tsFrequency);
        } else {
            return new FixedOutlier[0];
        }
    }

    private static FixedOutlier[] extractFixedOutliers(ec.tstoolkit.modelling.arima.x13.RegressionSpec regression, TsFrequency tsFrequency) {
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

    private static FixedOutlier[] extractFixedOutliers(ec.tstoolkit.modelling.arima.tramo.RegressionSpec regression, TsFrequency tsFrequency) {
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

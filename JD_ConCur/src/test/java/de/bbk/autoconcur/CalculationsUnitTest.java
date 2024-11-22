/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bbk.autoconcur;

import static de.bbk.autoconcur.Calculations.quantiles;
import static de.bbk.autoconcur.Calculations.truncDouble;
import static de.bbk.autoconcur.Calculations.truncStDev;
import static de.bbk.autoconcur.Calculations.truncMean;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import java.util.Arrays;
import org.junit.Assert;
import static org.junit.Assert.assertArrayEquals;
import org.junit.Test;

/**
 *
 * @author Jan Gerhardt
 */
public class CalculationsUnitTest {

    private final static double eps = Math.pow(10, -5);

    private void quantLength(double[] quants) {
        if (quants.length != 2) {
            Assert.fail("not same length");
        }
    }

    @Test
    public void testquantilesOnlyNaNs() {
        TsDomain tsDomain = new TsDomain(TsFrequency.Monthly, 2010, 1, 96);
        TsData ts = new TsData(tsDomain);
        double[] quant = Calculations.quantiles(ts, 0.4);
        quantLength(quant);
        assert (Double.isNaN(quant[0]));
        assert (Double.isNaN(quant[1]));
    }

    @Test
    public void testquantilesOnlyZeros() {
        double[] values = new double[10];
        Assert.assertArrayEquals(new double[]{0.0, 0.0}, Calculations.quantiles(values, 0.4), eps);
    }

    @Test
    public void testquantilesLengthZero() {
        double[] values = new double[]{};
        double[] quant = Calculations.quantiles(values, 0.4);
        quantLength(quant);
        assert (Double.isNaN(quant[0]));
        assert (Double.isNaN(quant[1]));
    }

    @Test
    public void testquantilesNull() {
        double[] values = null;
        double[] quant = Calculations.quantiles(values, 0.4);
        quantLength(quant);
        assert (Double.isNaN(quant[0]));
        assert (Double.isNaN(quant[1]));
    }

    @Test
    public void testquantilesInfinity() {
        double[] values = new double[]{Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY};
        Assert.assertArrayEquals(new double[]{Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY}, Calculations.quantiles(values, 0.10), eps);
    }

    @Test
    public void testquantilesWithValues1() {
        double[] values = new double[]{1.2, -8.9, -16.5, 7.9, 7.7, 6.3, -5.2, 90.5, -20.11, 14.7, -8.9, -0.2, -3.4, -0.997};
        Assert.assertArrayEquals(new double[]{-14.22, 12.66}, Calculations.quantiles(values, 0.10), eps);
    }

    @Test
    public void testquantilesWithValues2() {
        double[] values = new double[]{1.2, -8.9, -16.5, 7.9, 7.7, 6.3, -5.2, 90.5, -20.11, 14.7, -8.9, -0.2, -3.4, -0.997, 111.1, 1 / 3, -5 / 9, -16 / 11, 7.89};
        Assert.assertArrayEquals(new double[]{-10.42, 29.86}, Calculations.quantiles(values, 0.10), eps);
    }

    @Test
    public void testquantilesWithValues3() {
        double[] values = new double[]{1.2, -8.9, -16.5, 7.9, Double.NaN, 6.3, -5.2, Double.NEGATIVE_INFINITY, -20.11, 14.7, -8.9, -0.2, Double.POSITIVE_INFINITY, -0.997, 111.1, 1 / 3, -5 / 9, -16 / 11, 7.89};
        Assert.assertArrayEquals(new double[]{-17.583, 43.62}, Calculations.quantiles(values, 0.10), eps);
    }

    @Test
    public void testQuantilesWithValidInput() {

        double[] values = {1.0, 2.0, 3.0, 4.0, 5.0};

        double w = 0.25; // 25th percentile and 75th percentile

        double[] expectedQuants = {2.0, 4.0}; // Expected quantiles for the given array and weight

        double[] actualQuants = quantiles(values, w);

        assertArrayEquals(expectedQuants, actualQuants, 0.001); // Delta is the tolerance for comparing double values

    }

    @Test
    public void testQuantilesWithNaNValues() {

        double[] values = {Double.NaN, 2.0, Double.NaN, 4.0, 5.0};

        double w = 0.5; // 50th percentile (median)

        double[] expectedQuants = {4.0, 4.0}; // Expected quantiles for the given array and weight

        double[] actualQuants = quantiles(values, w);

        assertArrayEquals(expectedQuants, actualQuants, 0.001); // Delta is the tolerance for comparing double values

    }

    private TsData values() {
        double[] vals = new double[]{1.2, -8.9, -16.5, 7.9, 7.7, 6.3, -5.2, 90.5, -20.11, 14.7, -8.9, -0.2, -3.4, -0.997, 111.1, 1 / 3, -5 / 9, -16 / 11, 7.89};
        return new TsData(new TsPeriod(TsFrequency.Monthly), vals, true);
    }

    @Test
    public void testTruncDouble() {
        TsData data = values();
        double[] expectedDoubles = new double[]{1.2, -8.9, -16.5, 7.9, 7.7, 6.3, -5.2, 22.28, -16.861, 14.7, -8.9, -0.2, -3.4, -0.997, 1 / 3, -5 / 9, -16 / 11, 7.89};
        Arrays.sort(expectedDoubles);
        double[] actualDoubles = truncDouble(data.internalStorage().clone(), 0.05);
        assertArrayEquals(expectedDoubles, actualDoubles, eps);
    }

    @Test
    public void testTruncMean() {
        TsData data = values();

        double expectedMean = 0.334;
        double actualMean = truncMean(data, 0.05);
        Assert.assertEquals(expectedMean, actualMean, eps);
    }

    @Test
    public void testTruncStDev() {
        TsData data = values();

        double expectedStDev = 9.97397;
        double actualStDev = truncStDev(data, 0.05);
        Assert.assertEquals(expectedStDev, actualStDev, eps);
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bbk.concur.util;

import ec.tstoolkit.timeseries.simplets.TsData;
import org.junit.Assert;

/**
 * Diese Klasse wird verwendet zm zwei Zeitreihen zu vergleichen und dann mit
 * AssertTrue einen Test zu schreiben
 * Diese Klasse gibt es auch im jtstoolkit, jedoch ist sie hier nicht verfügbar,
 * so dass sie hier gedoppelt ist
 *
 * @author Christiane Hofer
 */
public class CompareTsData {

    public static boolean compareTS(TsData orignal, TsData test, double precision) {
        if (!orignal.getStart().equals(test.getStart())) {
            Assert.fail("not same start");
        }
        if (!(orignal.getLength() == test.getLength())) {
            Assert.fail("not same length");
        }
        Assert.assertArrayEquals(orignal.internalStorage(), test.internalStorage(), precision);
        return true;
    }

}

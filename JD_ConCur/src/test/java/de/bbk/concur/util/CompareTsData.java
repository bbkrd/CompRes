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

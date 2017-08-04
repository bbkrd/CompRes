/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bbk.outputcustomized.util;

import ec.tss.Ts;
import ec.tss.TsFactory;
import ec.tstoolkit.timeseries.simplets.TsData;
import javax.annotation.Nonnull;

/**
 * Klasse um eine Zeitreihe in Procent zu konvertieren,
 * damit hinterher der in der ZIS DB gespeicherte Wert mit den frisch
 * Berechneten übereinstimmt
 *
 * @author Christiane Hofer
 */
public class InPercent {

    /**
     * @param ts
     * @param isMult if DecompositionType is Mult or LogAdd
     *
     * @return
     */
    public static final Ts convertTsInPercentIfMult(Ts ts, boolean isMult) {
        return TsFactory.instance.createTs(ts.getName(), ts.getMetaData(), convertTsDataInPercentIfMult(ts.getTsData(), isMult));
    }

    /**
     * @param tsData
     * @param isMult if DecompositionType is Mult or LogAdd
     *
     * @return a new TsData is returned
     */
    public static final TsData convertTsDataInPercentIfMult(@Nonnull TsData tsData, boolean isMult) {
        return isMult ? tsData.times(100) : tsData.clone();
    }
}

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

import ec.tss.Ts;
import ec.tss.TsFactory;
import ec.tstoolkit.timeseries.simplets.TsData;
import javax.annotation.Nonnull;

/**
 * Klasse um eine Zeitreihe in Procent zu konvertieren, damit hinterher der in
 * der ZIS DB gespeicherte Wert mit den frisch Berechneten übereinstimmt
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

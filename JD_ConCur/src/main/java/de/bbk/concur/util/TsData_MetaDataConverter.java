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
import ec.tstoolkit.MetaData;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import java.util.Locale;

/**
 *
 * @author Thomas Witthohn
 */
public class TsData_MetaDataConverter {

    public static final String CONVERTERPERFIX = "@bbk" + InformationSet.STRSEP + "output" + InformationSet.STRSEP,
            FREQUENCY = InformationSet.STRSEP + "frequency",
            STARTYEAR = InformationSet.STRSEP + "startyear",
            STARTPERIOD = InformationSet.STRSEP + "startperiod",
            VALUES = InformationSet.STRSEP + "values";

    public static void convertTsToMetaData(TsData data, MetaData meta, String tableName) {
        TsPeriod start = data.getStart();
        double[] values = data.internalStorage().clone();

        StringBuilder sb = new StringBuilder();
        for (double value : values) {
            sb.append(value).append(" ");
        }

        meta.put(CONVERTERPERFIX + tableName + FREQUENCY, String.valueOf(start.getFrequency().intValue()));
        meta.put(CONVERTERPERFIX + tableName + STARTYEAR, String.valueOf(start.getYear()));
        meta.put(CONVERTERPERFIX + tableName + STARTPERIOD, String.valueOf(start.getPosition()));
        meta.put(CONVERTERPERFIX + tableName + VALUES, sb.substring(0, sb.length() - 1));
    }

    public static Ts convertMetaDataToTs(MetaData meta, String tableName) {
        String prefix = CONVERTERPERFIX + tableName.toLowerCase(Locale.ENGLISH);

        Ts ts;
        ts = TsFactory.instance.createTs("Saved " + tableName);

        if (meta == null
                || !meta.containsKey(prefix + FREQUENCY)
                || !meta.containsKey(prefix + STARTYEAR)
                || !meta.containsKey(prefix + STARTPERIOD)
                || !meta.containsKey(prefix + VALUES)) {
            ts.setInvalidDataCause("No Data for " + tableName + " " + "via Workspace");
            return ts;
        }
        try {
            TsFrequency freq = TsFrequency.valueOf(Integer.parseInt(meta.get(prefix + FREQUENCY)));
            int startYear = Integer.parseInt(meta.get(prefix + STARTYEAR));
            int startPeriod = Integer.parseInt(meta.get(prefix + STARTPERIOD));

            String[] valuesAsString = meta.get(prefix + VALUES).split(" ");
            double[] data = new double[valuesAsString.length];
            for (int i = 0; i < valuesAsString.length; i++) {
                data[i] = Double.parseDouble(valuesAsString[i]);
            }
            TsData tsData = new TsData(new TsPeriod(freq, startYear, startPeriod), data, false);
            ts.set(tsData);
            return ts;
        } catch (NumberFormatException ex) {
            ts.setInvalidDataCause("Error reading Data for " + tableName + ".(NFE)");
            return ts;
        }
    }
}

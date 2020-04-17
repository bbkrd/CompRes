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
import ec.tstoolkit.timeseries.simplets.TsDomain;

/**
 *
 * @author Christiane Hofer
 */
public class FixTimeDomain {

    private final TsDomain domMax5years;

    /**
     *
     * @param ts timeSeries to get the last 5 years form
     */
    public FixTimeDomain(Ts ts) {
        this.domMax5years = domLastFiveYears(ts);
    }

    /**
     *
     * @param ts
     *
     * @return a new timesereis with the domain set in the Constructor
     */
    public Ts getTsWithDomain(Ts ts) {
        if (ts == null || ts.getTsData() == null || ts.getTsData().isEmpty()) {
            return ts;
        } else {
            Ts t = TsFactory.instance.createTs(ts.getName(), null, ts.getMetaData(), ts.getTsData().fittoDomain(this.domMax5years));
            return t;
        }
    }

    /**
     *
     * @param ts ts to get the domain from
     *
     * @return reduce the domain to the min of the last 5 years or all
     */
    public static TsDomain domLastFiveYears(Ts ts) {
        return domLastYears(ts, 5);

    }

    /**
     *
     * @param ts ts to get the domain from
     * @param years how many years
     *
     * @return reduce the domain to the min of the years or all
     */
    public static TsDomain domLastYears(Ts ts, int years) {
        if (ts.getTsData() != null) {
            TsDomain domTsY = ts.getTsData().getDomain();
            int int5year = domTsY.getFrequency().intValue() * years;
            TsDomain domain = new TsDomain(domTsY.getEnd().minus(int5year), int5year);
            domain = domain.intersection(domTsY);
            return domain;
        }
        return null;
    }

    public static TsDomain domLastYear(TsDomain dom) {
        int int1Year = dom.getFrequency().intValue();
        return new TsDomain(dom.getEnd().minus(int1Year), int1Year);
    }

}

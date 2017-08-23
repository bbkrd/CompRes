/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bbk.outputcustomized.util;

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
        if (ts.getTsData() == null || ts.getTsData().isEmpty()) {
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
        TsDomain domTsY = ts.getTsData().getDomain();
        TsDomain domCharMax5years;
        int int5year = domTsY.getFrequency().intValue() * 5;
        domCharMax5years = new TsDomain(domTsY.getEnd().minus(int5year), int5year);
        domCharMax5years = domCharMax5years.intersection(domTsY);
        return domCharMax5years;

    }

    public static TsDomain domLastYear(TsDomain dom) {
        int int1Year = dom.getFrequency().intValue();
        return new TsDomain(dom.getEnd().minus(int1Year), int1Year);
    }

}

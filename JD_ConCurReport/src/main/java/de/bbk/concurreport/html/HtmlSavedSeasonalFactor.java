/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bbk.concurreport.html;

import de.bbk.concur.util.SavedTables;
import de.bbk.concur.util.TsData_Saved;
import ec.tss.Ts;
import ec.tss.html.HtmlStream;
import ec.tss.html.IHtmlElement;
import ec.tss.sa.documents.SaDocument;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import java.io.IOException;

/**
 *
 * @author s4504tw
 */
public class HtmlSavedSeasonalFactor implements IHtmlElement {

    private final SaDocument doc;

    public HtmlSavedSeasonalFactor(SaDocument doc) {
        this.doc = doc;
    }

    @Override
    public void write(HtmlStream stream) throws IOException {
        Ts savedSeasonalFactor = TsData_Saved.convertMetaDataToTs(doc.getMetaData(), SavedTables.SEASONALFACTOR);
        if (savedSeasonalFactor != null && savedSeasonalFactor.getTsData() != null) {
            TsDomain savedSeasonalFactorDomain = savedSeasonalFactor.getTsData().getDomain();
            stream.write("Last available forecast for the ")
                    .write(SavedTables.NAME_SEASONAL_FACTOR_SAVED)
                    .write(" is ")
                    .write(savedSeasonalFactorDomain.getLast().toString())
                    .write(".").newLine();
        }
    }

}

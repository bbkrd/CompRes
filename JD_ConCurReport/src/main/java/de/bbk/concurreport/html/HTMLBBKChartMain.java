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
package de.bbk.concurreport.html;

import de.bbk.concur.util.SavedTables;
import de.bbk.concur.util.SeasonallyAdjusted_Saved;
import de.bbk.concurreport.BBKMainChart;
import ec.tss.*;
import ec.tss.documents.DocumentManager;
import ec.tss.html.AbstractHtmlElement;
import ec.tss.html.HtmlStream;
import ec.tss.html.IHtmlElement;
import ec.tss.sa.documents.X13Document;
import ec.tstoolkit.modelling.ModellingDictionary;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import java.io.IOException;

/**
 *
 * @author Christiane Hofer
 */
public class HTMLBBKChartMain extends AbstractHtmlElement implements IHtmlElement {

    private final X13Document x13doc;
    private final TsDomain domMax5;

    public HTMLBBKChartMain(X13Document x13doc, TsDomain tsDom) {
        this.x13doc = x13doc;
        this.domMax5 = tsDom;
    }

    @Override
    public void write(HtmlStream stream) throws IOException {
        TsCollection tc = TsFactory.instance.createTsCollection();
        TsData tsYData = DocumentManager.instance.getTs(x13doc, ModellingDictionary.Y).getTsData();
        Ts tsY = TsFactory.instance.createTs(SavedTables.NAME_SERIES, null, tsYData.fittoDomain(domMax5));
        tc.add(tsY);

        TsData tsSAData = DocumentManager.instance.getTs(x13doc, ModellingDictionary.SA).getTsData();
        Ts tsSA = TsFactory.instance.createTs(SavedTables.NAME_SEASONALLY_ADJUSTED, null, tsSAData.fittoDomain(domMax5));
        tc.add(tsSA);

        Ts tsSASaved = SeasonallyAdjusted_Saved.calcSeasonallyAdjusted(x13doc);
        if (tsSASaved.getTsData() != null && !tsSASaved.getTsData().isEmpty()) {
            tsSASaved.set(tsSASaved.getTsData().fittoDomain(domMax5));
            tc.add(tsSASaved);
        }

        TsCollectionInformation collectionInformation = new TsCollectionInformation(tc, TsInformationType.Data);
        BBKMainChart tschart = new BBKMainChart();
        tschart.writeChart(collectionInformation, stream);

    }

}

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
package de.bbk.concurreport.html.graphic;

import de.bbk.concur.util.SavedTables;
import de.bbk.concur.util.SeasonallyAdjusted_Saved;
import de.bbk.concurreport.BBKMainChart;
import ec.tss.*;
import ec.tss.documents.DocumentManager;
import ec.tss.html.AbstractHtmlElement;
import ec.tss.html.HtmlStream;
import ec.tss.sa.documents.SaDocument;
import ec.tstoolkit.modelling.ModellingDictionary;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import java.io.IOException;

/**
 *
 * @author Christiane Hofer
 */
public class HTMLBBKChartMain extends AbstractHtmlElement {

    private final SaDocument doc;
    private final TsDomain domain;

    public HTMLBBKChartMain(SaDocument doc, TsDomain tsDom) {
        this.doc = doc;
        this.domain = tsDom;
    }

    @Override
    public void write(HtmlStream stream) throws IOException {
        TsCollection tc = TsFactory.instance.createTsCollection();
        TsData tsYData = DocumentManager.instance.getTs(doc, ModellingDictionary.Y).getTsData();
        Ts tsY = TsFactory.instance.createTs(SavedTables.NAME_SERIES, null, tsYData.fittoDomain(domain));
        tc.add(tsY);

        TsData tsSAData = DocumentManager.instance.getTs(doc, ModellingDictionary.SA).getTsData();
        Ts tsSA = TsFactory.instance.createTs(SavedTables.NAME_SEASONALLY_ADJUSTED, null, tsSAData.fittoDomain(domain));
        tc.add(tsSA);

        Ts tsSASaved = SeasonallyAdjusted_Saved.calcSeasonallyAdjusted(doc);
        if (tsSASaved.getTsData() != null && !tsSASaved.getTsData().isEmpty()) {
            tsSASaved.set(tsSASaved.getTsData().fittoDomain(domain));
            tc.add(tsSASaved);
        }

        TsCollectionInformation collectionInformation = tc.toInfo(TsInformationType.Data);
        BBKMainChart tschart = new BBKMainChart();
        tschart.writeChart(collectionInformation, stream);
        tschart.dispose();
    }

}

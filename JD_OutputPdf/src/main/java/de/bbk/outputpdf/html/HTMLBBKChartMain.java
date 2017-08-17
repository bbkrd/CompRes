/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bbk.outputpdf.html;

import de.bbk.outputcustomized.util.SeasonallyAdjusted_Saved;
import de.bbk.outputpdf.BBKMainChart;
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
        Ts tsY = TsFactory.instance.createTs(ModellingDictionary.Y, null, tsYData.fittoDomain(domMax5));
        tc.add(tsY);

        TsData tsSAData = DocumentManager.instance.getTs(x13doc, ModellingDictionary.SA).getTsData();
        Ts tsSA = TsFactory.instance.createTs(ModellingDictionary.SA, null, tsSAData.fittoDomain(domMax5));
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

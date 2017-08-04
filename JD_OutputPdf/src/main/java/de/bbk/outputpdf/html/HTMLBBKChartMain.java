/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bbk.outputpdf.html;

import de.bbk.outputcustomized.util.SeasonallyAdjusted_Saved;
import de.bbk.outputpdf.BBKChart;
import ec.tss.Ts;
import ec.tss.TsCollection;
import ec.tss.TsCollectionInformation;
import ec.tss.TsFactory;
import ec.tss.TsInformationType;
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
        //Die TSCollection Information um die write methode zu verwenden
        Ts tsY, tsSA, tsSASaved;
        TsData tsYData, tsSAData;
        tsYData = DocumentManager.instance.getTs(x13doc, ModellingDictionary.Y).getTsData();
        tsY = TsFactory.instance.createTs(ModellingDictionary.Y, null, tsYData.fittoDomain(domMax5));
        tsSAData = DocumentManager.instance.getTs(x13doc, ModellingDictionary.SA).getTsData();
        tsSA = TsFactory.instance.createTs(ModellingDictionary.SA, null, tsSAData.fittoDomain(domMax5));
        tsSASaved = SeasonallyAdjusted_Saved.calcSeasonallyAdjusted(x13doc);
        tsSASaved.set(tsSASaved.getTsData().fittoDomain(domMax5));
       
        TsCollection tc = TsFactory.instance.createTsCollection();

        tc.add(tsY);
        tc.add(tsSA);
        tc.add(tsSASaved);
        TsCollectionInformation collectionInformation;
        collectionInformation = new TsCollectionInformation(tc, TsInformationType.Data);
        final BBKChart tschart = new BBKChart();
        tschart.writeChart(collectionInformation, stream);

    }

}

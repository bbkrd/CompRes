/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bbk.concur.util;

import de.bbk.concur.FixedOutlier;
import ec.tss.Ts;
import ec.tss.TsFactory;
import ec.tss.documents.DocumentManager;
import ec.tss.sa.documents.SaDocument;
import ec.tss.sa.documents.X13Document;
import ec.tstoolkit.algorithm.IProcResults;
import ec.tstoolkit.modelling.ModellingDictionary;
import ec.tstoolkit.timeseries.regression.OutlierEstimation;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import java.util.Arrays;
import lombok.Value;

/**
 *
 * @author s4504tw
 */
@Value
public class D8BInfos {

    private final int frequency;
    private final Ts si, seasonalFactor, savedSeasonalFactor;
    private final TsData replacementValues;
    private final OutlierEstimation[] both;
    private final FixedOutlier[] fixedOutliers;
    private final boolean valid;

    public D8BInfos(SaDocument doc) {
        IProcResults decomposition = doc.getDecompositionPart();
        if (decomposition == null) {
            this.frequency = 0;
            this.si = null;
            this.replacementValues = null;
            this.both = null;
            this.fixedOutliers = null;
            this.seasonalFactor = null;
            this.savedSeasonalFactor = null;
            this.valid = false;
            return;
        }
        this.frequency = doc.getSeries().getFrequency().intValue();
        //  boolean isLog = doc.getResults().getData("log", Boolean.class);
        boolean multiplicative = doc.getFinalDecomposition().getMode().isMultiplicative();
        TsData siData;
        if (doc instanceof X13Document) {
            siData = DocumentManager.instance.getTs(doc, "decomposition.d-tables.d8").getTsData();
        } else {
            TsData seas = decomposition.getData(ModellingDictionary.S_CMP, TsData.class);
            TsData i = decomposition.getData(ModellingDictionary.I_CMP, TsData.class);

            if (multiplicative) {
                siData = TsData.multiply(seas, i);
            } else {
                siData = TsData.add(seas, i);
            }
        }
        siData = InPercent.convertTsDataInPercentIfMult(siData, multiplicative);

        if (siData.getDomain().getYearsCount() > 10) {
            TsDomain domMax10years = new TsDomain(siData.getEnd().minus(frequency * 10), frequency * 10);
            siData = siData.fittoDomain(domMax10years);
        }
        TsDomain domain = new TsDomain(siData.getEnd().minus(frequency), frequency);
        si = TsFactory.instance.createTs("SI", null, siData);

        if (doc.getPreprocessingPart() != null) {
            OutlierEstimation[] prespecified = doc.getPreprocessingPart().outliersEstimation(true, true);
            OutlierEstimation[] estimations = doc.getPreprocessingPart().outliersEstimation(true, false);

            both = Arrays.copyOf(prespecified, prespecified.length + estimations.length);
            System.arraycopy(estimations, 0, both, prespecified.length, estimations.length);
        } else {
            both = new OutlierEstimation[0];
        }

        fixedOutliers = FixedOutlier.extractFixedOutliers(doc, frequency);

        if (doc instanceof X13Document) {
            Ts d9 = DocumentManager.instance.getTs(doc, "decomposition.d-tables.d9");
            d9 = InPercent.convertTsInPercentIfMult(d9, multiplicative);
            replacementValues = d9.getTsData();
        } else {
            //TODO
            replacementValues = null;
        }
        Ts seasonalFactorTemp = DocumentManager.instance.getTs(doc, SavedTables.COMPOSITE_RESULTS_SEASONAL);
        if (seasonalFactorTemp != null && seasonalFactorTemp.getTsData() != null) {
            seasonalFactor = InPercent.convertTsInPercentIfMult(seasonalFactorTemp, multiplicative);
            seasonalFactor.set(seasonalFactor.getTsData().fittoDomain(domain));
        } else {
            seasonalFactor = null;
        }

        Ts savedSeasonalFactorTemp = TsData_Saved.convertMetaDataToTs(doc.getMetaData(), SavedTables.SEASONALFACTOR);
        if (savedSeasonalFactorTemp != null && savedSeasonalFactorTemp.getTsData() != null && savedSeasonalFactorTemp.getTsData().getFrequency().intValue() != frequency) {
            savedSeasonalFactor = TsFactory.instance.createTs(savedSeasonalFactorTemp.getName());
            savedSeasonalFactor.setInvalidDataCause("Frequency mismatch");
        } else if (savedSeasonalFactorTemp != null && savedSeasonalFactorTemp.getTsData() != null) {
            TsDomain intersectionDomain = savedSeasonalFactorTemp.getTsData().getDomain().intersection(domain);
            savedSeasonalFactor = TsFactory.instance.createTs(savedSeasonalFactorTemp.getName(), savedSeasonalFactorTemp.getMetaData(), savedSeasonalFactorTemp.getTsData().fittoDomain(intersectionDomain));
        } else {
            savedSeasonalFactor = null;
        }

        valid = true;
    }

}

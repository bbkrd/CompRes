/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bbk.autoconcur;

import static de.bbk.autoconcur.Calculations.growthRate;
import de.bbk.concur.options.DatasourceUpdateOptionsPanel;
import de.bbk.concur.util.SavedTables;
import de.bbk.concur.util.TsData_Saved;
import ec.nbdemetra.sa.MultiProcessingDocument;
import ec.nbdemetra.sa.MultiProcessingManager;
import ec.nbdemetra.ws.IWorkspaceItemManager;
import ec.nbdemetra.ws.Workspace;
import ec.nbdemetra.ws.WorkspaceFactory;
import ec.nbdemetra.ws.WorkspaceItem;
import ec.tss.documents.DocumentManager;
import ec.tss.sa.SaItem;
import ec.tss.sa.SaProcessing;
import ec.tss.sa.documents.SaDocument;
import ec.tss.sa.documents.TramoSeatsDocument;
import ec.tstoolkit.MetaData;
import ec.tstoolkit.modelling.arima.ModelDescription;
import ec.tstoolkit.timeseries.TsPeriodSelector;
import ec.tstoolkit.timeseries.regression.IOutlierVariable;
import ec.tstoolkit.timeseries.regression.ITsVariable;
import ec.tstoolkit.timeseries.regression.TsVariableSelection;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.openide.util.NbPreferences;

/**
 *
 * @author Jan Gerhardt
 */
public class AutoConCur {

    public static final String CHECKPREVIOUS = "compres.checkprevious", OUTPUTFILE = "compres.outputfile", PARTIAL = "compres.partial", MANUAL = "compres.manual", CHECKSIGN = "compres.checksign", NSD = "compres.nsd", ND8 = "compres.nd8", NGROWTH = "compres.ngrowth", TOLD8 = "compres.told8", TOLGROWTH = "compres.tolgrowth", TRIM = "compres.trim";
    public static final String CHECKPREVIOUSDEFAULT = "1", PARTIALDEFAULT = "0", MANUALDEFAULT = "0", CHECKSIGNDEFAULT = "0", NSDDEFAULT = "2", ND8DEFAULT = "3", NGROWTHDEFAULT = "5", TOLD8DEFAULT = "0.05", TOLGROWTHDEFAULT = "1.0", TRIMDEFAULT = "0.05";
    private final Map<String, List<SaItem>> map;

    public AutoConCur() {
        this.map = new TreeMap<>();
        Workspace workspace = WorkspaceFactory.getInstance().getActiveWorkspace();
        IWorkspaceItemManager mgr = WorkspaceFactory.getInstance().getManager(MultiProcessingManager.ID);
        if (mgr != null) {
            List<WorkspaceItem<MultiProcessingDocument>> list = workspace.searchDocuments(mgr.getItemClass());
            list.stream().forEach((item) -> {
                SaProcessing saProcessing = item.getElement().getCurrent();
                map.put(item.getDisplayName(), saProcessing);
            });
        }
    }

    public AutoConCur(Map<String, List<SaItem>> map) {
        this.map = map;
    }

    public static DecisionBean decision(String title, SaDocument doc) {
        try {
            if (MetaData.isNullOrEmpty(doc.getMetaData())) {
                throw new IllegalArgumentException(title + " does not contain any metadata.");
            }
            if (doc instanceof TramoSeatsDocument) {
                return DecisionBean.ErrorBean(title, "Recommendation not implemented for TramoSeats.");
            }
            boolean checkPrevious = "1".equalsIgnoreCase(doc.getMetaData().getOrDefault(CHECKPREVIOUS, CHECKPREVIOUSDEFAULT).trim())
                    || "TRUE".equalsIgnoreCase(doc.getMetaData().getOrDefault(CHECKPREVIOUS, CHECKPREVIOUSDEFAULT).trim());
            boolean partial = "1".equalsIgnoreCase(doc.getMetaData().getOrDefault(PARTIAL, PARTIALDEFAULT).trim())
                    || "TRUE".equalsIgnoreCase(doc.getMetaData().getOrDefault(PARTIAL, PARTIALDEFAULT).trim());
            boolean manual = "1".equalsIgnoreCase(doc.getMetaData().getOrDefault(MANUAL, MANUALDEFAULT).trim())
                    || "TRUE".equalsIgnoreCase(doc.getMetaData().getOrDefault(MANUAL, MANUALDEFAULT).trim());
            boolean checkSign = "1".equalsIgnoreCase(doc.getMetaData().getOrDefault(CHECKSIGN, CHECKSIGNDEFAULT).trim())
                    || "TRUE".equalsIgnoreCase(doc.getMetaData().getOrDefault(CHECKSIGN, CHECKSIGNDEFAULT).trim());
            int nSD = Integer.parseInt(doc.getMetaData().getOrDefault(NSD, NSDDEFAULT));
            int nD8 = Integer.parseInt(doc.getMetaData().getOrDefault(ND8, ND8DEFAULT));
            int nGrowth = Integer.parseInt(doc.getMetaData().getOrDefault(NGROWTH, NGROWTHDEFAULT));
            double tolD8 = Double.parseDouble(doc.getMetaData().getOrDefault(TOLD8, TOLD8DEFAULT));
            double tolGrowth = Double.parseDouble(doc.getMetaData().getOrDefault(TOLGROWTH, TOLGROWTHDEFAULT));
            double trim = Double.parseDouble(doc.getMetaData().getOrDefault(TRIM, TRIMDEFAULT));
            DecisionBean bean = decision(title, doc, partial, manual, checkSign, nSD, nD8, nGrowth, tolD8, tolGrowth, trim, checkPrevious);
            if (doc.getMetaData().containsKey(OUTPUTFILE)) {
                bean.setFile(doc.getMetaData().getOrDefault(OUTPUTFILE, "").trim());
            }
            DecisionBeanCollector.add(bean);
            return bean;
        } catch (NumberFormatException e) {
            return DecisionBean.ErrorBean(title, "Incorrect metadata. " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return DecisionBean.ErrorBean(title, e.getMessage());
        } catch (Exception e) {
            return DecisionBean.ErrorBean(title, e.getMessage());
        }
    }

    private static DecisionBean decision(String title, SaDocument doc, boolean partial, boolean manual, boolean checkSign, int nSD, int n, int m, double w, double t, double trim, boolean previous) {
        if (nSD <= 0) {
            return DecisionBean.ErrorBean(title, "nSD must be positive.");
        }
        if (n <= 0) {
            return DecisionBean.ErrorBean(title, "nD8 must be positive.");
        }
        if (m <= 0) {
            return DecisionBean.ErrorBean(title, "nGrowth must be positive.");
        }
        if (w < 0 || w > 1) {
            return DecisionBean.ErrorBean(title, "tolD8 must be between 0 and 1.");
        }
        if (t <= 0) {
            return DecisionBean.ErrorBean(title, "tolGrowth must be positive.");
        }
        if (trim < 0 || trim >= 0.5) {
            return DecisionBean.ErrorBean(title, "trim must be non-negative and smaller 0.5.");
        }
        DecisionBean bean0 = decide(doc, title, partial, manual, checkSign, nSD, n, m, w, t, trim, 0);
        if (bean0.getDecision() != Decision.UNKNOWN && previous) {
            DecisionBean bean1 = decide(doc, title, partial, manual, checkSign, nSD, n, m, w, t, trim, 1);
            switch (bean1.getDecision()) {
                case UNKNOWN:
                    return bean1;
                case CHECK:
                    bean0.setDecision(Decision.CHECK);
                    break;

                case UPDATE:
                    if (bean0.getDecision().equals(Decision.KEEP)) {
                        bean0.setDecision(Decision.UPDATE);
                        break;
                    }
            }
            bean0.setPreperiodbean(bean1);
        }
        return bean0;
    }

    private static TsData checkTsData(TsData tsdata, int preperiod, String error) throws Exception {
        if (tsdata == null || tsdata.isEmpty()) {
            throw new Exception(error);
        } else {//if (preperiod != 0) {
            tsdata = tsdata.drop(0, preperiod);
            return tsdata;
        }
    }

    private static DecisionBean decide(SaDocument doc, String name, boolean partial, boolean manual, boolean checkSign, int nSD, int nD8, int nGrowth, double tolD8, double tolGrowth, double trim, int preperiod) {
        try {
            DecisionBean bean = new DecisionBean(name, manual, checkSign, nSD, nD8, nGrowth, tolD8, tolGrowth, trim);
            //Get Tables: 
            //A1(unadjusted ts), D8(SI ratios), D9(extreme values), D10(new SF), D11(seasonally adjusted ts), Seasonal Factors, Calendar Factors, Growth rates(old), Growth rates(new)
            TsData a1, d8, d9, d10, d11, tsSeasonsalFactor, tsCalendarFactor;
            a1 = DocumentManager.instance.getTs(doc, "decomposition.a-tables.a1").getTsData();
            d8 = DocumentManager.instance.getTs(doc, "decomposition.d-tables.d8").getTsData();
            d9 = DocumentManager.instance.getTs(doc, "decomposition.d-tables.d9").getTsData();
            d10 = DocumentManager.instance.getTs(doc, "decomposition.d-tables.d10").getTsData();
            d11 = DocumentManager.instance.getTs(doc, "decomposition.d-tables.d11").getTsData();

            tsSeasonsalFactor = TsData_Saved.convertMetaDataToTs(doc.getMetaData(), SavedTables.SEASONALFACTOR).getTsData();
            tsCalendarFactor = TsData_Saved.convertMetaDataToTs(doc.getMetaData(), SavedTables.CALENDARFACTOR).getTsData();

            a1 = checkTsData(a1, preperiod, name + " contains empty time series A1 in the decomposition.");
            d8 = checkTsData(d8, preperiod, name + " contains empty time series D8 in the decomposition.");
            d9 = checkTsData(d9, preperiod, name + " contains empty time series D9 in the decomposition.");
            d10 = checkTsData(d10, preperiod, name + " contains empty time series D10 in the decomposition.");
            d11 = checkTsData(d11, preperiod, name + " contains empty time series D11 in the decomposition.");
            tsSeasonsalFactor = checkTsData(tsSeasonsalFactor, preperiod, name + " contains no seasonal factors.");
            if (NbPreferences.forModule(DatasourceUpdateOptionsPanel.class).getBoolean(DatasourceUpdateOptionsPanel.CONST_SF, false)
                    && tsSeasonsalFactor.stream().mapToDouble(x -> x.getValue()).distinct().count() == 1) {
                bean.setDecision(Decision.KEEP);
                bean.setGrowthNew(Double.NaN);
                bean.setGrowthOld(Double.NaN);
                bean.setLastSF(tsSeasonsalFactor.get(0));
                bean.setIntervalSF(new double[]{tsSeasonsalFactor.get(0), tsSeasonsalFactor.get(0)});
                return bean;
            }

            boolean multiplicative = doc.getFinalDecomposition().getMode().isMultiplicative();
            if (NbPreferences.forModule(DatasourceUpdateOptionsPanel.class).getBoolean(DatasourceUpdateOptionsPanel.MISSING_CF, false) && tsCalendarFactor == null) {
                tsCalendarFactor = new TsData(a1.getDomain(), multiplicative ? 100 : 0);
            } else {
                tsCalendarFactor = checkTsData(tsCalendarFactor, preperiod, name + " contains no calendar factors.");
            }

            if (a1.getLength() != d8.getLength()
                    || a1.getLength() != d9.getLength()
                    || a1.getLength() != d10.getLength()
                    || a1.getLength() != d11.getLength()) {
                throw new Exception(name + " requires time series of the same length.");
            } else if (a1.isEmpty()) {
                throw new Exception(name + " contains empty time series in the decomposition.");
            }

            TsFrequency freq = d8.getFrequency();
            int frequency = freq.intValue();

            //old seasonally adjusted ts
            TsData d11Old;      //calendar factors are longer than a1, so intersection will be used, which is a1's domain.
            if (multiplicative) {
                tsCalendarFactor = tsCalendarFactor.div(100);
                tsSeasonsalFactor = tsSeasonsalFactor.div(100);
                d11Old = a1.div(tsCalendarFactor).div(tsSeasonsalFactor);
            } else {
                d11Old = a1.minus(tsCalendarFactor).minus(tsSeasonsalFactor);
            }

            TsData growthOld = growthRate(d11Old);
            TsData growthNew = growthRate(d11);
            bean.setGrowthOld(growthOld.get(growthOld.getLength() - 1));
            bean.setGrowthNew(growthNew.get(growthNew.getLength() - 1));
            double diffGrowth = Math.abs(bean.getGrowthOld() - bean.getGrowthNew());

            //1. Fixed outlier
            boolean fixedOutlier = false;
            ModelDescription description = doc.getPreprocessingPart().description;
            TsVariableSelection.Item<ITsVariable>[] select = description.buildRegressionVariables().select(var -> var instanceof IOutlierVariable
                    && !description.isPrespecified((IOutlierVariable) var)).elements();
            TsPeriod last = a1.getDomain().getLast();
            for (TsVariableSelection.Item<ITsVariable> var : select) {
                if (last.equals(new TsPeriod(freq, ((IOutlierVariable) var.variable).getPosition()))) {
                    fixedOutlier = true;
                    break;
                }
            }
            bean.setFixOutlier(fixedOutlier);

//            //Large Movement
            //double lastGrowth = growthOld.internalStorage()[growthOld.getLength() - 1];
            //bean.setLastGrowth(lastGrowth);
            //double[] quantiles = Calculations.quantiles(lastMYears, tolD8);
            //bean.setQuantsGrowth(quantiles);
            //boolean largeMovement = lastGrowth > quantiles[1] || lastGrowth < quantiles[0];
            //bean.setDevelopment(largeMovement);
            //2. Extreme Value
            bean.setExtremevalue(!Double.isNaN(d9.internalStorage()[d9.getLength() - 1]));

            //3. sign change
            bean.setSignChange(Math.signum(bean.getGrowthNew()) != Math.signum(bean.getGrowthOld()));

            //4. GrowthRate
            TsPeriodSelector selector = new TsPeriodSelector();
            selector.last(nGrowth * frequency);
            TsData lastMYears = growthOld.select(selector);
            double meanTruncGrowthOld = Calculations.trimmedMean(lastMYears, trim);
            double stDevTruncGrowthOld = Calculations.trimmedStDev(lastMYears, trim);
            //ToDo: include mean and stDev in bean?
            if (bean.getGrowthOld() > (meanTruncGrowthOld + nSD * stDevTruncGrowthOld) || bean.getGrowthOld() < (meanTruncGrowthOld - nSD * stDevTruncGrowthOld)) {
                bean.setGrowthRate(true);
            }

            //Last nD8 non-extreme SI-ratios (seasonal D8)
            List<Double> seasonD8 = new ArrayList<>();
            for (int i = d8.getLength() - 1; i >= 0 && seasonD8.size() < nD8; i -= frequency) {
                if (Double.isNaN(d9.get(i))) {
                    seasonD8.add(d8.get(i));
                }
            }
            //No sublist needed since seasonD8.size()<=n
            seasonD8 = seasonD8.stream().filter(d -> !Double.isNaN(d)).collect(Collectors.toList());
            double minSF = Collections.min(seasonD8);
            double maxSF = Collections.max(seasonD8);
            double lastSF = tsSeasonsalFactor.get(a1.getLastPeriod());
            double lastD10 = d10.get(d10.getLength() - 1);
            if (multiplicative) {
                minSF = minSF * 100.0;
                maxSF = maxSF * 100.0;
                lastSF = lastSF * 100.0;
                lastD10 = lastD10 * 100.0;
            }
            bean.setIntervalSF(new double[]{minSF, maxSF});
            bean.setLastSF(lastSF);
            bean.setLastD10(lastD10);
            if (partial || lastSF > maxSF + tolD8 || lastSF < minSF - tolD8) {
                if ((lastD10 <= maxSF + tolD8 && lastD10 >= minSF - tolD8)) {
                    if (partial || diffGrowth >= tolGrowth) {
                        if (isGrowthRateOrSign(bean) || bean.isExtremevalue() || bean.isFixOutlier()) {
                            bean.setDecision(Decision.CHECK);
                            return bean;
                        } else {
                            bean.setDecision(Decision.UPDATE);
                            return bean;
                        }
                    }
                } else {
                    bean.setSeasonalFactor(true);
                    bean.setDecision(Decision.CHECK);
                    return bean;
                }
            }
            if (isGrowthRateOrSign(bean)) {
                bean.setDecision(Decision.CHECK);
            } else {
                bean.setDecision(Decision.KEEP);
            }
            return bean;

        } catch (Exception e) {
            return DecisionBean.ErrorBean(name, e.getMessage());
        }
    }

    private static boolean isGrowthRateOrSign(DecisionBean bean) {
        return bean.isGrowthRate() || (bean.isCheckSign() && bean.isSignChange());
    }
}

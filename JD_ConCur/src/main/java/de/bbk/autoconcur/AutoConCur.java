/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bbk.autoconcur;

import static de.bbk.autoconcur.Calculations.growthRate;
import static de.bbk.autoconcur.Calculations.quantiles;
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
import ec.tstoolkit.MetaData;
import ec.tstoolkit.timeseries.TsPeriodSelector;
import ec.tstoolkit.timeseries.simplets.TsData;
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

    public static final String ND8 = "compres.nd8", NGROWTH = "compres.ngrowth", TRIM = "compres.trim", TOLGROWTH = "compres.tolgrowth";
    public static final String ND8DEFAULT = "3", NGROWTHDEFAULT = "10", TRIMDEFAULT = "0.05", TOLGROWTHDEFAULT = "1.0";
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

    public List<DecisionBean> makeDecisions() {
        List<DecisionBean> decisionBeans = new ArrayList<>();
        map.values().forEach(itemlist -> {
            itemlist.forEach(item -> {
                decisionBeans.add(decision(item.getName(), item.toDocument()));
            });
        });
        return decisionBeans;
    }

    public static DecisionBean decision(String title, SaDocument doc) {
        try {
            if (MetaData.isNullOrEmpty(doc.getMetaData())) {
                throw new IllegalArgumentException(title + " does not contain any metadata.");
            }
            int nD8 = Integer.parseInt(doc.getMetaData().getOrDefault(ND8, ND8DEFAULT));
            int nGrowth = Integer.parseInt(doc.getMetaData().getOrDefault(NGROWTH, NGROWTHDEFAULT));
            double trim = Double.parseDouble(doc.getMetaData().getOrDefault(TRIM, TRIMDEFAULT));
            double tolerance = Double.parseDouble(doc.getMetaData().getOrDefault(TOLGROWTH, TOLGROWTHDEFAULT));
            return decision(title, doc, nD8, nGrowth, trim, tolerance);
        } catch (NumberFormatException e) {
            return DecisionBean.ErrorBean(title, "Incorrect metadata. " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return DecisionBean.ErrorBean(title, e.getMessage());
        } catch (Exception e) {
            return DecisionBean.ErrorBean(title, e.getMessage());
        }
    }

    private static DecisionBean decision(String title, SaDocument doc, int n, int m, double w, double t) {
        if (n <= 0) {
            return DecisionBean.ErrorBean(title, "nD8 must be positive.");
        }
        if (m <= 0) {
            return DecisionBean.ErrorBean(title, "nGrowth must be positive.");
        }
        if (w < 0 || w > 1) {
            return DecisionBean.ErrorBean(title, "trim must be between 0 and 1.");
        }
        if (t <= 0) {
            return DecisionBean.ErrorBean(title, "tolGrowth must be positive.");
        }
        DecisionBean bean0 = decide(doc, title, n, m, w, t, 0);
        if (bean0.getDecision() != Decision.UNKNOWN) {
            DecisionBean bean1 = decide(doc, title, n, m, w, t, 1);
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

    private static DecisionBean decide(SaDocument doc, String name, int n, int m, double w, double t, int preperiod) {
        try {
            DecisionBean bean = new DecisionBean(name, n, m, w, t);
            //Get Tables: A1, D8, D9, D10, Seasonal Factors, Calendar Factors, Growth rates(old), Growth rates(new)
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

            int frequency = d8.getFrequency().intValue();

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
            double diffGrowth = Math.abs(growthOld.get(growthOld.getLength() - 1) - growthNew.get(growthNew.getLength() - 1));

            TsPeriodSelector selector = new TsPeriodSelector();
            selector.last(m * frequency);
            TsData lastMYears = growthOld.select(selector);
            double[] quantiles = quantiles(lastMYears, w);
            bean.setQuantsGrowth(quantiles);

            //Set development and extreme value for bean
            double lastGrowth = growthOld.internalStorage()[growthOld.getLength() - 1];
            bean.setLastGrowth(lastGrowth);
            boolean largeMovement = lastGrowth > quantiles[1] || lastGrowth < quantiles[0];
            boolean extremeValue = !Double.isNaN(d9.internalStorage()[d9.getLength() - 1]);

            bean.setDevelopment(largeMovement);
            bean.setExtremevalue(extremeValue);

            List<Double> seasonD8 = new ArrayList<>();

            for (int i = d8.getLength() - 1; i >= 0 && seasonD8.size() < n; i -= frequency) {
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
            if (lastSF <= maxSF && lastSF >= minSF) {
                bean.setDecision(Decision.KEEP);
                return bean;
            }

            bean.setLastD10(lastD10);
            if (lastD10 > maxSF || lastD10 < minSF) {
                bean.setDecision(Decision.CHECK);
                return bean;
            }

            if (diffGrowth < t) {
                bean.setDecision(Decision.KEEP);
                return bean;
            } else {
                bean.setDecision(Decision.UPDATE);
                return bean;
            }
        } catch (Exception e) {
            return DecisionBean.ErrorBean(name, e.getMessage());
        }
    }
}

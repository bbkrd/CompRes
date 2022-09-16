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
package de.bbk.concur;

import de.bbk.concur.util.SIViewSaved;
import de.bbk.concur.util.SavedTables;
import de.bbk.concur.view.*;
import ec.satoolkit.tramoseats.TramoSeatsSpecification;
import ec.tss.documents.DocumentManager;
import ec.tss.sa.documents.TramoSeatsDocument;
import ec.tstoolkit.modelling.ModellingDictionary;
import ec.tstoolkit.modelling.SeriesInfo;
import ec.tstoolkit.utilities.DefaultInformationExtractor;
import ec.tstoolkit.utilities.Id;
import ec.tstoolkit.utilities.InformationExtractor;
import ec.tstoolkit.utilities.LinearId;
import ec.ui.view.tsprocessing.*;
import ec.ui.view.tsprocessing.sa.SaDocumentViewFactory;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Thomas Witthohn
 */
public class BbkSeatsViewFactory extends SaDocumentViewFactory<TramoSeatsSpecification, TramoSeatsDocument> {

    public static final String BBK = "ConCur";
    public static final String CCA = "CCA";
    public static final String SA = "SA";
    public static final String ONLYSA = "Only SA";
    public static final String OLD = "Old";
    public static final String PERCENTAGECHANGE = "Percentage Change";
    public static final Id BBK_MAIN = new LinearId(BBK);
    public static final Id BBK_CHARTS = new LinearId(BBK, CHARTS);
    public static final Id BBK_CHARTS_SA = new LinearId(BBK, CHARTS, SA);
    public static final Id BBK_CHARTS_ONLYSA = new LinearId(BBK, CHARTS, ONLYSA);
    public static final Id BBK_CHARTS_SIRATIO = new LinearId(BBK, CHARTS, SI_RATIO);
    public static final Id BBK_CHARTS_SEASONAL = new LinearId(BBK, CHARTS, SEASONAL);
    public static final Id BBK_CCA = new LinearId(BBK, CCA);
    public static final Id BBK_TABLE = new LinearId(BBK, TABLE);
    public static final Id BBK_TABLE_SERIES = new LinearId(BBK, TABLE, SERIES);
    public static final Id BBK_TABLE_PERCENTAGECHANGE = new LinearId(BBK, TABLE, PERCENTAGECHANGE);

    @Override
    public Id getPreferredView() {
        return BBK_MAIN;
    }

    @ServiceProvider(service = ProcDocumentItemFactory.class, position = 220000)
    public static class BBKOutputFactory extends ItemFactory<TramoSeatsDocument> {

        public BBKOutputFactory() {
            super(BBK_MAIN, new DefaultInformationExtractor<TramoSeatsDocument, TramoSeatsDocument>() {
                @Override
                public TramoSeatsDocument retrieve(TramoSeatsDocument source) {
                    return source;
                }
            }, new PooledItemUI<View, TramoSeatsDocument, MainBBKResultsView>(MainBBKResultsView.class) {
                @Override
                protected void init(MainBBKResultsView c, View host, TramoSeatsDocument information) {
                    c.setTsToolkit(host.getToolkit());
                    c.set(information);
                }
            });
        }
    }

    @ServiceProvider(service = ProcDocumentItemFactory.class, position = 220010)
    public static class ChartSAFactory extends ItemFactory<TramoSeatsDocument> {

        private static String[] generateItems() {
            StringBuilder y = new StringBuilder();
            y.append(DocumentManager.COMPOSITE).append(SavedTables.NAME_SERIES + "=,").append(ModellingDictionary.Y)
                    .append(',').append(ModellingDictionary.Y).append(SeriesInfo.F_SUFFIX);
            StringBuilder t = new StringBuilder();
            t.append(DocumentManager.COMPOSITE).append(SavedTables.NAME_TREND + "=,").append(ModellingDictionary.T)
                    .append(',').append(ModellingDictionary.T).append(SeriesInfo.F_SUFFIX);
            StringBuilder sa = new StringBuilder();
            sa.append(DocumentManager.COMPOSITE).append(SavedTables.NAME_SEASONALLY_ADJUSTED + "=,").append(ModellingDictionary.SA)
                    .append(',').append(ModellingDictionary.SA).append(SeriesInfo.F_SUFFIX);
            StringBuilder saOld = new StringBuilder();
            saOld.append(OLD).append(ModellingDictionary.SA);
            return new String[]{y.toString(), t.toString(), sa.toString(), saOld.toString()};
        }

        public ChartSAFactory() {
            super(BBK_CHARTS_SA, new DefaultInformationExtractor<TramoSeatsDocument, TramoSeatsDocument>() {
                @Override
                public TramoSeatsDocument retrieve(TramoSeatsDocument source) {
                    return source;
                }
            }, new PooledItemUI<View, TramoSeatsDocument, SAView>(SAView.class) {
                @Override
                protected void init(SAView c, View host, TramoSeatsDocument information) {
                    c.setNames(generateItems());
                    c.set(information);
                }
            });
        }
    }

    @ServiceProvider(service = ProcDocumentItemFactory.class, position = 220011)
    public static class ChartOnlySAFactory extends ItemFactory<TramoSeatsDocument> {

        private static String[] generateItems() {
            StringBuilder y = new StringBuilder();
            y.append(DocumentManager.COMPOSITE).append(SavedTables.NAME_SERIES + "=,").append(ModellingDictionary.Y)
                    .append(',').append(ModellingDictionary.Y).append(SeriesInfo.F_SUFFIX);
            StringBuilder sa = new StringBuilder();
            sa.append(DocumentManager.COMPOSITE).append(SavedTables.NAME_SEASONALLY_ADJUSTED + "=,").append(ModellingDictionary.SA)
                    .append(',').append(ModellingDictionary.SA).append(SeriesInfo.F_SUFFIX);
            StringBuilder t = new StringBuilder();
            t.append(DocumentManager.COMPOSITE).append(SavedTables.NAME_TREND + "=,").append(ModellingDictionary.T)
                    .append(',').append(ModellingDictionary.T).append(SeriesInfo.F_SUFFIX);
            return new String[]{y.toString(), sa.toString(), t.toString()};
        }

        public ChartOnlySAFactory() {
            super(BBK_CHARTS_ONLYSA, new DefaultInformationExtractor<TramoSeatsDocument, TramoSeatsDocument>() {
                @Override
                public TramoSeatsDocument retrieve(TramoSeatsDocument source) {
                    return source;
                }
            }, new PooledItemUI<View, TramoSeatsDocument, OnlySAView>(OnlySAView.class) {
                @Override
                protected void init(OnlySAView c, View host, TramoSeatsDocument information) {
                    c.setNames(generateItems());
                    c.set(information);
                }
            });
        }
    }

    @ServiceProvider(service = ProcDocumentItemFactory.class, position = 220012)
    public static class ChartSIRatioFactory extends ItemFactory<TramoSeatsDocument> {

        public ChartSIRatioFactory() {
            super(BBK_CHARTS_SIRATIO, new DefaultInformationExtractor<TramoSeatsDocument, TramoSeatsDocument>() {
                @Override
                public TramoSeatsDocument retrieve(TramoSeatsDocument source) {
                    return source;
                }
            }, new PooledItemUI<View, TramoSeatsDocument, SIViewSaved>(SIViewSaved.class) {
                @Override
                protected void init(SIViewSaved c, View host, TramoSeatsDocument information) {
                    c.setDoc(information);
                }
            });
        }
    }

    @ServiceProvider(service = ProcDocumentItemFactory.class, position = 220013)
    public static class ChartSeasonalFactory extends ItemFactory<TramoSeatsDocument> {

        public ChartSeasonalFactory() {
            super(BBK_CHARTS_SEASONAL, new DefaultInformationExtractor<TramoSeatsDocument, TramoSeatsDocument>() {
                @Override
                public TramoSeatsDocument retrieve(TramoSeatsDocument source) {
                    return source;
                }
            }, new PooledItemUI<View, TramoSeatsDocument, SeasonalView>(SeasonalView.class) {
                @Override
                protected void init(SeasonalView c, View host, TramoSeatsDocument information) {
                    c.set(information);
                }
            });
        }
    }

    //<editor-fold defaultstate="collapsed" desc="REGISTER CCA">
    @ServiceProvider(service = ProcDocumentItemFactory.class, position = 220020)
    public static class CCAFactory extends ItemFactory<TramoSeatsDocument> {

        public CCAFactory() {
            super(BBK_CCA, new DefaultInformationExtractor<TramoSeatsDocument, TramoSeatsDocument>() {
                @Override
                public TramoSeatsDocument retrieve(TramoSeatsDocument source) {
                    return source;
                }
            }, new PooledItemUI<View, TramoSeatsDocument, CCAView>(CCAView.class) {
                @Override
                protected void init(CCAView c, View host, TramoSeatsDocument information) {
                    c.setTsToolkit(host.getToolkit());
                    c.set(information);
                }
            });
        }
    }

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="REGISTER TablesSeries">
    @ServiceProvider(service = ProcDocumentItemFactory.class, position = 220020)
    public static class TablesSeriesViewFactory extends ItemFactory<TramoSeatsDocument> {

        public TablesSeriesViewFactory() {
            super(BBK_TABLE_SERIES, new DefaultInformationExtractor<TramoSeatsDocument, TramoSeatsDocument>() {
                @Override
                public TramoSeatsDocument retrieve(TramoSeatsDocument source) {
                    return source;
                }
            }, new PooledItemUI<View, TramoSeatsDocument, TablesSeriesView>(TablesSeriesView.class) {
                @Override
                protected void init(TablesSeriesView c, View host, TramoSeatsDocument information) {
                    c.set(information);
                }
            });
        }
    }

    @ServiceProvider(service = ProcDocumentItemFactory.class, position = 220030)
    public static class TablesPercentageChangeViewFactory extends ItemFactory<TramoSeatsDocument> {

        public TablesPercentageChangeViewFactory() {
            super(BBK_TABLE_PERCENTAGECHANGE, new DefaultInformationExtractor<TramoSeatsDocument, TramoSeatsDocument>() {
                @Override
                public TramoSeatsDocument retrieve(TramoSeatsDocument source) {
                    return source;
                }
            }, new PooledItemUI<View, TramoSeatsDocument, TablesPercentageChangeView>(TablesPercentageChangeView.class) {
                @Override
                protected void init(TablesPercentageChangeView c, View host, TramoSeatsDocument information) {
                    c.set(information);
                }
            });
        }
    }

    //</editor-fold>
    private static class ItemFactory<I> extends ComposedProcDocumentItemFactory<TramoSeatsDocument, I> {

        ItemFactory(Id itemId, InformationExtractor<? super TramoSeatsDocument, I> informationExtractor, ItemUI<? extends IProcDocumentView<TramoSeatsDocument>, I> itemUI) {
            super(TramoSeatsDocument.class, itemId, informationExtractor, itemUI);
        }
    }

}

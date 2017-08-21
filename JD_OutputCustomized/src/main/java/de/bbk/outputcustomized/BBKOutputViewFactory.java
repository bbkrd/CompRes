/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bbk.outputcustomized;

import de.bbk.outputcustomized.util.SavedTables;
import de.bbk.outputcustomized.view.*;
import ec.satoolkit.x13.X13Specification;
import ec.tss.documents.DocumentManager;
import ec.tss.sa.documents.X13Document;
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
public class BBKOutputViewFactory extends SaDocumentViewFactory<X13Specification, X13Document> {

    public static final String BBK = "Customized Output";
    public static final String CCA = "CCA (multiplicative in pct.)";
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

    //<editor-fold defaultstate="collapsed" desc="REGISTER MAIN">
    @ServiceProvider(service = ProcDocumentItemFactory.class, position = 220000)
    public static class BBKOutputFactory extends ItemFactory<X13Document> {

        public BBKOutputFactory() {
            super(BBK_MAIN, new DefaultInformationExtractor<X13Document, X13Document>() {
              @Override
              public X13Document retrieve(X13Document source) {
                  return source;
              }
          }, new PooledItemUI<View, X13Document, MainBBKResultsView>(MainBBKResultsView.class) {
              @Override
              protected void init(MainBBKResultsView c, View host, X13Document information) {
                  c.setTsToolkit(host.getToolkit());
                  c.set(information);
              }
          });
        }
    }

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="REGISTER CHARTS">
    @ServiceProvider(service = ProcDocumentItemFactory.class, position = 220010)
    public static class ChartSAFactory extends ItemFactory<X13Document> {

        private static String[] generateItems() {
            StringBuilder y = new StringBuilder();
            y.append(DocumentManager.COMPOSITE).append(SavedTables.NAME_SERIES+"=,").append(ModellingDictionary.Y)
                    .append(',').append(ModellingDictionary.Y).append(SeriesInfo.F_SUFFIX);
            StringBuilder t = new StringBuilder();
            t.append(DocumentManager.COMPOSITE).append(SavedTables.NAME_TREND+"=,").append(ModellingDictionary.T)
                    .append(',').append(ModellingDictionary.T).append(SeriesInfo.F_SUFFIX);
            StringBuilder sa = new StringBuilder();
            sa.append(DocumentManager.COMPOSITE).append(SavedTables.NAME_SEASONALLY_ADJUSTED+"=,").append(ModellingDictionary.SA)
                    .append(',').append(ModellingDictionary.SA).append(SeriesInfo.F_SUFFIX);
            StringBuilder saOld = new StringBuilder();
            saOld.append(OLD).append(ModellingDictionary.SA);
            return new String[]{y.toString(), t.toString(), sa.toString(), saOld.toString()};
        }

        public ChartSAFactory() {
            super(BBK_CHARTS_SA, new DefaultInformationExtractor<X13Document, X13Document>() {
              @Override
              public X13Document retrieve(X13Document source) {
                  return source;
              }
          }, new PooledItemUI<View, X13Document, SAView>(SAView.class) {
              @Override
              protected void init(SAView c, View host, X13Document information) {
                  c.setNames(generateItems());
                  c.set(information);
              }
          });
        }
    }

    @ServiceProvider(service = ProcDocumentItemFactory.class, position = 220011)
    public static class ChartOnlySAFactory extends ItemFactory<X13Document> {

        private static String[] generateItems() {
            StringBuilder y = new StringBuilder();
            y.append(DocumentManager.COMPOSITE).append(SavedTables.NAME_SERIES+"=,").append(ModellingDictionary.Y)
                    .append(',').append(ModellingDictionary.Y).append(SeriesInfo.F_SUFFIX);
            StringBuilder sa = new StringBuilder();
            sa.append(DocumentManager.COMPOSITE).append(SavedTables.NAME_SEASONALLY_ADJUSTED+"=,").append(ModellingDictionary.SA)
                    .append(',').append(ModellingDictionary.SA).append(SeriesInfo.F_SUFFIX);
                  StringBuilder t = new StringBuilder();
            t.append(DocumentManager.COMPOSITE).append(SavedTables.NAME_TREND+"=,").append(ModellingDictionary.T)
                    .append(',').append(ModellingDictionary.T).append(SeriesInfo.F_SUFFIX);
            return new String[]{y.toString(), sa.toString(),t.toString()};
        }

        public ChartOnlySAFactory() {
            super(BBK_CHARTS_ONLYSA, new DefaultInformationExtractor<X13Document, X13Document>() {
              @Override
              public X13Document retrieve(X13Document source) {
                  return source;
              }
          }, new PooledItemUI<View, X13Document, OnlySAView>(OnlySAView.class) {
              @Override
              protected void init(OnlySAView c, View host, X13Document information) {
                  c.setNames(generateItems());
                  c.set(information);
              }
          });
        }
    }

    @ServiceProvider(service = ProcDocumentItemFactory.class, position = 220012)
    public static class ChartSIRatioFactory extends ItemFactory<X13Document> {

        public ChartSIRatioFactory() {
            super(BBK_CHARTS_SIRATIO, new DefaultInformationExtractor<X13Document, X13Document>() {
              @Override
              public X13Document retrieve(X13Document source) {
                  return source;
              }
          }, new PooledItemUI<View, X13Document, SIRatioView>(SIRatioView.class) {
              @Override
              protected void init(SIRatioView c, View host, X13Document information) {
                  c.set(information);
              }
          });
        }
    }

    @ServiceProvider(service = ProcDocumentItemFactory.class, position = 220013)
    public static class ChartSeasonalFactory extends ItemFactory<X13Document> {

        public ChartSeasonalFactory() {
            super(BBK_CHARTS_SEASONAL, new DefaultInformationExtractor<X13Document, X13Document>() {
              @Override
              public X13Document retrieve(X13Document source) {
                  return source;
              }
          }, new PooledItemUI<View, X13Document, SeasonalView>(SeasonalView.class) {
              @Override
              protected void init(SeasonalView c, View host, X13Document information) {
                  c.set(information);
              }
          });
        }
    }

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="REGISTER CCA">
    @ServiceProvider(service = ProcDocumentItemFactory.class, position = 220020)
    public static class CCAFactory extends ItemFactory<X13Document> {

        public CCAFactory() {
            super(BBK_CCA, new DefaultInformationExtractor<X13Document, X13Document>() {
              @Override
              public X13Document retrieve(X13Document source) {
                  return source;
              }
          }, new PooledItemUI<View, X13Document, CCAView>(CCAView.class) {
              @Override
              protected void init(CCAView c, View host, X13Document information) {
                  c.setTsToolkit(host.getToolkit());
                  c.set(information);
              }
          });
        }
    }

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="REGISTER TablesSeries">
    @ServiceProvider(service = ProcDocumentItemFactory.class, position = 220020)
    public static class TablesSeriesViewFactory extends ItemFactory<X13Document> {

        public TablesSeriesViewFactory() {
            super(BBK_TABLE_SERIES, new DefaultInformationExtractor<X13Document, X13Document>() {
              @Override
              public X13Document retrieve(X13Document source) {
                  return source;
              }
          }, new PooledItemUI<View, X13Document, TablesSeriesView>(TablesSeriesView.class) {
              @Override
              protected void init(TablesSeriesView c, View host, X13Document information) {
                  c.set(information);
              }
          });
        }
    }

    @ServiceProvider(service = ProcDocumentItemFactory.class, position = 220030)
    public static class TablesPercentageChangeViewFactory extends ItemFactory<X13Document> {

        public TablesPercentageChangeViewFactory() {
            super(BBK_TABLE_PERCENTAGECHANGE, new DefaultInformationExtractor<X13Document, X13Document>() {
              @Override
              public X13Document retrieve(X13Document source) {
                  return source;
              }
          }, new PooledItemUI<View, X13Document, TablesPercentageChangeView>(TablesPercentageChangeView.class) {
              @Override
              protected void init(TablesPercentageChangeView c, View host, X13Document information) {
                  c.set(information);
              }
          });
        }
    }

    //</editor-fold>
    private static class ItemFactory<I> extends ComposedProcDocumentItemFactory<X13Document, I> {

        public ItemFactory(Id itemId, InformationExtractor<? super X13Document, I> informationExtractor, ItemUI<? extends IProcDocumentView<X13Document>, I> itemUI) {
            super(X13Document.class, itemId, informationExtractor, itemUI);
        }
    }

}

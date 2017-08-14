/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bbk.outputcustomized.view;

import de.bbk.outputcustomized.html.HtmlBBKSummary;
import static de.bbk.outputcustomized.util.SavedTables.*;
import de.bbk.outputcustomized.util.SeasonallyAdjusted_Saved;
import de.bbk.outputcustomized.util.FixTimeDomain;
import ec.nbdemetra.ui.NbComponents;
import ec.satoolkit.DecompositionMode;
import ec.tss.Ts;
import ec.tss.documents.DocumentManager;
import ec.tss.sa.documents.X13Document;
import ec.tss.tsproviders.utils.MultiLineNameUtil;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.ui.Disposables;
import ec.ui.chart.JTsChart;
import ec.ui.interfaces.IDisposable;
import ec.ui.interfaces.ITsCollectionView;
import ec.ui.view.tsprocessing.ITsViewToolkit;
import ec.ui.view.tsprocessing.TsViewToolkit;
import java.awt.BorderLayout;
import java.util.Arrays;
import java.util.List;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JSplitPane;

/**
 *
 * @author Thomas Witthohn
 */
public class MainBBKResultsView extends JComponent implements IDisposable {

    private transient ITsViewToolkit toolkit = TsViewToolkit.getInstance();
    private final Box document;
    private final JTsChart chart;
    private X13Document doc;
    private FixTimeDomain td;

    public MainBBKResultsView() {
        setLayout(new BorderLayout());

        this.chart = new JTsChart();
        chart.setTsUpdateMode(ITsCollectionView.TsUpdateMode.None);

        this.document = Box.createHorizontalBox();

        JSplitPane split = NbComponents.newJSplitPane(JSplitPane.VERTICAL_SPLIT, document, chart);
        split.setDividerLocation(0.5);
        split.setResizeWeight(.5);

        add(split, BorderLayout.CENTER);
    }

    public void setTsToolkit(ITsViewToolkit toolkit) {
        this.toolkit = toolkit;
    }

    public void set(X13Document doc) {
        this.doc = doc;
        if (doc == null) {
            return;
        }
        CompositeResults results = doc.getResults();
        if (results == null) {
            return;
        }

        HtmlBBKSummary summary = new HtmlBBKSummary(MultiLineNameUtil.join(doc.getInput().getName()), doc);
        Disposables.disposeAndRemoveAll(document).add(toolkit.getHtmlViewer(summary));

        td = new FixTimeDomain(getMainSeries(COMPOSITE_RESULTS_SERIES_WITH_FORECAST));
        List<Ts> list = Arrays.asList(
                getMainSeriesLast5Years(COMPOSITE_RESULTS_SERIES_WITH_FORECAST),
                getMainSeriesLast5Years(COMPOSITE_RESULTS_TREND_WITH_FORECAST),
                getMainSeriesLast5Years(COMPOSITE_RESULTS_SEASONALLY_ADJUSTED_WITH_FORECAST));
        chart.getTsCollection().replace(list);

        Ts savedSA = SeasonallyAdjusted_Saved.calcSeasonallyAdjusted(doc);
        chart.getTsCollection().add(td.getTsWithDomain(savedSA));

    }

    private Ts getMainSeries(String str) {
        return DocumentManager.instance.getTs(doc, str);
    }

    private Ts getMainSeriesLast5Years(String str) {
        Ts t = getMainSeries(str);
        return td.getTsWithDomain(t);
       
    }

    @Override
    public void dispose() {
        doc = null;
        chart.dispose();
        Disposables.disposeAndRemoveAll(document);
    }

}

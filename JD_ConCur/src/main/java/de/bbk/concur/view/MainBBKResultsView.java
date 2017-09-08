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
package de.bbk.concur.view;

import de.bbk.concur.html.HtmlBBKSummary;
import de.bbk.concur.util.FixTimeDomain;
import static de.bbk.concur.util.SavedTables.*;
import de.bbk.concur.util.SeasonallyAdjusted_Saved;
import ec.nbdemetra.ui.NbComponents;
import ec.tss.Ts;
import ec.tss.documents.DocumentManager;
import ec.tss.sa.documents.X13Document;
import ec.tss.tsproviders.utils.MultiLineNameUtil;
import ec.tstoolkit.algorithm.CompositeResults;
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

        td = new FixTimeDomain(getMainSeries(COMPOSITE_RESULTS_SERIES));
        List<Ts> list = Arrays.asList(
                getMainSeriesLast5Years(COMPOSITE_RESULTS_SERIES),
                getMainSeriesLast5Years(COMPOSITE_RESULTS_TREND),
                getMainSeriesLast5Years(COMPOSITE_RESULTS_SEASONALLY_ADJUSTED));
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

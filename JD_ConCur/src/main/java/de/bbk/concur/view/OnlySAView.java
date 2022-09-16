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

import de.bbk.concur.util.SavedTables;
import ec.satoolkit.DecompositionMode;
import ec.satoolkit.x11.X11Kernel;
import ec.tss.Ts;
import ec.tss.TsCollection;
import ec.tss.TsFactory;
import ec.tss.documents.DocumentManager;
import ec.tss.sa.documents.SaDocument;
import ec.tss.sa.documents.X13Document;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.ui.chart.JTsChart;
import ec.ui.interfaces.IDisposable;
import ec.ui.interfaces.ITsCollectionView;
import java.awt.BorderLayout;
import java.util.Arrays;
import javax.swing.JComponent;

/**
 *
 * @author Thomas Witthohn
 */
public class OnlySAView extends JComponent implements IDisposable {

    private final JTsChart chart;
    private String[] names;

    public OnlySAView() {
        setLayout(new BorderLayout());

        this.chart = new JTsChart();
        chart.setTsUpdateMode(ITsCollectionView.TsUpdateMode.None);

        add(chart, BorderLayout.CENTER);
    }

    public void setNames(String... names) {
        this.names = names;
    }

    public void set(SaDocument doc) {
        if (doc == null) {
            return;
        }

        chart.getTsCollection().clear();
        if (doc.getDecompositionPart() != null && doc.getFinalDecomposition() != null) {

            TsCollection items = DocumentManager.create(Arrays.asList(names), doc);
            chart.getTsCollection().append(items);

            TsData y = DocumentManager.instance.getTs(doc, SavedTables.COMPOSITE_RESULTS_SERIES_WITH_FORECAST).getTsData();
            TsData s_cmp = DocumentManager.instance.getTs(doc, SavedTables.COMPOSITE_RESULTS_SEASONAL_WITH_FORECAST).getTsData();
            if (doc instanceof X13Document) {
                TsData d10 = doc.getDecompositionPart().getData(X11Kernel.D10, TsData.class);
                TsData d10a = doc.getDecompositionPart().getData(X11Kernel.D10a, TsData.class);
                s_cmp = d10.update(d10a);
            }
            TsData onlySAData;
            if (doc.getFinalDecomposition().getMode() != DecompositionMode.Additive) {
                onlySAData = y.div(s_cmp);
            } else {
                onlySAData = y.minus(s_cmp);
            }

            Ts onlySA = TsFactory.instance.createTs(SavedTables.NAME_ONLY_SEASONALLY_ADJUSTED, null, onlySAData);
            chart.getTsCollection().add(onlySA);
            chart.setTitle(((Ts) doc.getInput()).getRawName());
        }
    }

    @Override
    public void dispose() {
        chart.dispose();
    }
}

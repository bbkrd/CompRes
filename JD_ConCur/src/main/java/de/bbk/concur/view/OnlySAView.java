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

import static de.bbk.concur.util.SavedTables.NAME_ONLY_SEASONALLY_ADJUSTED;
import ec.satoolkit.DecompositionMode;
import ec.satoolkit.x11.X11Results;
import ec.tss.Ts;
import ec.tss.TsCollection;
import ec.tss.TsFactory;
import ec.tss.documents.DocumentManager;
import ec.tss.sa.documents.X13Document;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.modelling.ModellingDictionary;
import ec.tstoolkit.modelling.SeriesInfo;
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

    public void set(X13Document doc) {
        if (doc == null) {
            return;
        }

        X11Results x11 = doc.getDecompositionPart();
        if (x11 != null) {
            chart.getTsCollection().clear();
            TsCollection items = DocumentManager.create(Arrays.asList(names), doc);

            chart.getTsCollection().append(items);

            CompositeResults results = doc.getResults();
            TsData y = results.getData(ModellingDictionary.Y, TsData.class).update(results.getData(ModellingDictionary.Y + SeriesInfo.F_SUFFIX, TsData.class));
            TsData s_cmp = results.getData(ModellingDictionary.S_CMP, TsData.class).update(results.getData(ModellingDictionary.S_CMP + SeriesInfo.F_SUFFIX, TsData.class));

            TsData onlySAData;
            if (doc.getFinalDecomposition().getMode() != DecompositionMode.Additive) {
                onlySAData = y.div(s_cmp);
            } else {
                onlySAData = y.minus(s_cmp);
            }

            Ts onlySA = TsFactory.instance.createTs(NAME_ONLY_SEASONALLY_ADJUSTED, null, onlySAData);
            chart.getTsCollection().add(onlySA);
            chart.setTitle(doc.getInput().getRawName());
        } else {
            chart.getTsCollection().clear();
        }
    }

    @Override
    public void dispose() {
        chart.dispose();
    }
}

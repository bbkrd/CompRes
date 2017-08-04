/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bbk.outputcustomized.view;

import static de.bbk.outputcustomized.util.SavedTables.NAME_ONLY_SEASONALLY_ADJUSTED;
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

        } else {
            chart.getTsCollection().clear();
        }
    }

    @Override
    public void dispose() {
        chart.dispose();
    }
}

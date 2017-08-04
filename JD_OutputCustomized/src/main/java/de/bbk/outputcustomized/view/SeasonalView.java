/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bbk.outputcustomized.view;

import static de.bbk.outputcustomized.util.InPercent.convertTsInPercentIfMult;
import de.bbk.outputcustomized.util.SavedTables;
import static de.bbk.outputcustomized.util.SavedTables.DECOMPOSITION_D10_D10A;
import static de.bbk.outputcustomized.util.SavedTables.NAME_SEASONAL_FACTOR;
import de.bbk.outputcustomized.util.TsData_Saved;
import ec.satoolkit.DecompositionMode;
import ec.satoolkit.x11.X11Results;
import ec.tss.Ts;
import ec.tss.TsCollection;
import ec.tss.documents.DocumentManager;
import ec.tss.sa.documents.X13Document;
import ec.ui.chart.JTsChart;
import ec.ui.interfaces.IDisposable;
import ec.ui.interfaces.ITsCollectionView;
import java.awt.BorderLayout;
import javax.swing.JComponent;

/**
 *
 * @author Christiane Hofer
 */
public class SeasonalView extends JComponent implements IDisposable {

    private final TsCollection chartContent;

    public SeasonalView() {
        setLayout(new BorderLayout());

        JTsChart chart = new JTsChart();
        chart.setTsUpdateMode(ITsCollectionView.TsUpdateMode.None);
        chartContent = chart.getTsCollection();
        add(chart, BorderLayout.CENTER);
    }

    public void set(X13Document doc) {
        if (doc == null) {
            return;
        }

        X11Results x11 = doc.getDecompositionPart();
        DecompositionMode mode = doc.getDecompositionPart().getSeriesDecomposition().getMode();
        if (x11 != null) {
            chartContent.clear();

            Ts tsd10 = DocumentManager.instance.getTs(doc, DECOMPOSITION_D10_D10A, false);
            tsd10.rename(NAME_SEASONAL_FACTOR);
            tsd10 = convertTsInPercentIfMult(tsd10, mode.isMultiplicative());

            chartContent.add(tsd10);

            Ts seasonalfactor = TsData_Saved.convertMetaDataToTs(doc.getMetaData(), SavedTables.SEASONALFACTOR);

            chartContent.add(seasonalfactor);

        } else {
            chartContent.clear();
        }

    }

    @Override
    public void dispose() {
    }

}

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

import static de.bbk.concur.util.InPercent.convertTsInPercentIfMult;
import de.bbk.concur.util.SavedTables;
import static de.bbk.concur.util.SavedTables.DECOMPOSITION_D10_D10A;
import static de.bbk.concur.util.SavedTables.NAME_SEASONAL_FACTOR;
import static de.bbk.concur.util.SavedTables.NAME_SEASONAL_FACTOR_SAVED;
import de.bbk.concur.util.TsData_Saved;
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
    private final JTsChart chart;

    public SeasonalView() {
        setLayout(new BorderLayout());

        chart = new JTsChart();
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
            tsd10 = convertTsInPercentIfMult(tsd10, mode.isMultiplicative());
            tsd10 = tsd10.rename(SavedTables.NAME_SEASONAL_FACTOR);
            chartContent.add(tsd10);

            Ts seasonalfactor = TsData_Saved.convertMetaDataToTs(doc.getMetaData(), SavedTables.SEASONALFACTOR);
            seasonalfactor = seasonalfactor.rename(NAME_SEASONAL_FACTOR_SAVED);
            chartContent.add(seasonalfactor);

            chart.setTitle(doc.getTs().getRawName());

        } else {
            chartContent.clear();
        }

    }

    @Override
    public void dispose() {
    }

}

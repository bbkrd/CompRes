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
import static de.bbk.concur.util.SavedTables.*;
import de.bbk.concur.util.TsData_Saved;
import ec.satoolkit.DecompositionMode;
import ec.tss.Ts;
import ec.tss.TsCollection;
import ec.tss.sa.documents.SaDocument;
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

    public void set(SaDocument doc) {
        if (doc == null) {
            return;
        }
        chartContent.clear();

        if (doc.getFinalDecomposition() != null) {
            DecompositionMode mode = doc.getFinalDecomposition().getMode();

            Ts seasonalFactor = SavedTables.getSeasonalFactorWithForecast(doc, COMPOSITE_RESULTS_SEASONAL_D10_WITH_FORECAST);

            if (seasonalFactor != null && seasonalFactor.getTsData() != null) {
                seasonalFactor = convertTsInPercentIfMult(seasonalFactor, mode.isMultiplicative());
                chartContent.add(seasonalFactor);
            }

            Ts savedSeasonalfactor = TsData_Saved.convertMetaDataToTs(doc.getMetaData(), SEASONALFACTOR);
            savedSeasonalfactor = savedSeasonalfactor.rename(NAME_SEASONAL_FACTOR_SAVED);
            chartContent.add(savedSeasonalfactor);

            chart.setTitle(((Ts) doc.getInput()).getRawName());
        }

    }

    @Override
    public void dispose() {
        chart.dispose();
    }

}

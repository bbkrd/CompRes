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

import de.bbk.concur.options.DatasourceUpdateOptionsPanel;
import de.bbk.concur.util.InPercent;
import ec.tss.Ts;
import ec.tss.TsCollection;
import ec.tss.documents.DocumentManager;
import ec.tss.sa.documents.SaDocument;
import ec.ui.grid.JTsGrid;
import ec.ui.interfaces.IDisposable;
import ec.ui.interfaces.ITsCollectionView;
import ec.ui.interfaces.ITsGrid;
import java.awt.BorderLayout;
import javax.swing.JComponent;
import org.openide.util.NbPreferences;

/**
 *
 */
public class MatrixView extends JComponent implements IDisposable {

    private final TsCollection seriesGridContent;
    private final JTsGrid seriesGrid;

    public MatrixView() {
        setLayout(new BorderLayout());
        seriesGrid = new JTsGrid();
        seriesGrid.setTsUpdateMode(ITsCollectionView.TsUpdateMode.None);
        seriesGrid.setMode(ITsGrid.Mode.SINGLETS);
        seriesGridContent = seriesGrid.getTsCollection();
        add(seriesGrid, BorderLayout.CENTER);
    }

    public void set(SaDocument doc, boolean automatic) {
        seriesGridContent.clear();
        if (doc == null) {
            return;
        }
        boolean multiplicative;
        String names;
        if (automatic) {
            multiplicative = doc.getFinalDecomposition().getMode().isMultiplicative();
            names = NbPreferences.forModule(DatasourceUpdateOptionsPanel.class).get(DatasourceUpdateOptionsPanel.MATRIX_VIEW_TWO, DatasourceUpdateOptionsPanel.MATRIX_VIEW_TWO_DEFAULT);
        } else {
            multiplicative = false;
            names = NbPreferences.forModule(DatasourceUpdateOptionsPanel.class).get(DatasourceUpdateOptionsPanel.MATRIX_VIEW_ONE, DatasourceUpdateOptionsPanel.MATRIX_VIEW_ONE_DEFAULT);
        }

        for (String name : names.split(";")) {
            Ts ts = DocumentManager.instance.getTs(doc, name);
            if (ts.getTsData() != null) {
                ts = InPercent.convertTsInPercentIfMult(ts, multiplicative);
            }
            seriesGridContent.add(ts);
        }
    }

    @Override
    public void dispose() {
        seriesGrid.dispose();
    }

}

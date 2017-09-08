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

import static de.bbk.concur.util.InPercent.convertTsDataInPercentIfMult;
import de.bbk.concur.util.SIViewSaved;
import de.bbk.concur.util.SavedTables;
import de.bbk.concur.util.TsData_Saved;
import ec.satoolkit.DecompositionMode;
import ec.satoolkit.x11.X11Results;
import ec.tss.Ts;
import ec.tss.sa.documents.X13Document;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.ui.interfaces.IDisposable;
import java.awt.BorderLayout;
import javax.swing.JComponent;

/**
 *
 * @author Thomas Witthohn
 */
public class SIRatioView extends JComponent implements IDisposable {

    private final SIViewSaved siView;

    public SIRatioView() {
        setLayout(new BorderLayout());

        this.siView = new SIViewSaved();
        add(siView, BorderLayout.CENTER);
    }

    public void set(X13Document doc) {
     siView.setDoc(doc);
    }

    @Override
    public void dispose() {
        siView.dispose();
    }
}

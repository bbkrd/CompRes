/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bbk.outputcustomized.view;

import static de.bbk.outputcustomized.util.InPercent.convertTsDataInPercentIfMult;
import de.bbk.outputcustomized.util.SIViewSaved;
import de.bbk.outputcustomized.util.SavedTables;
import de.bbk.outputcustomized.util.TsData_Saved;
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
        if (doc == null) {
            return;
        }

        Ts seasonalfactor = TsData_Saved.convertMetaDataToTs(doc.getMetaData(), SavedTables.SEASONALFACTOR);
        if (seasonalfactor == null) {
            return;
        }
        X11Results x11 = doc.getDecompositionPart();
        DecompositionMode mode = doc.getDecompositionPart().getSeriesDecomposition().getMode();
        if (x11 != null) {

            TsData si = x11.getData("d8", TsData.class);
            TsData seas = x11.getData("d10", TsData.class);

            if (x11.getSeriesDecomposition().getMode() == DecompositionMode.LogAdditive) {
                si = si.exp();
            }
            seas = convertTsDataInPercentIfMult(seas, mode.isMultiplicative());
            si = convertTsDataInPercentIfMult(si, mode.isMultiplicative());
            siView.setSiData(seas, si, seasonalfactor.getTsData());

        } else {
            siView.reset();
        }
    }

    @Override
    public void dispose() {
        siView.dispose();
    }
}

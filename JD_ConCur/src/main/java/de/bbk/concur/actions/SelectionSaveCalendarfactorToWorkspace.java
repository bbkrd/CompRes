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
package de.bbk.concur.actions;

import static de.bbk.concur.util.InPercent.convertTsDataInPercentIfMult;
import de.bbk.concur.util.SavedTables;
import de.bbk.concur.util.TsData_MetaDataConverter;
import ec.nbdemetra.sa.MultiProcessingManager;
import ec.nbdemetra.sa.SaBatchUI;
import ec.nbdemetra.ws.actions.AbstractViewAction;
import ec.satoolkit.DecompositionMode;
import ec.tss.sa.SaItem;
import ec.tstoolkit.MetaData;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.timeseries.simplets.TsData;
import static javax.swing.Action.NAME;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/**
 *
 * @author Christiane Hofer
 */
@ActionID(
        category = "Edit",
        id = "de.bbk.concur.SelectionSaveCalendarfactorToWorkspace"
)
@ActionRegistration(
        displayName = "#CTL_SelectionSaveCalendarfactorToWorkspace",
        lazy = false
)
@ActionReference(path = MultiProcessingManager.LOCALPATH + SelectionSaveToWorkspace.PATH, position = 1898)
@NbBundle.Messages({
    "CTL_SelectionSaveCalendarfactorToWorkspace=Calendar Factor",
    "CTL_ConfirmSaveCalendarfactorToWorkspace=Are you sure you want to remember the new Calendar factor? (This will delete the old Calendar factor)",
    "CTL_NoSaveCalendarfactorToWorkspace=There is no Calendar factor(A6,A7) to save!"})

public class SelectionSaveCalendarfactorToWorkspace extends AbstractViewAction<SaBatchUI> {

    public SelectionSaveCalendarfactorToWorkspace() {
        super(SaBatchUI.class);
        putValue(NAME, Bundle.CTL_SelectionSaveCalendarfactorToWorkspace());
        refreshAction();
    }

    @Override
    protected void refreshAction() {
        setEnabled(context().getSelectionCount() > 0);
    }

    @Override
    protected void process(SaBatchUI cur) {
        {
            NotifyDescriptor nd = new NotifyDescriptor.Confirmation(Bundle.CTL_ConfirmSaveCalendarfactorToWorkspace(), NotifyDescriptor.OK_CANCEL_OPTION);
            if (DialogDisplayer.getDefault().notify(nd) != NotifyDescriptor.OK_OPTION) {
                return;
            }
        }

        SaItem[] selection = cur.getSelection();
        for (SaItem item : selection) {
            CompositeResults results = item.process();
            MetaData meta = item.getMetaData();
            if (meta == null) {
                meta = new MetaData();
                item.setMetaData(meta);
            }
            if (results != null) {
                DecompositionMode mode = results.getData("mode", DecompositionMode.class);
                TsData cal = calcCalendarFactor(results, mode);
                if (cal != null) {
                    cal = convertTsDataInPercentIfMult(cal, mode.isMultiplicative());
                    TsData_MetaDataConverter.convertTsToMetaData(cal, meta, SavedTables.CALENDARFACTOR);
                } else {
                    NotifyDescriptor nd = new NotifyDescriptor.Message(Bundle.CTL_NoSaveCalendarfactorToWorkspace(), NotifyDescriptor.ERROR_MESSAGE);
                    if (DialogDisplayer.getDefault().notify(nd) != NotifyDescriptor.OK_OPTION) {
                        return;
                    }
                }
            }
        }
        cur.setSelection(new SaItem[0]);
        cur.setSelection(selection);
    }

    public static TsData calcCalendarFactor(CompositeResults results, DecompositionMode mode) {
        TsData a6 = results.getData("decomposition.a-tables.a6", TsData.class);
        TsData a7 = results.getData("decomposition.a-tables.a7", TsData.class);

        TsData cal = null;
        if (a6 == null && a7 != null) {
            cal = a7;
        }
        if (a7 == null && a6 != null) {
            cal = a6;
        }

        if (a6 != null && a7 != null) {
            if (mode.isMultiplicative()) {
                cal = a6.times(a7);
            } else {
                cal = a6.plus(a7);
            }
        }
        return cal;
    }

}

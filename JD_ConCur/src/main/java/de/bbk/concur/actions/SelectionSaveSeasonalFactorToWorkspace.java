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
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Christiane Hofer
 */
@ActionID(
        category = "Edit",
        id = "de.bbk.concur.SelectionSaveSeasonalFactorToWorkspace"
)
@ActionRegistration(
        displayName = "#CTL_SelectionSaveSeasonalFactorToWorkspace",
        lazy = false
)
@ActionReference(path = MultiProcessingManager.LOCALPATH + SelectionSaveToWorkspace.PATH, position = 1898)
@Messages({
    "CTL_SelectionSaveSeasonalFactorToWorkspace=Seasonal Factor",
    "CTL_ConfirmSaveSeasonalFactorToWorkspace=Are you sure you want to remember the new Seasonal Factor? (This will delete the old Seasonal Factor)",
    "CTL_NoSaveSeasonalFactorToWorkspace=There is no Seasonal Factor (D10 and D10a) to remember!"})
public class SelectionSaveSeasonalFactorToWorkspace extends AbstractViewAction<SaBatchUI> {

    public SelectionSaveSeasonalFactorToWorkspace() {
        super(SaBatchUI.class);
        putValue(NAME, Bundle.CTL_SelectionSaveSeasonalFactorToWorkspace());
        refreshAction();
    }

    @Override
    protected void refreshAction() {
        setEnabled(context().getSelectionCount() > 0);
    }

    @Override
    protected void process(SaBatchUI cur) {
        {
            NotifyDescriptor nd = new NotifyDescriptor.Confirmation(Bundle.CTL_ConfirmSaveSeasonalFactorToWorkspace(), NotifyDescriptor.OK_CANCEL_OPTION);
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
                TsData d10 = results.getData("decomposition.d-tables.d10", TsData.class);
                if (d10 != null) {
                    TsData d10a = results.getData("decomposition.d-tables.d10a", TsData.class);
                    TsData d10AndD10a = d10.update(d10a);

                    d10AndD10a = convertTsDataInPercentIfMult(d10AndD10a, mode.isMultiplicative());
                    TsData_MetaDataConverter.convertTsToMetaData(d10AndD10a, meta, SavedTables.SEASONALFACTOR);
                } else {
                    NotifyDescriptor nd = new NotifyDescriptor.Message(Bundle.CTL_NoSaveSeasonalFactorToWorkspace(), NotifyDescriptor.ERROR_MESSAGE);
                    if (DialogDisplayer.getDefault().notify(nd) != NotifyDescriptor.OK_OPTION) {
                        return;
                    }
                }
            }
        }
        cur.setSelection(new SaItem[0]);
        cur.setSelection(selection);
    }

}

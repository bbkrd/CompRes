/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bbk.outputcustomized.actions;

import static de.bbk.outputcustomized.util.InPercent.convertTsDataInPercentIfMult;
import de.bbk.outputcustomized.util.SavedTables;
import de.bbk.outputcustomized.util.TsData_MetaDataConverter;
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
        id = "de.bbk.outputcustomized.SelectionSaveSeasonalFactorToWorkspace"
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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bbk.outputcustomized.actions;

import de.bbk.outputcustomized.util.SavedTables;
import de.bbk.outputcustomized.util.TsData_MetaDataConverter;
import ec.nbdemetra.sa.MultiProcessingManager;
import ec.nbdemetra.sa.SaBatchUI;
import ec.nbdemetra.ws.actions.AbstractViewAction;
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
        id = "de.bbk.outputcustomized.SelectionSaveForecastToWorkspace"
)
@ActionRegistration(
        displayName = "#CTL_SelectionSaveForecastToWorkspace",
        lazy = false
)
@ActionReference(path = MultiProcessingManager.LOCALPATH + SelectionSaveToWorkspace.PATH, position = 1898)
@NbBundle.Messages({
    "CTL_SelectionSaveForecastToWorkspace=Series with Forecast",
    "CTL_ConfirmSaveForecastToWorkspace=Are you sure you want to remember the new Forecast? (This will delete the old Forecast)",
    "CTL_NoSaveForecastToWorkspace=There is no Forecast (A1 and A1a) to save!"})

public class SelectionSaveForecastToWorkspace extends AbstractViewAction<SaBatchUI> {

    public SelectionSaveForecastToWorkspace() {
        super(SaBatchUI.class);
        putValue(NAME, Bundle.CTL_SelectionSaveForecastToWorkspace());
        refreshAction();
    }

    @Override
    protected void refreshAction() {
        setEnabled(context().getSelectionCount() > 0);
    }

    @Override
    protected void process(SaBatchUI cur) {
        {
            NotifyDescriptor nd = new NotifyDescriptor.Confirmation(Bundle.CTL_ConfirmSaveForecastToWorkspace(), NotifyDescriptor.OK_CANCEL_OPTION);
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
                TsData a1 = results.getData("decomposition.a-tables.a1", TsData.class);
                if (a1 != null) {
                    TsData a1a = results.getData("decomposition.a-tables.a1a", TsData.class);
                    TsData a1AndA1a = a1.update(a1a);

                    TsData_MetaDataConverter.convertTsToMetaData(a1AndA1a, meta, SavedTables.FORECAST);

                } else {
                    NotifyDescriptor nd = new NotifyDescriptor.Message(Bundle.CTL_NoSaveForecastToWorkspace(), NotifyDescriptor.ERROR_MESSAGE);
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

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.bbk.autoconcur;

import ec.nbdemetra.sa.MultiProcessingManager;
import ec.nbdemetra.sa.SaBatchUI;
import ec.nbdemetra.ws.actions.AbstractViewAction;
import ec.tss.sa.SaItem;
import javax.swing.JScrollPane;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Jan Gerhardt
 */
@ActionID(
        category = "Edit",
        id = "de.bundesbank.jdemetra.concur.html.CompResSetMetadataAction"
)
@ActionRegistration(
        displayName = "#CTL_CompResSetMetadataAction",
        lazy = false
)
@ActionReference(path = MultiProcessingManager.LOCALPATH + AutoConCurMetadata.PATH, position = 3)
@Messages("CTL_CompResSetMetadataAction=Set compRes Metadata for selected items")
public class SetMetadataSelectedItemsAction extends AbstractViewAction<SaBatchUI> {

    public SetMetadataSelectedItemsAction() {
        super(SaBatchUI.class);
        putValue(NAME, Bundle.CTL_CompResSetMetadataAction());
        refreshAction();
    }

    @Override
    protected void refreshAction() {
        setEnabled(context() != null && context().getSelectionCount() > 0);
    }

    @Override
    protected void process(SaBatchUI cur) {
        CompResMetadataPanel op = new CompResMetadataPanel();
        JScrollPane pane = new JScrollPane();
        pane.getViewport().add(op);
        final DialogDescriptor dd = new DialogDescriptor(op, "Set Metadata");
        if (DialogDisplayer.getDefault().notify(dd) == NotifyDescriptor.OK_OPTION) {
            DecisionBean bean = op.getBean();
            MetadataBean metaBean = op.getMetaBean();
            SaItem[] selection = cur.getSelection();
            AutoConCurMetadataUtil.putMetadata(bean,metaBean, selection);
            cur.setSelection(new SaItem[0]);
            cur.setSelection(selection);
        }
    }

}

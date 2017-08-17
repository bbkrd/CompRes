/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bbk.outputpdf;

import ec.nbdemetra.sa.MultiProcessingManager;
import ec.nbdemetra.sa.SaBatchUI;
import ec.nbdemetra.ws.actions.AbstractViewAction;
import ec.tss.sa.SaItem;
import static javax.swing.Action.NAME;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(
        category = "SaProcessing",
        id = "de.bbk.outputpdf.Pdf"
)
@ActionRegistration(
        displayName = "#CTL_Pdf", lazy = false
)

@ActionReferences({
    @ActionReference(path = MultiProcessingManager.CONTEXTPATH, position = 2260),
    @ActionReference(path = MultiProcessingManager.LOCALPATH, position = 2260)
})

@Messages("CTL_Pdf=Create HTML")
public final class Pdf extends AbstractViewAction<SaBatchUI> {

    public Pdf() {
        super(SaBatchUI.class);
        putValue(NAME, Bundle.CTL_Pdf());
    }

    @Override
    protected void refreshAction() {

    }

    @Override
    protected void process(SaBatchUI cur) {
        SaItem[] selection = cur.getSelection();
        Processing p = new Processing();
        p.start(selection, cur.getName());
    }

}

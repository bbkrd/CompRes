/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.bbk.autoconcurreport;

import ec.nbdemetra.sa.MultiProcessingDocument;
import ec.nbdemetra.ws.nodes.ItemWsNode;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

@ActionID(
        category = "Tools",
        id = "de.bundesbank.jdemetra.concur.html.CompResSelectedMasterAction"
)

@ActionRegistration(
        displayName = "#CTL_CompResSelectedMasterAction", lazy = false
)
@ActionReference(path = "Menu/Tools", position = 2101)
@NbBundle.Messages({"CTL_CompResSelectedMasterAction=Create HTML for selected multi-documents w/ masterfile",
        "CTL_CompResSelectedMasterAction_Message=Do you really want to create the output for the selected multi-documents?"})
public final class CreateSelectedRecommendations  extends NodeAction {

    private static final Class<ItemWsNode> NODE_TYPE = ItemWsNode.class;
    private static final Class<MultiProcessingDocument> ITEM_TYPE = MultiProcessingDocument.class;

    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes.length == 0) {
            return false;
        }
        for (Node activatedNode : activatedNodes) {
            if (!NODE_TYPE.isInstance(activatedNode) || ((ItemWsNode) activatedNode).getItem(ITEM_TYPE) == null) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String getName() {
        return  Bundle.CTL_CompResSelectedMasterAction();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
        AutoConCurReportExecutor.executeAfterConfirmation(Bundle.CTL_CompResSelectedMasterAction(),new AutoProcessing(activatedNodes));
    }
    
}

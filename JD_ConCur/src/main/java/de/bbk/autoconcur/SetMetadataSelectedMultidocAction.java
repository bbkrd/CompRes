/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.bbk.autoconcur;

import ec.nbdemetra.sa.MultiProcessingDocument;
import ec.nbdemetra.ws.nodes.ItemWsNode;
import javax.swing.JScrollPane;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author Jan Gerhardt
 */
@ActionID(category = "Tools",
        id = "de.bundesbank.jdemetra.autoconcur.SetMetadataSelectedMultidocAction")
@ActionRegistration(
        displayName = "#CTL_SetMetadataSelectedMultidocAction", lazy = false)
@ActionReference(path = "Menu/Tools", position = 2099, separatorBefore = 2098)

@NbBundle.Messages("CTL_SetMetadataSelectedMultidocAction=Set compRes Metadata for selected multi-documents")
public final class SetMetadataSelectedMultidocAction extends NodeAction {

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
        return Bundle.CTL_SetMetadataSelectedMultidocAction();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
        CompResMetadataPanel op = new CompResMetadataPanel();
        JScrollPane pane = new JScrollPane();
        pane.getViewport().add(op);
        final DialogDescriptor dd = new DialogDescriptor(op, "Set Metadata");
        if (DialogDisplayer.getDefault().notify(dd) == NotifyDescriptor.OK_OPTION) {
            DecisionBean bean = op.getBean();
            MetadataBean metaBean = op.getMetaBean();
            AutoConCurMetadataUtil.putMetadata(bean,metaBean, activatedNodes);
        }
    }
}

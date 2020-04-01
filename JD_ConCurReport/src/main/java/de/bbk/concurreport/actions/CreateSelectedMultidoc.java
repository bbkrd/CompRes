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
package de.bbk.concurreport.actions;

import de.bbk.concurreport.Processing;
import ec.nbdemetra.sa.MultiProcessingDocument;
import ec.nbdemetra.ws.WorkspaceItem;
import ec.nbdemetra.ws.nodes.ItemWsNode;
import ec.tss.sa.SaItem;
import ec.tss.sa.SaProcessing;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle.Messages;
import org.openide.util.actions.NodeAction;

@ActionID(category = "Edit",
        id = "de.bundesbank.jdemetra.sa.multidocextension.CreateSelectedMultidoc")
@ActionRegistration(
        displayName = "#CTL_CreateSelectedMultidoc", lazy = false)
@ActionReference(path = "Menu/Tools", position = 2100)

@Messages({"CTL_CreateSelectedMultidoc=Create HTML for selected multi-documents",
    "CTL_CreateSelectedMultidoc_Message=Do you really want to create the output for the selected multi-documents?"})

public final class CreateSelectedMultidoc extends NodeAction {

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
        return Bundle.CTL_CreateSelectedMultidoc();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
        NotifyDescriptor nd = new NotifyDescriptor.Confirmation(Bundle.CTL_CreateSelectedMultidoc_Message(), NotifyDescriptor.OK_CANCEL_OPTION);
        if (DialogDisplayer.getDefault().notify(nd) != NotifyDescriptor.OK_OPTION) {
            return;
        }
        new Thread(() -> {
            Map<String, List<SaItem>> map = new TreeMap<>();

            for (Node activatedNode : activatedNodes) {
                WorkspaceItem<MultiProcessingDocument> item = ((ItemWsNode) activatedNode).getItem(ITEM_TYPE);
                SaProcessing saProcessing = item.getElement().getCurrent();
                map.put(item.getDisplayName(), saProcessing);
            }

            Processing p = new Processing();
            p.process(map);
        }).start();
    }
}

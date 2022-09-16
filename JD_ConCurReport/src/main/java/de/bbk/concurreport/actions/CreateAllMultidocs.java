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
import ec.nbdemetra.sa.MultiProcessingManager;
import ec.nbdemetra.ws.IWorkspaceItemManager;
import ec.nbdemetra.ws.Workspace;
import ec.nbdemetra.ws.WorkspaceFactory;
import ec.nbdemetra.ws.WorkspaceItem;
import ec.tss.sa.SaItem;
import ec.tss.sa.SaProcessing;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(
        category = "Tools",
        id = "de.bundesbank.jdemetra.concur.html.CreateAllMultidocs"
)
@ActionRegistration(
        displayName = "#CTL_CreateAllMultidocs"
)
@ActionReference(path = "Menu/Tools", position = 2000)
@Messages("CTL_CreateAllMultidocs=Create HTML for all multi-documents")
public final class CreateAllMultidocs implements ActionListener {

    @Messages({
        "CTL_Message=Do you really want to create the HTML for all multi-documents?"
    })
    @Override
    public void actionPerformed(ActionEvent ev) {
        NotifyDescriptor nd = new NotifyDescriptor.Confirmation(Bundle.CTL_Message(), NotifyDescriptor.OK_CANCEL_OPTION);
        if (DialogDisplayer.getDefault().notify(nd) != NotifyDescriptor.OK_OPTION) {
            return;
        }
        new Thread(() -> {
            Map<String, List<SaItem>> map = new TreeMap<>();
            Workspace workspace = WorkspaceFactory.getInstance().getActiveWorkspace();
            IWorkspaceItemManager mgr = WorkspaceFactory.getInstance().getManager(MultiProcessingManager.ID);
            if (mgr != null) {
                List<WorkspaceItem<MultiProcessingDocument>> list = workspace.searchDocuments(mgr.getItemClass());
                list.stream().forEach((item) -> {
                    SaProcessing saProcessing = item.getElement().getCurrent();
                    map.put(item.getDisplayName(), saProcessing);
                });
                Processing p = new Processing();
                p.process(map);

            }
        }).start();
    }
}

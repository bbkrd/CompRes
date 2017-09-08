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
package de.bbk.concurreport;

import de.bbk.concurreport.Bundle;
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
        id = "de.bbk.concurreport.concurreport"
)
@ActionRegistration(
        displayName = "#CTL_ConCurReport", lazy = false
)

@ActionReferences({
    @ActionReference(path = MultiProcessingManager.CONTEXTPATH, position = 2260),
    @ActionReference(path = MultiProcessingManager.LOCALPATH, position = 2260)
})

@Messages("CTL_ConCurReport=Create HTML")
public final class ConCurReport extends AbstractViewAction<SaBatchUI> {

    public ConCurReport() {
        super(SaBatchUI.class);
        putValue(NAME, Bundle.CTL_ConCurReport());
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

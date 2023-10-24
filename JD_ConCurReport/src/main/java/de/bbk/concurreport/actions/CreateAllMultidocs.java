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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
        ConCurReportExecutor.executeAfterConfirmation(Bundle.CTL_Message(), new Processing());
    }
}

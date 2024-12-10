package de.bbk.autoconcurreport;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Gerhardt
 */
@ActionID(
        category = "Tools",
        id = "de.bundesbank.jdemetra.concur.html.CompResMasterAction"
)

@ActionRegistration(
        displayName = "#CTL_CompResMasterAction"
)
@ActionReference(path = "Menu/Tools", position = 2400)
@NbBundle.Messages("CTL_CompResMasterAction=Create HTML for all multi-documents w/ masterfile")
public class CreateAllRecommendations implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent ev) {
        new AutoProcessing().callAndShowMessages();
    }

}

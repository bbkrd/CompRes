/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
        id = "de.bundesbank.jdemetra.concur.html.Tg"
)

@ActionRegistration(
        displayName = "#CTL_AutoConCurAction"
)
@ActionReference(path = "Menu/Tools", position = 2200)
@NbBundle.Messages("CTL_AutoConCurAction=Create compRes Recommendations")
public class AutoConCurAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent ev) {
        AutoConCurReport.call();
    }

}

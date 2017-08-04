/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bbk.outputcustomized.actions;

import ec.nbdemetra.sa.MultiProcessingManager;
import ec.nbdemetra.sa.SaBatchUI;
import ec.nbdemetra.ui.Menus;
import ec.nbdemetra.ws.actions.AbstractViewAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;

/**
 *
 * @author Christiane Hofer
 */
@ActionID(
        category = "SaProcessing",
        id = "de.bundesbank.webservice.saintegration.SelectionSaveToWorkspace"
)
@ActionRegistration(
        displayName = "#CTL_SelectionSaveToWorkspace",
        lazy = false
)
@ActionReference(path = MultiProcessingManager.LOCALPATH, position = 1910)
@NbBundle.Messages({"CTL_SelectionSaveToWorkspace=Save to Workspace"})
public class SelectionSaveToWorkspace extends AbstractViewAction<SaBatchUI> implements Presenter.Popup {

    public static final String PATH = "/SaveToWorkspace";

    public SelectionSaveToWorkspace() {
        super(SaBatchUI.class);
    }

    @Override
    protected void refreshAction() {
        setEnabled(context().getSelectionCount() > 0);
    }

    @Override
    protected void process(SaBatchUI cur) {
    }

    @Override
    public JMenuItem getPopupPresenter() {
        refreshAction();
        JMenu menu = new JMenu(Bundle.CTL_SelectionSaveToWorkspace());
        menu.setEnabled(enabled);
        Menus.fillMenu(menu, MultiProcessingManager.LOCALPATH + PATH);
        return menu;
    }

}

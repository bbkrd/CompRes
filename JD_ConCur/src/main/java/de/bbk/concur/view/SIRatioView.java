/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bbk.concur.view;

import de.bbk.concur.html.HtmlCCA;
import de.bbk.concur.util.SIViewSaved;
import ec.nbdemetra.ui.NbComponents;
import ec.tss.Ts;
import ec.tss.sa.documents.X13Document;
import ec.tss.tsproviders.utils.MultiLineNameUtil;
import ec.ui.interfaces.IDisposable;
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSplitPane;

/**
 *
 * @author Jan Gerhardt
 */
public class SIRatioView extends JComponent implements IDisposable {

    private final JLabel lblFilters;
    private static final int WIDTH_SIVIEWS = 450, HEIGHT_SIVIEWS = 250;
    private final SIViewSaved siViewSavedLast;

    public SIRatioView() {
        setLayout(new BorderLayout());
        this.siViewSavedLast = new SIViewSaved(true);

        siViewSavedLast.setSize(new Dimension(WIDTH_SIVIEWS, HEIGHT_SIVIEWS));
        siViewSavedLast.setPreferredSize(new Dimension(WIDTH_SIVIEWS, HEIGHT_SIVIEWS));

        lblFilters = new JLabel("", JLabel.LEFT);
        lblFilters.setMinimumSize(new Dimension(0, 40));
        JSplitPane filterSplit = NbComponents.newJSplitPane(JSplitPane.VERTICAL_SPLIT, lblFilters, siViewSavedLast);
        filterSplit.setDividerLocation(60);

        filterSplit.setOneTouchExpandable(true);
        filterSplit.setDividerSize(10);
        filterSplit.setDividerLocation(60);
        filterSplit.setResizeWeight(.5);

        add(filterSplit, BorderLayout.CENTER);
    }

    public void setDoc(X13Document doc) {
        if (doc == null || doc.getResults() == null) {
            return;
        }
        HtmlCCA ccaFilters = new HtmlCCA(MultiLineNameUtil.join(((Ts) doc.getInput()).getName()), doc);
        lblFilters.setText(ccaFilters.writeFilters()); 
        siViewSavedLast.setDoc(doc);
    }

    @Override
    public void dispose() {
        siViewSavedLast.dispose();
    }

}

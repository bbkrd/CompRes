/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bbk.concur.view;

import de.bbk.concur.html.HtmlCCA;
import de.bbk.concur.util.JPanelCCA;
import ec.nbdemetra.ui.NbComponents;
import ec.tss.sa.documents.X13Document;
import ec.tss.tsproviders.utils.MultiLineNameUtil;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.ui.Disposables;
import ec.ui.interfaces.IDisposable;
import ec.ui.view.tsprocessing.ITsViewToolkit;
import ec.ui.view.tsprocessing.TsViewToolkit;
import java.awt.BorderLayout;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

/**
 *
 * @author Thomas Witthohn
 */
public class CCAView extends JComponent implements IDisposable {

    private transient ITsViewToolkit toolkit = TsViewToolkit.getInstance();
    private final Box document;
    private final JPanelCCA jPanelCCA;

    public CCAView() {
        setLayout(new BorderLayout());
        this.document = Box.createHorizontalBox();
        this.jPanelCCA = new JPanelCCA();
        JScrollPane mainscroll = NbComponents.newJScrollPane(jPanelCCA);

        JSplitPane mainsplit = NbComponents.newJSplitPane(JSplitPane.VERTICAL_SPLIT, document, mainscroll);
        mainsplit.setDividerLocation(0.4);
        mainsplit.setResizeWeight(.4);

        add(mainsplit, BorderLayout.CENTER);
    }

    public void setTsToolkit(ITsViewToolkit toolkit) {
        this.toolkit = toolkit;
    }

    public void set(X13Document doc) {
        if (doc == null) {
            return;
        }
        CompositeResults results = doc.getResults();
        if (results == null) {
            return;
        }

        HtmlCCA summary = new HtmlCCA(MultiLineNameUtil.join(doc.getInput().getName()), doc);
        Disposables.disposeAndRemoveAll(document).add(toolkit.getHtmlViewer(summary));

        jPanelCCA.set(doc);

    }

    @Override
    public void dispose() {
        jPanelCCA.dispose();
        Disposables.disposeAndRemoveAll(document);
    }
}

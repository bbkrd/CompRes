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

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

import de.bbk.concur.html.HtmlAutomised;
import de.bbk.concur.util.JPanelCCA;
import de.bbk.concur.util.SIViewSaved;
import ec.nbdemetra.ui.NbComponents;
import ec.tss.Ts;
import ec.tss.sa.documents.SaDocument;
import ec.tss.tsproviders.utils.MultiLineNameUtil;
import ec.ui.interfaces.IDisposable;
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

/**
 *
 * @author Thomas Witthohn
 */
public class CCAShortView extends JComponent implements IDisposable {

    private final JLabel lblACC;
    private static final int WIDTH_SIVIEWS = 450, HEIGHT_SIVIEWS = 250;
    private final JPanelCCA jPanelCCA;
    private final SIViewSaved siViewSavedForelast;
    private final SIViewSaved siViewSavedLast;

    public CCAShortView() {
        setLayout(new BorderLayout());
        this.jPanelCCA = new JPanelCCA(5, false);
        this.siViewSavedForelast = new SIViewSaved(true);
        this.siViewSavedLast = new SIViewSaved(true);

        siViewSavedForelast.setSize(new Dimension(WIDTH_SIVIEWS, HEIGHT_SIVIEWS));
        siViewSavedForelast.setPreferredSize(new Dimension(WIDTH_SIVIEWS, HEIGHT_SIVIEWS));

        siViewSavedLast.setSize(new Dimension(WIDTH_SIVIEWS, HEIGHT_SIVIEWS));
        siViewSavedLast.setPreferredSize(new Dimension(WIDTH_SIVIEWS, HEIGHT_SIVIEWS));

        JSplitPane siViewSplit = NbComponents.newJSplitPane(JSplitPane.HORIZONTAL_SPLIT, siViewSavedForelast, siViewSavedLast);
        siViewSplit.setDividerSize(0);
        siViewSplit.setEnabled(false);
        siViewSplit.setResizeWeight(0.5);

        lblACC = new JLabel("", JLabel.LEFT);
        lblACC.setMinimumSize(new Dimension(0, 60));
        JSplitPane autoSplit = NbComponents.newJSplitPane(JSplitPane.VERTICAL_SPLIT, lblACC, siViewSplit);
        autoSplit.setDividerLocation(60);

        JScrollPane mainscroll = NbComponents.newJScrollPane(jPanelCCA);

        JSplitPane graphicsSplit = NbComponents.newJSplitPane(JSplitPane.VERTICAL_SPLIT, mainscroll, autoSplit);
        graphicsSplit.setOneTouchExpandable(true);
        graphicsSplit.setDividerSize(10);
        graphicsSplit.setDividerLocation(200);
        graphicsSplit.setResizeWeight(.5);

        add(graphicsSplit, BorderLayout.CENTER);
    }

    public void set(SaDocument doc) {
        if (doc == null || doc.getResults() == null) {
            return;
        }
        jPanelCCA.set(doc);
        HtmlAutomised automised = new HtmlAutomised(MultiLineNameUtil.join(((Ts) doc.getInput()).getName()), doc);
        lblACC.setText(automised.writeText());
        int forelastPeriod;
        int lastPeriod = doc.getSeries().getDomain().getLast().getPosition();
        if (lastPeriod == 0) {
            forelastPeriod = doc.getSeries().getFrequency().intValue() - 1;
        } else {
            forelastPeriod = lastPeriod - 1;
        }
        siViewSavedForelast.setDoc(doc);
        siViewSavedForelast.getDetailChart(forelastPeriod);

        siViewSavedLast.setDoc(doc);
        siViewSavedLast.getDetailChart(lastPeriod);
    }

    @Override
    public void dispose() {
        jPanelCCA.dispose();
        siViewSavedForelast.dispose();
        siViewSavedLast.dispose();
    }
}

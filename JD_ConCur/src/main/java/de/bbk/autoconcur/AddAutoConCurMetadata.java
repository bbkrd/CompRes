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
package de.bbk.autoconcur;

import ec.nbdemetra.sa.MultiProcessingManager;
import ec.nbdemetra.sa.SaBatchUI;
import ec.nbdemetra.ws.actions.AbstractViewAction;
import ec.tss.sa.SaItem;
import ec.tstoolkit.MetaData;
import static javax.swing.Action.NAME;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Gerhardt
 */
@ActionID(
        category = "Edit",
        id = "de.bbk.concur.AddAutoConCurMetadata"
)
@ActionRegistration(
        displayName = "#CTL_AddAutoConCurMetadata",
        lazy = false
)
@ActionReference(path = MultiProcessingManager.LOCALPATH + AutoConCurMetadata.PATH, position = 2)
@NbBundle.Messages({"CTL_AddAutoConCurMetadata=Add Metadata"})

public class AddAutoConCurMetadata extends AbstractViewAction<SaBatchUI> {

    public AddAutoConCurMetadata() {
        super(SaBatchUI.class);
        putValue(NAME, Bundle.CTL_AddAutoConCurMetadata());
        refreshAction();
    }

    @Override
    protected void refreshAction() {
        setEnabled(context() != null && context().getSelectionCount() > 0);
    }

    @Override
    protected void process(SaBatchUI cur) {
        SaItem[] selection = cur.getSelection();
        for (SaItem item : selection) {
            MetaData meta = item.getMetaData();
            if (meta == null) {
                meta = new MetaData();
            }
            meta.putIfAbsent(AutoConCur.MANUAL, AutoConCur.MANUALDEFAULT);
            meta.putIfAbsent(AutoConCur.CHECKSIGN, AutoConCur.CHECKSIGNDEFAULT);
            meta.putIfAbsent(AutoConCur.NSD, AutoConCur.NSDDEFAULT);
            meta.putIfAbsent(AutoConCur.ND8, AutoConCur.ND8DEFAULT);
            meta.putIfAbsent(AutoConCur.NGROWTH, AutoConCur.NGROWTHDEFAULT);
            meta.putIfAbsent(AutoConCur.TOLD8, AutoConCur.TOLD8DEFAULT);
            meta.putIfAbsent(AutoConCur.TOLGROWTH, AutoConCur.TOLGROWTHDEFAULT);
            meta.putIfAbsent(AutoConCur.TRIM, AutoConCur.TRIMDEFAULT);
            item.setMetaData(meta);
        }
        cur.setSelection(new SaItem[0]);
        cur.setSelection(selection);
    }
}

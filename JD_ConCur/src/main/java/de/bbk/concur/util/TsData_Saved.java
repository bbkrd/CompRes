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
package de.bbk.concur.util;

import de.bbk.concur.options.DatasourceUpdateOptionsPanel;
import de.bbk.concur.servicedefinition.IExternalDataProvider;
import ec.tss.Ts;
import ec.tss.TsFactory;
import ec.tstoolkit.MetaData;
import java.util.Optional;
import java.util.prefs.Preferences;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;

/**
 *
 * @author Christiane Hofer
 */
public class TsData_Saved {

    public static Ts convertMetaDataToTs(MetaData meta, String tableName) {

        Preferences node = NbPreferences.forModule(DatasourceUpdateOptionsPanel.class);
        boolean defaultDataSource = node.getBoolean(DatasourceUpdateOptionsPanel.USEDEFAULT, true);
        if (defaultDataSource) {
            return TsData_MetaDataConverter.convertMetaDataToTs(meta, tableName);
        }

        String providerName = node.get(DatasourceUpdateOptionsPanel.PROVIDERNAME, "");
        Ts ts = TsFactory.instance.createTs();
        if (providerName.isEmpty()) {
            ts.setInvalidDataCause("No provider specified in Options.");
            return ts;
        }
        Optional<? extends IExternalDataProvider> optProvider
                = Lookup.getDefault().lookupAll(IExternalDataProvider.class)
                        .stream()
                        .filter(x -> x.getClass().getName().equals(providerName))
                        .findFirst();
        if (optProvider.isPresent()) {
            Ts providedTs = optProvider.get().convertMetaDataToTs(meta, tableName);
            if (providedTs == null) {
                ts.setInvalidDataCause("Provider " + providerName + " had an unknown error.");
                return ts;
            }
            return providedTs;
        }
        ts.setInvalidDataCause("Provider " + providerName + " not present.");
        return ts;

    }

}

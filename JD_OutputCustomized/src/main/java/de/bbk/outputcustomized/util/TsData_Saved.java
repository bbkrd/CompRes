/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bbk.outputcustomized.util;

import de.bbk.outputcustomized.options.DatasourceUpdateOptionsPanel;
import de.bbk.outputcustomized.servicedefinition.IExternalDataProvider;
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
                return optProvider.get().convertMetaDataToTs(meta, tableName);
            }
        ts.setInvalidDataCause("Provider "+ providerName+ " not present.");
        return ts;

    }

}

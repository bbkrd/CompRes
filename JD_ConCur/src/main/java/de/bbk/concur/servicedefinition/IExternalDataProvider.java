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
package de.bbk.concur.servicedefinition;

import ec.tss.Ts;
import ec.tstoolkit.MetaData;
import ec.tstoolkit.design.ServiceDefinition;

/**
 *
 * @author Christiane Hofer
 */
@ServiceDefinition
public interface IExternalDataProvider {

    public Ts convertMetaDataToTs(MetaData meta, String tableName);
}

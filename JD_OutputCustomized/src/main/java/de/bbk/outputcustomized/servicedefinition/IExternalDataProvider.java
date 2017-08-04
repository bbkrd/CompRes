/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bbk.outputcustomized.servicedefinition;

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

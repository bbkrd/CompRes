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

import de.bbk.concur.BBKOutputViewFactory;
import de.bbk.concur.util.SeasonallyAdjusted_Saved;
import ec.satoolkit.DecompositionMode;
import ec.satoolkit.x11.X11Results;
import ec.tss.Ts;
import ec.tss.TsCollection;
import ec.tss.documents.DocumentManager;
import ec.tss.sa.documents.X13Document;
import ec.tstoolkit.MetaData;
import ec.ui.chart.JTsChart;
import ec.ui.interfaces.IDisposable;
import ec.ui.interfaces.ITsCollectionView;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;

/**
 *
 * @author Thomas Witthohn
 */
public class SAView extends JComponent implements IDisposable {
    
    private final JTsChart chart;
    private final List<String> names = new ArrayList<>(),
            saved = new ArrayList<>();
    
    public SAView() {
        setLayout(new BorderLayout());
        
        this.chart = new JTsChart();
        chart.setTsUpdateMode(ITsCollectionView.TsUpdateMode.None);
        
        add(chart, BorderLayout.CENTER);
    }
    
    public void setNames(String... names) {
        saved.clear();
        this.names.clear();
        for (String name : names) {
            if (name.startsWith(BBKOutputViewFactory.OLD)) {
                saved.add(name);
            } else {
                this.names.add(name);
            }
        }
    }
    
    public void set(X13Document doc) {
        if (doc == null) {
            return;
        }
        
        X11Results x11 = doc.getDecompositionPart();
        if (x11 != null) {
            chart.getTsCollection().clear();
            TsCollection items = DocumentManager.create(names, doc);
            chart.getTsCollection().append(items);
            chart.setTitle( doc.getTs().getRawName());
           
            MetaData metadata = doc.getMetaData();
            if (metadata != null) {
                Ts savedSA = SeasonallyAdjusted_Saved.calcSeasonallyAdjusted(doc);
                chart.getTsCollection().add(savedSA);
            }
            
        } else {
            chart.getTsCollection().clear();
        }
    }
    
    @Override
    public void dispose() {
        chart.dispose();
    }
}

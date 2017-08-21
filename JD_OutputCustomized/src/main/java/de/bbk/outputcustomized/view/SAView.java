/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bbk.outputcustomized.view;

import de.bbk.outputcustomized.BBKOutputViewFactory;
import de.bbk.outputcustomized.util.SeasonallyAdjusted_Saved;
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

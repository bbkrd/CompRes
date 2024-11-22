/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.bbk.autoconcur;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Deprecated
@lombok.experimental.UtilityClass
public class BeanCollector {

    private List<DecisionBean> beans = null;

    public void initialize() {
        beans = new ArrayList<>();
    }
    
    public void clear(){
         beans = new ArrayList<>();
    }

    public boolean add(DecisionBean bean) {
        if (beans != null) {
            return beans.add(bean);
        }
        return false;
    }

    public boolean addAll(Collection<? extends DecisionBean> beanRange) {
        if (beans != null) {
            return beans.addAll(beanRange);
        }
        return false;
    }

    public List<DecisionBean> getBeans() {
        if (beans == null) {
            return new ArrayList<>();
        }
        return beans;
    }

    public void dispose() {
        beans.clear();
        beans = null;
    }

}

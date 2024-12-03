/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.bbk.autoconcur;

import ec.nbdemetra.sa.MultiProcessingDocument;
import ec.nbdemetra.ws.WorkspaceItem;
import ec.nbdemetra.ws.nodes.ItemWsNode;
import ec.tss.sa.SaItem;
import ec.tss.sa.SaProcessing;
import ec.tstoolkit.MetaData;
import org.openide.nodes.Node;

/**
 *
 * @author Jan Gerhardt
 */
@lombok.experimental.UtilityClass
public class AutoConCurMetadataUtil {

    public void putMetadataIfAbsent(SaItem[] selection) {
        for (SaItem item : selection) {
            MetaData meta = item.getMetaData();
            if (meta == null) {
                meta = new MetaData();
            }
            meta.putIfAbsent(AutoConCur.CHECKPREVIOUS, AutoConCur.CHECKPREVIOUSDEFAULT);
            meta.putIfAbsent(AutoConCur.PARTIAL, AutoConCur.PARTIALDEFAULT);
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
    }

    public void putDefaultMetadata(SaItem[] selection) {
        for (SaItem item : selection) {
            MetaData meta = item.getMetaData();
            if (meta == null) {
                meta = new MetaData();
            }
            meta.put(AutoConCur.CHECKPREVIOUS, AutoConCur.CHECKPREVIOUSDEFAULT);
            meta.put(AutoConCur.PARTIAL, AutoConCur.PARTIALDEFAULT);
            meta.put(AutoConCur.MANUAL, AutoConCur.MANUALDEFAULT);
            meta.put(AutoConCur.CHECKSIGN, AutoConCur.CHECKSIGNDEFAULT);
            meta.put(AutoConCur.NSD, AutoConCur.NSDDEFAULT);
            meta.put(AutoConCur.ND8, AutoConCur.ND8DEFAULT);
            meta.put(AutoConCur.NGROWTH, AutoConCur.NGROWTHDEFAULT);
            meta.put(AutoConCur.TOLD8, AutoConCur.TOLD8DEFAULT);
            meta.put(AutoConCur.TOLGROWTH, AutoConCur.TOLGROWTHDEFAULT);
            meta.put(AutoConCur.TRIM, AutoConCur.TRIMDEFAULT);
            item.setMetaData(meta);
        }
    }

    public void putMetadata(DecisionBean bean, MetadataBean metaBean, SaItem[] selection) {
        for (SaItem item : selection) {
            MetaData meta = item.getMetaData();
            if (meta == null) {
                meta = new MetaData();
            }
            if (metaBean.isCheckPrevious()) {
                meta.put(AutoConCur.CHECKPREVIOUS, bean.isCheckPrevious() ? "1" : "0");
            }
            if (metaBean.isPartial()) {
                meta.put(AutoConCur.PARTIAL, bean.isPartial() ? "1" : "0");
            }
            if (metaBean.isManual()) {
                meta.put(AutoConCur.MANUAL, bean.isManual() ? "1" : "0");
            }
            if (metaBean.isCheckSign()) {
                meta.put(AutoConCur.CHECKSIGN, bean.isCheckSign() ? "1" : "0");
            }
            if (metaBean.isNSD()) {
                meta.put(AutoConCur.NSD, String.valueOf(bean.getNSD()));
            }
            if (metaBean.isND8()) {
                meta.put(AutoConCur.ND8, String.valueOf(bean.getND8()));
            }
            if (metaBean.isNGrowth()) {
                meta.put(AutoConCur.NGROWTH, String.valueOf(bean.getNGrowth()));
            }
            if (metaBean.isTolD8()) {
                meta.put(AutoConCur.TOLD8, String.valueOf(bean.getTolD8()));
            }
            if (metaBean.isToleranceGrowth()) {
                meta.put(AutoConCur.TOLGROWTH, String.valueOf(bean.getToleranceGrowth()));
            }
            if (metaBean.isTrim()) {
                meta.put(AutoConCur.TRIM, String.valueOf(bean.getTrim()));
            }
            item.setMetaData(meta);
        }
    }

    public void putMetadata(DecisionBean bean, MetadataBean metaBean, Node[] activatedNodes) {
        for (Node activatedNode : activatedNodes) {
            WorkspaceItem<MultiProcessingDocument> item = ((ItemWsNode) activatedNode).getItem(MultiProcessingDocument.class);
            SaProcessing saProcessing = item.getElement().getCurrent();
            putMetadata(bean, metaBean, saProcessing.toArray());
        }
    }
}

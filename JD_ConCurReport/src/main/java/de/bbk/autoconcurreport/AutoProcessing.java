package de.bbk.autoconcurreport;

import de.bbk.autoconcur.DecisionBeanCollector;
import de.bbk.concurreport.Processing;
import de.bbk.concurreport.ReportMessages;
import ec.nbdemetra.sa.MultiProcessingDocument;
import ec.nbdemetra.sa.MultiProcessingManager;
import ec.nbdemetra.sa.SaBatchUI;
import ec.nbdemetra.ws.IWorkspaceItemManager;
import ec.nbdemetra.ws.Workspace;
import ec.nbdemetra.ws.WorkspaceFactory;
import ec.nbdemetra.ws.WorkspaceItem;
import ec.nbdemetra.ws.nodes.ItemWsNode;
import ec.tss.sa.SaItem;
import ec.tss.sa.SaProcessing;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import org.openide.nodes.Node;

public class AutoProcessing implements Callable<ReportMessages> {

    private final Map<String, List<SaItem>> map;
    private static final Class<MultiProcessingDocument> ITEM_TYPE = MultiProcessingDocument.class;
    private final Processing concurProcessing;

    public AutoProcessing() {
        this.map = new TreeMap<>();
        Workspace workspace = WorkspaceFactory.getInstance().getActiveWorkspace();
        IWorkspaceItemManager mgr = WorkspaceFactory.getInstance().getManager(MultiProcessingManager.ID);
        if (mgr != null) {
            List<WorkspaceItem<MultiProcessingDocument>> list = workspace.searchDocuments(mgr.getItemClass());
            list.stream().forEach((item) -> {
                SaProcessing saProcessing = item.getElement().getCurrent();
                map.put(item.getDisplayName(), saProcessing);
            });
        }
        this.concurProcessing = new Processing(map);
    }

    public AutoProcessing(Map<String, List<SaItem>> map) {
        this.map = map;
        this.concurProcessing = new Processing(map);
    }

    public AutoProcessing(Node[] activatedNodes) {
        this.map = new TreeMap<>();

        for (Node activatedNode : activatedNodes) {
            WorkspaceItem<MultiProcessingDocument> item = ((ItemWsNode) activatedNode).getItem(ITEM_TYPE);
            SaProcessing saProcessing = item.getElement().getCurrent();
            map.put(item.getDisplayName(), saProcessing);
        }
        this.concurProcessing = new Processing(map);
    }

    public AutoProcessing(SaBatchUI cur) {
        this.map = new TreeMap<>();
        map.put(cur.getName(), Arrays.asList(cur.getSelection()));
        this.concurProcessing = new Processing(map);
    }

    @Override
    public ReportMessages call() {
        DecisionBeanCollector.initialize();
        ReportMessages messages1 = concurProcessing.call();
        ReportMessages messages2 = AutoConCurReport.call();
        
        DecisionBeanCollector.dispose();
        return new ReportMessages(messages1.getSuccessMessages().concat(messages2.getSuccessMessages()), messages1.getErrorMessages().concat(messages2.getErrorMessages()));
    }
    
    public void callAndShowMessages(){
        ReportMessages messages = this.call();
        AutoConCurReport.showMessages(messages);
    }
    
}

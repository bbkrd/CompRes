package de.bbk.concurreport.actions;

import de.bbk.concurreport.Processing;
import de.bbk.concurreport.ReportMessages;
import java.awt.Dimension;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import lombok.experimental.UtilityClass;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;

@UtilityClass
public class ConCurReportExecutor {

    public void executeAfterConfirmation(String confirmationText, Processing processing) {
        if (showConfirmationDialog(confirmationText)) {
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            CompletableFuture.supplyAsync(processing::call, executorService)
                    .handleAsync((t, u) -> {
                        if (u != null) {
                            Exceptions.printStackTrace(u);
                            String errorMessage = "Unknown error! Please report as Issue on GitHub. " + u.toString();
                            return new ReportMessages("", errorMessage);
                        }
                        return t;
                    }, executorService)
                    .thenAccept(ConCurReportExecutor::showMessages);

        }
    }

    private void showMessages(ReportMessages o) {
        if (!o.getErrorMessages().isEmpty()) {
            JTextArea jta = new JTextArea(o.getErrorMessages());
            jta.setEditable(false);
            JScrollPane jsp = new JScrollPane(jta);
            jsp.setPreferredSize(new Dimension(480, 120));
            JOptionPane.showMessageDialog(null, jsp, "Error", JOptionPane.ERROR_MESSAGE);
        }

        if (!o.getSuccessMessages().isEmpty()) {
            JTextArea jta = new JTextArea(o.getSuccessMessages());
            jta.setEditable(false);
            JScrollPane jsp = new JScrollPane(jta);
            jsp.setPreferredSize(new Dimension(480, 120));
            JOptionPane.showMessageDialog(null, jsp, "The output is available for: ", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private boolean showConfirmationDialog(String confirmationText) {
        if (confirmationText == null || confirmationText.isEmpty()) {
            return true;
        }
        NotifyDescriptor nd = new NotifyDescriptor.Confirmation(confirmationText, NotifyDescriptor.OK_CANCEL_OPTION);
        return DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.OK_OPTION;
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bbk.autoconcurreport;

import de.bbk.concurreport.files.HTMLFiles;
import de.bbk.concurreport.options.ConCurReportOptionsPanel;
import static de.bbk.concurreport.options.ConCurReportOptionsPanel.DEFAULT_IS_WORKSPACE_INITIAL_SAVE_LOCATION;
import static de.bbk.concurreport.options.ConCurReportOptionsPanel.IS_WORKSPACE_INITIAL_SAVE_LOCATION;
import ec.nbdemetra.ws.FileRepository;
import ec.nbdemetra.ws.WorkspaceFactory;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.swing.JFileChooser;
import org.openide.util.NbPreferences;
import org.openide.windows.WindowManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jan Gerhardt
 */
public class HTMLAutoConCurSummary {

    private String currentDir;
    private String filePath = "";
    private String errorMessage = "";
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    //private OverrideOption override = OverrideOption.ASK; //

    //private static final String LAST_FOLDER = "concurreport_lastfolder";
    public HTMLAutoConCurSummary() {
        currentDir = NbPreferences.forModule(HTMLFiles.class).get(HTMLFiles.LAST_FOLDER, null);
    }

    @lombok.Synchronized
    public boolean selectFolder() {
        errorMessage = "";
        JFileChooser fileChooser;
        String fileName = WorkspaceFactory.getInstance().getActiveWorkspace().getDataSource().getParams().get(FileRepository.FILENAME);
        boolean isWorkspaceInitialSaveLocation = NbPreferences.forModule(ConCurReportOptionsPanel.class)
                .getBoolean(IS_WORKSPACE_INITIAL_SAVE_LOCATION, DEFAULT_IS_WORKSPACE_INITIAL_SAVE_LOCATION);
        if (isWorkspaceInitialSaveLocation && fileName != null) {
            fileChooser = new JFileChooser(fileName);
        } else if (currentDir != null && !currentDir.isEmpty()) {
            fileChooser = new JFileChooser(currentDir);
        } else {
            fileChooser = new JFileChooser();
        }
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        currentDir = null;
        File file = null;
        try {
            if (fileChooser.showOpenDialog(WindowManager.getDefault().getMainWindow()) == JFileChooser.APPROVE_OPTION) {
                file = fileChooser.getSelectedFile();
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            fileChooser = new JFileChooser();
            if (fileChooser.showOpenDialog(WindowManager.getDefault().getMainWindow()) == JFileChooser.APPROVE_OPTION) {
                file = fileChooser.getSelectedFile();
            }
        } finally {
            if (file != null) {
                currentDir = file.getAbsolutePath();
                NbPreferences.forModule(HTMLFiles.class).put(HTMLFiles.LAST_FOLDER, currentDir);
                if (!Files.isWritable(file.toPath())) {
                    errorMessage = "You do not have the right to write in " + currentDir;
                }
            } else {
                currentDir = "";
            }
        }

        if ("".equals(currentDir)) {
            return false;
        }

        return errorMessage.isEmpty();
    }

    public File createHTMLAutoConCurSummaryFile(String fileName) throws IOException {
        return createHTMLAutoConCurSummaryFile(fileName, "");
    }

    public File createHTMLAutoConCurSummaryFile(String fileName, String directory) throws IOException {
        StringBuilder path = new StringBuilder();
        path.append(currentDir);
        if (!directory.isEmpty()) {
            path.append("\\").append(directory);
        }

        java.nio.file.Files.createDirectories(Paths.get(path.toString()));
        path.append("\\").append(fileName);
//        if (NbPreferences.forModule(ConCurReportOptionsPanel.class).getBoolean(ConCurReportOptionsPanel.JUST_ONE_HTML, true)) {
//            path = numberForFile(path);
//        }
        path.append(".html");
        return new File(path.toString());
    }

    private StringBuilder numberForFile(StringBuilder path) {
        int counter = 0;
        int startindex = path.length();
        while (Files.exists(Paths.get(path.toString() + ".html"))) {
            counter++;

            if (counter == 1) {
                path.append(" (1) ");
            } else {
                //before this all spaces are removed
                String strToReplace = " (" + (counter - 1) + ") ";
                path.replace(startindex, startindex + strToReplace.length(), " (" + counter + ") ");
            }
        }
        return path;
    }

    /**
     * @return the errorMessage
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * @return the fileName
     */
    public String getFilePath() {
        return filePath;
    }

}

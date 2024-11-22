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
package de.bbk.concurreport.files;

import de.bbk.concurreport.options.ConCurReportOptionsPanel;
import static de.bbk.concurreport.options.ConCurReportOptionsPanel.DEFAULT_IS_WORKSPACE_INITIAL_SAVE_LOCATION;
import static de.bbk.concurreport.options.ConCurReportOptionsPanel.IS_WORKSPACE_INITIAL_SAVE_LOCATION;
import de.bbk.concurreport.util.Frozen;
import ec.nbdemetra.ws.FileRepository;
import ec.nbdemetra.ws.WorkspaceFactory;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.openide.util.NbPreferences;
import org.openide.windows.WindowManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author s4504ch
 */
public class HTMLFiles {

    private String currentDir;
    private String filePath = "";
    private String errorMessage = "";
    private OverrideOption override = OverrideOption.ASK; //

    public static final String LAST_FOLDER = "concurreport_lastfolder";

    public HTMLFiles() {
        currentDir = NbPreferences.forModule(HTMLFiles.class).get(LAST_FOLDER, null);
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
                NbPreferences.forModule(HTMLFiles.class).put(LAST_FOLDER, currentDir);
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

    private File selectFileName(File file) {
        JFileChooser fileChooser = new JFileChooser(file.getParentFile());
        fileChooser.setDialogTitle("The file " + file.getName() + " already exists.");
        fileChooser.setFileFilter(new FileNameExtensionFilter("HTML (*.html)", "*.html"));

        filePath = null;
        File tmp = null;
        try {
            if (fileChooser.showSaveDialog(WindowManager.getDefault().getMainWindow()) == JFileChooser.APPROVE_OPTION) {
                tmp = fileChooser.getSelectedFile();
            }
        } finally {
            if (tmp != null) {
                filePath = tmp.getAbsolutePath();
                if (!Files.isWritable(tmp.toPath())) {
                    errorMessage = "You do not have the right to write in " + filePath;
                }
            } else {
                filePath = "";
            }
        }

        if ("".equals(filePath)) {
            errorMessage = "No file name selected";
            return null;
        } else {
            return new File(filePath);
        }

    }

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    /**
     * Creates all files in the selected folder
     *
     * @param html
     * @param saItemName
     */
    public boolean writeHTMLFile(String html, String saItemName) {
        errorMessage = "";
        boolean saved = false;
        try {
            File file = createHtmlFileForSaItem(saItemName);
            saved = writeHTMLFile(html, file);
        } catch (Exception ex) {
            errorMessage = ex.getMessage();
            LOGGER.error(ex.getMessage());

        } finally {
            return saved;
        }
    }

    public boolean writeHTMLFile(String html, File file) {
        errorMessage = "";
        boolean saved = false;
        try {
            if (file != null && file.exists()) {
                int option;
                String filename = file.getName();
                filename = filename.replaceAll(".html", "");
                switch (override) {
                    case ASK:
                        option = JOptionPane.showOptionDialog(WindowManager.getDefault().getMainWindow(), "The file for " + filename + " already exists! Do you want to override it?", "The file already exists!", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                                new Object[]{"Yes", "Yes to all", "Select new name", "No", "No to all"}, "");
                        break;
                    case YES:
                        option = 0;
                        break;
                    case NO:
                        option = 3;
                        break;
                    default:
                        option = 2;
                }
                switch (option) {
                    case 1:
                        override = OverrideOption.YES;
                    case 0:
                        break;
                    case 2:
                        file = selectFileName(file);
                        this.filePath = file.getPath();
                        break;
                    case 4:
                        override = OverrideOption.NO;
                    case 3:
                    default:
                        this.filePath = file.getPath();
                        errorMessage = "File already exists";
                        return false;
                }
            }
            if (file != null) {
                this.filePath = file.getPath();
                com.google.common.io.Files.write(html, file, Charset.defaultCharset());
                saved = true;
            } else {
                errorMessage = "No path selected";
            }
        } catch (Exception ex) {
            errorMessage = ex.getMessage();
            LOGGER.error(ex.getMessage());

        } finally {
            return saved;
        }
    }

    /**
     * Saves the html file in subfolder for each mulitdoc
     *
     * @param html
     * @param multiName
     * @param saItemName
     */
    public void writeHTMLFile(String html, String multiName, String saItemName) {
        errorMessage = "";
        try {
            String wsName = WorkspaceFactory.getInstance().getActiveWorkspace().getName();
            multiName = removeCharacters(multiName);
            saItemName = removeCharacters(saItemName);

            File pic = createHtmlFile(saItemName, wsName + "\\" + multiName);

            com.google.common.io.Files.write(html, pic, Charset.defaultCharset());
            //   Desktop.getDesktop().open(pic);
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
        }
    }

    public boolean createHTML(String html, String fileString) {
        errorMessage = "";
        try {
            String wsName = WorkspaceFactory.getInstance().getActiveWorkspace().getName();
            fileString = removeCharacters(fileString);

            File file = createHtmlFile(fileString, wsName);

            com.google.common.io.Files.write(html, file, Charset.defaultCharset());
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
            return false;
        }

        return true;
    }

    public File createHtmlFileForSaItem(String saItemName) throws IOException {
        saItemName = removeCharacters(saItemName);

        StringBuilder path = new StringBuilder();
        path.append(currentDir);
        path.append("\\").append(Frozen.removeFrozen(saItemName));
        path.append(".html");
        //   fileName = NumberForFile(fileName);
        this.filePath = path.toString();
        return new File(path.toString());
    }

    public File createHtmlFile(String fileName) throws IOException {
        return createHtmlFile(fileName, "");
    }

    public File createHtmlFile(String fileName, String directory) throws IOException {
        StringBuilder path = new StringBuilder();
        path.append(currentDir);
        if (!directory.isEmpty()) {
            path.append("\\").append(directory);
        }

        java.nio.file.Files.createDirectories(Paths.get(path.toString()));
        path.append("\\").append(fileName);
        path = numberForFile(path);
        path.append(".html");
        this.filePath = path.toString();
        return new File(path.toString());
    }

    private String removeCharacters(String name) {

        name = name.replace("\\", "");
        name = name.replace("/", "-");
        name = name.replace(":", "."); //nur Windows
        name = name.replace("*", "");
        name = name.replace("?", "");
        name = name.replace("<", "");
        name = name.replace(">", "");
        name = name.replace("|", "");
        name = name.replace("\n", "-");
        name = name.replace(" ", "");
        return name;

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

    private static enum OverrideOption {
        ASK,
        YES,
        NO;
    }
}

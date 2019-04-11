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

import de.bbk.concurreport.util.Frozen;
import ec.nbdemetra.ws.WorkspaceFactory;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.openide.util.NbPreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author s4504ch
 */
public class HTMLFiles {

    private String currentDir;
    private String fileName = "";
    private String errorMessage = "";
    private static HTMLFiles hTMLFiles;

    private static final String LAST_FOLDER = "concurreport_lastfolder";

    public static HTMLFiles getInstance() {
        if (hTMLFiles == null) {
            hTMLFiles = new HTMLFiles();
            return hTMLFiles;
        } else {
            return hTMLFiles;
        }
    }

    private HTMLFiles() {
        currentDir = NbPreferences.forModule(HTMLFiles.class).get(LAST_FOLDER, null);
    }

    public boolean selectFolder() {
        errorMessage = "";
        JFileChooser fileChooser;
        fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        if (currentDir != null) {
            fileChooser.setCurrentDirectory(new File(currentDir));
        }
        if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            currentDir = fileChooser.getSelectedFile().getAbsolutePath();
            fileChooser.setCurrentDirectory(new File(currentDir));
            NbPreferences.forModule(HTMLFiles.class).put(LAST_FOLDER, currentDir);
            if (!Files.isWritable(fileChooser.getSelectedFile().toPath())) {
                errorMessage = "You have not the right to write in " + currentDir;
                return false;
            }
            return true;
        }
        return false;

    }

    private File selectFileName(File file) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        FileFilter defaultFilter = new FileNameExtensionFilter("HTML (.html)", "html");
        fileChooser.addChoosableFileFilter(defaultFilter);
        fileChooser.setDialogTitle("The file " + file.getName() + " already exists.");
        fileChooser.setCurrentDirectory(file);
        switch (fileChooser.showSaveDialog(null)) {
            case JFileChooser.APPROVE_OPTION:
                fileName = fileChooser.getSelectedFile().toString();
                return fileChooser.getSelectedFile();
            case JFileChooser.CANCEL_OPTION:
                errorMessage = "No file name selected";
                return null;
            default:
                return null;
        }
    }

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    /**
     * Creats all files in the selected folder
     *
     * @param html
     * @param saItemName
     */
    public boolean writeHTMLFile(String html, String saItemName) {
        errorMessage = "";
        boolean saved = false;
        try {
            saItemName = removeCharacters(saItemName);

            StringBuilder fileName = new StringBuilder();
            fileName.append(currentDir);
            fileName.append("\\").append(Frozen.removeFrozen(saItemName));
            fileName.append(".html");
            //   fileName = NumberForFile(fileName);
            File file = new File(fileName.toString());
            if (file.exists()) {
                file = selectFileName(file);
            }
            if (file != null) {

                com.google.common.io.Files.write(html, file, Charset.defaultCharset());
                saved = true;
                //Desktop.getDesktop().open(file);
            }//ToDo löschen
        } catch (IOException ex) {
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

    public File createHtmlFile(String fileName) throws IOException {
        return createHtmlFile(fileName, "");
    }

    public File createHtmlFile(String fileName, String directory) throws IOException {
        StringBuilder filePath = new StringBuilder();
        filePath.append(currentDir);
        if (!directory.isEmpty()) {
            filePath.append("\\").append(directory);
        }

        java.nio.file.Files.createDirectories(Paths.get(filePath.toString()));
        filePath.append("\\").append(fileName);
        filePath = numberForFile(filePath);
        filePath.append(".html");
        return new File(filePath.toString());
    }

    public void createHTMLTempFiles(String html) {

        try {
            File pic = File.createTempFile("Test", ".html");
            com.google.common.io.Files.write(html, pic, Charset.defaultCharset());
            Desktop.getDesktop().open(pic);
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
        }

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
                //befor this all spaces are removed
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
    public String getFileName() {
        return fileName;
    }
}

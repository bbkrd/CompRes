/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bbk.outputpdf.files;

import com.google.common.io.Files;
import ec.nbdemetra.ws.Workspace;
import ec.nbdemetra.ws.WorkspaceFactory;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.swing.JFileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author s4504ch
 */
public class HTMLFiles {

   private String currentDir;

    private static HTMLFiles hTMLFiles;

    public static HTMLFiles getInstance() {
        if (hTMLFiles == null) {
            hTMLFiles = new HTMLFiles();
            return hTMLFiles;
        } else {
            return hTMLFiles;
        }
    }

    private HTMLFiles() {
    }

    public void selectFolder() {
        JFileChooser fileChooser;
        fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        if (currentDir != null) {
            fileChooser.setCurrentDirectory(new File(currentDir));
        }
        int showSaveDialog = fileChooser.showSaveDialog(null);

        if (showSaveDialog == JFileChooser.APPROVE_OPTION) {
            currentDir = fileChooser.getSelectedFile().getAbsolutePath();
            fileChooser.setCurrentDirectory(new File(currentDir));
            OutputfileSelected = true;
        }
        if (showSaveDialog == JFileChooser.CANCEL_OPTION) {
            OutputfileSelected = false;
        }

    }
    private boolean OutputfileSelected = false;

    public boolean isOutputfileSelected() {
        return OutputfileSelected;
    }
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    public void creatHTMLFile(String html, String multiName, String saItemName) {

        try {
            Workspace workspace = WorkspaceFactory.getInstance().getActiveWorkspace();
            String wsName = workspace.getName();

            multiName = removeCharacters(multiName);
            saItemName = removeCharacters(saItemName);

            StringBuilder fileName = new StringBuilder();
            fileName.append(currentDir).append("\\").append(wsName).append("\\").append(multiName);

            java.nio.file.Files.createDirectories(Paths.get(fileName.toString()));
            fileName.append("\\").append(saItemName);
            fileName = NumberForFile(fileName);
            fileName.append(".html");
            File pic = new File(fileName.toString());

            Files.write(html, pic, Charset.defaultCharset());
            Desktop.getDesktop().open(pic);
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
        }
    }

    public void createHTMLTempFiles(String html) {

        try {
            File pic = File.createTempFile("Test", ".html");
            Files.write(html, pic, Charset.defaultCharset());
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

    private StringBuilder NumberForFile(StringBuilder path) {
        int counter = 0;
        while (java.nio.file.Files.exists(Paths.get(path.toString() + ".html"))) {
            counter++;

            if (counter == 1) {
                path.append(" (1) ");
            } else {
                //befor this all spaces are removed
                int startindex;
                String strToReplace = " (" + String.valueOf(counter - 1) + ") ";
                startindex = path.indexOf(strToReplace);
                path.replace(startindex, startindex + strToReplace.length(), " (" + String.valueOf(counter) + ") ");
            }
        }
        return path;
    }
}

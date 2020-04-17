/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bbk.concurreport.options;

import de.bbk.concurreport.Graphic;
import de.bbk.concurreport.MainTable;
import de.bbk.concurreport.ReportStyle;
import de.bbk.concurreport.Value;
import ec.satoolkit.x11.X11Kernel;
import ec.util.list.swing.JListSelection;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import org.openide.util.NbPreferences;

public final class ConCurReportOptionsPanel extends javax.swing.JPanel {

    private final ConCurReportOptionsPanelController controller;
    private final JListSelection x13Selection, x13TransformSelection, x13ChartOne, x13ChartTwo, mainSelection, d8bSelection, valueSelection, graphicSelection;

//    private static final HashMap<String, Integer> POSITION = new HashMap<>();
    private static final List<String> ALL_X11_TABLES, MAIN_TABLES, D8B, VALUES, GRAPHICS;

    static {
        ALL_X11_TABLES = new ArrayList<>();
        ALL_X11_TABLES.addAll(Arrays.asList(X11Kernel.ALL_A));
        ALL_X11_TABLES.addAll(Arrays.asList(X11Kernel.ALL_B));
        ALL_X11_TABLES.addAll(Arrays.asList(X11Kernel.ALL_C));
        ALL_X11_TABLES.addAll(Arrays.asList(X11Kernel.ALL_D));
        ALL_X11_TABLES.addAll(Arrays.asList(X11Kernel.ALL_E));

//        int counter = 0;
//        for (String string : ALL_X11_TABLES) {
//            POSITION.put(string, counter++);
//        }
        MAIN_TABLES = new ArrayList<>();
        Arrays.stream(MainTable.values()).map(item -> item.toString()).forEach(MAIN_TABLES::add);

        D8B = new ArrayList<>();
        D8B.add("D8B");

        VALUES = new ArrayList<>();
        Arrays.stream(Value.values()).map(item -> item.toString()).forEach(VALUES::add);

        GRAPHICS = new ArrayList<>();
        Arrays.stream(Graphic.values()).map(item -> item.toString()).forEach(GRAPHICS::add);
    }

    ConCurReportOptionsPanel(ConCurReportOptionsPanelController controller) {
        this.controller = controller;
        initComponents();
        x13Selection = new JListSelection();
        userDefineTabs.add("X13 Tables", x13Selection);

        x13TransformSelection = new JListSelection();
        userDefineTabs.add("X13 Tables (transform)", x13TransformSelection);

        x13ChartOne = new JListSelection();
        userDefineTabs.add("X13 Chart 1", x13ChartOne);

        x13ChartTwo = new JListSelection();
        userDefineTabs.add("X13 Chart 2", x13ChartTwo);

        mainSelection = new JListSelection();
        userDefineTabs.add("Main", mainSelection);

        d8bSelection = new JListSelection();
        userDefineTabs.add("D8B", d8bSelection);

        valueSelection = new JListSelection();
        userDefineTabs.add("Values", valueSelection);

        graphicSelection = new JListSelection();
        userDefineTabs.add("Graphic", graphicSelection);

        reportPanel.add(userDefineTabs);
        // TODO listen to changes in form fields and call controller.changed()
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup = new javax.swing.ButtonGroup();
        reportPanel = new javax.swing.JPanel();
        reportStyle = new javax.swing.JComboBox<>(de.bbk.concurreport.ReportStyle.values());
        userDefineTabs = new javax.swing.JTabbedPane();
        includeShortReport = new javax.swing.JCheckBox();
        javax.swing.JPanel jPanel1 = new javax.swing.JPanel();
        javax.swing.JLabel jLabel3 = new javax.swing.JLabel();
        javax.swing.JLabel jLabel4 = new javax.swing.JLabel();
        decimalsTables = new javax.swing.JSpinner();
        autoCorrelation = new javax.swing.JSpinner();
        javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
        javax.swing.JLabel jLabel2 = new javax.swing.JLabel();
        tableSpinner = new javax.swing.JSpinner();
        graphicSpinner = new javax.swing.JSpinner();
        javax.swing.JPanel hTMLOptionPanel = new javax.swing.JPanel();
        buttonOneHTML = new javax.swing.JRadioButton();
        buttonHTMLforEach = new javax.swing.JRadioButton();

        setMinimumSize(new java.awt.Dimension(389, 100));

        reportPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(ConCurReportOptionsPanel.class, "ConCurReportOptionsPanel.reportPanel.border.title"))); // NOI18N
        reportPanel.setMaximumSize(new java.awt.Dimension(100, 25));
        reportPanel.setMinimumSize(new java.awt.Dimension(100, 25));
        reportPanel.setPreferredSize(new java.awt.Dimension(100, 25));

        reportStyle.setBorder(null);
        reportStyle.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                reportStyleItemStateChanged(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(includeShortReport, org.openide.util.NbBundle.getMessage(ConCurReportOptionsPanel.class, "ConCurReportOptionsPanel.includeShortReport.text")); // NOI18N

        javax.swing.GroupLayout reportPanelLayout = new javax.swing.GroupLayout(reportPanel);
        reportPanel.setLayout(reportPanelLayout);
        reportPanelLayout.setHorizontalGroup(
            reportPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(reportPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(reportPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(reportPanelLayout.createSequentialGroup()
                        .addComponent(reportStyle, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(includeShortReport, javax.swing.GroupLayout.DEFAULT_SIZE, 184, Short.MAX_VALUE))
                    .addComponent(userDefineTabs, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );
        reportPanelLayout.setVerticalGroup(
            reportPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(reportPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(reportPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(reportStyle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(includeShortReport))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(userDefineTabs, javax.swing.GroupLayout.DEFAULT_SIZE, 204, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(ConCurReportOptionsPanel.class, "ConCurReportOptionsPanel.jPanel1.border.title"))); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(ConCurReportOptionsPanel.class, "ConCurReportOptionsPanel.jLabel3.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(ConCurReportOptionsPanel.class, "ConCurReportOptionsPanel.jLabel4.text")); // NOI18N

        decimalsTables.setModel(new javax.swing.SpinnerNumberModel(2, 0, 9, 1));

        autoCorrelation.setModel(new javax.swing.SpinnerNumberModel(1.0d, 0.0d, 1.0d, 0.01d));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(ConCurReportOptionsPanel.class, "ConCurReportOptionsPanel.jLabel1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(ConCurReportOptionsPanel.class, "ConCurReportOptionsPanel.jLabel2.text")); // NOI18N

        tableSpinner.setModel(new javax.swing.SpinnerNumberModel(5, null, null, 1));

        graphicSpinner.setModel(new javax.swing.SpinnerNumberModel(5, null, null, 1));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(autoCorrelation, javax.swing.GroupLayout.DEFAULT_SIZE, 50, Short.MAX_VALUE)
                    .addComponent(decimalsTables))
                .addGap(28, 28, 28)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(20, 20, 20)))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(tableSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, 50, Short.MAX_VALUE)
                    .addComponent(graphicSpinner))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(autoCorrelation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(tableSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(decimalsTables, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(graphicSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        hTMLOptionPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(ConCurReportOptionsPanel.class, "ConCurReportOptionsPanel.hTMLOptionPanel.border.title"))); // NOI18N
        hTMLOptionPanel.setMinimumSize(new java.awt.Dimension(300, 100));
        hTMLOptionPanel.setPreferredSize(new java.awt.Dimension(300, 100));
        hTMLOptionPanel.setLayout(new java.awt.GridLayout(1, 0));

        buttonGroup.add(buttonOneHTML);
        org.openide.awt.Mnemonics.setLocalizedText(buttonOneHTML, org.openide.util.NbBundle.getMessage(ConCurReportOptionsPanel.class, "ConCurReportOptionsPanel.buttonOneHTML.text")); // NOI18N
        buttonOneHTML.setBorder(null);
        buttonOneHTML.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        buttonOneHTML.setMaximumSize(new java.awt.Dimension(100, 10));
        buttonOneHTML.setMinimumSize(new java.awt.Dimension(100, 10));
        buttonOneHTML.setPreferredSize(new java.awt.Dimension(100, 10));
        hTMLOptionPanel.add(buttonOneHTML);

        buttonGroup.add(buttonHTMLforEach);
        org.openide.awt.Mnemonics.setLocalizedText(buttonHTMLforEach, org.openide.util.NbBundle.getMessage(ConCurReportOptionsPanel.class, "ConCurReportOptionsPanel.buttonHTMLforEach.text")); // NOI18N
        buttonHTMLforEach.setBorder(null);
        buttonHTMLforEach.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        buttonHTMLforEach.setMaximumSize(new java.awt.Dimension(100, 10));
        buttonHTMLforEach.setMinimumSize(new java.awt.Dimension(100, 10));
        buttonHTMLforEach.setPreferredSize(new java.awt.Dimension(100, 10));
        hTMLOptionPanel.add(buttonHTMLforEach);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(reportPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 422, Short.MAX_VALUE)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(hTMLOptionPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(hTMLOptionPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(reportPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 273, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void reportStyleItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_reportStyleItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            includeShortReport.setVisible(reportStyle.getSelectedItem() == ReportStyle.INDIVIDUAL);
            userDefineTabs.setVisible(reportStyle.getSelectedItem() == ReportStyle.INDIVIDUAL);
        }
    }//GEN-LAST:event_reportStyleItemStateChanged

    void load() {
        Preferences preferences = NbPreferences.forModule(ConCurReportOptionsPanel.class);

        boolean oneHTML = preferences.getBoolean(JUST_ONE_HTML, DEFAULT_JUST_ONE_HTML);
        buttonOneHTML.setSelected(oneHTML);
        buttonHTMLforEach.setSelected(!oneHTML);

        String reportStyleName = preferences.get(REPORT_STYLE, DEFAULT_REPORT_STYLE);
        ReportStyle style = ReportStyle.valueOf(reportStyleName);
        reportStyle.setSelectedItem(style);

        boolean shortReport = preferences.getBoolean(INCLUDE_SHORT_REPORT, DEFAULT_INCLUDE_SHORT_REPORT);
        includeShortReport.setSelected(shortReport);
        includeShortReport.setVisible(style == ReportStyle.INDIVIDUAL);
        userDefineTabs.setVisible(style == ReportStyle.INDIVIDUAL);

        double autoCo = preferences.getDouble(AUTO_CORRELATION, DEFAULT_AUTO_CORRELATION);
        autoCorrelation.setValue(autoCo);

        int decimals = preferences.getInt(DECIMAL_PLACES, DEFAULT_DECIMAL_PLACES);
        decimalsTables.setValue(decimals);

        int tableTimespan = preferences.getInt(TIMESPAN_TABLE, DEFAULT_TIMESPAN_TABLE);
        tableSpinner.setValue(tableTimespan);

        int graphicTimespan = preferences.getInt(TIMESPAN_GRAPHIC, DEFAULT_TIMESPAN_GRAPHIC);
        graphicSpinner.setValue(graphicTimespan);

        loadUserDefined(preferences, USER_DEFINED_REPORT_CONTENT_X13, "", x13Selection, ALL_X11_TABLES);
        loadUserDefined(preferences, USER_DEFINED_REPORT_CONTENT_X13_TRANSFORMED, "", x13TransformSelection, ALL_X11_TABLES);
        loadUserDefined(preferences, USER_DEFINED_REPORT_CONTENT_X13_CHART_ONE, "", x13ChartOne, ALL_X11_TABLES);
        loadUserDefined(preferences, USER_DEFINED_REPORT_CONTENT_X13_CHART_TWO, "", x13ChartTwo, ALL_X11_TABLES);
        loadUserDefined(preferences, USER_DEFINED_REPORT_CONTENT_MAIN, "", mainSelection, MAIN_TABLES);
        loadUserDefined(preferences, USER_DEFINED_REPORT_CONTENT_D8B, "", d8bSelection, D8B);
        loadUserDefined(preferences, USER_DEFINED_REPORT_CONTENT_VALUE, "", valueSelection, VALUES);
        loadUserDefined(preferences, USER_DEFINED_REPORT_CONTENT_GRAPHIC, "", graphicSelection, GRAPHICS);
    }

    private void loadUserDefined(Preferences preferences, String option, String defaultResult, JListSelection selection, List<String> left) {
        String x13 = preferences.get(option, defaultResult);
        final List<String> rightElements = Arrays.asList(x13.split(";"));
        selection.getSourceModel().clear();
        left.stream().filter(x -> !rightElements.contains(x)).forEach(x -> selection.getSourceModel().addElement(x));
        selection.getTargetModel().clear();
        rightElements.stream().filter(x -> !"".equals(x)).forEach(x -> selection.getTargetModel().addElement(x));
    }

    void store() {
        Preferences preferences = NbPreferences.forModule(ConCurReportOptionsPanel.class);

        boolean oneHTML = buttonOneHTML.isSelected();
        preferences.putBoolean(JUST_ONE_HTML, oneHTML);

        ReportStyle selectedReportStyle = (ReportStyle) reportStyle.getSelectedItem();
        preferences.put(REPORT_STYLE, selectedReportStyle.name());

        boolean shortReport = includeShortReport.isSelected();
        preferences.putBoolean(INCLUDE_SHORT_REPORT, shortReport);

        Number autoCorrelationNumber = (Number) autoCorrelation.getValue();
        preferences.putDouble(AUTO_CORRELATION, autoCorrelationNumber.doubleValue());

        Number decimals = (Number) decimalsTables.getValue();
        preferences.putInt(DECIMAL_PLACES, decimals.intValue());

        Number tableTimespan = (Number) tableSpinner.getValue();
        preferences.putInt(TIMESPAN_TABLE, tableTimespan.intValue());

        Number graphicTimeSpan = (Number) graphicSpinner.getValue();
        preferences.putInt(TIMESPAN_GRAPHIC, graphicTimeSpan.intValue());

        String x13 = Arrays.stream(x13Selection.getTargetModel().toArray())
                .map(x -> x.toString())
                //.sorted(this::sortByPosition)
                .collect(Collectors.joining(";"));
        preferences.put(USER_DEFINED_REPORT_CONTENT_X13, x13);

        String x13Transformed = Arrays.stream(x13TransformSelection.getTargetModel().toArray())
                .map(x -> x.toString())
                .collect(Collectors.joining(";"));
        preferences.put(USER_DEFINED_REPORT_CONTENT_X13_TRANSFORMED, x13Transformed);

        String x13ChartOneString = Arrays.stream(x13ChartOne.getTargetModel().toArray())
                .map(x -> x.toString())
                .collect(Collectors.joining(";"));
        preferences.put(USER_DEFINED_REPORT_CONTENT_X13_CHART_ONE, x13ChartOneString);

        String x13ChartTwoString = Arrays.stream(x13ChartTwo.getTargetModel().toArray())
                .map(x -> x.toString())
                .collect(Collectors.joining(";"));
        preferences.put(USER_DEFINED_REPORT_CONTENT_X13_CHART_TWO, x13ChartTwoString);

        String main = Arrays.stream(mainSelection.getTargetModel().toArray())
                .map(x -> x.toString())
                .collect(Collectors.joining(";"));
        preferences.put(USER_DEFINED_REPORT_CONTENT_MAIN, main);

        String d8b = Arrays.stream(d8bSelection.getTargetModel().toArray())
                .map(x -> x.toString())
                .collect(Collectors.joining(";"));
        preferences.put(USER_DEFINED_REPORT_CONTENT_D8B, d8b);

        String value = Arrays.stream(valueSelection.getTargetModel().toArray())
                .map(x -> x.toString())
                .collect(Collectors.joining(";"));
        preferences.put(USER_DEFINED_REPORT_CONTENT_VALUE, value);

        String graphic = Arrays.stream(graphicSelection.getTargetModel().toArray())
                .map(x -> x.toString())
                .collect(Collectors.joining(";"));
        preferences.put(USER_DEFINED_REPORT_CONTENT_GRAPHIC, graphic);

    }

    public static final String JUST_ONE_HTML = "oneHTML";
    public static final String REPORT_STYLE = "reportStyle";
    public static final String INCLUDE_SHORT_REPORT = "include_short_report";
    public static final String AUTO_CORRELATION = "auto_correlation";
    public static final String DECIMAL_PLACES = "decimal_places";
    public static final String TIMESPAN_TABLE = "timespan_table";
    public static final String TIMESPAN_GRAPHIC = "timespan_graphic";
    public static final String USER_DEFINED_REPORT_CONTENT_X13 = "user_defined_report_content_x13";
    public static final String USER_DEFINED_REPORT_CONTENT_X13_TRANSFORMED = "user_defined_report_content_x13_transformed";
    public static final String USER_DEFINED_REPORT_CONTENT_X13_CHART_ONE = "user_defined_report_content_x13_chart_one";
    public static final String USER_DEFINED_REPORT_CONTENT_X13_CHART_TWO = "user_defined_report_content_x13_chart_two";
    public static final String USER_DEFINED_REPORT_CONTENT_MAIN = "user_defined_report_content_main";
    public static final String USER_DEFINED_REPORT_CONTENT_D8B = "user_defined_report_content_d8b";
    public static final String USER_DEFINED_REPORT_CONTENT_VALUE = "user_defined_report_content_value";
    public static final String USER_DEFINED_REPORT_CONTENT_GRAPHIC = "user_defined_report_content_graphic";

    public static final String DEFAULT_REPORT_STYLE = ReportStyle.SHORT.name();
    public static final boolean DEFAULT_INCLUDE_SHORT_REPORT = false;
    public static final boolean DEFAULT_JUST_ONE_HTML = true;
    public static final double DEFAULT_AUTO_CORRELATION = 1;
    public static final int DEFAULT_DECIMAL_PLACES = 2;
    public static final int DEFAULT_TIMESPAN_TABLE = 10;
    public static final int DEFAULT_TIMESPAN_GRAPHIC = 5;

    boolean valid() {
        return true;
    }

//    private int sortByPosition(String o1, String o2) {
//        Integer o1Position = POSITION.getOrDefault(o1, -1);
//        Integer o2Position = POSITION.getOrDefault(o2, -1);
//        return Integer.compare(o1Position, o2Position);
//    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSpinner autoCorrelation;
    private javax.swing.ButtonGroup buttonGroup;
    private javax.swing.JRadioButton buttonHTMLforEach;
    private javax.swing.JRadioButton buttonOneHTML;
    private javax.swing.JSpinner decimalsTables;
    private javax.swing.JSpinner graphicSpinner;
    private javax.swing.JCheckBox includeShortReport;
    private javax.swing.JPanel reportPanel;
    private javax.swing.JComboBox<de.bbk.concurreport.ReportStyle> reportStyle;
    private javax.swing.JSpinner tableSpinner;
    private javax.swing.JTabbedPane userDefineTabs;
    // End of variables declaration//GEN-END:variables
}

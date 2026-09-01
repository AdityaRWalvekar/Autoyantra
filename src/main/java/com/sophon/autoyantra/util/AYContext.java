package com.sophon.autoyantra.util;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class AYContext {
    private static AYContext instance;

    public Map<String, JComponent> currentWidgets = new LinkedHashMap<>();
    public Map<String, String> currentParamTypes = new LinkedHashMap<>();
    public Map<String, String> currentParamMin = new LinkedHashMap<>();
    public Map<String, String> currentParamMax = new LinkedHashMap<>();
    public Map<String, String> currentParamDescriptions = new LinkedHashMap<>(); // <-- ADDED
    
    public String currentModuleName;
    public String workspacePath = System.getProperty("user.dir");
    public Map<String, Map<String, String>> projectValues = new HashMap<>();
    public boolean isModified = false;

    public JTextArea descriptionArea;
    public JPanel moduleExplorerPanel;
    public DefaultListModel<String> generationErrorsModel;
    public DefaultListModel<String> configIssuesModel;
    public javax.swing.JLabel statusLabel;

    private AYContext() {}

    public static AYContext getInstance() {
        if (instance == null) instance = new AYContext();
        return instance;
    }

    public void clearModuleState() {
        currentWidgets.clear(); 
        currentParamTypes.clear();
        currentParamMin.clear(); 
        currentParamMax.clear();
        currentParamDescriptions.clear(); // <-- ADDED
        currentModuleName = null;
    }

    public void markModified() {
        this.isModified = true;
        updateStatus();
    }

    public void clearModified() {
        this.isModified = false;
        updateStatus();
    }

    private void updateStatus() {
        if (this.statusLabel != null) {
            String mod = this.currentModuleName != null ? this.currentModuleName : "None";
            String status = this.isModified ? " | [Modified *]" : " | [Saved]";
            this.statusLabel.setText(" Module: " + mod + "  |  Workspace: " + this.workspacePath + status);
        }
    }

    public String getWidgetValue(JComponent comp) {
        if (comp instanceof JCheckBox) return String.valueOf(((JCheckBox) comp).isSelected());
        if (comp instanceof JTextField) return ((JTextField) comp).getText();
        if (comp instanceof JComboBox) return String.valueOf(((JComboBox<?>) comp).getSelectedItem());
        return "";
    }
}
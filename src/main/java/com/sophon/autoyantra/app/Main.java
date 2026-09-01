package com.sophon.autoyantra.app;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Image;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.tree.DefaultMutableTreeNode;
import com.formdev.flatlaf.FlatLightLaf;
import com.sophon.autoyantra.action.AYActionManager;
import com.sophon.autoyantra.event.AYEventManager;
import com.sophon.autoyantra.generator.ConfigGenerator;
import com.sophon.autoyantra.parser.DbcParser;
import com.sophon.autoyantra.parser.EpdSchemaLoader;
import com.sophon.autoyantra.parser.SystemDescParser;
import com.sophon.autoyantra.project.ProjectManager;
import com.sophon.autoyantra.util.AYContext;

public class Main {
    static AYActionManager actionManager = new AYActionManager();
    static Map<String, String> nodeFolderMap = new HashMap<>();

    public static void main(String[] args) {
        FlatLightLaf.setup();
        AYContext ctx = AYContext.getInstance();
        ctx.generationErrorsModel = new DefaultListModel<>();
        ctx.configIssuesModel = new DefaultListModel<>();

        // 1. REGISTER ACTIONS (Using the new AYAction interface)
        actionManager.mapAction("project.generate", (context) -> {
            ConfigGenerator.generateConfig(context.currentModuleName, context.currentWidgets, context.generationErrorsModel);
        });

        actionManager.mapAction("project.verify", (context) -> {
            ConfigGenerator.verifyProject(context.currentWidgets, context.currentParamTypes, context.currentParamMin, context.currentParamMax, context.configIssuesModel);
        });

        // 2. REGISTER EVENT LISTENERS
        AYEventManager.getInstance().addListener("module.loaded", () -> {
            if (ctx.moduleExplorerPanel != null) {
                ctx.moduleExplorerPanel.revalidate();
                ctx.moduleExplorerPanel.repaint();
            }
        });

        // 3. BUILD UI
        JFrame f = new JFrame("AutoYantra Configuration Tool");
        f.setSize(1200, 800);
        f.setLayout(new BorderLayout());
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JMenuBar mb = new JMenuBar();
        JMenu filemenu = new JMenu("File");
        JMenu filesubmenu = new JMenu("New");
        
        JMenuItem i1 = new JMenuItem("Start New Project");
        i1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
        JMenuItem i2 = new JMenuItem("Open Existing Project");
        i2.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        JMenuItem i3 = new JMenuItem("Save Project");
        i3.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        JMenuItem i6 = new JMenuItem("Choose Workspace");
        JMenuItem i7 = new JMenuItem("Exit");
        i7.addActionListener(e -> System.exit(0));
        
        filemenu.add(filesubmenu); filesubmenu.add(i1); filesubmenu.add(i2); filemenu.add(i3);
        
        JMenu importMenu = new JMenu("Import");
        JMenuItem importXml = new JMenuItem("Import Configuration XML...");
        importXml.addActionListener(e -> importConfigXml(f, ctx));
        importMenu.add(importXml); filemenu.add(importMenu);
        JMenuItem importDbc = new JMenuItem("Import CAN DBC...");
        importDbc.addActionListener(e -> importCanDbc(f, ctx));
        importMenu.add(importDbc);
        JMenuItem importArxml = new JMenuItem("Import System Description (ARXML)...");
        importArxml.addActionListener(e -> importSystemDescription(f, ctx));
        importMenu.add(importArxml);
        
        filemenu.add(i6); filemenu.addSeparator(); filemenu.add(i7);
        mb.add(filemenu);

        JMenu editMenu = new JMenu("Edit");
        editMenu.add(new JMenuItem("Find"));
        mb.add(editMenu);

        JMenu searchMenu = new JMenu("Search");
        JMenuItem searchParam = new JMenuItem("Search Parameter...");
        searchParam.addActionListener(e -> showSearchDialog(f, ctx));
        JMenuItem clearSearch = new JMenuItem("Clear Search");
        clearSearch.addActionListener(e -> filterModules("", ctx));
        searchMenu.add(searchParam); searchMenu.add(clearSearch);
        mb.add(searchMenu);

        JMenu projectMenu = new JMenu("Project");
        JMenuItem projectVerify = new JMenuItem("Verify Project");
        projectVerify.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0));
        projectVerify.addActionListener(e -> actionManager.execute("project.verify", ctx)); // Pass ctx here
        
        JMenuItem projectGenerate = new JMenuItem("Generate Project");
        projectGenerate.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_DOWN_MASK));
        projectGenerate.addActionListener(e -> actionManager.execute("project.generate", ctx)); // Pass ctx here
        
        projectMenu.add(projectVerify); projectMenu.add(projectGenerate);
        mb.add(projectMenu);

        JMenu helpMenu = new JMenu("Help");
        JMenuItem helpAbout = new JMenuItem("About AutoYantra");
        helpAbout.addActionListener(e -> JOptionPane.showMessageDialog(f, "AutoYantra Configuration Tool\nVersion 1.0"));
        helpMenu.add(helpAbout); mb.add(helpMenu);
        f.setJMenuBar(mb);

        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        JButton newBtn = new JButton(new ImageIcon(new ImageIcon("icons/new_file.png").getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH)));
        JButton openBtn = new JButton(new ImageIcon(new ImageIcon("icons/folder.png").getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH)));
        JButton saveBtn = new JButton(new ImageIcon(new ImageIcon("icons/save.png").getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH)));
        JButton generateBtn = new JButton(new ImageIcon(new ImageIcon("icons/generate.png").getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH)));
        toolBar.add(newBtn); toolBar.add(openBtn); toolBar.add(saveBtn); toolBar.addSeparator(); toolBar.add(generateBtn);
        f.add(toolBar, BorderLayout.NORTH);

        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Project");
        File modulesFolder = new File(ctx.workspacePath, "modules");
        File[] entries = modulesFolder.listFiles();
        nodeFolderMap.clear(); 
        
        if (entries != null) {
            for (File entry : entries) {
                if (entry.isDirectory()) {
                    String displayName = entry.getName();
                    File infoFile = new File(entry, "module_info.xml");
                    if (infoFile.exists()) {
                        try {
                            org.w3c.dom.Document doc = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(infoFile);
                            String name = doc.getElementsByTagName("name").getLength() > 0 ? doc.getElementsByTagName("name").item(0).getTextContent() : entry.getName();
                            String version = doc.getElementsByTagName("version").getLength() > 0 ? " v" + doc.getElementsByTagName("version").item(0).getTextContent() : "";
                            displayName = name + version;
                        } catch (Exception e) {}
                    }
                    nodeFolderMap.put(displayName, entry.getName());
                    root.add(new DefaultMutableTreeNode(displayName));
                }
            }
        }

        JTree projectExplorerTree = new JTree(root);
        projectExplorerTree.setShowsRootHandles(true);
        JPanel projectExplorerPanel = new JPanel(new BorderLayout());
        projectExplorerPanel.setBorder(BorderFactory.createTitledBorder("Project Explorer"));
        JScrollPane projectScroll = new JScrollPane(projectExplorerTree);
        projectScroll.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4)); // Adds breathing room
         projectScroll.getVerticalScrollBar().setUnitIncrement(20);
        projectScroll.getVerticalScrollBar().setUnitIncrement(20);
        projectExplorerPanel.add(projectScroll, BorderLayout.CENTER);

        ctx.moduleExplorerPanel = new JPanel();
        ctx.moduleExplorerPanel.setLayout(new BoxLayout(ctx.moduleExplorerPanel, BoxLayout.Y_AXIS));
        JPanel moduleExplorerWrapper = new JPanel(new BorderLayout());
        moduleExplorerWrapper.setBorder(BorderFactory.createTitledBorder("Module Explorer"));
        JScrollPane moduleScroll = new JScrollPane(ctx.moduleExplorerPanel);
        moduleScroll.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4)); // Adds breathing room
        moduleScroll.getVerticalScrollBar().setUnitIncrement(20);
        moduleScroll.getVerticalScrollBar().setUnitIncrement(20);
        moduleExplorerWrapper.add(moduleScroll, BorderLayout.CENTER);
        
        JTabbedPane infoTabs = new JTabbedPane();
        JScrollPane errorsScroll = new JScrollPane(new JList<>(ctx.generationErrorsModel));
        errorsScroll.getVerticalScrollBar().setUnitIncrement(20);
        infoTabs.addTab("Generation Errors", errorsScroll);
        JScrollPane issuesScroll = new JScrollPane(new JList<>(ctx.configIssuesModel));
        issuesScroll.getVerticalScrollBar().setUnitIncrement(20);
        infoTabs.addTab("Config Issues", issuesScroll);
        JPanel infoExplorerPanel = new JPanel(new BorderLayout());
        infoExplorerPanel.setBorder(BorderFactory.createTitledBorder("Information Explorer"));
        infoExplorerPanel.add(infoTabs, BorderLayout.CENTER);

        ctx.descriptionArea = new JTextArea();
        ctx.descriptionArea.setEditable(false);
        ctx.descriptionArea.setLineWrap(true);
        ctx.descriptionArea.setWrapStyleWord(true);
        ctx.descriptionArea.setText("Select a module from the Project Explorer to begin configuration.");
        JPanel descriptionPanel = new JPanel(new BorderLayout());
        descriptionPanel.setBorder(BorderFactory.createTitledBorder("Description"));
        JScrollPane descScroll = new JScrollPane(ctx.descriptionArea);
        descScroll.getVerticalScrollBar().setUnitIncrement(20);
        descriptionPanel.add(descScroll, BorderLayout.CENTER);

        JSplitPane middleSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, moduleExplorerWrapper, infoExplorerPanel);
        middleSplit.setResizeWeight(0.75);
        JSplitPane rightSideSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, middleSplit, descriptionPanel);
        rightSideSplit.setResizeWeight(0.75);
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, projectExplorerPanel, rightSideSplit);
        mainSplit.setResizeWeight(0.20);
        f.add(mainSplit, BorderLayout.CENTER);

                // ==================== STATUS BAR ====================
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        statusBar.setBackground(new Color(245, 245, 245)); // Light gray background
        
        JLabel statusLabel = new JLabel(" Ready");
        statusLabel.setFont(statusLabel.getFont().deriveFont(12f));
        statusBar.add(statusLabel, BorderLayout.WEST);
        
        // Update this label dynamically later
        ctx.statusLabel = statusLabel; 
        
        f.add(statusBar, BorderLayout.SOUTH);

        projectExplorerTree.addTreeSelectionListener(e -> {
            Object selectedNode = projectExplorerTree.getLastSelectedPathComponent();
            if (selectedNode == null || selectedNode == root) return;
            
            String displayName = selectedNode.toString();
            ctx.currentModuleName = nodeFolderMap.getOrDefault(displayName, displayName);
               // Update the status bar instantly when a module is clicked
             if (ctx.statusLabel != null) {
                 ctx.statusLabel.setText(" Module: " + ctx.currentModuleName + "  |  Workspace: " + ctx.workspacePath);
             }
            
            setDescriptionText("Module: " + ctx.currentModuleName + "\n\nSelect a parameter to view description.", ctx);

            String schemaPath = ctx.workspacePath + "/modules/" + ctx.currentModuleName + "/xml/" + ctx.currentModuleName + ".xml";
            String epdPath = ctx.workspacePath + "/modules/" + ctx.currentModuleName + "/" + ctx.currentModuleName + ".epd";
            
                        EpdSchemaLoader parser = new EpdSchemaLoader();
            boolean loaded = false;

            try {
                if (new File(schemaPath).exists() && parser.canParse(schemaPath)) {
                    parser.parse(schemaPath, ctx.moduleExplorerPanel, ctx);
                    loaded = true;
                } else if (new File(epdPath).exists() && parser.canParse(epdPath)) {
                    parser.parse(epdPath, ctx.moduleExplorerPanel, ctx);
                    loaded = true;
                }
            } catch (Exception ex) {
                ctx.moduleExplorerPanel.removeAll();
                ctx.moduleExplorerPanel.add(new JLabel("Error loading schema: " + ex.getMessage()));
            }

            if (loaded) {
                ctx.markModified(); // Mark as modified when a module is loaded
                AYEventManager.getInstance().fireEvent("module.loaded");
            } else {
                ctx.moduleExplorerPanel.removeAll();
                ctx.moduleExplorerPanel.add(new JLabel("No schema found for " + ctx.currentModuleName));
                AYEventManager.getInstance().fireEvent("module.loaded");
            }


        });

        javax.swing.JPopupMenu popup = new javax.swing.JPopupMenu();
        JMenuItem addModItem = new JMenuItem("Add Modules...");
        addModItem.addActionListener(ev -> {
            File currentModulesFolder = new File(ctx.workspacePath, "modules");
            String[] dirs = currentModulesFolder.list((d, n) -> new File(d, n).isDirectory());
            if (dirs == null || dirs.length == 0) {
                JOptionPane.showMessageDialog(f, "No modules found in workspace.");
                return;
            }
            String sel = (String) JOptionPane.showInputDialog(f, "Select Module:", "Module Selection", JOptionPane.PLAIN_MESSAGE, null, dirs, dirs[0]);
            if (sel != null) {
                nodeFolderMap.put(sel, sel);
                root.add(new DefaultMutableTreeNode(sel));
                projectExplorerTree.updateUI();
            }
        });
        popup.add(addModItem);
        projectExplorerTree.setComponentPopupMenu(popup);

        i1.addActionListener(e -> {
            root.removeAllChildren();
            nodeFolderMap.clear();
            projectExplorerTree.updateUI();
            ctx.clearModuleState();
            
            ctx.moduleExplorerPanel.removeAll(); ctx.moduleExplorerPanel.revalidate(); ctx.moduleExplorerPanel.repaint();
            ctx.generationErrorsModel.clear(); ctx.configIssuesModel.clear();
            ctx.clearModified();
            setDescriptionText("Start a new project by right-clicking the Project tree.", ctx);
        });

        i2.addActionListener(e -> {
            ctx.projectValues = ProjectManager.load(ctx.workspacePath + "/project.xml");
            root.removeAllChildren();
            nodeFolderMap.clear();
            for (String name : ctx.projectValues.keySet()) {
                nodeFolderMap.put(name, name);
                root.add(new DefaultMutableTreeNode(name));
            }
            projectExplorerTree.updateUI();
            ctx.clearModified();
            setDescriptionText("Project loaded successfully.", ctx);
            
        });

        i3.addActionListener(e -> {
            Map<String, String> vals = new HashMap<>();
            for (Map.Entry<String, javax.swing.JComponent> entry : ctx.currentWidgets.entrySet()) {
                vals.put(entry.getKey(), ctx.getWidgetValue(entry.getValue()));
            }
            if (ctx.currentModuleName != null) ctx.projectValues.put(ctx.currentModuleName, vals);
            ProjectManager.save(ctx.workspacePath + "/project.xml", "MyProject", ctx.projectValues);
            ctx.clearModified();
            JOptionPane.showMessageDialog(f, "Project saved successfully.");
        });

        i6.addActionListener(e -> {
            javax.swing.JFileChooser fc = new javax.swing.JFileChooser();
            fc.setFileSelectionMode(javax.swing.JFileChooser.DIRECTORIES_ONLY);
            if (fc.showOpenDialog(f) == javax.swing.JFileChooser.APPROVE_OPTION) {
                ctx.workspacePath = fc.getSelectedFile().getAbsolutePath();
                setDescriptionText("Workspace changed to: " + ctx.workspacePath, ctx);
            }
        });

        newBtn.addActionListener(e -> i1.doClick());
        openBtn.addActionListener(e -> i2.doClick());
        saveBtn.addActionListener(e -> i3.doClick());
        generateBtn.addActionListener(e -> actionManager.execute("project.generate", ctx)); // Pass ctx here

        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }

    public static void setDescriptionText(String text, AYContext ctx) {
        if (ctx.descriptionArea != null) ctx.descriptionArea.setText(text);
    }

    private static void importCanDbc(JFrame parent, AYContext ctx) {
        if (ctx.currentModuleName == null) {
            JOptionPane.showMessageDialog(parent, "Please select a module first!", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        javax.swing.JFileChooser fc = new javax.swing.JFileChooser();
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("DBC Files", "dbc"));
        if (fc.showOpenDialog(parent) == javax.swing.JFileChooser.APPROVE_OPTION) {
            try {
                List<DbcParser.DbcMessage> messages = DbcParser.parse(fc.getSelectedFile().getAbsolutePath());
                JOptionPane.showMessageDialog(parent, "Successfully imported " + messages.size() + " messages.", "DBC Import Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(parent, "Error parsing DBC: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static void importSystemDescription(JFrame parent, AYContext ctx) {
        if (ctx.currentModuleName == null) {
            JOptionPane.showMessageDialog(parent, "Please select a module first!", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        javax.swing.JFileChooser fc = new javax.swing.JFileChooser();
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("ARXML Files", "arxml", "xml"));
        if (fc.showOpenDialog(parent) == javax.swing.JFileChooser.APPROVE_OPTION) {
            try {
                Map<String, SystemDescParser.ArxmlConfig> configs = SystemDescParser.parse(fc.getSelectedFile().getAbsolutePath());
                JOptionPane.showMessageDialog(parent, "Successfully parsed " + configs.size() + " parameters.", "ARXML Import Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(parent, "Error parsing ARXML: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private static void showSearchDialog(JFrame parent, AYContext ctx) {
        String text = JOptionPane.showInputDialog(parent, "Enter parameter name to search:", "Search Parameters", JOptionPane.PLAIN_MESSAGE);
        if (text != null) filterModules(text.toLowerCase(), ctx);
    }

    private static void filterModules(String searchText, AYContext ctx) {
        for (java.awt.Component comp : ctx.moduleExplorerPanel.getComponents()) {
            if (comp instanceof JPanel) {
                boolean match = false;
                for (java.awt.Component child : ((JPanel) comp).getComponents()) {
                    if (child instanceof javax.swing.JLabel && ((javax.swing.JLabel) child).getText().toLowerCase().contains(searchText)) {
                        match = true; break;
                    }
                }
                comp.setVisible(searchText.isEmpty() || match);
            }
        }
        ctx.moduleExplorerPanel.revalidate();
        ctx.moduleExplorerPanel.repaint();
    }
    
    private static void importConfigXml(JFrame parent, AYContext ctx) {
        javax.swing.JFileChooser fc = new javax.swing.JFileChooser();
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("XML Files", "xml"));
        if (fc.showOpenDialog(parent) == javax.swing.JFileChooser.APPROVE_OPTION) {
            try {
                org.w3c.dom.Document doc = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(fc.getSelectedFile());
                org.w3c.dom.NodeList params = doc.getElementsByTagName("param");
                int count = 0;
                for (int i = 0; i < params.getLength(); i++) {
                    org.w3c.dom.Element param = (org.w3c.dom.Element) params.item(i);
                    String name = param.getAttribute("name");
                    String value = param.getTextContent();
                    javax.swing.JComponent widget = ctx.currentWidgets.get(name);
                    if (widget != null) {
                        if (widget instanceof javax.swing.JCheckBox) ((javax.swing.JCheckBox) widget).setSelected(Boolean.parseBoolean(value));
                        else if (widget instanceof javax.swing.JTextField) ((javax.swing.JTextField) widget).setText(value);
                        else if (widget instanceof javax.swing.JComboBox) ((javax.swing.JComboBox<?>) widget).setSelectedItem(value);
                        count++;
                    }
                }
                JOptionPane.showMessageDialog(parent, "Successfully imported " + count + " parameters.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(parent, "Error importing XML: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
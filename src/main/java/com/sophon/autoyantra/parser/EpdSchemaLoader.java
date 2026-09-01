package com.sophon.autoyantra.parser;

import com.sophon.autoyantra.util.AYContext;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class EpdSchemaLoader implements AYParser {

    @Override
    public boolean canParse(String fileName) {
        String lower = fileName.toLowerCase();
        return lower.endsWith(".epd") || lower.endsWith(".xml");
    }

    @Override
    public void parse(String filePath, JPanel targetPanel, AYContext ctx) throws Exception {
        ctx.currentWidgets.clear();
        ctx.currentParamTypes.clear();
        ctx.currentParamMin.clear();
        ctx.currentParamMax.clear();
        ctx.currentParamDescriptions.clear();
        targetPanel.removeAll();

        File schemaFile = new File(filePath);
        if (!schemaFile.exists()) {
            targetPanel.add(new JLabel("Error: File not found: " + filePath));
            targetPanel.revalidate();
            targetPanel.repaint();
            return;
        }

        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(schemaFile);
        String[] tags = {
            "ECUC-INTEGER-PARAM-DEF",
            "ECUC-BOOLEAN-PARAM-DEF",
            "ECUC-ENUMERATION-PARAM-DEF",
            "ECUC-FLOAT-PARAM-DEF",
            "ECUC-STRING-PARAM-DEF"
        };

        for (String tag : tags) {
            NodeList nodes = doc.getElementsByTagName(tag);
            for (int i = 0; i < nodes.getLength(); i++) {
                Element el = (Element) nodes.item(i);
                NodeList sn = el.getElementsByTagName("SHORT-NAME");
                if (sn.getLength() == 0) continue;

                String name = sn.item(0).getTextContent();
                String type = "int";
                String def = "";

                if (tag.contains("BOOLEAN")) {
                    type = "bool";
                    NodeList d = el.getElementsByTagName("DEFAULT-VALUE");
                    if (d.getLength() > 0) def = d.item(0).getTextContent();
                } else if (tag.contains("ENUMERATION")) {
                    type = "enum";
                    NodeList d = el.getElementsByTagName("DEFAULT-VALUE");
                    if (d.getLength() > 0) def = d.item(0).getTextContent();
                }

                // Extract description for the right panel
                String desc = "Type: " + type + " | Default: " + def;
                NodeList defRef = el.getElementsByTagName("DEFINITION-REF");
                if (defRef.getLength() > 0) {
                    desc = defRef.item(0).getTextContent();
                } else {
                    NodeList longName = el.getElementsByTagName("LONG-NAME");
                    if (longName.getLength() > 0) {
                        desc = longName.item(0).getTextContent();
                    }
                }
                ctx.currentParamDescriptions.put(name, desc);
                ctx.currentParamTypes.put(name, type);

                // Build the clean UI row
                JPanel row = new JPanel(new BorderLayout());
                row.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
                row.setOpaque(false);

                JLabel label = new JLabel(name + ":");
                label.setPreferredSize(new java.awt.Dimension(200, 24));
                row.add(label, BorderLayout.WEST);

                if (type.equals("bool")) {
                    JCheckBox cb = new JCheckBox();
                    cb.setSelected(Boolean.parseBoolean(def));
                    JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
                    inputPanel.setOpaque(false);
                    inputPanel.add(cb);
                    row.add(inputPanel, BorderLayout.CENTER);
                    ctx.currentWidgets.put(name, cb);

                } else if (type.equals("enum")) {
                    JComboBox<String> combo = new JComboBox<>();
                    NodeList lits = el.getElementsByTagName("ECUC-ENUMERATION-LITERAL-DEF");
                    for (int j = 0; j < lits.getLength(); j++) {
                        NodeList ln = ((Element) lits.item(j)).getElementsByTagName("SHORT-NAME");
                        if (ln.getLength() > 0) combo.addItem(ln.item(0).getTextContent());
                    }
                    if (combo.getItemCount() == 0 && !def.isEmpty()) combo.addItem(def);
                    combo.setSelectedItem(def);
                    JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
                    inputPanel.setOpaque(false);
                    inputPanel.add(combo);
                    row.add(inputPanel, BorderLayout.CENTER);
                    ctx.currentWidgets.put(name, combo);

                } else {
                    String min = "";
                    String max = "";
                    NodeList minN = el.getElementsByTagName("MIN");
                    if (minN.getLength() > 0) min = minN.item(0).getTextContent();
                    NodeList maxN = el.getElementsByTagName("MAX");
                    if (maxN.getLength() > 0) max = maxN.item(0).getTextContent();

                    ctx.currentParamMin.put(name, min);
                    ctx.currentParamMax.put(name, max);

                    JTextField tf = new JTextField(def, 15);
                    if (!min.isEmpty() || !max.isEmpty()) {
                        tf.setToolTipText("Range: " + min + " to " + max);
                    }
                    JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
                    inputPanel.setOpaque(false);
                    inputPanel.add(tf);
                    row.add(inputPanel, BorderLayout.CENTER);
                    ctx.currentWidgets.put(name, tf);
                }

                // Click listener to show description in the right panel
                final String paramName = name;
                final String paramDesc = desc;
                row.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mousePressed(java.awt.event.MouseEvent e) {
                        if (ctx.descriptionArea != null) {
                            ctx.descriptionArea.setText(
                                "Parameter: " + paramName + "\n\n" +
                                "Description:\n" + paramDesc
                            );
                        }
                    }
                });

                targetPanel.add(row);
            }
        }

        targetPanel.revalidate();
        targetPanel.repaint();
        if (targetPanel.getParent() != null) {
            targetPanel.getParent().revalidate();
            targetPanel.getParent().repaint();
        }
    }
}
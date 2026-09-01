package com.sophon.autoyantra.project;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ProjectManager {
    public static void save(String path, String projName, Map<String, Map<String, String>> values) {
        try (FileWriter w = new FileWriter(path)) {
            w.write("<project name=\"" + projName + "\">\n");
            for (Map.Entry<String, Map<String, String>> mod : values.entrySet()) {
                w.write("  <module name=\"" + mod.getKey() + "\">\n");
                for (Map.Entry<String, String> p : mod.getValue().entrySet())
                    w.write("    <p n=\"" + p.getKey() + "\" v=\"" + p.getValue() + "\"/>\n");
                w.write("  </module>\n");
            }
            w.write("</project>");
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static Map<String, Map<String, String>> load(String path) {
        Map<String, Map<String, String>> res = new HashMap<>();
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(path));
            NodeList mods = doc.getElementsByTagName("module");
            for (int i = 0; i < mods.getLength(); i++) {
                Element modEl = (Element) mods.item(i);
                Map<String, String> vals = new HashMap<>();
                NodeList params = modEl.getElementsByTagName("p");
                for (int j = 0; j < params.getLength(); j++) {
                    Element pEl = (Element) params.item(j);
                    vals.put(pEl.getAttribute("n"), pEl.getAttribute("v"));
                }
                res.put(modEl.getAttribute("name"), vals);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return res;
    }
}
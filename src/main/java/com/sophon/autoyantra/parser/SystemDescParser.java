package com.sophon.autoyantra.parser;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class SystemDescParser {

    public static class ArxmlConfig {
        public String paramName;
        public String paramValue;
        public String toString() { return paramName + " = " + paramValue; }
    }

    public static Map<String, ArxmlConfig> parse(String path) throws Exception {
        Map<String, ArxmlConfig> configs = new HashMap<>();
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(path));
        NodeList all = doc.getElementsByTagName("*");
        for (int i = 0; i < all.getLength(); i++) {
            Element el = (Element) all.item(i);
            NodeList sn = el.getElementsByTagName("SHORT-NAME");
            NodeList vv = el.getElementsByTagName("VALUE");
            if (sn.getLength() > 0 && vv.getLength() > 0) {
                ArxmlConfig c = new ArxmlConfig();
                c.paramName = sn.item(0).getTextContent().trim();
                c.paramValue = vv.item(0).getTextContent().trim();
                configs.put(c.paramName, c);
            }
        }
        return configs;
    }
}
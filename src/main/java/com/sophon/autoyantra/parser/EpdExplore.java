package com.sophon.autoyantra.parser;
import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class EpdExplore {
    public static void main(String[] args) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new File("Fls.epd"));

        printParams(doc, "ECUC-INTEGER-PARAM-DEF", "int");
        printParams(doc, "ECUC-BOOLEAN-PARAM-DEF", "bool");
        printParams(doc, "ECUC-STRING-PARAM-DEF", "string");
        printParams(doc, "ECUC-FLOAT-PARAM-DEF", "float");
    }

static void printParams(Document doc, String tagName, String label) 
{
    NodeList nodes = doc.getElementsByTagName(tagName);
    for (int i = 0; i < nodes.getLength(); i++) 
    {
        Element param = (Element) nodes.item(i);
        String name = getChildText(param, "SHORT-NAME");
        String defaultValue = getChildText(param, "DEFAULT-VALUE");
        String min = getChildText(param, "MIN");
        String max = getChildText(param, "MAX");

        System.out.println(label + ": " + name
                + " | default=" + defaultValue
                + " | min=" + min
                + " | max=" + max);
    }
}

static String getChildText(Element parent, String tagName) 
{
    NodeList nodes = parent.getElementsByTagName(tagName);
    if (nodes.getLength() > 0) 
        {
            return nodes.item(0).getTextContent();
        }
    return "(none)";
}
}
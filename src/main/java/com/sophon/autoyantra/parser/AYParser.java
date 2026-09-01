package com.sophon.autoyantra.parser;

import com.sophon.autoyantra.util.AYContext;
import javax.swing.JPanel;

public interface AYParser {
    boolean canParse(String fileName);
    void parse(String filePath, JPanel targetPanel, AYContext ctx) throws Exception;
}
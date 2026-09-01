package com.sophon.autoyantra.parser;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class DbcParser {

    public static class DbcSignal {
        public String name;
        public String toString() { return "  SG_ " + name; }
    }

    public static class DbcMessage {
        public String name;
        public List<DbcSignal> signals = new ArrayList<>();
        public String toString() { return "BO_ " + name + " (" + signals.size() + " signals)"; }
    }

    public static List<DbcMessage> parse(String path) throws Exception {
        List<DbcMessage> messages = new ArrayList<>();
        DbcMessage current = null;
        for (String line : Files.readAllLines(Paths.get(path))) {
            String t = line.trim();
            if (t.startsWith("BO_ ")) {
                current = new DbcMessage();
                String[] parts = t.split(" ");
                current.name = parts.length > 2 ? parts[2].replace(":", "") : "unknown";
                messages.add(current);
            } else if (t.startsWith("SG_ ") && current != null) {
                DbcSignal sig = new DbcSignal();
                String[] parts = t.split(" ");
                sig.name = parts.length > 1 ? parts[1] : "unknown";
                current.signals.add(sig);
            }
        }
        return messages;
    }
}
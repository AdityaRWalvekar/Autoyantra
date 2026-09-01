package com.sophon.autoyantra.action;

import java.util.HashMap;
import java.util.Map;

public class AYActionManager {
    private Map<String, AYAction> actions = new HashMap<>();

    // Now we register Interfaces, not Runnables!
    public void mapAction(String id, AYAction action) {
        actions.put(id, action);
    }

    public void execute(String id, com.sophon.autoyantra.util.AYContext ctx) {
        AYAction action = actions.get(id);
        if (action != null) {
            action.execute(ctx); // Pass the context to the action
        } else {
            System.out.println("Error: No action found for " + id);
        }
    }
}
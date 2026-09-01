package com.sophon.autoyantra.app;

import com.sophon.autoyantra.action.AYActionManager;
import com.sophon.autoyantra.action.impl.project.GenerateProjectAction;
import com.sophon.autoyantra.action.impl.project.VerifyProjectAction;
import com.sophon.autoyantra.util.AYContext;

public class ActionInitializer {
    public static void init(AYContext ctx, AYActionManager actionManager) {
        // We just pass the new tiny classes here!
        actionManager.mapAction("project.generate", new GenerateProjectAction());
        actionManager.mapAction("project.verify", new VerifyProjectAction());
    }
}
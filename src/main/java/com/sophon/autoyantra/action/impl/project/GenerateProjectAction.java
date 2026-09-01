package com.sophon.autoyantra.action.impl.project;

import com.sophon.autoyantra.action.AYAction;
import com.sophon.autoyantra.generator.ConfigGenerator;
import com.sophon.autoyantra.util.AYContext;

public class GenerateProjectAction implements AYAction {
    @Override
    public void execute(AYContext ctx) {
        ConfigGenerator.generateConfig(ctx.currentModuleName, ctx.currentWidgets, ctx.generationErrorsModel);
    }
}
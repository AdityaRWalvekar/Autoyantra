package com.sophon.autoyantra.action.impl.project;

import com.sophon.autoyantra.action.AYAction;
import com.sophon.autoyantra.generator.ConfigGenerator;
import com.sophon.autoyantra.util.AYContext;

public class VerifyProjectAction implements AYAction {
    @Override
    public void execute(AYContext ctx) {
        ConfigGenerator.verifyProject(ctx.currentWidgets, ctx.currentParamTypes, ctx.currentParamMin, ctx.currentParamMax, ctx.configIssuesModel);
    }
}
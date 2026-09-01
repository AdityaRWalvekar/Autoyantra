package com.sophon.autoyantra.action;

import com.sophon.autoyantra.util.AYContext;

// This is the "Contract". Every action MUST implement this.
public interface AYAction {
    void execute(AYContext ctx);
}
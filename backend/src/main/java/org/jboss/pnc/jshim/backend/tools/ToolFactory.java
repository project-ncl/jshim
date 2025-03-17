package org.jboss.pnc.jshim.backend.tools;

import java.util.HashMap;
import java.util.Map;

public class ToolFactory {

    private static final BasicTool[] TOOLS = {
            new MavenTool(),
            new GradleTool(),
            new NodeTool(),
            new SbtTool(),
            new GolangTool(),
            new JavaTool(),
    };

    private static final Map<String, BasicTool> TOOLS_MAP = new HashMap<>();

    static {
        for (BasicTool tool : TOOLS) {
            TOOLS_MAP.put(tool.name(), tool);
        }
    }

    public static BasicTool getTool(String toolToFind) throws ToolNotFoundException {
        BasicTool tool = TOOLS_MAP.get(toolToFind);
        if (tool == null) {
            throw new ToolNotFoundException();
        }
        return tool;
    }

    public static Map<String, BasicTool> getToolsMap() {
        return TOOLS_MAP;
    }

    public static class ToolNotFoundException extends Exception {
    }

}

package org.jboss.pnc.jshim.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import lombok.extern.slf4j.Slf4j;

import org.jboss.pnc.jshim.constants.DefaultConstants;
import org.jboss.pnc.jshim.tools.BasicTool;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

@Slf4j
public class Hook {
    /**
     * hook script should be in the hook folder, named: {tool}-postdownload.groovy
     *
     * @param tool
     * @param version
     */
    public static void runPostDownloadHookScript(BasicTool tool, String version) throws IOException {

        Path jarHome = FilesCommon.getJarFolder();
        Path hookFolder = jarHome.resolve(DefaultConstants.HOOK_FOLDER);
        Path toolPostHook = hookFolder.resolve(tool.name() + DefaultConstants.HOOK_POSTDOWNLOAD_SUFFIX);

        if (!Files.exists(toolPostHook)) {
            log.debug("Postdownload script doesnt exist: '{}'", toolPostHook.toAbsolutePath());
            // if the hook file doesn't exist, nothing to do
            return;
        }
        log.info("Running postdownload script '{}'", toolPostHook);
        GroovyShell shell = new GroovyShell(getBinding(tool, version));
        shell.run(toolPostHook.toFile(), new ArrayList<>());
    }

    private static Binding getBinding(BasicTool tool, String version) {

        Binding b = new Binding();
        b.setVariable("TOOL_NAME", tool.name());
        b.setVariable("TOOL_VERSION", version);
        b.setVariable("TOOL_HOME", BasicTool.homeFolder(tool, version).toAbsolutePath());

        return b;
    }
}

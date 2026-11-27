package org.jboss.pnc.jshim.backend.tools;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.text.StringSubstitutor;
import org.jboss.pnc.jshim.backend.common.NpmRegistry;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NpmTool implements BasicTool {

    private static final String DOWNLOAD_URL = "https://registry.npmjs.org/npm/-/npm-${version}.tgz";

    private static final Map<String, String> COMMANDS = Map.of("npm", "npm-cli.js", "npx", "npx-cli.js");

    @Override
    public String name() {
        return "npm";
    }

    @Override
    public String downloadUrl(String versionToDownload) {

        Map<String, String> valuesMap = new HashMap<>();
        valuesMap.put("version", versionToDownload);

        StringSubstitutor sub = new StringSubstitutor(valuesMap);

        return sub.replace(DOWNLOAD_URL);
    }

    /**
     * Only npm and npx get a shim, linked to bin/npm-cli.js and bin/npx-cli.js.
     * The other files in bin are Windows launchers and helpers. If npx is missing, it's skipped.
     */
    @Override
    public Map<String, Path> shims(Path home) {
        Map<String, Path> shims = new LinkedHashMap<>();
        Path binaryFolder = home.resolve(binaryFolderName());

        COMMANDS.forEach((command, script) -> {
            Path target = binaryFolder.resolve(script).toAbsolutePath();
            if (Files.isRegularFile(target) && Files.isExecutable(target)) {
                shims.put(command, target);
            }
        });

        return shims;
    }

    @Override
    public List<String> getDownloadableVersions() {
        try {
            return NpmRegistry.getStableVersions("npm");
        } catch (IOException e) {
            log.error("Error", e);
            throw new RuntimeException(e);
        }
    }
}

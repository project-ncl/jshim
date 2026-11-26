package org.jboss.pnc.jshim.backend.tools;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.text.StringSubstitutor;
import org.jboss.pnc.jshim.backend.common.NpmRegistry;

import com.github.zafarkhaja.semver.Version;

import lombok.extern.slf4j.Slf4j;

/**
 * Classic Yarn (1.x) is the yarn package
 * Newer Yarn (2.x and later) is the @yarnpkg/cli-dist package
 */
@Slf4j
public class YarnTool implements BasicTool {

    private static final String CLASSIC_PACKAGE = "yarn";
    private static final String MODERN_PACKAGE = "@yarnpkg/cli-dist";

    private static final String CLASSIC_DOWNLOAD_URL = "https://registry.npmjs.org/yarn/-/yarn-${version}.tgz";
    private static final String MODERN_DOWNLOAD_URL = "https://registry.npmjs.org/@yarnpkg/cli-dist/-/cli-dist-${version}.tgz";

    private static final Map<String, String> COMMANDS = Map.of("yarn", "yarn.js", "yarnpkg", "yarn.js");

    @Override
    public String name() {
        return "yarn";
    }

    @Override
    public String downloadUrl(String versionToDownload) {
        Map<String, String> valuesMap = new HashMap<>();
        valuesMap.put("version", versionToDownload);

        StringSubstitutor sub = new StringSubstitutor(valuesMap);

        return sub.replace(isClassic(versionToDownload) ? CLASSIC_DOWNLOAD_URL : MODERN_DOWNLOAD_URL);
    }

    /**
     * yarn and yarnpkg get a shim, both linked to bin/yarn.js.
     * The other files in bin are Windows launchers, etc.
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
            return mergeVersions(
                    NpmRegistry.getStableVersions(CLASSIC_PACKAGE),
                    NpmRegistry.getStableVersions(MODERN_PACKAGE));
        } catch (IOException e) {
            log.error("Error", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Versions before 2.x from the classic package, the rest from the modern one. The classic package also has 2.4.3,
     * which the modern package doesn't, so it's left out. Both lists are sorted and every classic version is older,
     * so the result is sorted too.
     */
    static List<String> mergeVersions(List<String> classicPackageVersions, List<String> modernPackageVersions) {
        List<String> versions = new ArrayList<>();

        classicPackageVersions.stream().filter(YarnTool::isClassic).forEach(versions::add);
        modernPackageVersions.stream().filter(version -> !isClassic(version)).forEach(versions::add);

        return versions;
    }

    private static boolean isClassic(String version) {
        return Version.parse(version).majorVersion() < 2;
    }
}

package org.jboss.pnc.jshim.backend.tools;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.text.StringSubstitutor;
import org.jboss.pnc.jshim.backend.common.MavenMetadata;

import lombok.extern.slf4j.Slf4j;

/**
 * Kotlin compiler (kotlinc, kotlin). It requires Java runtime.
 */
@Slf4j
public class KotlinTool implements BasicTool {

    private static final String DOWNLOAD_URL = "https://github.com/JetBrains/kotlin/releases/download/v${version}/kotlin-compiler-${version}.zip";
    private static final String VERSIONS_URL = "https://repo1.maven.org/maven2/org/jetbrains/kotlin/kotlin-compiler/maven-metadata.xml";

    /**
     * Stable releases (no -RC, -Beta, -dev, etc.)
     */
    private static final Pattern STABLE_VERSION = Pattern.compile("[1-9]\\d*\\.\\d+\\.\\d+");

    @Override
    public String name() {
        return "kotlin";
    }

    @Override
    public String downloadUrl(String versionToDownload) {

        Map<String, String> valuesMap = new HashMap<>();
        valuesMap.put("version", versionToDownload);

        StringSubstitutor sub = new StringSubstitutor(valuesMap);

        return sub.replace(DOWNLOAD_URL);
    }

    @Override
    public List<String> getDownloadableVersions() {
        try {
            return filterStableVersions(MavenMetadata.getVersions(VERSIONS_URL));
        } catch (Exception e) {
            log.error("Error", e);
            throw new RuntimeException(e);
        }
    }

    static List<String> filterStableVersions(List<String> versions) {
        return versions.stream().filter(version -> STABLE_VERSION.matcher(version).matches()).toList();
    }
}

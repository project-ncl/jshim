package org.jboss.pnc.jshim.backend.tools;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.text.StringSubstitutor;
import org.jboss.pnc.jshim.backend.common.MavenMetadata;

import com.github.zafarkhaja.semver.Version;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SbtTool implements BasicTool {

    private static final String DOWNLOAD_URL = "https://github.com/sbt/sbt/releases/download/v${version}/sbt-${version}.zip";
    private static final String VERSIONS_URL = "https://repo1.maven.org/maven2/org/scala-sbt/sbt/maven-metadata.xml";

    @Override
    public String name() {
        return "sbt";
    }

    @Override
    public String downloadUrl(String versionToDownload) {

        Version v = Version.parse(versionToDownload);

        Map<String, String> valuesMap = new HashMap<>();

        valuesMap.put("version", versionToDownload);
        StringSubstitutor sub = new StringSubstitutor(valuesMap);

        return sub.replace(DOWNLOAD_URL);
    }

    @Override
    public List<String> getDownloadableVersions() {
        try {
            return MavenMetadata.getVersions(VERSIONS_URL);
        } catch (Exception e) {
            log.error("Error", e);
        }
        return Collections.emptyList();
    }
}

package org.jboss.pnc.jshim.backend.tools;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.text.StringSubstitutor;
import org.jboss.pnc.jshim.backend.common.MavenMetadata;

import com.github.zafarkhaja.semver.Version;

import lombok.extern.slf4j.Slf4j;

/**
 * Maven tool. Assumes that folder is of format 'apache-maven-{version}'
 */
@Slf4j
public class MavenTool implements BasicTool {

    private static final String DOWNLOAD_URL = "https://archive.apache.org/dist/maven/maven-${major}/${version}/binaries/apache-maven-${version}-bin.zip";
    private static final String VERSIONS_URL = "https://repo1.maven.org/maven2/org/apache/maven/apache-maven/maven-metadata.xml";

    @Override
    public String name() {
        return "maven";
    }

    @Override
    public String downloadUrl(String versionToDownload) {

        Version v = Version.parse(versionToDownload);

        Map<String, String> valuesMap = new HashMap<>();

        valuesMap.put("major", "" + v.majorVersion());
        valuesMap.put("version", versionToDownload);
        StringSubstitutor sub = new StringSubstitutor(valuesMap);

        return sub.replace(DOWNLOAD_URL);
    }

    @Override
    public Optional<String> envVarHomeDefinition() {
        return Optional.of("M2_HOME");
    }

    @Override
    public List<String> getDownloadableVersions() {
        try {
            return MavenMetadata.getVersions(VERSIONS_URL);
        } catch (Exception e) {
            log.error("Error", e);
            throw new RuntimeException(e);
        }
    }
}

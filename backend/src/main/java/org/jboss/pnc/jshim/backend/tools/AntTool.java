package org.jboss.pnc.jshim.backend.tools;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.text.StringSubstitutor;
import org.jboss.pnc.jshim.backend.common.MavenMetadata;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AntTool implements BasicTool {

    private static final String DOWNLOAD_URL = "https://archive.apache.org/dist/ant/binaries/apache-ant-${version}-bin.zip";
    private static final String VERSIONS_URL = "https://repo1.maven.org/maven2/org/apache/ant/ant/maven-metadata.xml";

    @Override
    public String name() {
        return "ant";
    }

    @Override
    public String downloadUrl(String versionToDownload) {
        Map<String, String> valuesMap = new HashMap<>();
        valuesMap.put("version", versionToDownload);

        StringSubstitutor sub = new StringSubstitutor(valuesMap);

        return sub.replace(DOWNLOAD_URL);
    }

    @Override
    public Optional<String> envVarHomeDefinition() {
        return Optional.of("ANT_HOME");
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

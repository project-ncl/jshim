package org.jboss.pnc.jshim.backend.tools;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.text.StringSubstitutor;

import com.github.zafarkhaja.semver.Version;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NodeTool implements BasicTool {

    private static String DOWNLOAD_URL = "https://nodejs.org/dist/v${version}/node-v${version}-linux-x64.tar.gz";

    @Override
    public String name() {
        return "nodejs";
    }

    @Override
    public String downloadUrl(String versionToDownload) {

        Version v = Version.parse(versionToDownload);

        Map<String, String> valuesMap = new HashMap<>();

        valuesMap.put("version", versionToDownload);
        StringSubstitutor sub = new StringSubstitutor(valuesMap);

        return sub.replace(DOWNLOAD_URL);
    }
}

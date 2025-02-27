package org.jboss.pnc.jshim.tools;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.text.StringSubstitutor;

import com.github.zafarkhaja.semver.Version;

/**
 * BIG TODO: figure out what to do with the unzipped sbt, which unzips to just folder sbt
 */
public class SbtTool implements BasicTool {

    private static String DOWNLOAD_URL = "https://github.com/sbt/sbt/releases/download/v${version}/sbt-${version}.zip";

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
}

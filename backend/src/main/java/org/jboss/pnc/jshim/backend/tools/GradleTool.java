package org.jboss.pnc.jshim.backend.tools;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.text.StringSubstitutor;

public class GradleTool implements BasicTool {

    private static String DOWNLOAD_URL = "https://downloads.gradle.org/distributions/gradle-${version}-bin.zip";

    @Override
    public String name() {
        return "gradle";
    }

    @Override
    public String downloadUrl(String versionToDownload) {

        Map<String, String> valuesMap = new HashMap<>();

        valuesMap.put("version", versionToDownload);
        StringSubstitutor sub = new StringSubstitutor(valuesMap);

        return sub.replace(DOWNLOAD_URL);
    }
}

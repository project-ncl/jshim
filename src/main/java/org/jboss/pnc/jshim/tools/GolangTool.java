package org.jboss.pnc.jshim.tools;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.text.StringSubstitutor;

public class GolangTool implements BasicTool {

    public static String DOWNLOAD_URL = "https://go.dev/dl/go${version}.linux-amd64.tar.gz";

    @Override
    public String name() {
        return "golang";
    }

    @Override
    public String downloadUrl(String versionToDownload) {

        Map<String, String> valuesMap = new HashMap<>();

        valuesMap.put("version", versionToDownload);
        StringSubstitutor sub = new StringSubstitutor(valuesMap);

        return sub.replace(DOWNLOAD_URL);
    }

}

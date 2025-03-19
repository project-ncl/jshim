package org.jboss.pnc.jshim.backend.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.text.StringSubstitutor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.github.zafarkhaja.semver.Version;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NodeTool implements BasicTool {

    private static final String DOWNLOAD_URL = "https://nodejs.org/dist/v${version}/node-v${version}-linux-x64.tar.gz";
    private static final String VERSIONS_URL = "https://nodejs.org/dist/";

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

    @Override
    public List<String> getDownloadableVersions() {
        List<String> versions = new ArrayList<>();
        try {
            // can also parse json with index.json
            Document doc = Jsoup.connect(VERSIONS_URL).get();
            Elements elements = doc.select("a");
            for (Element element : elements) {
                versions.add(element.text());
            }
            // at this point, the versions list contains quit a few junk.
            return versions.stream()
                    .filter(version -> version.startsWith("v"))
                    .map(version -> version.replace("v", "").replace("/", ""))
                    .toList();
        } catch (Exception e) {
            log.error("Error", e);
            throw new RuntimeException(e);
        }
    }
}

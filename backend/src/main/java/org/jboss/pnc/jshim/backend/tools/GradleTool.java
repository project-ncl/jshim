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

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GradleTool implements BasicTool {

    private static final String DOWNLOAD_URL = "https://downloads.gradle.org/distributions/gradle-${version}-bin.zip";
    private static final String VERSIONS_URL = "https://services.gradle.org/distributions/";

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

    @Override
    public List<String> getDownloadableVersions() {
        List<String> versions = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(VERSIONS_URL).get();
            Elements elements = doc.getElementsByClass("items");
            for (Element element : elements) {
                Elements list = element.select("li");
                for (Element item : list) {
                    Element span = item.selectFirst("span");
                    if (span != null) {
                        versions.add(span.text());
                    }
                }
            }
            // at this point, the versions list contains quit a few junk.
            return versions.stream()
                    .filter(version -> version.startsWith("gradle-") && version.endsWith("-bin.zip"))
                    .filter(version -> !version.contains("-rc-")) // we don't want rc releases
                    .filter(version -> !version.contains("-milestone-")) // we don't want milestone releases
                    .map(version -> version.replace("gradle-", "").replace("-bin.zip", ""))
                    .toList();
        } catch (Exception e) {
            log.error("Error", e);
            throw new RuntimeException(e);
        }
    }

}

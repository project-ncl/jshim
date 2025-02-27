package org.jboss.pnc.jshim.tools;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.FileUtils;
import org.jboss.pnc.jshim.common.FilesCommon;

@Slf4j
public class JavaTool implements BasicTool {

    private static final String JAVA_AVAILABLE_VERSIONS_URL = "https://raw.githubusercontent.com/halcyon/asdf-java/refs/heads/master/data/jdk-linux-x86_64-ga.tsv";

    @Override
    public String name() {
        return "java";
    }

    @Override
    public String downloadUrl(String version) {
        LinkedHashMap<String, String> downloadableVersions = getDownloadableVersionsAndUrl();
        return downloadableVersions.get(version);
    }

    @Override
    public Map<String, Path> availableVersions() {

        // Get the JDKs already installed via rpms
        LinkedHashMap<String, Path> all = FilesCommon
                .availableVersionsBasedOnPrefix(Paths.get("/usr/lib/jvm"), "java-");

        // get the JDKs in the data path
        Map<String, Path> inDataPath = BasicTool.super.availableVersions();

        all.putAll(inDataPath);
        return all;
    }

    @Override
    public List<String> getDownloadableVersions() {
        return getDownloadableVersionsAndUrl().keySet().stream().toList();
    }

    /**
     * Return a map of downloadable jdks, with key the version and the url as the value
     * 
     * @return
     */
    private LinkedHashMap<String, String> getDownloadableVersionsAndUrl() {
        File tempFile = null;
        try {
            tempFile = Files.createTempFile("java-download-", ".archive").toFile();
            URL downloadUrlUrl = new URL(JAVA_AVAILABLE_VERSIONS_URL);
            FileUtils.copyURLToFile(downloadUrlUrl, tempFile);
            List<String[]> contents = FilesCommon.tsvr(tempFile);

            LinkedHashMap<String, String> toReturn = new LinkedHashMap<>();

            for (String[] item : contents) {
                // 0 is the "version", 2 is the download link
                String version = item[0];
                String downloadLink = item[2];
                if (!version.contains("jre") && !version.contains("openj9") && !version.contains("graalvm")
                        && (version.contains("temurin") || version.contains("oracle") || version.contains("mandrel")
                                || version.contains("adoptopenjdk"))) {
                    toReturn.put(version, downloadLink);
                }
            }

            return toReturn;

        } catch (Exception e) {
            log.error("Error", e);
            throw new RuntimeException(e);
        } finally {
            if (tempFile != null) {
                tempFile.delete();
            }
        }
    }
}

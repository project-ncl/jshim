package org.jboss.pnc.jshim.backend.common;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.zafarkhaja.semver.Version;

/**
 * Helper class to read the versions of a package from the npm registry
 */
public class NpmRegistry {

    private static final String REGISTRY_URL = "https://registry.npmjs.org/";

    private static final Pattern STABLE_VERSION = Pattern.compile("\\d+\\.\\d+\\.\\d+");

    /**
     * Stable versions of the package, sorted from oldest to newest
     *
     * @param packageName package name, e.g. npm or @yarnpkg/cli-dist
     */
    public static List<String> getStableVersions(String packageName) throws IOException {
        URLConnection connection = new URL(REGISTRY_URL + packageName).openConnection();
        connection.setRequestProperty("Accept", "application/vnd.npm.install-v1+json");

        try (InputStream metadata = connection.getInputStream()) {
            return parseStableVersions(metadata);
        }
    }

    /**
     * Stable releases (no -pre, -alpha, etc.) from the registry metadata, sorted from oldest to newest
     */
    static List<String> parseStableVersions(InputStream metadata) throws IOException {
        List<Version> versions = new ArrayList<>();

        new ObjectMapper().readTree(metadata).path("versions").fieldNames().forEachRemaining(version -> {
            if (STABLE_VERSION.matcher(version).matches()) {
                versions.add(Version.parse(version));
            }
        });

        return versions.stream().sorted().map(Version::toString).toList();
    }
}

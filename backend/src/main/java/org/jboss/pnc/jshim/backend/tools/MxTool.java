package org.jboss.pnc.jshim.backend.tools;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.text.StringSubstitutor;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Ref;

import com.github.zafarkhaja.semver.Version;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MxTool implements BasicTool {

    private static final String DOWNLOAD_URL = "https://github.com/graalvm/mx/archive/refs/tags/${version}.tar.gz";
    private static final String GIT_REPO = "https://github.com/graalvm/mx";

    private static final Pattern RELEASE_TAG = Pattern.compile("refs/tags/(\\d+\\.\\d+\\.\\d+)");

    @Override
    public String name() {
        return "mx";
    }

    @Override
    public String downloadUrl(String versionToDownload) {
        Map<String, String> valuesMap = new HashMap<>();
        valuesMap.put("version", versionToDownload);

        StringSubstitutor sub = new StringSubstitutor(valuesMap);

        return sub.replace(DOWNLOAD_URL);
    }

    @Override
    public String binaryFolderName() {
        return ""; // no bin folder
    }

    @Override
    public Map<String, Path> shims(Path home) {
        Path mx = home.resolve(binaryFolderName()).resolve("mx").toAbsolutePath();

        if (Files.isRegularFile(mx) && Files.isExecutable(mx)) {
            return Map.of("mx", mx);
        }

        return Map.of();
    }

    @Override
    public List<String> getDownloadableVersions() {
        try {
            Collection<Ref> refs = Git.lsRemoteRepository().setRemote(GIT_REPO).setTags(true).setHeads(false).call();
            return releaseVersionsFromTags(refs.stream().map(Ref::getName).toList());
        } catch (Exception e) {
            log.error("Error", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * plain versions (e.g. 7.89.0)
     */
    static List<String> releaseVersionsFromTags(Collection<String> tags) {
        return tags.stream()
                .map(RELEASE_TAG::matcher)
                .filter(Matcher::matches)
                .map(matcher -> Version.parse(matcher.group(1)))
                .sorted()
                .map(Version::toString)
                .toList();
    }
}

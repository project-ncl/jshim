package org.jboss.pnc.jshim.backend.tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.text.StringSubstitutor;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Ref;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GolangTool implements BasicTool {

    private static final String DOWNLOAD_URL = "https://go.dev/dl/go${version}.linux-amd64.tar.gz";
    private static final String GIT_REPO = "https://github.com/golang/go";

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

    @Override
    public List<String> getDownloadableVersions() {
        List<String> versions = new ArrayList<>();
        try {
            Collection<Ref> refs = Git.lsRemoteRepository().setRemote(GIT_REPO).setTags(true).setHeads(false).call();
            for (Ref ref : refs) {
                String refName = ref.getName();
                if (refName.startsWith("refs/tags/go") && !refName.contains("rc") && !refName.contains("beta")) {
                    versions.add(ref.getName().replace("refs/tags/go", ""));
                }
            }
            Collections.sort(versions);
            return versions;
        } catch (Exception e) {
            log.error("Error", e);
            throw new RuntimeException(e);
        }
    }
}

package org.jboss.pnc.jshim.tools;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import org.jboss.pnc.jshim.common.FilesCommon;
import org.jboss.pnc.jshim.constants.DefaultConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encapsulate the logic of how to find the tool commands and where to find the tool folder for a specific version
 *
 * Check out {@link org.jboss.pnc.jshim.constants.DefaultConstants} for how the tool folder is resolved
 */
public interface BasicTool {
    /**
     * The tool name that will have to match what the user wants to install.
     *
     * @return
     */
    String name();

    /**
     * The path within the specific tool version folder where the binaries are found
     */
    default String binaryFolderName() {
        return "bin";
    }

    /**
     * List all versions available, and the path
     */
    default Map<String, Path> availableVersions() {
        // assumption that all downloaded versions are in <data path>/<tool>/<tool>-<version>
        // see DefaultConstants.getVersionedToolFolder
        return FilesCommon.availableVersionsBasedOnPrefix(DefaultConstants.getToolFolder(name()), name() + "-");
    }

    default List<String> getDownloadableVersions() {
        return Collections.emptyList();
    }

    /**
     * Return the name of the shims, and where the shims should symlink to. By default, the shim name is the same as
     * the commands found inside the binary folder for that tool. The command must be a regular file and have the
     * executable permission
     *
     * @param version version of the tool
     *
     * @return map of shims and the symlink it should point to
     */
    default Map<String, Path> shimAndSymlink(String version) {
        Map<String, Path> available = availableVersions();

        if (!available.containsKey(version)) {
            // TODO: print instead?
            throw new RuntimeException(version + " is not found in the tool folder");
        }
        Path versionBinaryFolder = available.get(version).resolve(binaryFolderName());

        // we only want regular files that has the executable permission set
        Predicate<Path> isRegularFileAndExecutable = path -> path.toFile().isFile() && path.toFile().canExecute();
        return FilesCommon.nameOfFileAndPathFromFolder(versionBinaryFolder, isRegularFileAndExecutable);
    }

    /**
     * Download into the toolFolder the version of the application. By default, this isn't supported
     *
     * @param version
     * @return
     * @throws UnsupportedOperationException
     */
    String downloadUrl(String version);

    default void download(String versionToDownload) {

        Map<String, Path> availableVersions = this.availableVersions();

        if (availableVersions.containsKey(versionToDownload)) {
            log().error(
                    "Version: {} is already present in: {}",
                    versionToDownload,
                    availableVersions.get(versionToDownload));
            return;
        }

        Path toolFolder = DefaultConstants.getToolFolder(this.name());
        FilesCommon.createFolderAndParent(toolFolder);

        String downloadUrl = this.downloadUrl(versionToDownload);

        Path toolVersionedFolder = DefaultConstants.getVersionedToolFolder(this.name(), versionToDownload);
        FilesCommon.downloadAndUnarchive(downloadUrl, toolVersionedFolder);
    }

    private Logger log() {
        return LoggerFactory.getLogger(BasicTool.class);
    }
}

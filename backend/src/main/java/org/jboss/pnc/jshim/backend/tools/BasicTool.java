package org.jboss.pnc.jshim.backend.tools;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import org.jboss.pnc.jshim.backend.common.FilesCommon;
import org.jboss.pnc.jshim.backend.constants.DefaultConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encapsulate the logic of how to find the tool commands and where to find the tool folder for a specific version.
 *
 * We tried to minimize the number of methods to implement, providing default values where possible. The static methods
 * aren't "default" method implementations since it's highly unlikely an implementaion will need to override its logic.
 * If that assumption is wrong, move the static method back to a default method.
 *
 * Check out {@link DefaultConstants} for how the tool folder is resolved
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
     * List all versions downloaded, and their path
     */
    default Map<String, Path> availableVersions() {
        // assumption that all downloaded versions are in <data path>/<tool>/<tool>-<version>
        // see DefaultConstants.getVersionedToolFolder
        return FilesCommon.availableVersionsBasedOnPrefix(DefaultConstants.getToolFolder(name()), name() + "-");
    }

    /**
     * List all the versions we can download. It's kinda hard to implement for every tool so right now the default is
     * to just return nothing.
     * 
     * @return
     */
    default List<String> getDownloadableVersions() {
        return Collections.emptyList();
    }

    /**
     * Get the download url for the version of the application
     *
     * @param version
     * @return
     * @throws UnsupportedOperationException
     */
    String downloadUrl(String version);

    /**
     * Override this method if you want to define an environment variable pointing to the home folder of the tool
     * version
     *
     * @return an optional string which contains the environment variable name to define, which has as value the tool
     *         version home folder
     *         e.g "JAVA_HOME"
     */
    default Optional<String> envVarHomeDefinition() {
        return Optional.empty();
    }

    // *****************************************************************************************************************
    // Static Method Definitions
    // *****************************************************************************************************************
    /**
     * Get the home folder of the tool, given the specific version
     *
     * @param version version of the home folder
     * @return
     */
    static Path homeFolder(BasicTool tool, String version) {
        Map<String, Path> available = tool.availableVersions();

        if (!available.containsKey(version)) {
            throw new RuntimeException(version + " is not found in the tool folder");
        }
        return available.get(version);
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
    static Map<String, Path> shimAndSymlink(BasicTool tool, String version) {
        Path versionBinaryFolder = BasicTool.homeFolder(tool, version).resolve(tool.binaryFolderName());

        // we only want regular files that has the executable permission set
        Predicate<Path> isRegularFileAndExecutable = path -> path.toFile().isFile() && path.toFile().canExecute();
        return FilesCommon.nameOfFileAndPathFromFolder(versionBinaryFolder, isRegularFileAndExecutable);
    }

    /**
     * Default implementation of download method, based on the {@link #downloadUrl(String)} method
     * It checks whether the tool version has already been downloaded.
     *
     * @param versionToDownload
     */
    static void download(BasicTool tool, String versionToDownload) {

        Map<String, Path> availableVersions = tool.availableVersions();

        if (availableVersions.containsKey(versionToDownload)) {
            log().error(
                    "Version: {} is already present in: {}",
                    versionToDownload,
                    availableVersions.get(versionToDownload));
            return;
        }

        Path toolFolder = DefaultConstants.getToolFolder(tool.name());
        FilesCommon.createFolderAndParent(toolFolder);

        String downloadUrl = tool.downloadUrl(versionToDownload);

        Path toolVersionedFolder = DefaultConstants.getVersionedToolFolder(tool.name(), versionToDownload);
        FilesCommon.downloadAndUnarchive(downloadUrl, toolVersionedFolder);
    }

    private static Logger log() {
        return LoggerFactory.getLogger(BasicTool.class);
    }
}

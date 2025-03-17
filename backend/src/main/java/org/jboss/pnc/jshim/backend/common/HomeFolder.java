package org.jboss.pnc.jshim.backend.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import org.jboss.pnc.jshim.backend.constants.DefaultConstants;
import org.jboss.pnc.jshim.backend.tools.BasicTool;

import lombok.extern.slf4j.Slf4j;

/**
 * Class containing logic for setting home environment variables for tools. The assumption is that on every tool
 * installation,
 * a symlinked folder is created that points to the real tool version home folder. The symlinked folder is created using
 * a specific format.
 *
 * Based on that format, we then use it to extract the env variable that we need to export and where it should point to.
 * That information is written into the ENVIRONMENT_VARIABLE_FILE that the user will need to 'source' to properly export
 * them to the shell
 */
@Slf4j
public class HomeFolder {

    /**
     * Set the environment variable home folder symlink to the correct tool version home folder, and also "export" the
     * environment variable via the ENVIRONMENV_VARIABLE_FILE to the symlinked home folder.
     *
     * The assumption is that the symlinked home folder is named as: ${HOME_FOLDER_PREFIX}{environmentVariable}
     *
     * @param tool
     * @param version
     * @throws IOException
     */
    public static void setEnvironmentVariableHome(BasicTool tool, String version) throws IOException {

        Path shimFolder = DefaultConstants.getShimFolder();

        // Create a symlink to the home folder of the tool
        Optional<String> homeEnvVar = tool.envVarHomeDefinition();

        if (homeEnvVar.isPresent()) {
            String envVar = homeEnvVar.get();
            Path shimToolHomeFolder = shimFolder.resolve(DefaultConstants.HOME_FOLDER_PREFIX + envVar);
            Files.deleteIfExists(shimToolHomeFolder);

            Path realToolVersHomeFolder = BasicTool.homeFolder(tool, version);
            log.info("Setting home folder '{}' to path '{}'", shimToolHomeFolder, realToolVersHomeFolder);
            Files.createSymbolicLink(shimToolHomeFolder, realToolVersHomeFolder);

            writeEnvVariableHomeToDefine();
        }
    }

    /**
     * Write the environment variables into the $ENVIRONMENT_VARIABLE_FILE for the user to "source"
     * The assumption is that the symlinked home folder is named as: ${HOME_FOLDER_PREFIX}{environmentVariable}
     */
    public static void writeEnvVariableHomeToDefine() {

        Path shimFolder = DefaultConstants.getShimFolder();

        Predicate<Path> filterHomeFolder = path -> path.getFileName()
                .toString()
                .startsWith(DefaultConstants.HOME_FOLDER_PREFIX) && Files.isSymbolicLink(path)
                && Files.isDirectory(path);
        Map<String, Path> paths = FilesCommon.nameOfFileAndPathFromFolder(shimFolder, filterHomeFolder);

        Path sourceInitScript = shimFolder.resolve(DefaultConstants.ENVIRONMENT_VARIABLE_FILE);

        StringBuilder builder = new StringBuilder();

        for (Map.Entry<String, Path> entry : paths.entrySet()) {
            String envVar = entry.getKey().replace(DefaultConstants.HOME_FOLDER_PREFIX, "");
            builder.append("export ").append(envVar).append("=").append(entry.getValue().toAbsolutePath()).append("\n");
        }
        try {
            log.info("Writing to '{}' the environment variables for the project", sourceInitScript);
            Files.writeString(sourceInitScript, builder.toString());
            log.info("");
            log.info(
                    "Please run in your terminal: 'source {}' to export the environment variables to your shell!",
                    sourceInitScript);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

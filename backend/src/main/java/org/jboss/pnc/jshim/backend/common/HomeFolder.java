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
 * Class containing logic for setting home environment variables for tools. The assumption is that for every tool
 * download, a symlinked folder is created in the shims folder that points to the tool version home folder. The
 * symlinked folder is created using a specific format (PREFIX-environmentVariable).
 *
 * Based on that format, we then use it to extract the env variable that we need to export and where it should point to.
 * That information is written into the ENVIRONMENT_VARIABLE_FILE that the user will need to 'source' to properly export
 * them to the shell
 *
 * e.g: At download time, inside the shims folder (/home/user/.local/share/jshim/shims), those symlinks are created:
 * - binary1 --> symlinked to /home/user/.local/share/jshim/downloaded/binary1/binary1-123/bin/binary1
 * - PREFIX-BINARY1_HOME --> symlinked to /home/user/.local/share/jshim/downloaded/binary1/binary1-123 , the home folder
 * of the tool
 *
 * When the user wants to load up all the environment variables in the shims folder, she'll have to run:
 * - `source /home/user/.local/share/jshim/shims/source-file.sh`
 *
 * The script will export the home environment variable for the tools
 * - `export BINARY1_HOME=/home/user/.local/share/jshim/shims/PREFIX_BINARY1_HOME`
 */
@Slf4j
public class HomeFolder {

    /**
     * Make the environment variable home folder symlink to the correct tool version home folder, and also "export" the
     * environment variable via the ENVIRONMENT_VARIABLE_FILE to the symlinked home folder.
     *
     * The assumption is that the symlinked home folder is named as: ${HOME_FOLDER_PREFIX}{environmentVariable}
     *
     * @param tool name of tool
     * @param version version
     * @throws IOException Something went wrong
     */
    public static void setEnvironmentVariableHome(BasicTool tool, String version) throws IOException {

        Path shimFolder = DefaultConstants.getShimFolder();

        // Create shim folder if not created yet
        FilesCommon.createFolderAndParent(shimFolder);

        // get any environment variable to define, if any
        Optional<String> homeEnvVar = tool.envVarHomeDefinition();

        // Create a symlink to the home folder of the tool
        if (homeEnvVar.isPresent()) {
            String envVar = homeEnvVar.get();
            Path shimToolHomeFolder = shimFolder.resolve(DefaultConstants.HOME_FOLDER_PREFIX + envVar);

            // delete existing home folder symlink if present
            Files.deleteIfExists(shimToolHomeFolder);

            Path realToolVersionHomeFolder = BasicTool.homeFolder(tool, version);
            log.info("Setting home folder '{}' to path '{}'", shimToolHomeFolder, realToolVersionHomeFolder);
            Files.createSymbolicLink(shimToolHomeFolder, realToolVersionHomeFolder);

            writeEnvVariableHomeToDefine();
        }
    }

    /**
     * Write the environment variables into the $ENVIRONMENT_VARIABLE_FILE for the user to "source"
     * The assumption is that the symlinked home folder is named as: ${HOME_FOLDER_PREFIX}{environmentVariable}
     */
    static void writeEnvVariableHomeToDefine() {

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

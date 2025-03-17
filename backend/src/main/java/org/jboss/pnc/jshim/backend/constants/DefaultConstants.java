package org.jboss.pnc.jshim.backend.constants;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Class containing all hte default paths
 */
public class DefaultConstants {

    /**
     * The default path where we'll put downloaded binaries and the shim symlinks
     */
    public static Path DEFAULT_DATA_PATH = Paths.get(System.getProperty("user.home"), ".local", "share", "jshim");

    /**
     * Allow ability to override the data path. Used for testing only
     */
    private static Path OVERRIDE_DATA_PATH;

    /**
     * User can override the data path using that environment variable
     */
    public static String DATA_PATH_ENV_VAR = "JSHIM_DATA_PATH";

    public static String SHIM_FOLDER = "shims";
    public static String SHIM_ENV_VAR = "JSHIM_SHIM_PATH";

    public static String DOWNLOADED_FOLDER = "downloaded";

    /**
     * Symlink for home folders
     */
    public static String HOME_FOLDER_PREFIX = "TOOL_HOME_FOLDER-";

    /**
     * Source file user needs to 'source' to load the environment variables
     */
    public static String ENVIRONMENT_VARIABLE_FILE = "source-file.sh";

    public static String HOOK_FOLDER = "hooks";
    public static String HOOK_POSTDOWNLOAD_SUFFIX = "-postdownload.groovy";

    public DefaultConstants() {
    }

    public static Path getDataPath() {
        if (OVERRIDE_DATA_PATH != null) {
            System.out.println(OVERRIDE_DATA_PATH);
            return OVERRIDE_DATA_PATH;
        } else {
            String userDataPath = System.getenv(DATA_PATH_ENV_VAR);
            return userDataPath != null ? Paths.get(userDataPath) : DEFAULT_DATA_PATH;
        }
    }

    public static Path getShimFolder() {
        String userShimPath = System.getenv(SHIM_ENV_VAR);
        return userShimPath != null ? Paths.get(userShimPath) : getDataPath().resolve(SHIM_FOLDER);
    }

    public static Path getDownloadedFolder() {
        return getDataPath().resolve(DOWNLOADED_FOLDER);
    }

    /**
     * Return path where the tool is downloaded
     * 
     * @param nameOfTool
     * @return
     */
    public static Path getToolFolder(String nameOfTool) {
        return getDownloadedFolder().resolve(nameOfTool);
    }

    /**
     * Return path where the specific tool version is downloaded
     * 
     * @param nameOfTool
     * @param version
     * @return
     */
    public static Path getVersionedToolFolder(String nameOfTool, String version) {
        return getToolFolder(nameOfTool).resolve(nameOfTool + "-" + version);
    }

    public static void setOverrideDataPath(Path path) {
        OVERRIDE_DATA_PATH = path;
    }

    public static void resetOverrideDataPath() {
        OVERRIDE_DATA_PATH = null;
    }
}

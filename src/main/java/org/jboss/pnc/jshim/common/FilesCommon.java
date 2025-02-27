package org.jboss.pnc.jshim.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.function.Predicate;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.FileUtils;

/**
 * Helper methods for files
 */
@Slf4j
public class FilesCommon {

    public static LinkedHashMap<String, Path> nameOfFileAndPathFromFolder(Path folder, Predicate<Path> predicate) {
        LinkedHashMap<String, Path> fileToPath = new LinkedHashMap<>();

        boolean errors = checkForFolderErrors(folder);

        if (errors) {
            return fileToPath;
        }

        File[] files = folder.toFile().listFiles();
        if (files == null) {
            throw new RuntimeException("Path " + folder + " is not a directory");
        }

        for (File file : files) {
            Path path = file.toPath();
            if (predicate.test(path)) {
                // TODO: document the absolute path?
                fileToPath.put(path.getFileName().toString(), path.toAbsolutePath());
            }
        }

        return fileToPath;
    }

    /**
     * Helper method to handle cases where the version inside of a folder is of type {prefix}-{version}
     *
     * @param folder
     * @param prefix
     * @return
     */
    public static LinkedHashMap<String, Path> availableVersionsBasedOnPrefix(Path folder, String prefix) {

        LinkedHashMap<String, Path> versions = new LinkedHashMap<>();
        // I added the not symbolic link to handle JDK versions in /var/lib/jvm
        Predicate<Path> folderThatBeginWithPrefix = path -> path.toFile().isDirectory()
                && !Files.isSymbolicLink(path)
                && path.getFileName().toString().startsWith(prefix);

        LinkedHashMap<String, Path> versionFolders = nameOfFileAndPathFromFolder(folder, folderThatBeginWithPrefix);

        versionFolders.forEach((versionFolder, path) -> {
            versions.put(versionFolder.replace(prefix, ""), path);
        });

        return versions;
    }

    /**
     * Just runs sanity check on that folder.
     *
     * @param folder
     */
    private static boolean checkForFolderErrors(Path folder) {

        if (folder == null) {
            log.warn("Folder is null");
            return true;
        }

        if (!Files.exists(folder)) {
            log.warn("Folder {} doesn't exist", folder);
            return true;
        }

        File binaryFolderFile = folder.toFile();

        if (!binaryFolderFile.isDirectory()) {
            log.warn("{} is not a directory!", folder);
            return true;
        }

        return false;
    }

    /**
     * Simple helper method to create a folder and parents if it doesn't already exist
     *
     * @param folder
     */
    public static void createFolderAndParent(Path folder) {

        if (Files.exists(folder)) {
            // do nothing if folder already exists
            return;
        }

        folder.toFile().mkdirs();
    }

    /**
     * Download the url, unarchive it and put it in the target folder
     *
     * @param downloadUrl
     * @param target
     */
    public static void downloadAndUnarchive(String downloadUrl, Path target) {
        File tempFile = null;

        try {
            tempFile = Files.createTempFile("temp-download-", ".archive").toFile();
            log.info("Downloading: {}", downloadUrl);
            URL downloadUrlUrl = new URL(downloadUrl);
            FileUtils.copyURLToFile(downloadUrlUrl, tempFile);
            log.info("Unzipping to: {}", target);

            if (downloadUrl.endsWith(".tar.gz")) {

                Files.createDirectory(target);

                new ProcessBuilder(
                        "tar",
                        "xf",
                        tempFile.getAbsolutePath(),
                        "-C",
                        ".",
                        "--strip-components=1")
                                .directory(target.toFile())
                                .start()
                                .waitFor();

            } else if (downloadUrl.endsWith(".zip")) {

                Path tempFolder = Files.createTempDirectory("jshim-download-unzip");
                new ProcessBuilder("unzip", tempFile.getAbsolutePath())
                        .directory(tempFolder.toFile())
                        .start()
                        .waitFor();

                File[] files = tempFolder.toFile().listFiles();

                if (files.length >= 1) {
                    FileUtils.copyDirectory(files[0], target.toFile());
                }
            } else {
                log.error("Not supported archive: {}", downloadUrl);
            }
        } catch (Exception e) {
            log.error(e.toString());
        } finally {
            if (tempFile != null) {
                tempFile.delete();
            }
        }
    }

    public static ArrayList<String[]> tsvr(File toRead) {

        ArrayList<String[]> Data = new ArrayList<>(); //initializing a new ArrayList out of String[]'s

        try (BufferedReader TSVReader = new BufferedReader(new FileReader(toRead))) {
            String line = null;
            while ((line = TSVReader.readLine()) != null) {
                String[] lineItems = line.split("\t"); //splitting the line and adding its items in String[]
                Data.add(lineItems); //adding the splitted line array to the ArrayList
            }
        } catch (Exception e) {
            System.out.println("Something went wrong");
        }
        return Data;
    }
}

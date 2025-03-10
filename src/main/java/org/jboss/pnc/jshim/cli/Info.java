package org.jboss.pnc.jshim.cli;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import lombok.extern.slf4j.Slf4j;

import org.jboss.pnc.jshim.common.FilesCommon;
import org.jboss.pnc.jshim.constants.DefaultConstants;

import picocli.CommandLine;

// print TOOL folder
// print shim folder
// list all current shims and where it is pointing to
@CommandLine.Command(
        name = "info",
        description = "Print information about the application")
@Slf4j
public class Info implements Runnable {
    @Override
    public void run() {

        printEnvVars();
        printPathInformation();
        printInstalledTools();
    }

    private static void printEnvVars() {

        System.out.println();
        String userDataPath = System.getenv(DefaultConstants.DATA_PATH_ENV_VAR);
        System.out.format("${%s} set to: %s\n", DefaultConstants.DATA_PATH_ENV_VAR, userDataPath);
        String shimPath = System.getenv(DefaultConstants.SHIM_ENV_VAR);
        System.out.format("${%s} set to: %s\n", DefaultConstants.SHIM_ENV_VAR, shimPath);
    }

    private static void printPathInformation() {

        Map<String, String> info = new LinkedHashMap<>();

        System.out.println();
        info.put("Data Path", DefaultConstants.getDataPath().toAbsolutePath().toString());
        info.put("Tool Path", DefaultConstants.getDownloadedFolder().toAbsolutePath().toString());
        info.put("Shim Path", DefaultConstants.getShimFolder().toAbsolutePath().toString());

        System.out.println();
        for (Map.Entry<String, String> entry : info.entrySet()) {
            System.out.format("%-13s%-15s%n", entry.getKey(), entry.getValue());
        }
    }

    private static void printInstalledTools() {
        System.out.println();
        System.out.println("Installed tools:\n");

        Path shimPath = DefaultConstants.getShimFolder();

        if (Files.isDirectory(shimPath)) {
            Predicate<Path> symlinkAndExecutable = path -> Files.isSymbolicLink(path) && Files.isExecutable(path);
            Map<String, Path> shims = FilesCommon.nameOfFileAndPathFromFolder(shimPath, symlinkAndExecutable);

            List<String> shimNames = new ArrayList<>(shims.keySet());

            // make sure we iterate alphabetically through the names
            Collections.sort(shimNames);

            for (String shimName : shimNames) {
                try {
                    System.out.format(
                            "%-13s%-15s%n",
                            shimName,
                            Files.readSymbolicLink(shims.get(shimName)).toAbsolutePath());
                } catch (IOException e) {
                    log.error("{}", e.getMessage());
                }
            }
            System.out.println();
        }

    }
}

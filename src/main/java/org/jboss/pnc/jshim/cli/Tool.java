package org.jboss.pnc.jshim.cli;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import org.jboss.pnc.jshim.common.NameAndVersion;
import org.jboss.pnc.jshim.constants.DefaultConstants;
import org.jboss.pnc.jshim.tools.BasicTool;
import org.jboss.pnc.jshim.tools.ToolFactory;

import picocli.CommandLine;

@CommandLine.Command(
        subcommands = {
                Tool.ListSupported.class,
                Tool.ListAvailableLocal.class,
                Tool.ListAvailableDownload.class,
                Tool.Download.class,
                Tool.Use.class },
        name = "tool",
        description = "tool subcommand")
@Slf4j
public class Tool {

    @CommandLine.Command(
            name = "list-supported",
            description = "list supported tools")
    public static class ListSupported implements Runnable {
        @Override
        public void run() {
            for (String toolName : ToolFactory.getToolsMap().keySet()) {
                System.out.println(toolName);
            }
        }
    }

    @CommandLine.Command(
            name = "list-versions-local",
            description = "List available versions already installed or in the tools folder")
    public static class ListAvailableLocal implements Runnable {

        @CommandLine.Parameters(
                arity = "1",
                paramLabel = "tool",
                description = "Specify the tool name")
        String toolName;

        @Override
        public void run() {

            try {
                BasicTool tool = ToolFactory.getTool(toolName);
                Set<String> versions = tool.availableVersions().keySet();
                for (String version : versions) {
                    System.out.println(version);
                }
            } catch (ToolFactory.ToolNotFoundException e) {
                log.error("Tool {} is not supported", toolName);
            } catch (RuntimeException f) {
                log.error(f.getMessage());
            }
        }
    }

    @CommandLine.Command(
            name = "list-versions-download",
            description = "List available versions we can download")
    public static class ListAvailableDownload implements Runnable {

        @CommandLine.Parameters(
                arity = "1",
                paramLabel = "tool",
                description = "Specify the tool name")
        String toolName;

        @Override
        public void run() {

            try {
                BasicTool tool = ToolFactory.getTool(toolName);
                Set<String> downloadableVersions = tool.getDownloadableVersions().keySet();
                Set<String> installed = tool.availableVersions().keySet();

                for (String version : downloadableVersions) {
                    if (installed.contains(version)) {
                        System.out.println("* " + version);
                    } else {
                        System.out.println("  " + version);
                    }
                }

                System.out.println("\n* versions are already installed locally\n");
            } catch (ToolFactory.ToolNotFoundException e) {
                log.error("Tool {} is not supported", toolName);
            } catch (RuntimeException f) {
                log.error(f.getMessage());
            }
        }
    }

    @CommandLine.Command(
            name = "download",
            description = "Download the tool's version")
    public static class Download implements Runnable {

        @CommandLine.Parameters(
                arity = "1",
                paramLabel = "tool@version",
                description = "Specify the tool name and version")
        String toolNameAndVersion;

        @Override
        public void run() {

            try {
                NameAndVersion.NameAndVersionInfo nameAndVersionInfo = NameAndVersion.parseString(toolNameAndVersion);

                BasicTool tool = ToolFactory.getTool(nameAndVersionInfo.getName());
                tool.download(nameAndVersionInfo.getVersion());

            } catch (ToolFactory.ToolNotFoundException e) {
                log.error("Tool is not supported");
            } catch (RuntimeException f) {
                log.error(f.getMessage());
            }
        }
    }

    @CommandLine.Command(
            name = "use",
            description = "select which maven version to use")
    public static class Use implements Runnable {

        @CommandLine.Parameters(
                arity = "1",
                paramLabel = "tool@version",
                description = "Specify the tool name and version")
        String toolNameAndVersion;

        @Override
        public void run() {
            try {
                NameAndVersion.NameAndVersionInfo nameAndVersionInfo = NameAndVersion.parseString(toolNameAndVersion);

                BasicTool tool = ToolFactory.getTool(nameAndVersionInfo.getName());
                Map<String, Path> availableVersions = tool.availableVersions();

                if (!availableVersions.containsKey(nameAndVersionInfo.getVersion())) {
                    log.error(
                            "Version {} not present in the installed folder: {}",
                            nameAndVersionInfo.getVersion(),
                            DefaultConstants.getToolFolder(tool.name()));
                }

                Path shimFolder = DefaultConstants.getShimFolder();
                Map<String, Path> shimAndSymlink = tool.shimAndSymlink(nameAndVersionInfo.getVersion());
                for (Map.Entry<String, Path> entry : shimAndSymlink.entrySet()) {

                    Path shim = shimFolder.resolve(entry.getKey());
                    Files.deleteIfExists(shim);

                    log.info("Setting '{}' to path '{}'", entry.getKey(), entry.getValue());
                    Files.createSymbolicLink(shim, entry.getValue());
                }
            } catch (ToolFactory.ToolNotFoundException e) {
                log.error("Tool is not supported");
            } catch (IOException | RuntimeException f) {
                log.error(f.getMessage());
            }
        }
    }
}

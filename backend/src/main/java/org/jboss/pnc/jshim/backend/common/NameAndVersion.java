package org.jboss.pnc.jshim.backend.common;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO + methods to represent the name and version of the tool to install
 */
public class NameAndVersion {

    @Data
    @AllArgsConstructor
    public static class NameAndVersionInfo {
        private String name;
        private String version;
    }

    /**
     * Parse string of type {tool}@{version} into this DTO object
     *
     * @param nameAndVersion string to parse
     * @return object
     *
     * @throws IllegalArgumentException if the string is not in the right format
     */
    public static NameAndVersionInfo parseString(String nameAndVersion) {
        if (nameAndVersion.contains("@")) {
            String[] data = nameAndVersion.split("@");
            if (data.length >= 2) {
                return new NameAndVersionInfo(data[0], data[1]);
            } else {
                throw new IllegalArgumentException(
                        "Name and version should be in format: name@version, value provided: " + nameAndVersion);
            }
        } else {
            throw new IllegalArgumentException(
                    "Name and version should be in format: name@version, value provided: " + nameAndVersion);
        }
    }
}

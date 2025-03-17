package org.jboss.pnc.jshim.backend.common;

import lombok.AllArgsConstructor;
import lombok.Data;

public class NameAndVersion {

    @Data
    @AllArgsConstructor
    public static class NameAndVersionInfo {
        private String name;
        private String version;
    }

    public static NameAndVersionInfo parseString(String nameAndVersion) {
        if (nameAndVersion.contains("@")) {
            String[] data = nameAndVersion.split("@");
            if (data.length >= 2) {
                return new NameAndVersionInfo(data[0], data[1]);
            } else {
                throw new IllegalArgumentException("Name and version should be in format: name@version");
            }
        } else {
            throw new IllegalArgumentException("Name and version should be in format: name@version");
        }
    }
}

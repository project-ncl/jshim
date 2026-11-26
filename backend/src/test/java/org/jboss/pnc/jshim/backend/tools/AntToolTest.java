package org.jboss.pnc.jshim.backend.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.junit.jupiter.api.Test;

class AntToolTest {

    @Test
    void downloadUrl() {
        assertEquals(
                "https://archive.apache.org/dist/ant/binaries/apache-ant-1.10.15-bin.zip",
                new AntTool().downloadUrl("1.10.15"));
    }

    @Test
    void antHomeIsDefined() {
        assertEquals(Optional.of("ANT_HOME"), new AntTool().envVarHomeDefinition());
    }
}

package org.jboss.pnc.jshim.backend.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Test;

class NpmRegistryTest {

    @Test
    void onlyStableVersionsAreKeptSorted() throws IOException {
        String metadata = """
                {"name": "npm", "versions": {"10.9.0": {}, "9.8.1": {}, "10.10.0": {}, "11.0.0-pre.1": {}, "2.0.0-alpha-5": {}}}
                """;

        assertEquals(
                List.of("9.8.1", "10.9.0", "10.10.0"),
                NpmRegistry.parseStableVersions(new ByteArrayInputStream(metadata.getBytes(StandardCharsets.UTF_8))));
    }
}

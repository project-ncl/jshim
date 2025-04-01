package org.jboss.pnc.jshim.backend.common;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class NameAndVersionTest {

    @Test
    void parseString() {
        NameAndVersion.NameAndVersionInfo tool = NameAndVersion.parseString("tool@1.5.6");
        assertEquals("tool", tool.getName());
        assertEquals("1.5.6", tool.getVersion());

        assertThrows(IllegalArgumentException.class, () -> NameAndVersion.parseString("testme"));
    }
}
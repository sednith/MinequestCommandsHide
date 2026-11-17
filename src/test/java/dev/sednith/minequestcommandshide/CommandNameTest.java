package dev.sednith.minequestcommandshide;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CommandNameTest {
    @Test
    void stripsSlashNamespaceArgumentsAndCase() {
        assertEquals("msg", CommandName.normalize("/MSG Steve привет"));
        assertEquals("tp", CommandName.normalize("minecraft:tp 10 64 10"));
        assertEquals("server", CommandName.normalize("  /velocity:server lobby "));
    }

    @Test
    void emptyInputIsEmpty() {
        assertEquals("", CommandName.normalize(null));
        assertEquals("", CommandName.normalize("   "));
        assertEquals("", CommandName.normalize("/"));
    }

    @Test
    void firstTokenKeepsArgumentsOut() {
        assertEquals("help", CommandName.firstToken("/help 2"));
        assertEquals("msg", CommandName.firstToken("msg"));
        assertEquals("", CommandName.firstToken(null));
    }

    @Test
    void normalizesGroupNames() {
        assertEquals("helper", CommandName.normalizeGroup("  HeLpEr "));
    }
}

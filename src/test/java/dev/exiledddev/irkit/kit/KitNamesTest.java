package dev.exiledddev.irkit.kit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class KitNamesTest {

    @Test
    void normalizesToLowercase() {
        assertEquals("knight_red", KitNames.normalize("  Knight_RED "));
    }

    @Test
    void validatesNames() {
        assertNull(KitNames.validate("knight"));
        assertNull(KitNames.validate("team-1_archer"));
        assertNotNull(KitNames.validate(""));
        assertNotNull(KitNames.validate("two words"));
        assertNotNull(KitNames.validate("Knight"));
        assertNotNull(KitNames.validate("a.b"));
        assertNotNull(KitNames.validate("x".repeat(33)));
    }

    @Test
    void parsesGiveModes() {
        assertEquals(GiveMode.REPLACE, GiveMode.parse("replace"));
        assertEquals(GiveMode.ADD, GiveMode.parse(" ADD "));
        assertNull(GiveMode.parse("merge"));
    }
}

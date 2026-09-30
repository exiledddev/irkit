package dev.exiledddev.irkit.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.exiledddev.irkit.kit.Kit;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class EditorLayoutTest {

    @Test
    void everyKitSlotHasItsOwnMenuSlotAndMapsBack() {
        final Set<Integer> menuSlots = new HashSet<>();
        for (int kitSlot = 0; kitSlot < Kit.SIZE; kitSlot++) {
            final int menuSlot = EditorLayout.menuSlot(kitSlot);
            assertTrue(menuSlot >= 0 && menuSlot < EditorLayout.SIZE);
            assertTrue(menuSlots.add(menuSlot), "menu slot used twice: " + menuSlot);
            assertEquals(kitSlot, EditorLayout.kitSlot(menuSlot));
        }
    }

    @Test
    void hotbarIsTheFourthRowLikeThePlayerInventory() {
        assertEquals(27, EditorLayout.menuSlot(0));
        assertEquals(35, EditorLayout.menuSlot(8));
        assertEquals(0, EditorLayout.menuSlot(9));
        assertEquals(26, EditorLayout.menuSlot(35));
    }

    @Test
    void armorAndOffhandSitInTheBottomRow() {
        assertEquals(EditorLayout.HELMET, EditorLayout.menuSlot(Kit.HELMET));
        assertEquals(EditorLayout.CHESTPLATE, EditorLayout.menuSlot(Kit.CHESTPLATE));
        assertEquals(EditorLayout.LEGGINGS, EditorLayout.menuSlot(Kit.LEGGINGS));
        assertEquals(EditorLayout.BOOTS, EditorLayout.menuSlot(Kit.BOOTS));
        assertEquals(EditorLayout.OFFHAND, EditorLayout.menuSlot(Kit.OFFHAND));
    }

    @Test
    void onlyKitSlotsAndTheIconAreEditable() {
        int editable = 0;
        for (int slot = 0; slot < EditorLayout.SIZE; slot++) {
            if (EditorLayout.isEditable(slot)) {
                editable++;
            }
        }
        assertEquals(Kit.SIZE + 1, editable);
        assertTrue(EditorLayout.isEditable(EditorLayout.ICON));
        for (final int button : new int[] {EditorLayout.SAVE, EditorLayout.CANCEL, EditorLayout.CLEAR, EditorLayout.LOAD_INVENTORY, EditorLayout.FIRST_LABEL, 44, 51}) {
            assertFalse(EditorLayout.isEditable(button), "slot " + button);
        }
        assertEquals(-1, EditorLayout.kitSlot(EditorLayout.ICON));
    }
}

package dev.exiledddev.irkit.gui;

import dev.exiledddev.irkit.kit.Kit;

/**
 * Where each kit slot sits in the 6-row editor and preview menus:
 * <pre>
 *  rows 1-3  (0-26)   inventory slots 9-35
 *  row 4     (27-35)  hotbar, inventory slots 0-8
 *  row 5     (36-44)  labels for the slots below | load inventory | clear | filler
 *  row 6     (45-53)  helmet chestplate leggings boots offhand icon | filler | cancel | save
 * </pre>
 * The layout matches the player's own inventory screen, so a kit is built the same way you'd
 * arrange your inventory.
 */
public final class EditorLayout {

    public static final int SIZE = 54;
    public static final int FIRST_LABEL = 36;
    public static final int LOAD_INVENTORY = 42;
    public static final int CLEAR = 43;
    public static final int HELMET = 45;
    public static final int CHESTPLATE = 46;
    public static final int LEGGINGS = 47;
    public static final int BOOTS = 48;
    public static final int OFFHAND = 49;
    public static final int ICON = 50;
    public static final int CANCEL = 52;
    public static final int SAVE = 53;

    private EditorLayout() {
    }

    /** The kit slot shown at {@code menuSlot}, or -1 if that menu slot isn't a kit slot. */
    public static int kitSlot(final int menuSlot) {
        if (menuSlot >= 0 && menuSlot < 27) {
            return menuSlot + 9;
        }
        if (menuSlot >= 27 && menuSlot < 36) {
            return menuSlot - 27;
        }
        return switch (menuSlot) {
            case HELMET -> Kit.HELMET;
            case CHESTPLATE -> Kit.CHESTPLATE;
            case LEGGINGS -> Kit.LEGGINGS;
            case BOOTS -> Kit.BOOTS;
            case OFFHAND -> Kit.OFFHAND;
            default -> -1;
        };
    }

    /** The menu slot that shows {@code kitSlot}. */
    public static int menuSlot(final int kitSlot) {
        if (kitSlot >= 0 && kitSlot < 9) {
            return kitSlot + 27;
        }
        if (kitSlot >= 9 && kitSlot < Kit.STORAGE_SIZE) {
            return kitSlot - 9;
        }
        return switch (kitSlot) {
            case Kit.HELMET -> HELMET;
            case Kit.CHESTPLATE -> CHESTPLATE;
            case Kit.LEGGINGS -> LEGGINGS;
            case Kit.BOOTS -> BOOTS;
            case Kit.OFFHAND -> OFFHAND;
            default -> throw new IllegalArgumentException("Not a kit slot: " + kitSlot);
        };
    }

    /** Slots the player can put items in while editing: every kit slot plus the icon. */
    public static boolean isEditable(final int menuSlot) {
        return kitSlot(menuSlot) >= 0 || menuSlot == ICON;
    }
}

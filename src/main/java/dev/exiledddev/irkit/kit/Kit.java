package dev.exiledddev.irkit.kit;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * A saved kit: 36 inventory slots (same numbering as a player's inventory, 0-8 is the hotbar),
 * four armor slots and the offhand, plus an optional icon for the /kits menu.
 */
public final class Kit {

    public static final int STORAGE_SIZE = 36;
    public static final int HELMET = 36;
    public static final int CHESTPLATE = 37;
    public static final int LEGGINGS = 38;
    public static final int BOOTS = 39;
    public static final int OFFHAND = 40;
    public static final int SIZE = 41;

    private static final int[] ICON_FALLBACK_ORDER = {CHESTPLATE, HELMET, 0, 1, 2, 3, 4, 5, 6, 7, 8, OFFHAND, LEGGINGS, BOOTS};

    private final String name;
    private final ItemStack[] items;
    private @Nullable ItemStack icon;

    public Kit(final String name, final ItemStack[] items, final @Nullable ItemStack icon) {
        if (items.length != SIZE) {
            throw new IllegalArgumentException("A kit has " + SIZE + " slots, got " + items.length);
        }
        this.name = name;
        this.items = new ItemStack[SIZE];
        for (int i = 0; i < SIZE; i++) {
            this.items[i] = copy(items[i]);
        }
        this.icon = copy(icon);
    }

    public String name() {
        return this.name;
    }

    /** A copy of the item in {@code slot}, or {@code null} if it's empty. */
    public @Nullable ItemStack item(final int slot) {
        return copy(this.items[slot]);
    }

    /** Copies of all 41 slots; empty slots are {@code null}. */
    public ItemStack[] items() {
        final ItemStack[] copy = new ItemStack[SIZE];
        for (int i = 0; i < SIZE; i++) {
            copy[i] = copy(this.items[i]);
        }
        return copy;
    }

    public @Nullable ItemStack icon() {
        return copy(this.icon);
    }

    /** The icon to show in menus: the chosen icon, else the chestplate, helmet or first hotbar item, else a chest. */
    public ItemStack displayIcon() {
        if (this.icon != null) {
            return this.icon.clone();
        }
        for (final int slot : ICON_FALLBACK_ORDER) {
            if (this.items[slot] != null) {
                return this.items[slot].asOne();
            }
        }
        return new ItemStack(Material.CHEST);
    }

    public boolean isEmpty() {
        for (final ItemStack item : this.items) {
            if (item != null) {
                return false;
            }
        }
        return true;
    }

    /** The same contents under another name. */
    public Kit withName(final String newName) {
        return new Kit(newName, this.items, this.icon);
    }

    private static @Nullable ItemStack copy(final @Nullable ItemStack item) {
        return item == null || item.isEmpty() ? null : item.clone();
    }
}

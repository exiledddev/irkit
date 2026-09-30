package dev.exiledddev.irkit.gui;

import dev.exiledddev.irkit.kit.Kit;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/** Draws a kit into an editor-layout menu (shared by the editor and the preview). */
final class KitView {

    private static final String[] LABELS = {"Helmet", "Chestplate", "Leggings", "Boots", "Offhand", "Menu icon"};

    private KitView() {
    }

    /** Puts each kit item in its menu slot and the icon (if any) in the icon slot. */
    static void place(final Inventory inventory, final Kit kit) {
        for (int slot = 0; slot < Kit.SIZE; slot++) {
            inventory.setItem(EditorLayout.menuSlot(slot), kit.item(slot));
        }
        inventory.setItem(EditorLayout.ICON, kit.icon());
    }

    /** The "Helmet ↓" ... "Menu icon ↓" labels above the armor, offhand and icon slots. */
    static void labels(final Inventory inventory, final boolean editing) {
        for (int i = 0; i < LABELS.length; i++) {
            final String hint = switch (i) {
                case 4 -> "Shield, totem, torch...";
                case 5 -> editing ? "Optional: shown in /kits (not given)" : "Shown in /kits";
                default -> "Any item works, even a carved pumpkin";
            };
            inventory.setItem(EditorLayout.FIRST_LABEL + i,
                Items.named(Material.LIGHT_BLUE_STAINED_GLASS_PANE, LABELS[i] + " ↓", NamedTextColor.AQUA, hint));
        }
    }

    static void fill(final Inventory inventory, final int... slots) {
        final ItemStack filler = Items.filler();
        for (final int slot : slots) {
            inventory.setItem(slot, filler);
        }
    }
}

package dev.exiledddev.irkit.kit;

import java.util.Collection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jspecify.annotations.Nullable;

/**
 * Puts a kit on a player. Armor and the offhand are always equipped straight away.
 */
public final class KitGiver {

    private KitGiver() {
    }

    public static void give(final Player player, final Kit kit, final GiveMode mode) {
        if (mode == GiveMode.REPLACE) {
            replace(player, kit);
        } else {
            add(player, kit);
        }
    }

    /** Clears everything, then puts every item in exactly the slot it has in the kit. */
    private static void replace(final Player player, final Kit kit) {
        final PlayerInventory inventory = player.getInventory();
        inventory.clear();
        for (int slot = 0; slot < Kit.STORAGE_SIZE; slot++) {
            inventory.setItem(slot, kit.item(slot));
        }
        inventory.setHelmet(kit.item(Kit.HELMET));
        inventory.setChestplate(kit.item(Kit.CHESTPLATE));
        inventory.setLeggings(kit.item(Kit.LEGGINGS));
        inventory.setBoots(kit.item(Kit.BOOTS));
        inventory.setItemInOffHand(kit.item(Kit.OFFHAND));
    }

    /**
     * Puts each kit item in its kit slot if that slot is free, or anywhere else if it isn't, then
     * equips the kit's armor and offhand, moving what was there into the inventory. Anything that
     * doesn't fit drops at the player's feet.
     */
    private static void add(final Player player, final Kit kit) {
        final PlayerInventory inventory = player.getInventory();

        // Kit items first, so the armor that gets unequipped below can't take their slots.
        for (int slot = 0; slot < Kit.STORAGE_SIZE; slot++) {
            final ItemStack item = kit.item(slot);
            if (item == null) {
                continue;
            }
            final ItemStack current = inventory.getItem(slot);
            if (current == null || current.isEmpty()) {
                inventory.setItem(slot, item);
            } else {
                stash(player, item);
            }
        }

        final ItemStack helmet = kit.item(Kit.HELMET);
        if (helmet != null) {
            stash(player, inventory.getHelmet());
            inventory.setHelmet(helmet);
        }
        final ItemStack chestplate = kit.item(Kit.CHESTPLATE);
        if (chestplate != null) {
            stash(player, inventory.getChestplate());
            inventory.setChestplate(chestplate);
        }
        final ItemStack leggings = kit.item(Kit.LEGGINGS);
        if (leggings != null) {
            stash(player, inventory.getLeggings());
            inventory.setLeggings(leggings);
        }
        final ItemStack boots = kit.item(Kit.BOOTS);
        if (boots != null) {
            stash(player, inventory.getBoots());
            inventory.setBoots(boots);
        }
        final ItemStack offhand = kit.item(Kit.OFFHAND);
        if (offhand != null) {
            stash(player, inventory.getItemInOffHand());
            inventory.setItemInOffHand(offhand);
        }

    }

    /** Adds an item to the player's inventory, dropping whatever doesn't fit at their feet. */
    public static void stash(final Player player, final @Nullable ItemStack item) {
        if (item == null || item.isEmpty()) {
            return;
        }
        drop(player, player.getInventory().addItem(item).values());
    }

    public static void drop(final Player player, final Collection<ItemStack> items) {
        for (final ItemStack item : items) {
            player.getWorld().dropItemNaturally(player.getLocation(), item);
        }
    }
}

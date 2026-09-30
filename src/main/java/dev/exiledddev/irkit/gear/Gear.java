package dev.exiledddev.irkit.gear;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jspecify.annotations.Nullable;

/**
 * Helpers shared by the random gear and power suit builders.
 */
final class Gear {

    private Gear() {
    }

    static Material pick(final Material[] options, final ThreadLocalRandom random) {
        return options[random.nextInt(options.length)];
    }

    /** Drops a replaced armor or offhand piece at the player's feet, or just discards it. */
    static void replaced(final Player player, final @Nullable ItemStack existing, final boolean drop) {
        if (drop && existing != null && !existing.isEmpty()) {
            player.getWorld().dropItemNaturally(player.getLocation(), existing);
        }
    }

    /** Adds items to the player's inventory, dropping whatever doesn't fit at their feet. */
    static void giveOrDrop(final Player player, final ItemStack... items) {
        for (final ItemStack leftover : player.getInventory().addItem(items).values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), leftover);
        }
    }

    /**
     * Builds an item with the given enchantments, skipping any that conflict with one already added.
     * With {@code maxTier} false every level is halved (rounded up).
     */
    static ItemStack enchanted(final Material material, final Map<Enchantment, Integer> enchants, final boolean maxTier) {
        final ItemStack item = new ItemStack(material);
        final ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }
        for (final Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
            final Enchantment enchant = entry.getKey();
            if (conflicts(meta, enchant)) {
                continue;
            }
            final int maxLevel = entry.getValue();
            final int level = maxTier ? maxLevel : (int) Math.ceil(maxLevel / 2.0);
            meta.addEnchant(enchant, level, true);
        }
        item.setItemMeta(meta);
        return item;
    }

    private static boolean conflicts(final ItemMeta meta, final Enchantment candidate) {
        for (final Enchantment existing : meta.getEnchants().keySet()) {
            if (existing.conflictsWith(candidate) || candidate.conflictsWith(existing)) {
                return true;
            }
        }
        return false;
    }
}

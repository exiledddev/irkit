package dev.exiledddev.irkit.gear;

import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * /randarmor and /randitem: the same random gear the old IsMP plugin handed out.
 */
public final class RandomGear {

    private static final Material[] HELMETS = {Material.DIAMOND_HELMET, Material.LEATHER_HELMET, Material.GOLDEN_HELMET, Material.IRON_HELMET, Material.COPPER_HELMET};
    private static final Material[] CHESTPLATES = {Material.DIAMOND_CHESTPLATE, Material.LEATHER_CHESTPLATE, Material.GOLDEN_CHESTPLATE, Material.IRON_CHESTPLATE, Material.COPPER_CHESTPLATE};
    private static final Material[] LEGGINGS = {Material.DIAMOND_LEGGINGS, Material.LEATHER_LEGGINGS, Material.GOLDEN_LEGGINGS, Material.IRON_LEGGINGS, Material.COPPER_LEGGINGS};
    private static final Material[] BOOTS = {Material.DIAMOND_BOOTS, Material.LEATHER_BOOTS, Material.GOLDEN_BOOTS, Material.IRON_BOOTS, Material.COPPER_BOOTS};
    private static final Material[] SWORDS = {Material.DIAMOND_SWORD, Material.STONE_SWORD, Material.IRON_SWORD, Material.COPPER_SWORD, Material.GOLDEN_SWORD, Material.WOODEN_SWORD};
    private static final Material[] PICKAXES = {Material.GOLDEN_PICKAXE, Material.DIAMOND_PICKAXE, Material.IRON_PICKAXE, Material.COPPER_PICKAXE, Material.WOODEN_PICKAXE};
    private static final Material[] AXES = {Material.GOLDEN_AXE, Material.DIAMOND_AXE, Material.IRON_AXE, Material.COPPER_AXE, Material.WOODEN_AXE};

    private static final int MENDING_CHANCE_PERCENT = 60;
    private static final int UNBREAKING_CHANCE_PERCENT = 80;
    private static final int UNBREAKING_LEVEL = 3;

    private RandomGear() {
    }

    /** Equips a random helmet, chestplate, leggings and boots, each with random Protection. */
    public static void armor(final Player player, final boolean dropReplaced) {
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        final PlayerInventory inventory = player.getInventory();
        Gear.replaced(player, inventory.getHelmet(), dropReplaced);
        inventory.setHelmet(piece(Gear.pick(HELMETS, random), Enchantment.PROTECTION, random));
        Gear.replaced(player, inventory.getChestplate(), dropReplaced);
        inventory.setChestplate(piece(Gear.pick(CHESTPLATES, random), Enchantment.PROTECTION, random));
        Gear.replaced(player, inventory.getLeggings(), dropReplaced);
        inventory.setLeggings(piece(Gear.pick(LEGGINGS, random), Enchantment.PROTECTION, random));
        Gear.replaced(player, inventory.getBoots(), dropReplaced);
        inventory.setBoots(piece(Gear.pick(BOOTS, random), Enchantment.PROTECTION, random));
    }

    /** Gives a random sword, pickaxe and axe plus random amounts of golden apples, wind charges and steak. */
    public static void items(final Player player) {
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        Gear.giveOrDrop(player,
            piece(Gear.pick(SWORDS, random), Enchantment.SHARPNESS, random),
            piece(Gear.pick(PICKAXES, random), Enchantment.EFFICIENCY, random),
            piece(Gear.pick(AXES, random), Enchantment.EFFICIENCY, random),
            new ItemStack(Material.GOLDEN_APPLE, random.nextInt(1, 33)),
            new ItemStack(Material.WIND_CHARGE, random.nextInt(1, 65)),
            new ItemStack(Material.COOKED_BEEF, random.nextInt(1, 41)));
    }

    /** The primary enchantment at level 0-4, plus a chance of Mending and of Unbreaking III. */
    private static ItemStack piece(final Material material, final Enchantment primary, final ThreadLocalRandom random) {
        final ItemStack item = new ItemStack(material);
        final ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }
        final int primaryLevel = random.nextInt(5);
        if (primaryLevel > 0) {
            meta.addEnchant(primary, primaryLevel, true);
        }
        if (random.nextInt(100) < MENDING_CHANCE_PERCENT) {
            meta.addEnchant(Enchantment.MENDING, 1, true);
        }
        if (random.nextInt(100) < UNBREAKING_CHANCE_PERCENT) {
            meta.addEnchant(Enchantment.UNBREAKING, UNBREAKING_LEVEL, true);
        }
        item.setItemMeta(meta);
        return item;
    }
}

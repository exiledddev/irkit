package dev.exiledddev.irkit.gear;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

/**
 * /powersuit: the same five tiers of gear the old IsMP plugin handed out.
 */
public final class PowerSuit {

    public static final int MIN_LEVEL = 1;
    public static final int MAX_LEVEL = 5;

    private static final Map<Enchantment, Integer> ARMOR_COMMON = table(Enchantment.PROTECTION, 4, Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1);
    private static final Map<Enchantment, Integer> HELMET = with(ARMOR_COMMON, Enchantment.AQUA_AFFINITY, 1, Enchantment.RESPIRATION, 3);
    private static final Map<Enchantment, Integer> CHESTPLATE = ARMOR_COMMON;
    private static final Map<Enchantment, Integer> LEGGINGS = with(ARMOR_COMMON, Enchantment.SWIFT_SNEAK, 3);
    private static final Map<Enchantment, Integer> BOOTS = with(ARMOR_COMMON, Enchantment.FEATHER_FALLING, 4, Enchantment.DEPTH_STRIDER, 3, Enchantment.FROST_WALKER, 2, Enchantment.SOUL_SPEED, 3);
    private static final Map<Enchantment, Integer> SWORD = table(Enchantment.SHARPNESS, 5, Enchantment.KNOCKBACK, 1, Enchantment.FIRE_ASPECT, 2, Enchantment.LOOTING, 3, Enchantment.SWEEPING_EDGE, 3, Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1);
    private static final Map<Enchantment, Integer> AXE = table(Enchantment.SHARPNESS, 5, Enchantment.EFFICIENCY, 5, Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1);
    private static final Map<Enchantment, Integer> PICKAXE = table(Enchantment.EFFICIENCY, 5, Enchantment.FORTUNE, 3, Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1);
    private static final Map<Enchantment, Integer> SHOVEL = table(Enchantment.EFFICIENCY, 5, Enchantment.FORTUNE, 3, Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1);
    private static final Map<Enchantment, Integer> SHIELD = table(Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1);
    private static final Map<Enchantment, Integer> SPEAR = table(Enchantment.SHARPNESS, 5, Enchantment.KNOCKBACK, 1, Enchantment.FIRE_ASPECT, 2, Enchantment.LOOTING, 3, Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1, Enchantment.LUNGE, 3);

    private PowerSuit() {
    }

    /**
     * @param level 1 (diamond, halved enchantments) to 5 (full netherite, spear, totems)
     * @param dropReplaced drop the armor and offhand the suit replaces instead of deleting them
     */
    public static void apply(final Player player, final int level, final boolean dropReplaced) {
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        switch (level) {
            case 1 -> {
                armor(player, new boolean[4], false, dropReplaced);
                tools(player, 0.0, false, random);
                shield(player, false, dropReplaced);
                Gear.giveOrDrop(player, new ItemStack(Material.GOLDEN_APPLE, 32), new ItemStack(Material.COOKED_BEEF, 64));
                potions(player, PotionType.STRENGTH, 1, PotionType.SWIFTNESS, 1, 1);
            }
            case 2 -> {
                armor(player, netheriteSlots(1, 0.25, random), true, dropReplaced);
                tools(player, 0.0, true, random);
                shield(player, true, dropReplaced);
                Gear.giveOrDrop(player, new ItemStack(Material.GOLDEN_APPLE, 64), new ItemStack(Material.COOKED_BEEF, 64));
                potions(player, PotionType.STRONG_STRENGTH, 1, PotionType.STRONG_SWIFTNESS, 1, 1);
            }
            case 3 -> {
                armor(player, netheriteSlots(2, 0.5, random), true, dropReplaced);
                tools(player, 0.2, true, random);
                shield(player, true, dropReplaced);
                Gear.giveOrDrop(player, new ItemStack(Material.GOLDEN_APPLE, 64), new ItemStack(Material.COOKED_BEEF, 64));
                potions(player, PotionType.STRONG_STRENGTH, 1, PotionType.STRONG_SWIFTNESS, 1, 1);
            }
            case 4 -> {
                armor(player, netheriteSlots(4, 0.5, random), true, dropReplaced);
                tools(player, 0.5, true, random);
                shield(player, true, dropReplaced);
                Gear.giveOrDrop(player, new ItemStack(Material.GOLDEN_APPLE, 64), new ItemStack(Material.COOKED_BEEF, 64), new ItemStack(Material.TOTEM_OF_UNDYING, 1));
                potions(player, PotionType.STRONG_STRENGTH, 2, PotionType.STRONG_SWIFTNESS, 2, 2);
            }
            case 5 -> {
                armor(player, new boolean[] {true, true, true, true}, true, dropReplaced);
                tools(player, 1.0, true, random);
                shield(player, true, dropReplaced);
                Gear.giveOrDrop(player, Gear.enchanted(Material.NETHERITE_SPEAR, SPEAR, true));
                Gear.giveOrDrop(player,
                    new ItemStack(Material.ENDER_PEARL, 16), new ItemStack(Material.BREEZE_ROD, 16),
                    new ItemStack(Material.GOLDEN_CARROT, 64), new ItemStack(Material.WATER_BUCKET, 1),
                    new ItemStack(Material.COBWEB, 48), new ItemStack(Material.TOTEM_OF_UNDYING, 2),
                    new ItemStack(Material.GOLDEN_APPLE, 64));
                potions(player, PotionType.STRONG_STRENGTH, 3, PotionType.STRONG_SWIFTNESS, 3, 3);
            }
            default -> throw new IllegalArgumentException("Power suit level must be between 1 and 5, was " + level);
        }
    }

    /**
     * Picks {@code candidates} random armor slots (helmet, chestplate, leggings, boots) and makes each
     * one netherite with probability {@code chance}.
     */
    static boolean[] netheriteSlots(final int candidates, final double chance, final Random random) {
        final boolean[] result = new boolean[4];
        final List<Integer> slots = new ArrayList<>(List.of(0, 1, 2, 3));
        Collections.shuffle(slots, random);
        for (int i = 0; i < candidates && i < slots.size(); i++) {
            if (random.nextDouble() < chance) {
                result[slots.get(i)] = true;
            }
        }
        return result;
    }

    private static void armor(final Player player, final boolean[] netherite, final boolean maxTier, final boolean dropReplaced) {
        final PlayerInventory inventory = player.getInventory();
        Gear.replaced(player, inventory.getHelmet(), dropReplaced);
        inventory.setHelmet(Gear.enchanted(netherite[0] ? Material.NETHERITE_HELMET : Material.DIAMOND_HELMET, HELMET, maxTier));
        Gear.replaced(player, inventory.getChestplate(), dropReplaced);
        inventory.setChestplate(Gear.enchanted(netherite[1] ? Material.NETHERITE_CHESTPLATE : Material.DIAMOND_CHESTPLATE, CHESTPLATE, maxTier));
        Gear.replaced(player, inventory.getLeggings(), dropReplaced);
        inventory.setLeggings(Gear.enchanted(netherite[2] ? Material.NETHERITE_LEGGINGS : Material.DIAMOND_LEGGINGS, LEGGINGS, maxTier));
        Gear.replaced(player, inventory.getBoots(), dropReplaced);
        inventory.setBoots(Gear.enchanted(netherite[3] ? Material.NETHERITE_BOOTS : Material.DIAMOND_BOOTS, BOOTS, maxTier));
    }

    private static void tools(final Player player, final double netheriteChance, final boolean maxTier, final Random random) {
        Gear.giveOrDrop(player,
            Gear.enchanted(tier(Material.NETHERITE_SWORD, Material.DIAMOND_SWORD, netheriteChance, random), SWORD, maxTier),
            Gear.enchanted(tier(Material.NETHERITE_AXE, Material.DIAMOND_AXE, netheriteChance, random), AXE, maxTier),
            Gear.enchanted(tier(Material.NETHERITE_PICKAXE, Material.DIAMOND_PICKAXE, netheriteChance, random), PICKAXE, maxTier),
            Gear.enchanted(tier(Material.NETHERITE_SHOVEL, Material.DIAMOND_SHOVEL, netheriteChance, random), SHOVEL, maxTier));
    }

    private static Material tier(final Material netherite, final Material diamond, final double netheriteChance, final Random random) {
        if (netheriteChance >= 1.0) {
            return netherite;
        }
        if (netheriteChance <= 0.0) {
            return diamond;
        }
        return random.nextDouble() < netheriteChance ? netherite : diamond;
    }

    private static void shield(final Player player, final boolean maxTier, final boolean dropReplaced) {
        final PlayerInventory inventory = player.getInventory();
        Gear.replaced(player, inventory.getItemInOffHand(), dropReplaced);
        inventory.setItemInOffHand(Gear.enchanted(Material.SHIELD, SHIELD, maxTier));
    }

    private static void potions(final Player player, final PotionType strength, final int strengthCount,
                                final PotionType speed, final int speedCount, final int fireResistanceCount) {
        final List<ItemStack> potions = new ArrayList<>();
        for (int i = 0; i < strengthCount; i++) {
            potions.add(potion(strength));
        }
        for (int i = 0; i < speedCount; i++) {
            potions.add(potion(speed));
        }
        for (int i = 0; i < fireResistanceCount; i++) {
            potions.add(potion(PotionType.LONG_FIRE_RESISTANCE));
        }
        Gear.giveOrDrop(player, potions.toArray(ItemStack[]::new));
    }

    private static ItemStack potion(final PotionType type) {
        final ItemStack item = new ItemStack(Material.SPLASH_POTION);
        if (item.getItemMeta() instanceof PotionMeta meta) {
            meta.setBasePotionType(type);
            item.setItemMeta(meta);
        }
        return item;
    }

    private static Map<Enchantment, Integer> table(final Object... pairs) {
        return with(Map.of(), pairs);
    }

    private static Map<Enchantment, Integer> with(final Map<Enchantment, Integer> base, final Object... pairs) {
        final Map<Enchantment, Integer> map = new LinkedHashMap<>(base);
        for (int i = 0; i < pairs.length; i += 2) {
            map.put((Enchantment) pairs[i], (Integer) pairs[i + 1]);
        }
        return Collections.unmodifiableMap(map);
    }
}

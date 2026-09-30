package dev.exiledddev.irkit.kit;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * Keeps kits in memory and saves each one to {@code plugins/IRKit/kits/<name>.yml}. Items are stored
 * with Paper's item serialization, which upgrades them automatically when Minecraft updates.
 */
public final class KitStore {

    private final File folder;
    private final Logger logger;
    private final Map<String, Kit> kits = new TreeMap<>();

    public KitStore(final File folder, final Logger logger) {
        this.folder = folder;
        this.logger = logger;
    }

    public void load() {
        this.kits.clear();
        if (!this.folder.isDirectory() && !this.folder.mkdirs()) {
            this.logger.severe("Could not create " + this.folder);
            return;
        }
        final File[] files = this.folder.listFiles((dir, fileName) -> fileName.endsWith(".yml"));
        if (files == null) {
            return;
        }
        for (final File file : files) {
            try {
                final Kit kit = read(file);
                this.kits.put(kit.name(), kit);
            } catch (final RuntimeException e) {
                this.logger.log(Level.SEVERE, "Could not load kit " + file.getName(), e);
            }
        }
        this.logger.info("Loaded " + this.kits.size() + " kit(s).");
    }

    public @Nullable Kit get(final String name) {
        return this.kits.get(KitNames.normalize(name));
    }

    public boolean exists(final String name) {
        return this.kits.containsKey(KitNames.normalize(name));
    }

    /** All kits, sorted by name. */
    public Collection<Kit> all() {
        return List.copyOf(this.kits.values());
    }

    public List<String> names() {
        return new ArrayList<>(this.kits.keySet());
    }

    /** Adds or replaces a kit and writes it to disk. */
    public void save(final Kit kit) {
        this.kits.put(kit.name(), kit);
        final YamlConfiguration yaml = new YamlConfiguration();
        yaml.options().setHeader(List.of("IRKit kit. Edit it in game with /kit edit " + kit.name() + "."));
        yaml.set("name", kit.name());
        final ItemStack icon = kit.icon();
        if (icon != null) {
            yaml.set("icon", Base64.getEncoder().encodeToString(icon.serializeAsBytes()));
        }
        yaml.set("items", Base64.getEncoder().encodeToString(ItemStack.serializeItemsAsBytes(kit.items())));
        try {
            yaml.save(this.file(kit.name()));
        } catch (final IOException e) {
            this.logger.log(Level.SEVERE, "Could not save kit " + kit.name(), e);
        }
    }

    public boolean delete(final String name) {
        final Kit removed = this.kits.remove(KitNames.normalize(name));
        if (removed == null) {
            return false;
        }
        final File file = this.file(removed.name());
        if (file.exists() && !file.delete()) {
            this.logger.warning("Could not delete " + file);
        }
        return true;
    }

    private File file(final String name) {
        return new File(this.folder, name + ".yml");
    }

    private static Kit read(final File file) {
        final YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        final String fileName = file.getName();
        final String name = KitNames.normalize(yaml.getString("name", fileName.substring(0, fileName.length() - ".yml".length())));
        final String error = KitNames.validate(name);
        if (error != null) {
            throw new IllegalArgumentException(error);
        }

        final String itemsData = yaml.getString("items");
        if (itemsData == null) {
            throw new IllegalArgumentException("missing 'items'");
        }
        final ItemStack[] stored = ItemStack.deserializeItemsFromBytes(Base64.getDecoder().decode(itemsData));
        final ItemStack[] items = new ItemStack[Kit.SIZE];
        System.arraycopy(stored, 0, items, 0, Math.min(stored.length, Kit.SIZE));

        final String iconData = yaml.getString("icon");
        final ItemStack icon = iconData == null ? null : ItemStack.deserializeBytes(Base64.getDecoder().decode(iconData));
        return new Kit(name, items, icon);
    }
}

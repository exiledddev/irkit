package dev.exiledddev.irkit.gui;

import dev.exiledddev.irkit.IRKitPlugin;
import dev.exiledddev.irkit.Msg;
import dev.exiledddev.irkit.kit.Kit;
import dev.exiledddev.irkit.kit.KitNames;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jspecify.annotations.Nullable;

/**
 * Build a kit like you'd arrange your own inventory. Save writes it straight away if the kit has a
 * name, or asks for one in an anvil if it doesn't.
 */
public final class KitEditorMenu implements Menu {

    private final IRKitPlugin plugin;
    private final Inventory inventory;
    private final @Nullable String name;
    /** Set when the menu closes because we moved on (saved, cancelled, opened the name prompt). */
    private boolean handedOff;

    /**
     * @param kit  contents to start from, or null for an empty editor
     * @param name the kit name, or null to ask for one when saving
     */
    public KitEditorMenu(final IRKitPlugin plugin, final @Nullable Kit kit, final @Nullable String name) {
        this.plugin = plugin;
        this.name = name;
        final Component title = name == null
            ? Component.text("New kit")
            : Component.text("Editing kit: ").append(Component.text(name, NamedTextColor.DARK_BLUE));
        this.inventory = Bukkit.createInventory(this, EditorLayout.SIZE, title);

        if (kit != null) {
            KitView.place(this.inventory, kit);
        }
        KitView.labels(this.inventory, true);
        this.inventory.setItem(EditorLayout.LOAD_INVENTORY, Items.named(Material.CHEST, "Load my inventory", NamedTextColor.YELLOW,
            "Copies your inventory, armor and offhand", "into the editor (replaces what's here)."));
        this.inventory.setItem(EditorLayout.CLEAR, Items.named(Material.LAVA_BUCKET, "Clear", NamedTextColor.GOLD,
            "Empties the editor."));
        this.inventory.setItem(EditorLayout.CANCEL, Items.named(Material.BARRIER, "Cancel", NamedTextColor.RED,
            "Close without saving.", "Closing with Esc also discards changes."));
        this.inventory.setItem(EditorLayout.SAVE, Items.named(Material.LIME_CONCRETE, "Save kit", NamedTextColor.GREEN,
            name == null ? "You'll name the kit next." : "Saves kit " + name + "."));
        KitView.fill(this.inventory, 44, 51);
    }

    public void open(final Player player) {
        player.openInventory(this.inventory);
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    @Override
    public void onClick(final InventoryClickEvent event) {
        final int slot = event.getRawSlot();
        if (slot < 0 || slot >= EditorLayout.SIZE || EditorLayout.isEditable(slot)) {
            return; // the player's own inventory or a kit slot: normal inventory behaviour
        }
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        switch (slot) {
            case EditorLayout.LOAD_INVENTORY -> this.loadFrom(player.getInventory());
            case EditorLayout.CLEAR -> {
                for (int i = 0; i < EditorLayout.SIZE; i++) {
                    if (EditorLayout.isEditable(i)) {
                        this.inventory.setItem(i, null);
                    }
                }
            }
            case EditorLayout.CANCEL -> {
                this.handedOff = true;
                Msg.info(player, "Closed the kit editor without saving.");
                this.later(player::closeInventory);
            }
            case EditorLayout.SAVE -> this.save(player);
            default -> {
            }
        }
    }

    @Override
    public void onDrag(final InventoryDragEvent event) {
        for (final int slot : event.getRawSlots()) {
            if (slot < EditorLayout.SIZE && !EditorLayout.isEditable(slot)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @Override
    public void onClose(final InventoryCloseEvent event) {
        if (!this.handedOff) {
            Msg.info(event.getPlayer(), "Closed the kit editor without saving.");
        }
    }

    private void loadFrom(final PlayerInventory source) {
        for (int slot = 0; slot < Kit.STORAGE_SIZE; slot++) {
            this.inventory.setItem(EditorLayout.menuSlot(slot), copy(source.getItem(slot)));
        }
        this.inventory.setItem(EditorLayout.HELMET, copy(source.getHelmet()));
        this.inventory.setItem(EditorLayout.CHESTPLATE, copy(source.getChestplate()));
        this.inventory.setItem(EditorLayout.LEGGINGS, copy(source.getLeggings()));
        this.inventory.setItem(EditorLayout.BOOTS, copy(source.getBoots()));
        this.inventory.setItem(EditorLayout.OFFHAND, copy(source.getItemInOffHand()));
    }

    private void save(final Player player) {
        final ItemStack[] items = new ItemStack[Kit.SIZE];
        for (int slot = 0; slot < Kit.SIZE; slot++) {
            items[slot] = this.inventory.getItem(EditorLayout.menuSlot(slot));
        }
        final ItemStack icon = this.inventory.getItem(EditorLayout.ICON);
        final Kit draft = new Kit(this.name == null ? "unnamed" : this.name, items, icon == null ? null : icon.asOne());
        if (draft.isEmpty()) {
            Msg.error(player, "The kit is empty. Put some items in first.");
            return;
        }

        this.handedOff = true;
        if (this.name != null) {
            this.plugin.kits().save(draft);
            Msg.success(player, "Saved kit <kit>.", Msg.text("kit", this.name));
            this.later(player::closeInventory);
            return;
        }

        // Unnamed: ask for a name, and come back here if the player backs out.
        this.later(() -> this.plugin.menus().promptName(player, "kit_name", typed -> {
            final String kitName = KitNames.normalize(typed);
            final String error = KitNames.validate(kitName);
            if (error != null) {
                return error;
            }
            if (this.plugin.kits().exists(kitName)) {
                return "A kit named " + kitName + " already exists. Pick another name.";
            }
            this.plugin.kits().save(draft.withName(kitName));
            Msg.success(player, "Saved kit <kit>. Give it with /kit give <kit> \\<players>.", Msg.text("kit", kitName));
            return null;
        }, () -> {
            Msg.info(player, "Naming cancelled. Back to the editor; your items are still here.");
            new KitEditorMenu(this.plugin, draft, null).open(player);
        }));
    }

    /** Opening or closing inventories from inside a click handler must wait a tick. */
    private void later(final Runnable task) {
        Bukkit.getScheduler().runTask(this.plugin, task);
    }

    private static @Nullable ItemStack copy(final @Nullable ItemStack item) {
        return item == null || item.isEmpty() ? null : item.clone();
    }
}

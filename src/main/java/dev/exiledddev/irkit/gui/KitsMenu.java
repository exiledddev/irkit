package dev.exiledddev.irkit.gui;

import dev.exiledddev.irkit.IRKitPlugin;
import dev.exiledddev.irkit.Msg;
import dev.exiledddev.irkit.Permissions;
import dev.exiledddev.irkit.kit.Kit;
import dev.exiledddev.irkit.kit.KitGiver;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * /kits: every kit the player may take. Left-click takes it, right-click previews it, and players
 * who manage kits can shift-click to edit or add a new one.
 */
public final class KitsMenu implements Menu {

    private static final int PER_PAGE = 45;
    private static final int PREVIOUS = 45;
    private static final int NEW_KIT = 47;
    private static final int INFO = 49;
    private static final int CLOSE = 51;
    private static final int NEXT = 53;

    private final IRKitPlugin plugin;
    private final Player viewer;
    private final List<Kit> kits;
    private final int page;
    private final Inventory inventory;

    public KitsMenu(final IRKitPlugin plugin, final Player viewer, final int page) {
        this.plugin = plugin;
        this.viewer = viewer;
        this.kits = new ArrayList<>(plugin.kits().all().stream().filter(kit -> plugin.canClaim(viewer, kit) || canManage(viewer)).toList());
        final int pages = Math.max(1, (this.kits.size() + PER_PAGE - 1) / PER_PAGE);
        this.page = Math.clamp(page, 0, pages - 1);
        this.inventory = Bukkit.createInventory(this, 54, Component.text("Kits" + (pages > 1 ? " (" + (this.page + 1) + "/" + pages + ")" : "")));

        final boolean manage = canManage(viewer);
        for (int i = 0; i < PER_PAGE; i++) {
            final int index = this.page * PER_PAGE + i;
            if (index >= this.kits.size()) {
                break;
            }
            this.inventory.setItem(i, this.icon(this.kits.get(index), manage));
        }

        KitView.fill(this.inventory, 46, 48, 50, 52);
        this.inventory.setItem(PREVIOUS, this.page > 0
            ? Items.named(Material.ARROW, "Previous page", NamedTextColor.YELLOW)
            : Items.filler());
        this.inventory.setItem(NEXT, this.page < pages - 1
            ? Items.named(Material.ARROW, "Next page", NamedTextColor.YELLOW)
            : Items.filler());
        this.inventory.setItem(NEW_KIT, manage
            ? Items.named(Material.WRITABLE_BOOK, "New kit", NamedTextColor.GREEN, "Opens an empty kit editor.")
            : Items.filler());
        this.inventory.setItem(INFO, Items.named(Material.BOOK, this.kits.size() + " kit(s)", NamedTextColor.GOLD,
            "Left-click a kit to take it.", "Right-click to preview it first."));
        this.inventory.setItem(CLOSE, Items.named(Material.BARRIER, "Close", NamedTextColor.RED));
    }

    public void open() {
        this.viewer.openInventory(this.inventory);
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    @Override
    public void onClick(final InventoryClickEvent event) {
        event.setCancelled(true);
        final int slot = event.getRawSlot();
        if (!(event.getWhoClicked() instanceof Player player) || slot < 0 || slot >= 54) {
            return;
        }

        if (slot < PER_PAGE) {
            final int index = this.page * PER_PAGE + slot;
            if (index >= this.kits.size()) {
                return;
            }
            final Kit kit = this.kits.get(index);
            if (event.isShiftClick() && canManage(player)) {
                this.later(() -> new KitEditorMenu(this.plugin, kit, kit.name()).open(player));
            } else if (event.isRightClick()) {
                this.later(() -> new KitPreviewMenu(this.plugin, player, kit, this.page).open());
            } else if (event.isLeftClick()) {
                this.claim(player, kit);
            }
            return;
        }

        switch (slot) {
            case PREVIOUS -> {
                if (this.page > 0) {
                    this.later(() -> new KitsMenu(this.plugin, player, this.page - 1).open());
                }
            }
            case NEXT -> this.later(() -> new KitsMenu(this.plugin, player, this.page + 1).open());
            case NEW_KIT -> {
                if (canManage(player)) {
                    this.later(() -> new KitEditorMenu(this.plugin, null, null).open(player));
                }
            }
            case CLOSE -> this.later(player::closeInventory);
            default -> {
            }
        }
    }

    /** Gives the kit to the player who clicked it, if they're allowed to take it. */
    void claim(final Player player, final Kit kit) {
        if (!this.plugin.canClaim(player, kit)) {
            Msg.error(player, "You don't have permission to take kit <kit>.", Msg.text("kit", kit.name()));
            return;
        }
        this.later(() -> {
            player.closeInventory();
            KitGiver.give(player, kit, this.plugin.settings().claimGiveMode());
            Msg.success(player, "You got kit <kit>.", Msg.text("kit", kit.name()));
        });
    }

    private ItemStack icon(final Kit kit, final boolean manage) {
        final ItemStack icon = kit.displayIcon();
        final boolean canClaim = this.plugin.canClaim(this.viewer, kit);
        final List<Component> lore = new ArrayList<>();
        lore.add(Component.text(countItems(kit) + " item stack(s)", NamedTextColor.DARK_GRAY));
        lore.add(canClaim
            ? Component.text("Left-click: take this kit", NamedTextColor.GREEN)
            : Component.text("You can't take this kit", NamedTextColor.RED));
        lore.add(Component.text("Right-click: preview", NamedTextColor.GRAY));
        if (manage) {
            lore.add(Component.text("Shift-click: edit", NamedTextColor.YELLOW));
        }
        icon.editMeta(meta -> {
            meta.customName(Component.text(kit.name(), NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false));
            meta.lore(lore.stream().map(line -> line.decoration(TextDecoration.ITALIC, false)).toList());
        });
        return icon;
    }

    private static int countItems(final Kit kit) {
        int count = 0;
        for (final ItemStack item : kit.items()) {
            if (item != null) {
                count++;
            }
        }
        return count;
    }

    private static boolean canManage(final Player player) {
        return player.hasPermission(Permissions.MANAGE);
    }

    private void later(final Runnable task) {
        Bukkit.getScheduler().runTask(this.plugin, task);
    }
}

package dev.exiledddev.irkit.gui;

import dev.exiledddev.irkit.IRKitPlugin;
import dev.exiledddev.irkit.Msg;
import dev.exiledddev.irkit.kit.Kit;
import dev.exiledddev.irkit.kit.KitGiver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

/**
 * A read-only look at a kit, laid out like the editor.
 */
public final class KitPreviewMenu implements Menu {

    private static final int BACK = 52;
    private static final int TAKE = 53;

    private final IRKitPlugin plugin;
    private final Player viewer;
    private final Kit kit;
    private final int returnPage;
    private final Inventory inventory;

    /**
     * @param returnPage the /kits page to go back to, or -1 if the preview wasn't opened from /kits
     */
    public KitPreviewMenu(final IRKitPlugin plugin, final Player viewer, final Kit kit, final int returnPage) {
        this.plugin = plugin;
        this.viewer = viewer;
        this.kit = kit;
        this.returnPage = returnPage;
        this.inventory = Bukkit.createInventory(this, EditorLayout.SIZE,
            Component.text("Kit: ").append(Component.text(kit.name(), NamedTextColor.DARK_BLUE)));

        KitView.place(this.inventory, kit);
        KitView.labels(this.inventory, false);
        KitView.fill(this.inventory, EditorLayout.LOAD_INVENTORY, EditorLayout.CLEAR, 44, 51);
        this.inventory.setItem(BACK, returnPage >= 0
            ? Items.named(Material.ARROW, "Back to kits", NamedTextColor.YELLOW)
            : Items.named(Material.BARRIER, "Close", NamedTextColor.RED));
        this.inventory.setItem(TAKE, plugin.canClaim(viewer, kit)
            ? Items.named(Material.LIME_CONCRETE, "Take this kit", NamedTextColor.GREEN)
            : Items.filler());
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
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (event.getRawSlot() == BACK) {
            Bukkit.getScheduler().runTask(this.plugin, () -> {
                if (this.returnPage >= 0) {
                    new KitsMenu(this.plugin, player, this.returnPage).open();
                } else {
                    player.closeInventory();
                }
            });
        } else if (event.getRawSlot() == TAKE && this.plugin.canClaim(player, this.kit)) {
            Bukkit.getScheduler().runTask(this.plugin, () -> {
                player.closeInventory();
                KitGiver.give(player, this.kit, this.plugin.settings().claimGiveMode());
                Msg.success(player, "You got kit <kit>.", Msg.text("kit", this.kit.name()));
            });
        }
    }
}

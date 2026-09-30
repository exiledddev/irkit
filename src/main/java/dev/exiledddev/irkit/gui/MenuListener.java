package dev.exiledddev.irkit.gui;

import dev.exiledddev.irkit.Msg;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.MenuType;
import org.bukkit.inventory.view.AnvilView;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.Nullable;

/**
 * Routes inventory events to IRKit's chest menus, and runs the anvil prompts used to name kits.
 */
public final class MenuListener implements Listener {

    /** Anvil result slot. */
    private static final int ANVIL_RESULT = 2;

    /** An open "type a name" anvil. */
    private record NamePrompt(AnvilView view, String placeholder, Function<String, @Nullable String> onConfirm, Runnable onCancel) {
    }

    private final Plugin plugin;
    private final Map<UUID, NamePrompt> prompts = new HashMap<>();
    /** Players whose prompt finished, so closing the anvil doesn't count as cancelling. */
    private final Map<UUID, Boolean> finished = new HashMap<>();

    public MenuListener(final Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Opens an anvil where the player types a name and clicks the result to confirm.
     *
     * @param onConfirm gets the typed text; returns an error to show (the anvil stays open) or null when done
     * @param onCancel  runs if the player closes the anvil without confirming
     */
    public void promptName(final Player player, final String placeholder, final Function<String, @Nullable String> onConfirm, final Runnable onCancel) {
        final AnvilView view = MenuType.ANVIL.builder()
            .title(Component.text("Name your kit"))
            .checkReachable(false)
            .build(player);
        this.prompts.put(player.getUniqueId(), new NamePrompt(view, placeholder, onConfirm, onCancel));
        view.open();
        view.getTopInventory().setFirstItem(Items.named(Material.PAPER, Component.text(placeholder)));
        view.setRepairCost(0);
        Msg.info(player, "Type the kit name in the anvil, then click the paper on the right to save. Close it to go back.");
    }

    // ---- Chest menus ----------------------------------------------------------------------

    @EventHandler(priority = EventPriority.HIGH)
    public void onClick(final InventoryClickEvent event) {
        if (this.promptOf(event.getView()) != null) {
            this.onPromptClick(event);
            return;
        }
        if (event.getInventory().getHolder(false) instanceof Menu menu) {
            menu.onClick(event);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDrag(final InventoryDragEvent event) {
        if (this.promptOf(event.getView()) != null) {
            event.setCancelled(true);
            return;
        }
        if (event.getInventory().getHolder(false) instanceof Menu menu) {
            menu.onDrag(event);
        }
    }

    @EventHandler
    public void onClose(final InventoryCloseEvent event) {
        final UUID id = event.getPlayer().getUniqueId();
        final NamePrompt prompt = this.promptOf(event.getView());
        if (prompt != null) {
            // Empty the anvil so the paper isn't handed to the player.
            event.getView().getTopInventory().clear();
            this.prompts.remove(id);
            if (this.finished.remove(id) == null && event.getPlayer() instanceof Player player && player.isOnline()
                && event.getReason() != InventoryCloseEvent.Reason.DISCONNECT) {
                Bukkit.getScheduler().runTask(this.plugin, prompt.onCancel());
            }
            return;
        }
        if (event.getInventory().getHolder(false) instanceof Menu menu) {
            menu.onClose(event);
        }
    }

    @EventHandler
    public void onQuit(final PlayerQuitEvent event) {
        this.prompts.remove(event.getPlayer().getUniqueId());
        this.finished.remove(event.getPlayer().getUniqueId());
    }

    // ---- Name prompt ----------------------------------------------------------------------

    /** Shows the typed name on the result paper so there's something to click. */
    @EventHandler
    public void onPrepareAnvil(final PrepareAnvilEvent event) {
        final NamePrompt prompt = this.promptOf(event.getView());
        if (prompt == null) {
            return;
        }
        final String typed = typedName(event.getView(), prompt);
        event.setResult(Items.named(Material.PAPER, Component.text(typed, NamedTextColor.GREEN),
            Component.text("Click to save the kit as " + typed, NamedTextColor.GRAY)));
        event.getView().setRepairCost(0);
    }

    private void onPromptClick(final InventoryClickEvent event) {
        event.setCancelled(true);
        if (event.getRawSlot() != ANVIL_RESULT || !(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        final NamePrompt prompt = this.prompts.get(player.getUniqueId());
        if (prompt == null) {
            return;
        }
        final String error = prompt.onConfirm().apply(typedName(prompt.view(), prompt));
        if (error != null) {
            Msg.errorText(player, error);
            return;
        }
        this.finished.put(player.getUniqueId(), true);
        Bukkit.getScheduler().runTask(this.plugin, () -> player.closeInventory());
    }

    private static String typedName(final AnvilView view, final NamePrompt prompt) {
        final String text = view.getRenameText();
        return text == null || text.isBlank() ? prompt.placeholder() : text.strip();
    }

    private @Nullable NamePrompt promptOf(final InventoryView view) {
        for (final NamePrompt prompt : this.prompts.values()) {
            if (prompt.view() == view) {
                return prompt;
            }
        }
        return null;
    }

    /** Closes every IRKit menu, e.g. when the plugin is disabled, so nobody is left in a dead menu. */
    public void closeAll() {
        for (final Player player : Bukkit.getOnlinePlayers()) {
            final InventoryView view = player.getOpenInventory();
            if (this.promptOf(view) != null || view.getTopInventory().getHolder(false) instanceof Menu) {
                this.finished.put(player.getUniqueId(), true);
                player.closeInventory();
            }
        }
    }
}

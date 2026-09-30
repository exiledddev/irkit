package dev.exiledddev.irkit.gui;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryHolder;

/**
 * A chest menu owned by IRKit. {@link MenuListener} routes inventory events to it.
 */
public interface Menu extends InventoryHolder {

    void onClick(InventoryClickEvent event);

    default void onDrag(final InventoryDragEvent event) {
        event.setCancelled(true);
    }

    default void onClose(final InventoryCloseEvent event) {
    }
}

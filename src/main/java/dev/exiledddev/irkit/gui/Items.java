package dev.exiledddev.irkit.gui;

import java.util.Arrays;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/** Builds the buttons, labels and filler panes used in menus. */
final class Items {

    private Items() {
    }

    static ItemStack named(final Material material, final Component name, final Component... lore) {
        final ItemStack item = new ItemStack(material);
        item.editMeta(meta -> {
            meta.customName(plain(name));
            if (lore.length > 0) {
                meta.lore(Arrays.stream(lore).map(Items::plain).toList());
            }
        });
        return item;
    }

    static ItemStack named(final Material material, final String name, final NamedTextColor color, final String... lore) {
        return named(material, Component.text(name, color),
            Arrays.stream(lore).map(line -> (Component) Component.text(line, NamedTextColor.GRAY)).toArray(Component[]::new));
    }

    static ItemStack filler() {
        return named(Material.GRAY_STAINED_GLASS_PANE, Component.text(" "));
    }

    /** Item names and lore are italic by default; menus look cleaner without it. */
    private static Component plain(final Component component) {
        return component.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }
}

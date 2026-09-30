package dev.exiledddev.irkit.kit;

import java.util.Locale;
import org.jspecify.annotations.Nullable;

/** What giving a kit does with the items a player already has. */
public enum GiveMode {
    /** Clear the player's inventory, armor and offhand, then place every item in its kit slot. */
    REPLACE,
    /** Keep the player's items and add the kit on top. */
    ADD;

    public static @Nullable GiveMode parse(final String raw) {
        return switch (raw.strip().toLowerCase(Locale.ROOT)) {
            case "replace" -> REPLACE;
            case "add" -> ADD;
            default -> null;
        };
    }
}

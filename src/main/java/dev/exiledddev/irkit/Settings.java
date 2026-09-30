package dev.exiledddev.irkit;

import dev.exiledddev.irkit.kit.GiveMode;
import java.util.logging.Logger;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * A snapshot of config.yml.
 */
public record Settings(GiveMode defaultGiveMode, GiveMode claimGiveMode, boolean perKitPermissions, boolean dropReplacedGear) {

    public static Settings load(final FileConfiguration config, final Logger logger) {
        return new Settings(
            mode(config.getString("kits.default-give-mode", "replace"), "kits.default-give-mode", logger),
            mode(config.getString("kits.claim-give-mode", "replace"), "kits.claim-give-mode", logger),
            config.getBoolean("kits.per-kit-permissions", false),
            config.getBoolean("gear.drop-replaced-gear", true)
        );
    }

    private static GiveMode mode(final String raw, final String key, final Logger logger) {
        final GiveMode mode = GiveMode.parse(raw);
        if (mode == null) {
            logger.warning("Unknown value '" + raw + "' for " + key + " in config.yml, using 'replace'.");
            return GiveMode.REPLACE;
        }
        return mode;
    }
}

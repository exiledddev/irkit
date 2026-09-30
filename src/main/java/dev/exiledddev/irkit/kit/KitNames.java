package dev.exiledddev.irkit.kit;

import java.util.Locale;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;

/**
 * Kit names are lowercase so they're easy to type in commands, tab-complete, and map to a file
 * and a permission node ({@code irkit.kit.<name>}).
 */
public final class KitNames {

    public static final Pattern VALID = Pattern.compile("[a-z0-9_-]{1,32}");

    private KitNames() {
    }

    public static String normalize(final String name) {
        return name.strip().toLowerCase(Locale.ROOT);
    }

    /**
     * @return an error message, or {@code null} if the (normalized) name is fine
     */
    public static @Nullable String validate(final String name) {
        if (name.isEmpty()) {
            return "The kit needs a name.";
        }
        if (!VALID.matcher(name).matches()) {
            return "Kit names can only use letters, numbers, _ and - (max 32 characters): " + name;
        }
        return null;
    }
}

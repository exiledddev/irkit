package dev.exiledddev.irkit.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.exiledddev.irkit.Msg;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

/** Shared argument helpers. */
final class Args {

    static final String TARGETS = "targets";

    private Args() {
    }

    /**
     * Resolves a {@code <targets>} argument. It's Paper's vanilla player selector, so names, UUIDs and
     * every selector form work: {@code @a[team=red]}, {@code @p[distance=..10]}, {@code @r[limit=3]}...
     * Throws the vanilla "No player was found" error if it matches nobody.
     */
    static List<Player> targets(final CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return ctx.getArgument(TARGETS, PlayerSelectorArgumentResolver.class).resolve(ctx.getSource());
    }

    /**
     * The player running the command. Under {@code /execute as <player>} that's the executing player,
     * so menus open for them. Tells the sender and returns null if it isn't a player.
     */
    static @Nullable Player player(final CommandContext<CommandSourceStack> ctx) {
        final CommandSourceStack source = ctx.getSource();
        if (source.getExecutor() instanceof Player player) {
            return player;
        }
        if (source.getSender() instanceof Player player) {
            return player;
        }
        Msg.error(source.getSender(), "Only players can do that.");
        return null;
    }

    static boolean hasAny(final CommandSender sender, final String... permissions) {
        for (final String permission : permissions) {
            if (sender.hasPermission(permission)) {
                return true;
            }
        }
        return false;
    }

    /** "Steve" for one player, "5 players" for more. */
    static String describe(final List<Player> players) {
        return players.size() == 1 ? players.getFirst().getName() : players.size() + " players";
    }
}

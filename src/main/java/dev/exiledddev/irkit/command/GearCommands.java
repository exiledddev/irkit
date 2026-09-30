package dev.exiledddev.irkit.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.exiledddev.irkit.IRKitPlugin;
import dev.exiledddev.irkit.Msg;
import dev.exiledddev.irkit.Permissions;
import dev.exiledddev.irkit.gear.PowerSuit;
import dev.exiledddev.irkit.gear.RandomGear;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import java.util.List;
import org.bukkit.entity.Player;

/**
 * /randarmor, /randitem and /powersuit, ported from the old IsMP plugin with real vanilla selectors.
 */
public final class GearCommands {

    private final IRKitPlugin plugin;

    public GearCommands(final IRKitPlugin plugin) {
        this.plugin = plugin;
    }

    public LiteralCommandNode<CommandSourceStack> randArmor() {
        return Commands.literal("randarmor")
            .requires(source -> source.getSender().hasPermission(Permissions.GIVE))
            .then(Commands.argument(Args.TARGETS, ArgumentTypes.players())
                .executes(ctx -> {
                    final List<Player> targets = Args.targets(ctx);
                    final boolean drop = this.plugin.settings().dropReplacedGear();
                    targets.forEach(player -> RandomGear.armor(player, drop));
                    Msg.success(ctx.getSource().getSender(), "Gave random armor to <targets>.", Msg.text("targets", Args.describe(targets)));
                    return targets.size();
                }))
            .build();
    }

    public LiteralCommandNode<CommandSourceStack> randItem() {
        return Commands.literal("randitem")
            .requires(source -> source.getSender().hasPermission(Permissions.GIVE))
            .then(Commands.argument(Args.TARGETS, ArgumentTypes.players())
                .executes(ctx -> {
                    final List<Player> targets = Args.targets(ctx);
                    targets.forEach(RandomGear::items);
                    Msg.success(ctx.getSource().getSender(), "Gave random items to <targets>.", Msg.text("targets", Args.describe(targets)));
                    return targets.size();
                }))
            .build();
    }

    public LiteralCommandNode<CommandSourceStack> powerSuit() {
        return Commands.literal("powersuit")
            .requires(source -> source.getSender().hasPermission(Permissions.GIVE))
            .then(Commands.argument("level", IntegerArgumentType.integer(PowerSuit.MIN_LEVEL, PowerSuit.MAX_LEVEL))
                .then(Commands.argument(Args.TARGETS, ArgumentTypes.players())
                    .executes(this::powerSuit)))
            .build();
    }

    private int powerSuit(final CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        final int level = IntegerArgumentType.getInteger(ctx, "level");
        final List<Player> targets = Args.targets(ctx);
        final boolean drop = this.plugin.settings().dropReplacedGear();
        targets.forEach(player -> PowerSuit.apply(player, level, drop));
        Msg.success(ctx.getSource().getSender(), "Gave a level <level> power suit to <targets>.",
            Msg.text("level", level), Msg.text("targets", Args.describe(targets)));
        return targets.isEmpty() ? 0 : Command.SINGLE_SUCCESS;
    }
}

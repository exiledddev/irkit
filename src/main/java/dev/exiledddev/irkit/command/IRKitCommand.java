package dev.exiledddev.irkit.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.exiledddev.irkit.IRKitPlugin;
import dev.exiledddev.irkit.Msg;
import dev.exiledddev.irkit.Permissions;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

/**
 * {@code /irkit reload}.
 */
public final class IRKitCommand {

    private final IRKitPlugin plugin;

    public IRKitCommand(final IRKitPlugin plugin) {
        this.plugin = plugin;
    }

    public LiteralCommandNode<CommandSourceStack> build() {
        return Commands.literal("irkit")
            .requires(source -> source.getSender().hasPermission(Permissions.MANAGE))
            .executes(ctx -> {
                Msg.info(ctx.getSource().getSender(), "IRKit <version>. Run /kit help for commands, /irkit reload to reload config and kits.",
                    Msg.text("version", this.plugin.getPluginMeta().getVersion()));
                return Command.SINGLE_SUCCESS;
            })
            .then(Commands.literal("reload").executes(ctx -> {
                this.plugin.reload();
                Msg.success(ctx.getSource().getSender(), "Reloaded config.yml and <count> kit(s).",
                    Msg.text("count", this.plugin.kits().all().size()));
                return Command.SINGLE_SUCCESS;
            }))
            .build();
    }
}

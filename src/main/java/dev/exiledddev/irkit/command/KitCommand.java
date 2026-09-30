package dev.exiledddev.irkit.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.exiledddev.irkit.IRKitPlugin;
import dev.exiledddev.irkit.Msg;
import dev.exiledddev.irkit.Permissions;
import dev.exiledddev.irkit.gui.KitEditorMenu;
import dev.exiledddev.irkit.gui.KitPreviewMenu;
import dev.exiledddev.irkit.gui.KitsMenu;
import dev.exiledddev.irkit.kit.GiveMode;
import dev.exiledddev.irkit.kit.Kit;
import dev.exiledddev.irkit.kit.KitGiver;
import dev.exiledddev.irkit.kit.KitNames;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

/**
 * {@code /kit} and {@code /kits}.
 */
public final class KitCommand {

    /** Usage line, text a click puts in the chat box, what it does, and the permission it needs. */
    private record HelpEntry(String usage, String suggestion, String description, String permission) {
    }

    private static final List<HelpEntry> HELP = List.of(
        new HelpEntry("/kits", "/kits", "browse kits and take one", Permissions.KITS),
        new HelpEntry("/kit claim <kit>", "/kit claim ", "take a kit for yourself", Permissions.KITS),
        new HelpEntry("/kit preview <kit>", "/kit preview ", "look at a kit without taking it", Permissions.KITS),
        new HelpEntry("/kit list", "/kit list", "list kits", Permissions.KITS),
        new HelpEntry("/kit give <kit> <targets> [--add|--replace]", "/kit give ", "give a kit to players (any selector)", Permissions.GIVE),
        new HelpEntry("/kit create [name]", "/kit create ", "build a new kit in the editor", Permissions.MANAGE),
        new HelpEntry("/kit edit <kit>", "/kit edit ", "change a kit in the editor", Permissions.MANAGE),
        new HelpEntry("/kit copy <kit> <new name>", "/kit copy ", "duplicate a kit", Permissions.MANAGE),
        new HelpEntry("/kit rename <kit> <new name>", "/kit rename ", "rename a kit", Permissions.MANAGE),
        new HelpEntry("/kit delete <kit>", "/kit delete ", "delete a kit", Permissions.MANAGE),
        new HelpEntry("/randarmor <targets>", "/randarmor ", "random armor", Permissions.GIVE),
        new HelpEntry("/randitem <targets>", "/randitem ", "random weapons, tools and food", Permissions.GIVE),
        new HelpEntry("/powersuit <1-5> <targets>", "/powersuit ", "tiered power suit gear", Permissions.GIVE)
    );

    private final IRKitPlugin plugin;

    public KitCommand(final IRKitPlugin plugin) {
        this.plugin = plugin;
    }

    public LiteralCommandNode<CommandSourceStack> kits() {
        return Commands.literal("kits")
            .requires(source -> source.getSender().hasPermission(Permissions.KITS))
            .executes(ctx -> {
                final Player player = Args.player(ctx);
                if (player == null) {
                    return 0;
                }
                new KitsMenu(this.plugin, player, 0).open();
                return Command.SINGLE_SUCCESS;
            })
            .build();
    }

    public LiteralCommandNode<CommandSourceStack> kit() {
        return Commands.literal("kit")
            .requires(source -> Args.hasAny(source.getSender(), Permissions.KITS, Permissions.GIVE, Permissions.MANAGE))
            .executes(this::help)
            .then(Commands.literal("help").executes(this::help))
            .then(Commands.literal("list").executes(this::list))
            .then(Commands.literal("claim")
                .requires(source -> source.getSender().hasPermission(Permissions.KITS))
                .then(this.kitArgument(true).executes(this::claim)))
            .then(Commands.literal("preview")
                .requires(source -> Args.hasAny(source.getSender(), Permissions.KITS, Permissions.MANAGE))
                .then(this.kitArgument(false).executes(this::preview)))
            .then(Commands.literal("give")
                .requires(source -> source.getSender().hasPermission(Permissions.GIVE))
                .then(this.kitArgument(false)
                    .then(Commands.argument(Args.TARGETS, ArgumentTypes.players())
                        .executes(ctx -> this.give(ctx, null))
                        .then(Commands.literal("--add").executes(ctx -> this.give(ctx, GiveMode.ADD)))
                        .then(Commands.literal("--replace").executes(ctx -> this.give(ctx, GiveMode.REPLACE))))))
            .then(Commands.literal("create")
                .requires(source -> source.getSender().hasPermission(Permissions.MANAGE))
                .executes(ctx -> this.create(ctx, null))
                .then(Commands.argument("name", StringArgumentType.word())
                    .executes(ctx -> this.create(ctx, StringArgumentType.getString(ctx, "name")))))
            .then(Commands.literal("edit")
                .requires(source -> source.getSender().hasPermission(Permissions.MANAGE))
                .then(this.kitArgument(false).executes(this::edit)))
            .then(Commands.literal("copy")
                .requires(source -> source.getSender().hasPermission(Permissions.MANAGE))
                .then(this.kitArgument(false)
                    .then(Commands.argument("new", StringArgumentType.word())
                        .executes(ctx -> this.copy(ctx, false)))))
            .then(Commands.literal("rename")
                .requires(source -> source.getSender().hasPermission(Permissions.MANAGE))
                .then(this.kitArgument(false)
                    .then(Commands.argument("new", StringArgumentType.word())
                        .executes(ctx -> this.copy(ctx, true)))))
            .then(Commands.literal("delete")
                .requires(source -> source.getSender().hasPermission(Permissions.MANAGE))
                .then(this.kitArgument(false)
                    .executes(ctx -> this.delete(ctx, false))
                    .then(Commands.literal("confirm").executes(ctx -> this.delete(ctx, true)))))
            .build();
    }

    /** A {@code <kit>} argument that suggests kit names (only the ones the sender can take, if {@code claimable}). */
    private RequiredArgumentBuilder<CommandSourceStack, String> kitArgument(final boolean claimable) {
        return Commands.argument("kit", StringArgumentType.word())
            .suggests((ctx, builder) -> {
                final CommandSender sender = ctx.getSource().getSender();
                final List<String> names = this.plugin.kits().all().stream()
                    .filter(kit -> !claimable || !(sender instanceof Player player) || this.plugin.canClaim(player, kit))
                    .map(Kit::name)
                    .toList();
                return Suggest.matching(builder, names);
            });
    }

    /** Looks up the {@code <kit>} argument, telling the sender if it doesn't exist. */
    private @Nullable Kit resolveKit(final CommandContext<CommandSourceStack> ctx) {
        final String name = StringArgumentType.getString(ctx, "kit");
        final Kit kit = this.plugin.kits().get(name);
        if (kit == null) {
            final List<String> names = this.plugin.kits().names();
            if (names.isEmpty()) {
                Msg.error(ctx.getSource().getSender(), "There's no kit named <kit>, and no kits yet. Make one with /kit create.", Msg.text("kit", name));
            } else {
                Msg.error(ctx.getSource().getSender(), "There's no kit named <kit>. Kits: <kits>", Msg.text("kit", name), Msg.text("kits", Msg.join(names)));
            }
        }
        return kit;
    }

    // ---- Subcommands ----------------------------------------------------------------------

    private int help(final CommandContext<CommandSourceStack> ctx) {
        final CommandSender sender = ctx.getSource().getSender();
        Msg.info(sender, "Commands <dark_gray>(click one to type it)</dark_gray>:");
        for (final HelpEntry entry : HELP) {
            if (!sender.hasPermission(entry.permission())) {
                continue;
            }
            sender.sendMessage(Component.text()
                .append(Component.text(" " + entry.usage(), NamedTextColor.GOLD)
                    .clickEvent(ClickEvent.suggestCommand(entry.suggestion()))
                    .hoverEvent(HoverEvent.showText(Component.text("Click to type " + entry.suggestion().strip()))))
                .append(Component.text(" - " + entry.description(), NamedTextColor.GRAY)));
        }
        return Command.SINGLE_SUCCESS;
    }

    private int list(final CommandContext<CommandSourceStack> ctx) {
        final CommandSender sender = ctx.getSource().getSender();
        final List<String> names = this.plugin.kits().all().stream()
            .filter(kit -> !(sender instanceof Player player) || this.plugin.canClaim(player, kit) || sender.hasPermission(Permissions.MANAGE)
                || sender.hasPermission(Permissions.GIVE))
            .map(Kit::name)
            .toList();
        if (names.isEmpty()) {
            Msg.info(sender, "There are no kits yet.");
        } else {
            Msg.info(sender, "<count> kit(s): <white><kits>", Msg.text("count", names.size()), Msg.text("kits", Msg.join(names)));
        }
        return names.size();
    }

    private int claim(final CommandContext<CommandSourceStack> ctx) {
        final Player player = Args.player(ctx);
        final Kit kit = this.resolveKit(ctx);
        if (player == null || kit == null) {
            return 0;
        }
        if (!this.plugin.canClaim(player, kit)) {
            Msg.error(player, "You don't have permission to take kit <kit>.", Msg.text("kit", kit.name()));
            return 0;
        }
        KitGiver.give(player, kit, this.plugin.settings().claimGiveMode());
        Msg.success(player, "You got kit <kit>.", Msg.text("kit", kit.name()));
        return Command.SINGLE_SUCCESS;
    }

    private int preview(final CommandContext<CommandSourceStack> ctx) {
        final Player player = Args.player(ctx);
        final Kit kit = this.resolveKit(ctx);
        if (player == null || kit == null) {
            return 0;
        }
        new KitPreviewMenu(this.plugin, player, kit, -1).open();
        return Command.SINGLE_SUCCESS;
    }

    private int give(final CommandContext<CommandSourceStack> ctx, final @Nullable GiveMode requested) throws CommandSyntaxException {
        final Kit kit = this.resolveKit(ctx);
        if (kit == null) {
            return 0;
        }
        final List<Player> targets = Args.targets(ctx);
        final GiveMode mode = requested != null ? requested : this.plugin.settings().defaultGiveMode();
        for (final Player target : targets) {
            KitGiver.give(target, kit, mode);
        }
        Msg.success(ctx.getSource().getSender(), "Gave kit <kit> to <targets><mode>.",
            Msg.text("kit", kit.name()), Msg.text("targets", Args.describe(targets)),
            Msg.text("mode", mode == GiveMode.ADD ? " (added to their items)" : ""));
        return targets.size();
    }

    private int create(final CommandContext<CommandSourceStack> ctx, final @Nullable String rawName) {
        final Player player = Args.player(ctx);
        if (player == null) {
            return 0;
        }
        String name = null;
        if (rawName != null) {
            name = KitNames.normalize(rawName);
            final String error = KitNames.validate(name);
            if (error != null) {
                Msg.errorText(player, error);
                return 0;
            }
            if (this.plugin.kits().exists(name)) {
                Msg.error(player, "Kit <kit> already exists. Use /kit edit <kit> to change it.", Msg.text("kit", name));
                return 0;
            }
        }
        new KitEditorMenu(this.plugin, null, name).open(player);
        Msg.info(player, "Arrange the kit like your own inventory: armor and offhand go in the bottom row. Click Save when you're done.");
        return Command.SINGLE_SUCCESS;
    }

    private int edit(final CommandContext<CommandSourceStack> ctx) {
        final Player player = Args.player(ctx);
        final Kit kit = this.resolveKit(ctx);
        if (player == null || kit == null) {
            return 0;
        }
        new KitEditorMenu(this.plugin, kit, kit.name()).open(player);
        return Command.SINGLE_SUCCESS;
    }

    /** {@code /kit copy} and {@code /kit rename}. */
    private int copy(final CommandContext<CommandSourceStack> ctx, final boolean rename) {
        final CommandSender sender = ctx.getSource().getSender();
        final Kit kit = this.resolveKit(ctx);
        if (kit == null) {
            return 0;
        }
        final String newName = KitNames.normalize(StringArgumentType.getString(ctx, "new"));
        final String error = KitNames.validate(newName);
        if (error != null) {
            Msg.errorText(sender, error);
            return 0;
        }
        if (this.plugin.kits().exists(newName)) {
            Msg.error(sender, "Kit <kit> already exists.", Msg.text("kit", newName));
            return 0;
        }
        this.plugin.kits().save(kit.withName(newName));
        if (rename) {
            this.plugin.kits().delete(kit.name());
            Msg.success(sender, "Renamed kit <old> to <new>.", Msg.text("old", kit.name()), Msg.text("new", newName));
        } else {
            Msg.success(sender, "Copied kit <old> to <new>. Edit it with /kit edit <new>.", Msg.text("old", kit.name()), Msg.text("new", newName));
        }
        return Command.SINGLE_SUCCESS;
    }

    private int delete(final CommandContext<CommandSourceStack> ctx, final boolean confirmed) {
        final CommandSender sender = ctx.getSource().getSender();
        final Kit kit = this.resolveKit(ctx);
        if (kit == null) {
            return 0;
        }
        if (!confirmed) {
            final String confirm = "/kit delete " + kit.name() + " confirm";
            Msg.info(sender, "Delete kit <kit>? This can't be undone. <button>",
                Msg.text("kit", kit.name()),
                Msg.component("button", Component.text("[Delete]", NamedTextColor.RED)
                    .clickEvent(ClickEvent.runCommand(confirm))
                    .hoverEvent(HoverEvent.showText(Component.text(confirm)))));
            return 0;
        }
        this.plugin.kits().delete(kit.name());
        Msg.success(sender, "Deleted kit <kit>.", Msg.text("kit", kit.name()));
        return Command.SINGLE_SUCCESS;
    }
}

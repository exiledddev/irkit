package dev.exiledddev.irkit;

import dev.exiledddev.irkit.command.GearCommands;
import dev.exiledddev.irkit.command.IRKitCommand;
import dev.exiledddev.irkit.command.KitCommand;
import dev.exiledddev.irkit.gui.MenuListener;
import dev.exiledddev.irkit.kit.Kit;
import dev.exiledddev.irkit.kit.KitStore;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import java.io.File;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class IRKitPlugin extends JavaPlugin {

    private Settings settings;
    private KitStore kits;
    private MenuListener menus;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.settings = Settings.load(this.getConfig(), this.getLogger());
        this.kits = new KitStore(new File(this.getDataFolder(), "kits"), this.getLogger());
        this.kits.load();
        this.menus = new MenuListener(this);
        this.getServer().getPluginManager().registerEvents(this.menus, this);

        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            final KitCommand kitCommand = new KitCommand(this);
            final GearCommands gear = new GearCommands(this);
            commands.register(kitCommand.kit(), "Create, edit and give kits");
            commands.register(kitCommand.kits(), "Browse kits and take one");
            commands.register(gear.randArmor(), "Give players random armor");
            commands.register(gear.randItem(), "Give players random weapons, tools and food");
            commands.register(gear.powerSuit(), "Give players tiered power suit gear");
            commands.register(new IRKitCommand(this).build(), "IRKit info and reload");
        });
    }

    @Override
    public void onDisable() {
        if (this.menus != null) {
            this.menus.closeAll();
        }
    }

    public Settings settings() {
        return this.settings;
    }

    public KitStore kits() {
        return this.kits;
    }

    public MenuListener menus() {
        return this.menus;
    }

    public void reload() {
        this.reloadConfig();
        this.settings = Settings.load(this.getConfig(), this.getLogger());
        this.kits.load();
    }

    /**
     * Whether a player may take a kit for themselves (from /kits or /kit claim): they need
     * irkit.kits, and with per-kit-permissions on also irkit.kit.&lt;name&gt; or irkit.kit.*.
     */
    public boolean canClaim(final Player player, final Kit kit) {
        if (!player.hasPermission(Permissions.KITS)) {
            return false;
        }
        if (!this.settings.perKitPermissions()) {
            return true;
        }
        return player.hasPermission(Permissions.KIT_PREFIX + kit.name()) || player.hasPermission(Permissions.ALL_KITS);
    }
}

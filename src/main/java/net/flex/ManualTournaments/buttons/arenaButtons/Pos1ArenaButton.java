package net.flex.ManualTournaments.buttons.arenaButtons;

import net.flex.ManualTournaments.factories.ArenaFactory;
import net.flex.ManualTournaments.buttons.Button;
import net.flex.ManualTournaments.buttons.ButtonBuilder;
import net.flex.ManualTournaments.utils.gui.item.ItemBuilder;
import net.flex.ManualTournaments.utils.gui.menu.SGMenu;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import static net.flex.ManualTournaments.Main.*;

public class Pos1ArenaButton extends ButtonBuilder {
    static FileConfiguration config = getPlugin().getConfig();

    public Pos1ArenaButton(Player sender, String arenaName, SGMenu menu) {
        super(sender, arenaName, menu);
    }

    @Override
    protected Button configureButton(Player sender, String name, SGMenu menu) {
        String path = "Arenas." + name + ".pos1.";
        return new Button(new ItemBuilder(Material.MAP)
                .name(config.getString("gui-arena-settings-pos1-name"))
                .lore(config.getString("gui-arena-settings-lore-color") + "x: " + config.getString("gui-arena-settings-lore-value-color") + getArenaConfig().getDouble(path + "x"),
                        config.getString("gui-arena-settings-lore-color") + "y: " + config.getString("gui-arena-settings-lore-value-color") + getArenaConfig().getDouble(path + "y"),
                        config.getString("gui-arena-settings-lore-color") + "z: " + config.getString("gui-arena-settings-lore-value-color") + getArenaConfig().getDouble(path + "z"),
                        config.getString("gui-arena-settings-lore-color") + "yaw: " + config.getString("gui-arena-settings-lore-value-color") + getArenaConfig().getDouble(path + "yaw"),
                        config.getString("gui-arena-settings-lore-color") + "pitch: " + config.getString("gui-arena-settings-lore-value-color") + getArenaConfig().getDouble(path + "pitch"),
                        config.getString("gui-arena-settings-lore-color") + "world: " + config.getString("gui-arena-settings-lore-value-color") + getArenaConfig().getString(path + "world"))
                .build())
                .withListener(event1 -> {
                    ArenaFactory.getCommand("POS1").execute(sender, name, arenaNames.contains(name));
                    menu.refreshInventory(sender);
                });
    }
}
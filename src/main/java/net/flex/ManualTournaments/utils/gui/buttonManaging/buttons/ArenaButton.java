package net.flex.ManualTournaments.utils.gui.buttonManaging.buttons;

import net.flex.ManualTournaments.utils.gui.buttonManaging.Button;
import net.flex.ManualTournaments.utils.gui.buttonManaging.ButtonBuilder;
import net.flex.ManualTournaments.utils.gui.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import static net.flex.ManualTournaments.Main.getPlugin;

public class ArenaButton extends ButtonBuilder {
    static FileConfiguration config = getPlugin().getConfig();

    public ArenaButton(Player sender, String arenaName) {
        super(sender, arenaName);
    }

    @Override
    protected Button configureButton(Player sender, String arenaName) {
        return new Button(new ItemBuilder(Material.GRASS_BLOCK)
                .name(config.getString("gui-arena-name-color") + arenaName)
                .lore(
                        config.getString("gui-arena-lore-right-click"),
                        config.getString("gui-arena-lore-left-click"))
                .build());
    }
}

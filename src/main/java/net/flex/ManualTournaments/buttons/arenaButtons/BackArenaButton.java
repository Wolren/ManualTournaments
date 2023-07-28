package net.flex.ManualTournaments.buttons.arenaButtons;

import net.flex.ManualTournaments.guis.ArenaGUI;
import net.flex.ManualTournaments.buttons.Button;
import net.flex.ManualTournaments.buttons.ButtonBuilder;
import net.flex.ManualTournaments.utils.gui.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import static net.flex.ManualTournaments.Main.getPlugin;

public class BackArenaButton extends ButtonBuilder {
    static FileConfiguration config = getPlugin().getConfig();

    public BackArenaButton(Player sender) {
        super(sender);
    }

    @Override
    protected Button configureButton(Player sender) {
        return new Button(new ItemBuilder(Material.ARROW)
                .name(config.getString("gui-arena-back-name"))
                .build())
                .withListener(event -> {
                    if (event.isLeftClick()) {
                        sender.openInventory(ArenaGUI.arenaMenu.getInventory());
                    }
                });
    }
}
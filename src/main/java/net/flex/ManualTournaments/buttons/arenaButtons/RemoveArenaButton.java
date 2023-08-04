package net.flex.ManualTournaments.buttons.arenaButtons;

import net.flex.ManualTournaments.buttons.Button;
import net.flex.ManualTournaments.buttons.ButtonBuilder;
import net.flex.ManualTournaments.factories.ArenaFactory;
import net.flex.ManualTournaments.utils.gui.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import static net.flex.ManualTournaments.Main.arenaNames;
import static net.flex.ManualTournaments.guis.ArenaGUI.arenaGUI;
import static net.flex.ManualTournaments.utils.SharedComponents.config;

public class RemoveArenaButton extends ButtonBuilder {
    public RemoveArenaButton(Player sender, String arenaName) {
        super(sender, arenaName);
    }

    @Override
    protected Button configureButton(Player sender, String name) {
        return new Button(new ItemBuilder(Material.REDSTONE_BLOCK)
                .name(config.getString("gui-arena-settings-remove-name"))
                .lore(config.getString("gui-arena-settings-remove-lore"))
                .build()).withListener(event -> {
            if (event.isLeftClick()) {
                sender.closeInventory();
                ArenaFactory.getCommand("REMOVE").execute(sender, name, arenaNames.contains(name));
                arenaGUI(sender);
            }
        });
    }
}

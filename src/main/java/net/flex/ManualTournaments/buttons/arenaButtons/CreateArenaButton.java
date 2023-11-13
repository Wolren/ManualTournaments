package net.flex.ManualTournaments.buttons.arenaButtons;

import net.flex.ManualTournaments.buttons.Button;
import net.flex.ManualTournaments.buttons.ButtonBuilder;
import net.flex.ManualTournaments.guis.ArenaGUI;
import net.flex.ManualTournaments.utils.gui.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import static net.flex.ManualTournaments.utils.SharedComponents.config;
import static net.flex.ManualTournaments.utils.SharedComponents.send;

public class CreateArenaButton extends ButtonBuilder {
    public CreateArenaButton(Player sender) {
        super(sender);
    }

    @Override
    protected Button configureButton(Player sender) {
        return new Button(new ItemBuilder(Material.EMERALD_BLOCK)
                .name(config.getString("gui-arena-create-name"))
                .lore(config.getString("gui-arena-create-lore"))
                .build()).withListener(event -> {
            if (event.isLeftClick()) {
                sender.closeInventory();
                send(sender, "gui-arena-create-message");
                ArenaGUI.isOpenerActive = true;
            }
        });
    }
}



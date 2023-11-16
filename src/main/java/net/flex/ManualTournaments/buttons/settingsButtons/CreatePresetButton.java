package net.flex.ManualTournaments.buttons.settingsButtons;

import net.flex.ManualTournaments.buttons.Button;
import net.flex.ManualTournaments.buttons.ButtonBuilder;
import net.flex.ManualTournaments.guis.SettingsGUI;
import net.flex.ManualTournaments.utils.gui.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import static net.flex.ManualTournaments.utils.SharedComponents.config;
import static net.flex.ManualTournaments.utils.SharedComponents.send;

public class CreatePresetButton extends ButtonBuilder {
    public CreatePresetButton(Player sender) {
        super(sender);
    }

    @Override
    protected Button configureButton(Player sender) {
        return new Button(new ItemBuilder(Material.EMERALD_BLOCK)
                .name(config.getString("gui-preset-create-name"))
                .lore(config.getString("gui-preset-create-lore"))
                .build()).withListener(event -> {
            if (event.isLeftClick()) {
                sender.closeInventory();
                send(sender, "gui-preset-create-message");
                SettingsGUI.isOpenerActive = true;
            }
        });
    }
}

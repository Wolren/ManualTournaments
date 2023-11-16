package net.flex.ManualTournaments.buttons.settingsButtons;

import net.flex.ManualTournaments.buttons.Button;
import net.flex.ManualTournaments.buttons.ButtonBuilder;
import net.flex.ManualTournaments.guis.SettingsPresetGUI;
import net.flex.ManualTournaments.utils.gui.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import static net.flex.ManualTournaments.utils.SharedComponents.config;

public class PresetButton extends ButtonBuilder {
    public PresetButton(Player sender, String presetName) {
        super(sender, presetName);
    }

    protected Button configureButton(Player sender, String presetName) {
        return new Button(new ItemBuilder(Material.MAP)
                .name(config.getString("gui-arena-name-color") + presetName)
                .lore(config.getString("gui-arena-lore-right-click"), config.getString("gui-arena-lore-left-click"))
                .build()).withListener(event -> {
            if (event.isLeftClick()) {
                SettingsPresetGUI.settingsPresetGUI(sender);
            }
        });
    }
}

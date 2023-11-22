package net.flex.ManualTournaments.buttons.settingsButtons;

import net.flex.ManualTournaments.buttons.Button;
import net.flex.ManualTournaments.buttons.ButtonBuilder;
import net.flex.ManualTournaments.factories.SettingsShortFactory;
import net.flex.ManualTournaments.utils.gui.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import static net.flex.ManualTournaments.utils.SharedComponents.config;

public class EndspawnSettingsButton extends ButtonBuilder {
    public EndspawnSettingsButton(Player sender, String context) {
        super(sender, context);
    }

    @Override
    protected Button configureButton(Player player, String context) {
        return new Button(new ItemBuilder(Material.MAP)
                .name(config.getString("gui-settings-endspawn-name"))
                .lore(
                        config.getString("gui-settings-endspawn-lore-color") + "x: " + config.getString("gui-settings-endspawn-lore-value-color") + config.getDouble("fight-end-spawn." + "x"),
                        config.getString("gui-settings-endspawn-lore-color") + "y: " + config.getString("gui-settings-endspawn-lore-value-color") + config.getDouble("fight-end-spawn." + "y"),
                        config.getString("gui-settings-endspawn-lore-color") + "z: " + config.getString("gui-settings-endspawn-lore-value-color") + config.getDouble("fight-end-spawn." + "z"),
                        config.getString("gui-settings-endspawn-lore-color") + "yaw: " + config.getString("gui-settings-endspawn-lore-value-color") + config.getDouble("fight-end-spawn." + "yaw"),
                        config.getString("gui-settings-endspawn-lore-color") + "pitch: " + config.getString("gui-settings-endspawn-lore-value-color") + config.getDouble("fight-end-spawn." + "pitch"),
                        config.getString("gui-settings-endspawn-lore-color") + "world: " + config.getString("gui-settings-endspawn-lore-value-color") + config.getString("fight-end-spawn." + "world"))
                .build()).withListener(event1 -> {
            SettingsShortFactory.getCommand("ENDSPAWN").execute(player, context);
        });
    }
}

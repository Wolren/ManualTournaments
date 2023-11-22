package net.flex.ManualTournaments.commands.settingsCommand;

import lombok.SneakyThrows;
import net.flex.ManualTournaments.interfaces.SettingsShortCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import static net.flex.ManualTournaments.Main.*;
import static net.flex.ManualTournaments.utils.SharedComponents.*;

public final class EndspawnSettings implements SettingsShortCommand {
    @SneakyThrows
    @Override
    public void execute(Player player, String context) {
        if (context.equals("default")) {
            getLocation("fight-end-spawn.", player, config);
            send(player, "config-updated-successfully");
            config.save(getCustomConfigFile());
        } else {
            String path = "Presets." + context + ".";
            ConfigurationSection preset = getPresetConfig().getConfigurationSection(path);
            if (preset != null) {
                getLocation("fight-end-spawn.", player, preset);
                send(player, "config-updated-successfully");
                config.save(getCustomConfigFile());
            }
        }
    }
}

package net.flex.ManualTournaments.commands.settingsCommand;

import lombok.SneakyThrows;
import net.flex.ManualTournaments.factories.SettingsFactory;
import net.flex.ManualTournaments.interfaces.SettingsCommand;
import org.bukkit.entity.Player;

import static net.flex.ManualTournaments.Main.*;
import static net.flex.ManualTournaments.utils.SharedComponents.config;
import static net.flex.ManualTournaments.utils.SharedComponents.send;

public final class CurrentKitSettings implements SettingsCommand {
    @SneakyThrows
    @Override
    public void execute(Player player, String context, String value) {
        if (context.equals("default")) {
            if (kitNames.contains(value)) {
                config.set("current-kit", value);
                getPlugin().saveConfig();
                config = getPlugin().getConfig();
                send(player, "config-updated-successfully");
            } else send(player, "kit-not-exists");
        } else {
            if (kitNames.contains(value)) {
                SettingsFactory.updatePresetConfig(player, "current-kit", context, value);
            }
        }
    }
}

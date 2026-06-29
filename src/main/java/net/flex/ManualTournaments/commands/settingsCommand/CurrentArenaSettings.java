package net.flex.ManualTournaments.commands.settingsCommand;

import lombok.SneakyThrows;
import net.flex.ManualTournaments.factories.SettingsFactory;
import net.flex.ManualTournaments.interfaces.SettingsCommand;
import org.bukkit.entity.Player;

import static net.flex.ManualTournaments.Main.arenaNames;
import static net.flex.ManualTournaments.Main.getPlugin;
import static net.flex.ManualTournaments.utils.SharedComponents.config;
import static net.flex.ManualTournaments.utils.SharedComponents.send;

public final class CurrentArenaSettings implements SettingsCommand {
    @SneakyThrows
    @Override
    public void execute(Player player, String context, String value) {
        if (context.equals("default")) {
            if (arenaNames.contains(value)) {
                config.set("current-arena", value);
                getPlugin().saveConfig();
                config = getPlugin().getConfig();
                send(player, "config-updated-successfully");
            } else send(player, "arena-not-exists");
        } else {
            if (arenaNames.contains(value)) {
                SettingsFactory.updatePresetConfig(player, "current-arena", context, value);
            }
        }
    }
}

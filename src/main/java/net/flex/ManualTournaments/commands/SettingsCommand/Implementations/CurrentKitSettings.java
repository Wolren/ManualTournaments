package net.flex.ManualTournaments.commands.SettingsCommand.Implementations;

import net.flex.ManualTournaments.commands.SettingsCommand.SettingsCommand;
import org.bukkit.entity.Player;

import static net.flex.ManualTournaments.Main.getPlugin;
import static net.flex.ManualTournaments.utils.SharedComponents.config;
import static net.flex.ManualTournaments.utils.SharedComponents.send;

public class CurrentKitSettings implements SettingsCommand {
    @Override
    public void execute(Player player, String setting, String value) {
        if (getPlugin().kitNames.contains(value)) {
            config.set("current-kit", value);
            send(player, "config-updated-successfully");
        } else send(player, "kit-not-exists");
    }
}

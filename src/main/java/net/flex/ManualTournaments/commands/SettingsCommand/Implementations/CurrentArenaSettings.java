package net.flex.ManualTournaments.commands.SettingsCommand.Implementations;

import lombok.SneakyThrows;
import net.flex.ManualTournaments.commands.SettingsCommand.SettingsCommand;
import org.bukkit.entity.Player;

import static net.flex.ManualTournaments.Main.getPlugin;
import static net.flex.ManualTournaments.utils.SharedComponents.config;
import static net.flex.ManualTournaments.utils.SharedComponents.send;

public final class CurrentArenaSettings implements SettingsCommand {
    @SneakyThrows
    @Override
    public void execute(Player player, String setting, String value) {
        if (getPlugin().arenaNames.contains(value)) {
            config.set("current-arena", value);
            config.save(getPlugin().customConfigFile);
            send(player, "config-updated-successfully");
        } else send(player, "arena-not-exists");
    }
}

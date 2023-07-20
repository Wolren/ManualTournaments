package net.flex.ManualTournaments.commands.SettingsCommand.Short.Implementations;

import lombok.SneakyThrows;
import net.flex.ManualTournaments.commands.SettingsCommand.Short.SettingsShortCommand;
import org.bukkit.entity.Player;

import static net.flex.ManualTournaments.Main.getPlugin;
import static net.flex.ManualTournaments.utils.SharedComponents.*;

public class EndspawnSettings implements SettingsShortCommand {
    @SneakyThrows
    @Override
    public void execute(Player player, String setting) {
        getLocation("fight-end-spawn.", player, config);
        send(player, "config-updated-successfully");
        config.save(getPlugin().customConfigFile);
    }
}

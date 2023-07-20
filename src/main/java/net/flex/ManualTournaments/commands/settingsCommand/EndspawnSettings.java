package net.flex.ManualTournaments.commands.settingsCommand;

import lombok.SneakyThrows;
import net.flex.ManualTournaments.interfaces.SettingsShortCommand;
import org.bukkit.entity.Player;

import static net.flex.ManualTournaments.Main.getPlugin;
import static net.flex.ManualTournaments.utils.SharedComponents.*;

public final class EndspawnSettings implements SettingsShortCommand {
    @SneakyThrows
    @Override
    public void execute(Player player, String setting) {
        getLocation("fight-end-spawn.", player, config);
        send(player, "config-updated-successfully");
        config.save(getPlugin().customConfigFile);
    }
}

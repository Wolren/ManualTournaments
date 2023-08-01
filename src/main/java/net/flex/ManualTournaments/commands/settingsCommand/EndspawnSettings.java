package net.flex.ManualTournaments.commands.settingsCommand;

import lombok.SneakyThrows;
import net.flex.ManualTournaments.interfaces.SettingsShortCommand;
import org.bukkit.entity.Player;

import static net.flex.ManualTournaments.Main.*;
import static net.flex.ManualTournaments.utils.SharedComponents.*;

public final class EndspawnSettings implements SettingsShortCommand {
    @SneakyThrows
    @Override
    public void execute(Player player) {
        getLocation("fight-end-spawn.", player, getPlugin().getConfig());
        send(player, "config-updated-successfully");
        getPlugin().getConfig().save(getCustomConfigFile());
    }
}

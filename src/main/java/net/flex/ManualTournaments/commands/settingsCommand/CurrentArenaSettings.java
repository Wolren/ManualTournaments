package net.flex.ManualTournaments.commands.settingsCommand;

import lombok.SneakyThrows;
import net.flex.ManualTournaments.interfaces.SettingsCommand;
import org.bukkit.entity.Player;

import static net.flex.ManualTournaments.Main.getCustomConfigFile;
import static net.flex.ManualTournaments.Main.getPlugin;
import static net.flex.ManualTournaments.utils.SharedComponents.send;

public final class CurrentArenaSettings implements SettingsCommand {
    @SneakyThrows
    @Override
    public void execute(Player player, String setting, String value) {
        if (getPlugin().arenaNames.contains(value)) {
            getPlugin().getConfig().set("current-arena", value);
            getPlugin().getConfig().save(getCustomConfigFile());
            send(player, "config-updated-successfully");
        } else send(player, "arena-not-exists");
    }
}

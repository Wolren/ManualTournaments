package net.flex.ManualTournaments.commands.settingsCommand;

import lombok.SneakyThrows;
import net.flex.ManualTournaments.interfaces.SettingsCommand;
import org.bukkit.entity.Player;

import static net.flex.ManualTournaments.Main.getPlugin;
import static net.flex.ManualTournaments.utils.SharedComponents.send;

public final class CurrentKitSettings implements SettingsCommand {
    @SneakyThrows
    @Override
    public void execute(Player player, String setting, String value) {
        if (getPlugin().kitNames.contains(value)) {
            getPlugin().getConfig().set("current-kit", value);
            getPlugin().getConfig().save(getPlugin().customConfigFile);
            send(player, "config-updated-successfully");
        } else send(player, "kit-not-exists");
    }
}

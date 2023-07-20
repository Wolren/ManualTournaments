package net.flex.ManualTournaments.commands.SettingsCommand;

import org.bukkit.entity.Player;

public interface SettingsCommand {
    void execute(Player player, String setting, String value);
}

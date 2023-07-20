package net.flex.ManualTournaments.interfaces;

import org.bukkit.entity.Player;

public interface SettingsCommand {
    void execute(Player player, String setting, String value);
}

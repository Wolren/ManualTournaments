package net.flex.ManualTournaments.commands.ArenaCommand;

import org.bukkit.entity.Player;

public interface ArenaCommand {
    void execute(Player player, String arenaName, boolean arenaExists);
}
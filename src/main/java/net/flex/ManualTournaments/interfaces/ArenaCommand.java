package net.flex.ManualTournaments.interfaces;

import org.bukkit.entity.Player;

public interface ArenaCommand {
    void execute(Player player, String arenaName, boolean arenaExists);
}
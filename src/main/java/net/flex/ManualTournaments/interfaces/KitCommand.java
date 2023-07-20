package net.flex.ManualTournaments.interfaces;

import org.bukkit.entity.Player;

public interface KitCommand {
    void execute(Player player, String kitName, boolean kitExists);
}

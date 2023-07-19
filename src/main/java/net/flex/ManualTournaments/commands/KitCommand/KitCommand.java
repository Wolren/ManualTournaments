package net.flex.ManualTournaments.commands.KitCommand;

import org.bukkit.entity.Player;

public interface KitCommand {
    void execute(Player player, String kitName, boolean kitExists);
}

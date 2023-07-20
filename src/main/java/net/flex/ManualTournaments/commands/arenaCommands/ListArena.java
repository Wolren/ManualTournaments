package net.flex.ManualTournaments.commands.arenaCommands;

import net.flex.ManualTournaments.interfaces.ArenaShortCommand;
import org.bukkit.entity.Player;

import static net.flex.ManualTournaments.Main.getPlugin;
import static net.flex.ManualTournaments.utils.SharedComponents.message;

public final class ListArena implements ArenaShortCommand {
    @Override
    public void execute(Player player, String arg) {
        player.sendMessage(message("arena-list") + String.join(", ", getPlugin().arenaNames));
    }
}

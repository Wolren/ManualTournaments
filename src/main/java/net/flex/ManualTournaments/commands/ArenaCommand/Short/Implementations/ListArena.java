package net.flex.ManualTournaments.commands.ArenaCommand.Short.Implementations;

import net.flex.ManualTournaments.commands.ArenaCommand.Short.ArenaCommandType;
import org.bukkit.entity.Player;

import static net.flex.ManualTournaments.Main.getPlugin;
import static net.flex.ManualTournaments.utils.SharedComponents.message;

public final class ListArena implements ArenaCommandType {

    @Override
    public boolean arenaCommand(Player player, String arg) {
        player.sendMessage(message("arena-list") + getPlugin().arenaNames.toString());
        return true;
    }
}

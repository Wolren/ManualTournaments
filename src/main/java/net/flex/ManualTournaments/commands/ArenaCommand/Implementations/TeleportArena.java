package net.flex.ManualTournaments.commands.ArenaCommand.Implementations;

import net.flex.ManualTournaments.commands.ArenaCommand.ArenaCommand;
import org.bukkit.entity.Player;

import static net.flex.ManualTournaments.Main.getArenaConfig;
import static net.flex.ManualTournaments.utils.SharedComponents.*;

public final class TeleportArena implements ArenaCommand {
    @Override
    public void execute(Player player, String arenaName, boolean arenaExists) {
        if (arenaExists) {
            if (getArenaConfig().isSet("Arenas." + arenaName + ".spectator")) {
                player.teleport(location("Arenas." + arenaName + "." + "spectator.", getArenaConfig()));
            } else send(player, "arena-not-set");
        } else sendNotExists(player);
    }
}

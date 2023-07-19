package net.flex.ManualTournaments.commands.ArenaCommand;

import org.bukkit.entity.Player;

import static net.flex.ManualTournaments.Main.getArenaConfig;
import static net.flex.ManualTournaments.utils.SharedComponents.*;

public final class SpectatorArena implements ArenaCommand {
    @Override
    public void execute(Player player, String arenaName, boolean arenaExists) {
        if (arenaExists) {
            getLocation("Arenas." + arenaName + ".spectator.", player, getArenaConfig());
            send(player, "arena-spectator");
        } else sendNotExists(player);
    }
}

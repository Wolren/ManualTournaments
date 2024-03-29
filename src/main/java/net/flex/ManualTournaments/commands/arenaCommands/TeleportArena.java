package net.flex.ManualTournaments.commands.arenaCommands;

import net.flex.ManualTournaments.interfaces.ArenaCommand;
import org.bukkit.entity.Player;

import static net.flex.ManualTournaments.Main.getArenaConfig;
import static net.flex.ManualTournaments.utils.SharedComponents.*;

public final class TeleportArena implements ArenaCommand {
    @Override
    public void execute(Player player, String arenaName, boolean arenaExists) {
        if (arenaExists) {
            if (getArenaConfig().isSet("Arenas." + arenaName + ".spectator")) {
                player.teleport(location("Arenas." + arenaName + ".spectator.", getArenaConfig()));
            } else send(player, "arena-spectator-not-set");
        } else sendNotExists(player);
    }
}

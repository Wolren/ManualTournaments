package net.flex.ManualTournaments.commands.ArenaCommand;

import org.bukkit.entity.Player;

import static net.flex.ManualTournaments.Main.getArenaConfig;
import static net.flex.ManualTournaments.utils.SharedComponents.*;

public final class Pos2Arena implements ArenaCommand {
    @Override
    public void execute(Player player, String arenaName, boolean arenaExists) {
        if (arenaExists) {
            String pathPos2 = "Arenas." + arenaName + ".pos2.";
            getLocation(pathPos2, player, getArenaConfig());
            send(player, "arena-pos2");
        } else sendNotExists(player);
    }
}
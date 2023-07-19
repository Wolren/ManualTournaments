package net.flex.ManualTournaments.commands.ArenaCommand;

import org.bukkit.entity.Player;

import static net.flex.ManualTournaments.Main.getArenaConfig;
import static net.flex.ManualTournaments.utils.SharedComponents.*;

public final class Pos1Arena implements ArenaCommand {
    @Override
    public void execute(Player player, String arenaName, boolean arenaExists) {
        if (arenaExists) {
            String pathPos1 = "Arenas." + arenaName + ".pos1.";
            getLocation(pathPos1, player, getArenaConfig());
            send(player, "arena-pos1");
        } else sendNotExists(player);
    }
}

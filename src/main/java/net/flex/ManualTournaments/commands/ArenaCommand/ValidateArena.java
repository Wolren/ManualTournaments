package net.flex.ManualTournaments.commands.ArenaCommand;

import org.bukkit.entity.Player;

import static net.flex.ManualTournaments.Main.getArenaConfig;
import static net.flex.ManualTournaments.utils.SharedComponents.send;
import static net.flex.ManualTournaments.utils.SharedComponents.sendNotExists;

public class ValidateArena implements ArenaCommand {
    @Override
    public void execute(Player player, String arenaName, boolean arenaExists) {
        if (arenaExists) {
            String path = "Arenas." + arenaName + ".";
            boolean pos1 = getArenaConfig().isSet(path + "pos1");
            boolean pos2 = getArenaConfig().isSet(path + "pos2");
            boolean spectator = getArenaConfig().isSet(path + "spectator");
            if (pos1 && pos2 && spectator) send(player, "arena-set-correctly");
            else {
                if (!pos1) send(player, "arena-lacks-pos1");
                if (!pos2) send(player, "arena-lacks-pos2");
                if (!spectator) send(player, "arena-lacks-spectator");
            }
        } else sendNotExists(player);
    }
}
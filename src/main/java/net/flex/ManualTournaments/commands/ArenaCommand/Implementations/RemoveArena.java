package net.flex.ManualTournaments.commands.ArenaCommand.Implementations;

import net.flex.ManualTournaments.commands.Arena;
import net.flex.ManualTournaments.commands.ArenaCommand.ArenaCommand;
import org.bukkit.entity.Player;

import static net.flex.ManualTournaments.Main.getArenaConfig;
import static net.flex.ManualTournaments.utils.SharedComponents.send;
import static net.flex.ManualTournaments.utils.SharedComponents.sendNotExists;

public final class RemoveArena implements ArenaCommand {
    @Override
    public void execute(Player player, String arenaName, boolean arenaExists) {
        if (arenaExists) {
            getArenaConfig().set("Arenas." + arenaName, null);
            Arena.arenas.remove(arenaName);
            send(player, "arena-removed");
        } else sendNotExists(player);
    }
}

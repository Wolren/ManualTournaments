package net.flex.ManualTournaments.commands.ArenaCommand.Implementations;

import lombok.SneakyThrows;
import net.flex.ManualTournaments.commands.Arena;
import net.flex.ManualTournaments.commands.ArenaCommand.ArenaCommand;
import org.bukkit.entity.Player;

import static net.flex.ManualTournaments.Main.getArenaConfig;
import static net.flex.ManualTournaments.Main.getPlugin;
import static net.flex.ManualTournaments.utils.SharedComponents.*;

public final class CreateArena implements ArenaCommand {
    @SneakyThrows
    @Override
    public void execute(Player player, String arenaName, boolean arenaExists) {
        if (!arenaExists) {
            if (config.getString("current-arena") == null)  {
                config.set("current-arena", arenaName);
                config.save(getPlugin().customConfigFile);
            }
            getArenaConfig().set("Arenas." + arenaName, "");
            getArenaConfig().save(getPlugin().ArenaConfigFile);
            Arena.arenas.add(arenaName);
            send(player, "arena-create");
        } else sendNotExists(player);
    }
}
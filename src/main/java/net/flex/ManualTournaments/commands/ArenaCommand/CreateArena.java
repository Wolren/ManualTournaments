package net.flex.ManualTournaments.commands.ArenaCommand;

import lombok.SneakyThrows;
import net.flex.ManualTournaments.commands.Arena;
import org.bukkit.entity.Player;

import static net.flex.ManualTournaments.Main.getArenaConfig;
import static net.flex.ManualTournaments.Main.getPlugin;
import static net.flex.ManualTournaments.utils.SharedComponents.*;

public class CreateArena implements ArenaCommand {
    @SneakyThrows
    @Override
    public void execute(Player player, String arenaName, boolean arenaExists) {
        if (!arenaExists) {
            if (config.getString("current-arena") == null) config.set("current-arena", arenaName);
            config.save(getPlugin().customConfigFile);
            getArenaConfig().set("Arenas." + arenaName, null);
            getArenaConfig().save(getPlugin().ArenaConfigFile);
            Arena.arenas.add(arenaName);
            send(player, "arena-create");
        } else sendNotExists(player);
    }
}
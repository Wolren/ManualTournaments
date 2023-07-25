package net.flex.ManualTournaments.commands.arenaCommands;

import lombok.SneakyThrows;
import net.flex.ManualTournaments.commands.Arena;
import net.flex.ManualTournaments.interfaces.ArenaCommand;
import org.bukkit.entity.Player;

import java.util.Objects;

import static net.flex.ManualTournaments.Main.getArenaConfig;
import static net.flex.ManualTournaments.Main.getPlugin;
import static net.flex.ManualTournaments.utils.SharedComponents.*;

public final class RemoveArena implements ArenaCommand {
    @SneakyThrows
    @Override
    public void execute(Player player, String arenaName, boolean arenaExists) {
        if (arenaExists) {
            if (Objects.equals(getPlugin().getConfig().getString("current-arena"), arenaName)) {
                getPlugin().getConfig().set("current-arena", null);
                getPlugin().getConfig().save(getPlugin().customConfigFile);
            }
            getArenaConfig().set("Arenas." + arenaName, null);
            getArenaConfig().save(getPlugin().ArenaConfigFile);
            Arena.arenas.remove(arenaName);
            send(player, "arena-removed");
        } else sendNotExists(player);
    }
}

package net.flex.ManualTournaments.commands.arenaCommands;

import lombok.SneakyThrows;
import net.flex.ManualTournaments.Main;
import net.flex.ManualTournaments.interfaces.ArenaCommand;
import org.bukkit.entity.Player;

import java.util.Objects;

import static net.flex.ManualTournaments.Main.*;
import static net.flex.ManualTournaments.utils.SharedComponents.*;

public final class RemoveArena implements ArenaCommand {
    @SneakyThrows
    @Override
    public void execute(Player player, String arenaName, boolean arenaExists) {
        if (arenaExists) {
            if (Objects.requireNonNull(getPlugin().getConfig().getString("current-arena")).equalsIgnoreCase(arenaName)) {
                config.set("current-arena", null);
                config.save(getCustomConfigFile());
            }
            getArenaConfig().set("Arenas." + arenaName, null);
            getArenaConfig().save(getArenaConfigFile());
            Main.arenaNames.remove(arenaName);
            send(player, "arena-removed");
        } else sendNotExists(player);
    }
}

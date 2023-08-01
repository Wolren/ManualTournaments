package net.flex.ManualTournaments.commands.arenaCommands;

import lombok.SneakyThrows;
import net.flex.ManualTournaments.Main;
import net.flex.ManualTournaments.interfaces.ArenaCommand;
import org.bukkit.entity.Player;

import java.util.Objects;

import static net.flex.ManualTournaments.Main.*;
import static net.flex.ManualTournaments.utils.SharedComponents.send;
import static net.flex.ManualTournaments.utils.SharedComponents.sendNotExists;

public final class CreateArena implements ArenaCommand {
    @SneakyThrows
    @Override
    public void execute(Player player, String arenaName, boolean arenaExists) {
        if (!arenaExists) {
            if (Objects.equals(getPlugin().getConfig().getString("current-arena"), ""))  {
                getPlugin().getConfig().set("current-arena", arenaName);
                getPlugin().getConfig().save(getCustomConfigFile());
            }
            getArenaConfig().set("Arenas." + arenaName, "");
            getArenaConfig().save(getArenaConfigFile());
            Main.arenaNames.add(arenaName);
            send(player, "arena-create");
        } else sendNotExists(player);
    }
}
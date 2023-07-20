package net.flex.ManualTournaments.commands.ArenaCommand;

import net.flex.ManualTournaments.commands.ArenaCommand.Implementations.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static net.flex.ManualTournaments.utils.SharedComponents.send;

public class ArenaFactory {
    private static final Map<String, ArenaCommand> arenaCommandMap;

    static {
        arenaCommandMap = new ConcurrentHashMap<>();
        arenaCommandMap.put("CREATE", new CreateArena());
        arenaCommandMap.put("POS1", new Pos1Arena());
        arenaCommandMap.put("POS2", new Pos2Arena());
        arenaCommandMap.put("REMOVE", new RemoveArena());
        arenaCommandMap.put("SPECTATOR", new SpectatorArena());
        arenaCommandMap.put("TELEPORT", new TeleportArena());
        arenaCommandMap.put("VALIDATE", new ValidateArena());
    }

    public static ArenaCommand getCommand(String command) {
        return arenaCommandMap.getOrDefault(command, (player, arenaName, arenaExists) -> send(player, "arena-usage"));
    }
}

package net.flex.ManualTournaments.commands.ArenaCommand;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ArenaCommandFactory {
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
        return arenaCommandMap.getOrDefault(command, (player, arenaName, arenaExists) -> {
        });
    }
}

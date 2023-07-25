package net.flex.ManualTournaments.factories;

import net.flex.ManualTournaments.commands.arenaCommands.*;
import net.flex.ManualTournaments.interfaces.ArenaCommand;

import java.util.HashMap;
import java.util.Map;

import static net.flex.ManualTournaments.utils.SharedComponents.send;

public class ArenaFactory {
    private static final Map<String, ArenaCommand> arenaCommandMap = new HashMap<String, ArenaCommand>() {{
        put("CREATE", new CreateArena());
        put("POS1", new Pos1Arena());
        put("POS2", new Pos2Arena());
        put("REMOVE", new RemoveArena());
        put("SPECTATOR", new SpectatorArena());
        put("TELEPORT", new TeleportArena());
        put("VALIDATE", new ValidateArena());
    }};

    public static ArenaCommand getCommand(String command) {
        return arenaCommandMap.getOrDefault(command, (player, arenaName, arenaExists) -> send(player, "arena-usage"));
    }
}

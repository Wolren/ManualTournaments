package net.flex.ManualTournaments.commands.ArenaCommand.Short;

import net.flex.ManualTournaments.commands.ArenaCommand.Short.Implementations.ListArena;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static net.flex.ManualTournaments.utils.SharedComponents.send;

public class ArenaShortFactory {
    private static final Map<String, ArenaShortCommand> arenaShortCommandMap;

    static {
        arenaShortCommandMap = new ConcurrentHashMap<>();
        arenaShortCommandMap.put("LIST", new ListArena());
    }

    public static ArenaShortCommand getCommand(String command) {
        return arenaShortCommandMap.getOrDefault(command, (player, arg) -> send(player, "arena-usage"));
    }
}

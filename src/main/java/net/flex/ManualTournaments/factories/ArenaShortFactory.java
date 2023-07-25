package net.flex.ManualTournaments.factories;

import net.flex.ManualTournaments.commands.arenaCommands.ListArena;
import net.flex.ManualTournaments.interfaces.ArenaShortCommand;

import java.util.HashMap;
import java.util.Map;

import static net.flex.ManualTournaments.utils.SharedComponents.send;

public class ArenaShortFactory {
    private static final Map<String, ArenaShortCommand> arenaShortCommandMap = new HashMap<String, ArenaShortCommand>() {{
        put("LIST", new ListArena());
    }};

    public static ArenaShortCommand getCommand(String command) {
        return arenaShortCommandMap.getOrDefault(command, (player, arg) -> send(player, "arena-usage"));
    }
}

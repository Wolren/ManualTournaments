package net.flex.ManualTournaments.commands.ArenaCommand.Short;

import net.flex.ManualTournaments.commands.ArenaCommand.Short.Implementations.ListArena;

public class ArenaHandler {
    public ArenaCommandType executeCommand(String arg) {
        if (arg.equalsIgnoreCase("list")) {
            return new ListArena();
        } else return null;
    }
}

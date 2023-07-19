package net.flex.ManualTournaments.commands.KitCommand.Short;

import net.flex.ManualTournaments.commands.KitCommand.Short.Implementations.GiveShortKit;
import net.flex.ManualTournaments.commands.KitCommand.Short.Implementations.ListKit;
import net.flex.ManualTournaments.commands.KitCommand.Short.Implementations.UnbreakableKit;

public class KitHandler {
    public KitCommandType executeCommand(String arg) {
        if (arg.equalsIgnoreCase("list")) return new ListKit();
        else if (arg.equalsIgnoreCase("unbreakable")) return new UnbreakableKit();
        else return new GiveShortKit();
    }
}

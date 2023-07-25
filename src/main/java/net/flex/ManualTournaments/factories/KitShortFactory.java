package net.flex.ManualTournaments.factories;

import net.flex.ManualTournaments.commands.kitCommands.ListKit;
import net.flex.ManualTournaments.commands.kitCommands.UnbreakableKit;
import net.flex.ManualTournaments.interfaces.KitShortCommand;

import java.util.HashMap;
import java.util.Map;

import static net.flex.ManualTournaments.Main.getPlugin;

public class KitShortFactory {
    private static final Map<String, KitShortCommand> kitShortCommandMap = new HashMap<String, KitShortCommand>() {{
        put("LIST", new ListKit());
        put("UNBREAKABLE", new UnbreakableKit());
    }};

    public static KitShortCommand getCommand(String command) {
        return kitShortCommandMap.getOrDefault(command, (player, arg) -> KitFactory.getCommand("GIVE").execute(player, arg, getPlugin().kitNames.contains(arg)));
    }
}

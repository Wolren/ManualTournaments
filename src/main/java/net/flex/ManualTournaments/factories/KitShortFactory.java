package net.flex.ManualTournaments.factories;

import net.flex.ManualTournaments.commands.kitCommands.ListKit;
import net.flex.ManualTournaments.commands.kitCommands.UnbreakableKit;
import net.flex.ManualTournaments.interfaces.KitShortCommand;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static net.flex.ManualTournaments.Main.getPlugin;

public class KitShortFactory {
    private static final Map<String, KitShortCommand> kitShortCommandMap;

    static {
        kitShortCommandMap = new ConcurrentHashMap<>();
        kitShortCommandMap.put("LIST", new ListKit());
        kitShortCommandMap.put("UNBREAKABLE", new UnbreakableKit());
    }

    public static KitShortCommand getCommand(String command) {
        return kitShortCommandMap.getOrDefault(command, (player, arg) -> KitFactory.getCommand("GIVE").execute(player, arg, getPlugin().kitNames.contains(arg)));
    }
}

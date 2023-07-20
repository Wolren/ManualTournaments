package net.flex.ManualTournaments.commands.KitCommand.Short;

import net.flex.ManualTournaments.commands.KitCommand.KitFactory;
import net.flex.ManualTournaments.commands.KitCommand.Short.Implementations.ListKit;
import net.flex.ManualTournaments.commands.KitCommand.Short.Implementations.UnbreakableKit;

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

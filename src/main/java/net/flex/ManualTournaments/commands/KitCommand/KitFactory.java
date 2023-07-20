package net.flex.ManualTournaments.commands.KitCommand;

import net.flex.ManualTournaments.commands.KitCommand.Implementations.CreateKit;
import net.flex.ManualTournaments.commands.KitCommand.Implementations.GiveKit;
import net.flex.ManualTournaments.commands.KitCommand.Implementations.RemoveKit;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static net.flex.ManualTournaments.utils.SharedComponents.send;

public class KitFactory {
    private static final Map<String, KitCommand> kitCommandMap;

    static {
        kitCommandMap = new ConcurrentHashMap<>();
        kitCommandMap.put("CREATE", new CreateKit());
        kitCommandMap.put("REMOVE", new RemoveKit());
        kitCommandMap.put("GIVE", new GiveKit());
    }

    public static KitCommand getCommand(String command) {
        return kitCommandMap.getOrDefault(command, (player, kitName, kitExists) -> send(player, "kit-usage"));
    }
}

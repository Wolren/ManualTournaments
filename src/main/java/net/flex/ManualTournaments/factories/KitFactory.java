package net.flex.ManualTournaments.factories;

import net.flex.ManualTournaments.commands.kitCommands.CreateKit;
import net.flex.ManualTournaments.commands.kitCommands.GiveKit;
import net.flex.ManualTournaments.commands.kitCommands.RemoveKit;
import net.flex.ManualTournaments.interfaces.KitCommand;

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
